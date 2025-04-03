package org.knock.knock_back.service.crawling.movie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.component.util.converter.StringDateConvertLongTimeStamp;
import org.knock.knock_back.dto.document.category.CATEGORY_LEVEL_TWO_INDEX;
import org.knock.knock_back.dto.document.movie.KOFIC_INDEX;
import org.knock.knock_back.repository.category.CategoryLevelTwoRepository;
import org.knock.knock_back.repository.movie.KOFICRepository;

/**
 * @author nks
 * @apiNote KOFIC (영화진흥위원회) openAPI 통해 영화 정보를 가져온다.
 *          해당 정보는 multi thread 방식으로 작동
 */
@Service
public class KOFIC {

    // Constructor Field
    private final String REQUEST_URL;
    private final String REQUEST_URL_SUB;
    private final String AUTH_KEY;
    private final CategoryLevelTwoRepository categoryLevelTwoRepository;
    private final KOFICRepository koficRepository;

    // Global Field
    private static final AtomicBoolean flag = new AtomicBoolean(false);
    private static final Logger logger = LoggerFactory.getLogger(KOFIC.class);
    private final StringDateConvertLongTimeStamp SDCLTS = new StringDateConvertLongTimeStamp();
    private Map<String, CATEGORY_LEVEL_TWO_INDEX> categoryLevelTwoList;

    public KOFIC(@Value("${api.kofic.url}") String requestUrl, @Value("${api.kofic.urlsub}") String requestUrlSub, @Value("${api.kofic.key}")String authKey,
                 CategoryLevelTwoRepository categoryLevelTwoRepository, KOFICRepository koficRepository) {
        REQUEST_URL = requestUrl;
        REQUEST_URL_SUB = requestUrlSub;
        AUTH_KEY = authKey;
        this.categoryLevelTwoRepository = categoryLevelTwoRepository;
        this.koficRepository = koficRepository;
    }

    /**
     * GET 방식 호출하기 위해 queryString 을 가변적으로 생성한다.
     */
    public String makeQueryString(Map<String, String> paramMap) {
        final StringBuilder sb = new StringBuilder();

        paramMap.forEach((key, value) -> {

            if (!sb.isEmpty()) {
                sb.append('&');
            }
            sb.append(key).append('=').append(value);
        });

        return sb.toString();
    }

    /**
     * KOFIC OPEN API 호출 메서드.
     * Async - Multi Thread 방식
     */
    @Async
    public void requestAPI() {

        if(flag.get()) return;

        /*
         * 영화 별 장르는 전역 변수를 통해 관리한다.
         */
        Iterable<CATEGORY_LEVEL_TWO_INDEX> movieSubCategoryIndex = null;
        if (categoryLevelTwoRepository.findAllByParentNm(CategoryLevelOne.MOVIE).isPresent())
        {
            movieSubCategoryIndex =
                    categoryLevelTwoRepository.findAllByParentNm(CategoryLevelOne.MOVIE).orElseThrow();
        }

        categoryLevelTwoList = new HashMap<>();

        assert movieSubCategoryIndex != null;
        for (CATEGORY_LEVEL_TWO_INDEX category : movieSubCategoryIndex)
        {
            categoryLevelTwoList.put(category.getNm(), category);
        }

        // 변수 설정
        //   - 요청(Request) 인터페이스 Map
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("key"          , AUTH_KEY);    // 발급받은 인증키
        paramMap.put("itemPerPage"  , "100");       // 페이지 당 보여줄 row
        paramMap.put("curPage"      , "1");         // 페이지

        try {

            while (true)
            {

                if(flag.get()) return;
                // Request URL 연결 객체 생성
                URL requestURL = URI.create(REQUEST_URL + "?" + makeQueryString(paramMap)).toURL();

                /*
                 * curPage 3000 이상일 경우 종료, 간혹 DB가 더 남아 있는데
                 * 페이지에 요소가 없는 경우가 있어 이를 대응하기 위함.
                 */
                if (Integer.parseInt(paramMap.get("curPage")) > 3000) break;
                JSONObject boxOfficeResult = getJsonObject(requestURL, "movieListResult");

                if (null == boxOfficeResult)
                {
                    paramMap.put("curPage", String.valueOf(Integer.parseInt(paramMap.get("curPage")) + 1));
                    continue;
                }

                int totCnt = boxOfficeResult.getInt("totCnt");

                // 요소가 0이라면 break.
                if (totCnt == 0) break;

                JSONArray dailyBoxOfficeList = boxOfficeResult.getJSONArray("movieList");
                parseMovieList(dailyBoxOfficeList);

                paramMap.put("curPage", String.valueOf(Integer.parseInt(paramMap.get("curPage")) + 1));
            }

        }
        catch (IOException e)
        {
            logger.debug("{} ERROR", e.getMessage());
        }

        logger.debug("{} END", getClass().getSimpleName());
    }

    /**
     * API 호출 위한 connection 생성하고
     * 결과 값을 JSONObject 형식으로 반환한다.
     */
    private static JSONObject getJsonObject(URL requestURL, String resultTarget) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) requestURL.openConnection();

        try
        {
            // GET 방식으로 요청
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setReadTimeout(5000);
            conn.setDoInput(true);

            StringBuilder response = getStringBuilder(conn);

            JSONObject responseBody = null;

            try
            {
                responseBody = new JSONObject(response.toString()).getJSONObject(resultTarget);
            }
            catch (Exception e)
            {
                logger.debug(e.getMessage());
            }

            // JSON 객체로  변환
            return responseBody;
        }

        finally {
            conn.disconnect();
        }


    }

    /**
     * API 호출 결과값을 StringBuilder 타입으로 반환
     */
    private static StringBuilder getStringBuilder(HttpURLConnection conn) throws IOException {

        int responseCodeKOFIC = conn.getResponseCode();
        if (responseCodeKOFIC != HttpURLConnection.HTTP_OK)
        {
            throw new IOException("HTTP 요청 실패 " + responseCodeKOFIC);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)))
        {
            String readline;
            StringBuilder response = new StringBuilder();
            while ((readline = br.readLine()) != null) {
                response.append(readline);
            }
            return response;
        }
    }

    /**
     * response 된 값을 INDEX 형태에 맞게 가공한다.
     */
    @Async
    protected void parseMovieList(JSONArray movieJsonArray) {

        List<KOFIC_INDEX> movieList = new ArrayList<>();

        for (int i = 0; i < movieJsonArray.length(); i++)
        {

            JSONObject movieJson = movieJsonArray.getJSONObject(i);

            if (koficRepository.findByKOFICCode(movieJson.optString("movieCd")) == null)
            {
                flag.set(true);
            }
            else continue;

            String movieCd = movieJson.optString("movieCd").isEmpty() ? "" : movieJson.optString("movieCd");
            String movieNm = movieJson.optString("movieNm").isEmpty() ? "" : movieJson.optString("movieNm");
            Long prdtYear = movieJson.optString("prdtYear").isEmpty() ? 0L : SDCLTS.Converter(movieJson.optString("prdtYear"));
            Long openingTime = movieJson.optString("openDt").isEmpty() ? 0L : SDCLTS.Converter(movieJson.optString("openDt"));

            JSONArray array = movieJson.getJSONArray("directors");
            String[] directors = new String[array.length()];

            if (!movieJson.getJSONArray("directors").isEmpty())
            {
                for (int index = 0; index < array.length(); index++)
                {
                    JSONObject object = array.getJSONObject(index);
                    directors[index] = object.optString("peopleNm");
                }
            }

            array = movieJson.getJSONArray("companys");
            String[] companys = new String[array.length()];
            if (!movieJson.getJSONArray("companys").isEmpty())
            {
                array = movieJson.getJSONArray("companys");

                for (int index = 0; index < array.length(); index++)
                {
                    JSONObject object = array.getJSONObject(index);
                    companys[index] = object.optString("companyNm");
                }
            }

            Set<CATEGORY_LEVEL_TWO_INDEX> set = new HashSet<>();
            if (!movieJson.optString("genreAlt").isEmpty())
            {
                String[] genres = movieJson.optString("genreAlt").toUpperCase().split(",");

                for (String genre : genres)
                {
                    if (categoryLevelTwoList.containsKey(genre))
                    {
                        set.add(categoryLevelTwoList.get(genre));
                    }
                    else
                    {
                        CATEGORY_LEVEL_TWO_INDEX categoryLevelTwoIndex =
                                new CATEGORY_LEVEL_TWO_INDEX(genre, CategoryLevelOne.MOVIE);
                        categoryLevelTwoRepository.save(categoryLevelTwoIndex);
                        categoryLevelTwoList.put(genre, categoryLevelTwoIndex);

                        set.add(categoryLevelTwoIndex);
                    }
                }
            }

            KOFIC_INDEX movie = new KOFIC_INDEX
                    (movieCd, movieNm, prdtYear, openingTime, directors, companys, CategoryLevelOne.MOVIE, set);

            setDetailInfo(movie, movieJson.optString("movieCd"));

            movieList.add(movie);
        }

        koficRepository.saveAll(movieList);
    }

    /**
     * 목록 페이지에서 각 영화별 id를 가져온 뒤 해당 id의 영화 상세페이지를 가져온다.
     */
    @Async
    protected void setDetailInfo (KOFIC_INDEX movieIndex, String movieCd) {

        String[] returnValue;
        try {

            URL requestURL = URI.create(REQUEST_URL_SUB + "?key=" + AUTH_KEY + "&movieCd=" + movieCd).toURL();
            JSONObject boxOfficeResult = getJsonObject(requestURL, "movieInfoResult").getJSONObject("movieInfo");
            JSONArray array = boxOfficeResult.getJSONArray("actors");

            if (!boxOfficeResult.optString("movieCd").isEmpty())
            {
                movieIndex.setRunningTime(Long.parseLong(boxOfficeResult.optString("showTm").isEmpty() ? "0" : boxOfficeResult.optString("showTm")));
            }
            returnValue = new String[array.length()];

            for (int i = 0; i < array.length(); i++)
            {
                JSONObject actor = array.getJSONObject(i);
                returnValue[i] = actor.optString("peopleNm");
            }

            movieIndex.setActors(returnValue);
        }
        catch (IOException e) { logger.debug(e.getMessage()); }

    }
}
 