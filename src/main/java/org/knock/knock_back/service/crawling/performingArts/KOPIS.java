package org.knock.knock_back.service.crawling.performingArts;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.knock.knock_back.component.util.converter.StringDateConvertLongTimeStamp;
import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.Enum.PrfState;
import org.knock.knock_back.dto.document.category.CATEGORY_LEVEL_TWO_INDEX;
import org.knock.knock_back.dto.document.performingArts.KOPIS_INDEX;
import org.knock.knock_back.repository.category.CategoryLevelTwoRepository;
import org.knock.knock_back.repository.performingArts.KOPISRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class KOPIS {

    private static final Logger logger = LoggerFactory.getLogger(KOPIS.class);
    private final StringDateConvertLongTimeStamp SDCLTS = new StringDateConvertLongTimeStamp();
    private final CategoryLevelTwoRepository categoryLevelTwoRepository;
    private final KOPISRepository kopisRepository;
    private final WebClient webClient = WebClient.create();

    @Value("${api.kopis.url}")
    private String REQUEST_URL;

    @Value("${api.kopis.key}")
    private String AUTH_KEY;

    private Map<String, CATEGORY_LEVEL_TWO_INDEX> categoryLevelTwoList;
    private final Set<String> processedCodes = new HashSet<>();

    private String makeQueryString(Map<String, String> paramMap) {
        StringBuilder sb = new StringBuilder();
        paramMap.forEach((key, value) -> {
            if (!sb.isEmpty()) sb.append('&');
            sb.append(key).append('=').append(value);
        });
        return sb.toString();
    }

    @Async
    public CompletableFuture<List<KOPIS_INDEX>> requestAPIAsync() {
        logger.info("Running in thread: {}", Thread.currentThread().getName());

        categoryLevelTwoList = categoryLevelTwoRepository.findAllByParentNm(CategoryLevelOne.PERFORMING_ARTS)
                .map(list -> {
                    Map<String, CATEGORY_LEVEL_TWO_INDEX> map = new HashMap<>();
                    list.forEach(cat -> map.put(cat.getNm(), cat));
                    return map;
                }).orElseGet(HashMap::new);

        LocalDate oneYearAgo = LocalDate.now().minusYears(1).withMonth(1).withDayOfMonth(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        Map<String, String> paramMap = new HashMap<>(Map.of(
                "service", AUTH_KEY,
                "stdate", oneYearAgo.format(formatter),
                "eddate", "29991231",
                "rows", "100",
                "cpage", "1"
        ));

        List<KOPIS_INDEX> allPerformances = new ArrayList<>();

        while (true) {
            int currentPage = Integer.parseInt(paramMap.get("cpage"));
            if (currentPage > 100) break;

            String url = REQUEST_URL + "?" + makeQueryString(paramMap);
            JSONArray performanceList = fetchJSONArray(url);

            if (performanceList == null || performanceList.isEmpty()) {
                logger.warn("Page {} 응답 실패 또는 결과 없음. 종료.", currentPage);
                break;
            }

            try {
                List<KOPIS_INDEX> performances = processPerformanceList(performanceList);
                allPerformances.addAll(performances);
            } catch (Exception e) {
                logger.warn("Page {} 처리 중 예외 발생: {}", currentPage, e.getMessage());
            }

            paramMap.put("cpage", String.valueOf(currentPage + 1));
        }

        try {
            kopisRepository.saveAll(allPerformances);
            logger.info("총 {}건 저장 완료", allPerformances.size());
        } catch (Exception e) {
            logger.error("bonsai 저장 중 오류 발생: {}", e.getMessage());
        }

        return CompletableFuture.completedFuture(allPerformances);
    }

    private JSONArray fetchJSONArray(String url) {
        try {
            String xml = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (xml == null || xml.isBlank()) return null;

            JSONObject jsonObject = XML.toJSONObject(xml);
            return jsonObject.getJSONObject("dbs").optJSONArray("db");
        } catch (Exception e) {
            logger.warn("API 응답 실패: {}", e.getMessage());
            return null;
        }
    }

    private List<KOPIS_INDEX> processPerformanceList(JSONArray performanceList) {
        List<KOPIS_INDEX> performanceEntities = new ArrayList<>();

        for (int i = 0; i < performanceList.length(); i++) {
            JSONObject performanceJson = performanceList.getJSONObject(i);
            String mt20id = performanceJson.optString("mt20id");

            if (kopisRepository.existsByCode(mt20id) || processedCodes.contains(mt20id)) continue;
            processedCodes.add(mt20id);

            String genre = performanceJson.optString("genrenm").toUpperCase();
            CATEGORY_LEVEL_TWO_INDEX category = categoryLevelTwoList.computeIfAbsent(genre, g -> {
                CATEGORY_LEVEL_TWO_INDEX newCategory = new CATEGORY_LEVEL_TWO_INDEX(g, CategoryLevelOne.PERFORMING_ARTS);
                categoryLevelTwoRepository.save(newCategory);
                return newCategory;
            });

            KOPIS_INDEX performance = new KOPIS_INDEX();
            performance.setCode(mt20id);
            performance.setCategoryLevelOne(CategoryLevelOne.PERFORMING_ARTS);
            performance.setCategoryLevelTwo(category);

            fetchPerformanceDetails(performance);
            performanceEntities.add(performance);
        }

        return performanceEntities;
    }

    private void fetchPerformanceDetails(KOPIS_INDEX performance) {
        try {
            String url = REQUEST_URL + "/" + performance.getCode() + "?service=" + AUTH_KEY;
            String xml = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (xml == null || xml.isBlank()) return;

            JSONObject detailJson = XML.toJSONObject(xml).getJSONObject("dbs").getJSONObject("db");

            performance.setName(detailJson.optString("prfnm"));
            performance.setFrom(new Date(SDCLTS.Converter(detailJson.optString("prfpdfrom"))));
            performance.setTo(new Date(SDCLTS.Converter(detailJson.optString("prfpdto"))));
            performance.setDirectors(detailJson.optString("prfcrew").split(","));
            performance.setActors(detailJson.optString("prfcast").split(","));
            performance.setCompanyNm(detailJson.optString("entrpsnmP").split(","));
            performance.setHoleNm(detailJson.optString("fcltynm"));
            performance.setPoster(detailJson.optString("poster"));
            performance.setStory(detailJson.optString("sty"));
            performance.setArea(detailJson.optString("area"));
            performance.setPrfState(PrfState.fromKorean(detailJson.optString("prfstate")));
            performance.setDtguidance(detailJson.optString("dtguidance").split(","));

            performance.setRelates(extractRelates(detailJson.opt("relates")));
            performance.setStyurls(extractStyUrls(detailJson.opt("styurls")));
            performance.setRunningTime(parseRuntime(detailJson.optString("prfruntime")));

        } catch (Exception e) {
            logger.warn("상세정보 조회 실패 ({}): {}", performance.getCode(), e.getMessage());
        }
    }

    private String[] extractRelates(Object relatesObj) {
        List<String> relatesList = new ArrayList<>();
        if (relatesObj instanceof JSONObject relatesJson) {
            Object relateData = relatesJson.opt("relate");
            if (relateData instanceof JSONArray jsonArray) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject r = jsonArray.optJSONObject(i);
                    if (r != null) relatesList.add(r.optString("relatenm") + " : " + r.optString("relateurl"));
                }
            } else if (relateData instanceof JSONObject r) {
                relatesList.add(r.optString("relatenm") + " : " + r.optString("relateurl"));
            }
        }
        return relatesList.toArray(new String[0]);
    }

    private String[] extractStyUrls(Object styUrlsObject) {
        List<String> styUrlList = new ArrayList<>();
        if (styUrlsObject instanceof JSONObject jsonObj) {
            Object styurlRaw = jsonObj.opt("styurl");
            if (styurlRaw instanceof JSONArray arr) {
                for (int i = 0; i < arr.length(); i++) {
                    String urlStr = arr.optString(i);
                    if (urlStr != null && !urlStr.isBlank()) styUrlList.add(urlStr);
                }
            } else if (styurlRaw instanceof String str) {
                if (!str.isBlank()) styUrlList.add(str);
            }
        }
        return styUrlList.toArray(new String[0]);
    }

    private long parseRuntime(String runTime) {
        int time = 0;
        if (runTime.contains("시간")) time = Integer.parseInt(runTime.split("시간")[0].trim()) * 60;
        if (runTime.contains("분")) time += Integer.parseInt(runTime.replaceAll(".*시간", "").replace("분", "").trim());
        return time;
    }
}
