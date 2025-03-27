package org.knock.knock_back.service.crawling.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.knock.knock_back.component.util.converter.ConvertDTOAndIndex;
import org.knock.knock_back.dto.document.movie.KOFIC_INDEX;
import org.knock.knock_back.dto.dto.crawling.CrawlingConfig;
import org.knock.knock_back.dto.dto.crawling.CrawlingProperties;
import org.knock.knock_back.dto.dto.movie.MOVIE_DTO;
import org.knock.knock_back.service.layerClass.Movie;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nks
 * @apiNote CrawlingInterface 를 구현한 AbstractCrawlingService 추상 Interface 를 상속받아
 *          영화 (CGV, MEGABOX, LOTTE CINEMA) 페이지들을 크롤링한다.
 *          가급적 application.yml 파일에 정의되어 있는 요소들을 통해 제어한다.
 */
@Service
public class CrawlingService extends AbstractCrawlingService {

    private final ConvertDTOAndIndex movieDtoToIndex;
    private final Map<String, CrawlingConfig> sourceConfigMap;
    Logger logger = LoggerFactory.getLogger(CrawlingService.class);

    protected CrawlingService(Movie movieService, ConvertDTOAndIndex movieDtoToIndex,
                              CrawlingProperties crawlingProperties) {
        super(movieService);
        this.movieDtoToIndex = movieDtoToIndex;
        this.sourceConfigMap = crawlingProperties.getSources();
    }

    private CrawlingConfig currentConfig;

    /**
     * 서비스 클래스를 호출하기 위한 진입 포인트. currentConfig 검증 후 addNewIndex() 메서드 실행
     */
    public void startCrawling(String sourceName) {

        logger.info("Current config: {}", sourceConfigMap.get(sourceName.toLowerCase()));
        this.currentConfig = sourceConfigMap.get(sourceName.toLowerCase());

        logger.info(this.currentConfig.toString());
        if (this.currentConfig == null) {
            throw new IllegalArgumentException("Invalid source name: " + sourceName);
        }
        addNewIndex();
    }

    /**
     * application.yml 파일에 정의된 각 영화사 별 urlPath 가져온다.
     */
    @Override
    protected String getUrlPath() {
        return currentConfig.getUrl();
    }

    /**
     * application.yml 파일에 정의된 각 영화사 별 cssQuery 가져온다.
     */
    @Override
    protected String getCssQuery() {
        return currentConfig.getCssQuery1();
    }

    /**
     * application.yml 파일에 정의된 각 영화사 별 cssQuery 가져온 뒤 
     * 부모 객체의 prepareCss() 메서드를 통해 HTML 페이지를 크롤링 하기 전 
     * 무한 스크롤, 다음 페이지 등의 요소에 대응한다.
     */
    @Override
    protected String[] prepareCss() {
        return new String[]{ currentConfig.getCssQuery2(), currentConfig.getCssQuery3() };
    }

    /**
     * 실제 크롤링 구현, 부모 객체를 통해 공유 자원에 요소들을 넣어 개별 객체를 생성한다.
     * @param element : 부모 객체에서 생성된 크롤링할 HTML 페이지 요소
     * @param dtos : 부모 객체에서 공유 자원인 Set<MOVIE_DTO> 객체
     */
    @Override
    protected void processElement(Element element, Set<MOVIE_DTO> dtos) {

        MOVIE_DTO dto = new MOVIE_DTO();

        /*
         * 영화 제목 가져오기
         */
        Elements titleElements = element.select(currentConfig.getTitleQuery());

        String title;
        try
        {
            /*
             * Title 없다면 에러 띄우고 리턴
             * Lotte 의 경우, 광고 요소가 HTML 페이지에 같이 있어
             * 이에 대응하기 위함
             */
            title = Objects.requireNonNull(titleElements.first()).text();
        }
        catch (NullPointerException e)
        {
            logger.debug(e.getMessage());
            return;
        }

        /*
         * 크롤링 한 제목이 MOVIE_INDEX 이미 있다면
         * 중복으로 모든 크롤링을 진행하지 않는다.
         * 각 영화사 벤더별 예매 링크가 다르기에 해당 부분과
         * 이전 영화서 벤더에서 poster 정보가 없었을 경우 해당 부분
         * plot 이 없었을 경우 해당 부분을 추가한 뒤 반환한다.
         */
        if (movieService.checkMovie(title).isPresent()) {
            logger.info("{} Already Exists Movie ", title);
            dto = movieDtoToIndex.MovieIndexToDTO(movieService.checkMovie(title).get());
            setReservationLink(element, dto);
            if (dto.getPosterBase64().isEmpty()) setPoster(element, dto);
            if (null == dto.getPlot() || dto.getPlot().equals("정보없음")) setPlot(element, dto);

            dtos.add(dto);
            return;
        }

        /*
         * KOFIC INDEX 에 유사한 제목이 있는지 검색한다.
         * equal 검색이 아닌 이유는 영화사별 영화 제목이 다를 수 있기 때문 (영문, 한글, 띄어쓰기 등)
         * KOFIC 에서 영화 정보를 가져온다.
         * 이 때 _score 가 동일한 영화가 있을 경우, 감독을 포함하여 재검색하여 정합도를 높인다.
         */
        KOFIC_INDEX kofic = null;

        try
        {
            kofic = movieService.similaritySearch(title);
        }
        catch (Exception e)
        {

            /*
             * _score 가 동일하다면 감독 정보가 필요하다.
             * 감독 정보는 목록 페이지에 없으므로, 세부 페이지로 다시 연결이 필요
             */
            switch (currentConfig.getName()) {
                case "MEGABOX" -> {
                    Elements reservationElement = element.select(currentConfig.getReservationQuery());
                    String detailLinks = currentConfig.getReservationPrefix() + reservationElement.attr(currentConfig.getReservationExtract());

                    ElementExtractor extractor3 = new ElementExtractor(detailLinks, currentConfig.getPlotQuery());
                    extractor3.setUpDriver();
                    extractor3.run();

                    WebDriver driver = extractor3.getDriver();
                    driver.get(detailLinks);

                    String directorName;
                    try {
                        WebElement directorElement = driver.findElement(By.xpath("//div[@class='botInfo']/li[span[contains(text(), '감독')]]"));
                        directorName = directorElement.getText().replace("감독", "").trim();
                    } catch (Exception xPathExceptionE) {
                        directorName = "";
                    }

                    kofic = movieService.similaritySearch(title, directorName);

                }
                case "LOTTE" -> {

                    Elements detailLinks = element.select("a.btn_col3.ty3");
                    String idValue = "";

                    for (Element link : detailLinks) {
                        String hrefValue = link.attr("href");

                        Pattern pattern = Pattern.compile("movie=(\\d+)(?:&|$)");
                        Matcher matcher = pattern.matcher(hrefValue);

                        if (matcher.find()) {
                            idValue = matcher.group(1); // 첫 번째 그룹 (숫자 ID) 추출
                            break; // 첫 번째로 발견된 올바른 movie= 값을 사용하고 루프 종료
                        }
                    }

                    ElementExtractor extractor3 = new ElementExtractor(currentConfig.getDetailPrefix() + idValue, currentConfig.getPlotQuery());
                    extractor3.setUpDriver();
                    extractor3.run();

                    WebDriver driver = extractor3.getDriver();
                    // 롯데시네마 영화 상세 페이지 열기
                    driver.get(currentConfig.getDetailPrefix() + idValue);

                    String directorName;

                    try {
                        WebElement directorElement = driver.findElement(By.xpath("//em[contains(text(), '감독')]/following-sibling::span[contains(@class, 'line_type')]/a"));
                        directorName = Objects.requireNonNull(directorElement).getText();
                    } catch (Exception xPathExceptionE) {
                        directorName = "";
                    }

                    kofic = movieService.similaritySearch(title, directorName);

                }
                case "CGV" -> {
                    Elements reservationElement = element.select(currentConfig.getReservationQuery());
                    String detailLinks = currentConfig.getReservationPrefix() + reservationElement.attr(currentConfig.getReservationExtract());

                    ElementExtractor extractor3 = new ElementExtractor(detailLinks, currentConfig.getPlotQuery());
                    extractor3.setUpDriver();
                    extractor3.run();

                    WebDriver driver = extractor3.getDriver();
                    driver.get(detailLinks);

                    String directorName;

                    try {
                        WebElement directorElement = driver.findElement(By.xpath("//div[@class='spec']//dt[contains(text(), '감독')]/following-sibling::dd[1]/a"));
                        directorName = directorElement.getText().trim();
                    } catch (Exception xPathExceptionE) {
                        directorName = "";
                    }

                    kofic = movieService.similaritySearch(title, directorName);
                }
            }
        }


        if (kofic != null) {
            dto = movieDtoToIndex.koficIndexToMovieDTO(kofic);
        }

        /*
         * 상영일자 가져오기
         */
        Elements dateElements = element.select(currentConfig.getDateQuery());

        if (!dateElements.isEmpty()) {

            /*
             * LOTTE CINEMA 경우 개봉일이 yyyyMMdd 가 아닌 D-X 형식으로 되어 있기에, 해당 부분 대응
             */
            String date;
            if (currentConfig.getName().equals("LOTTE"))
            {
                date = convertReleaseDate(dateElements);
            }
            else
            {
                date = Objects.requireNonNull(dateElements.first()).text().replace(currentConfig.getDateExtract(), "");
                date = date.trim();
            }
            dto.setOpeningTime(date);
        }

        /*
         * 예매 링크 가져오기
         */
        setReservationLink(element, dto);
        /*
         * 포스터 가져오기
         */
        setPoster(element, dto);
        /*
         * Plot 가져오기
         */
        setPlot(element, dto);

        /*
         * 공유 자원에 신규 MOVIE_INDEX 객체 추가
         */
        dtos.add(dto);
    }

    /**
     * 예매 링크 크롤링
     * @param element : 해당 HTML 페이지 요소
     * @param dto : 해당 영화 객체
     */
    private void setReservationLink(Element element, MOVIE_DTO dto) {

        /*
         * application.yml 에 정의된 css 선택자 요소로 해당 값 가져오기
         */
        Elements reservationElement = element.select(currentConfig.getReservationQuery());

        if (!reservationElement.isEmpty()) {

            /*
             * 예매 링크 a href 요소 가져오기
             */
            String reservationLink = reservationElement.attr(currentConfig.getReservationExtract());

            /*
             * 영화사 벤더별 대응
             */
            if (currentConfig.getName().equals("CGV"))
            {
                Pattern pattern = Pattern.compile("fnQuickReserve\\('(\\d+)'");
                Matcher matcher = pattern.matcher(reservationLink);
                if (matcher.find()) {
                    reservationLink = matcher.group(1);
                } else {
                    logger.warn("No Movie ID Found in OnClick: {}", reservationLink);
                    return;
                }

            }

            /*
             * 영화사 벤더별 대응
             */
            if (!currentConfig.getName().equals("LOTTE")) reservationLink = currentConfig.getReservationPrefix() + reservationLink;

            String[] reservationLinks;
            int idx = currentConfig.getName().equals("MEGABOX") ? 0 : currentConfig.getName().equals("CGV") ? 1 : 2;

            if (dto.getReservationLink() == null) {
                reservationLinks = new String[3];
            } else {
                reservationLinks = dto.getReservationLink();
            }
            reservationLinks[idx] = reservationLink;
            dto.setReservationLink(reservationLinks);
        }
    }

    // 포스터 이미지 가져온 뒤 파일 생성, base64 encoding 후 저장
    private void setPoster(Element element, MOVIE_DTO dto)
    {

        Elements imgElement = element.select(currentConfig.getPosterQuery());
        // TODO : 이미지 없을 때 이미지 base64 파일
        String base64File;

        if (!imgElement.isEmpty())
        {
            String srcPath = Objects.requireNonNull(imgElement.first()).attr(currentConfig.getPosterExtract());
            String outPath = "//";
            if (srcPath.contains("//") && currentConfig.getName().equals("LOTTE")) { srcPath = srcPath.replace("//", "/"); }

            try {
                InputStream in = new URL(srcPath).openStream();
                OutputStream out = new FileOutputStream(outPath);
                byte[] buffer = new byte[4096];
                int n;

                while ((n = in.read(buffer)) != -1)
                {
                    out.write(buffer, 0, n);
                }

                byte[] fileContent = Files.readAllBytes(new File(outPath).toPath());

                base64File = Base64.getEncoder().encodeToString(fileContent);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            dto.setPosterBase64(base64File);
        }
    }

    // 줄거리 가져오기
    private void setPlot (Element element, MOVIE_DTO dto) {

        /*
         * 영화사 벤더별 대응
         */
        if (currentConfig.getName().equals("CGV"))
        {
            try
            {
                String classAttribute = element.className();
                String seqValue = classAttribute.replaceAll("\\D+", ""); // 숫자만 추출
                Document doc = Jsoup.connect(currentConfig.getDetailPrefix() + seqValue).get();
                Element metaTag =  doc.selectFirst(currentConfig.getPlotQuery());

                if (metaTag != null)
                {
                    String content = metaTag.attr("content");
                    dto.setPlot(content);
                }

            }
            catch (IOException e)
            {
                logger.debug(e.getMessage());
            }
        }

        /*
         * 영화사 벤더별 대응
         */
        else if (currentConfig.getName().equals("LOTTE"))
        {

            Elements detailLinks = element.select("a.btn_col3.ty3");
            String idValue = "";

            for (Element link : detailLinks) {
                String hrefValue = link.attr("href");

                Pattern pattern = Pattern.compile("movie=(\\d+)(?:&|$)");
                Matcher matcher = pattern.matcher(hrefValue);

                if (matcher.find()) {
                    idValue = matcher.group(1); // 첫 번째 그룹 (숫자 ID) 추출
                    break; // 첫 번째로 발견된 올바른 movie= 값을 사용하고 루프 종료
                }
            }

            ElementExtractor extractor2 = new ElementExtractor(currentConfig.getDetailPrefix() + idValue, currentConfig.getPlotQuery());
            extractor2.setUpDriver();
            extractor2.preparePage(extractor2.getDriver(), new String[] {currentConfig.getCssQuery2(), currentConfig.getPlotQuery()});
            extractor2.run();

            Elements elements = extractor2.getElements();

            for (Element elementValue : elements) {
                String content = elementValue.attr("content");
                dto.setPlot(content);
            }

        }

        /*
         * 영화사 벤더별 대응
         */
        else
        {
            Elements codeElement = element.select(currentConfig.getPlotQuery());

            if(!codeElement.isEmpty()) {

                String summeryText = codeElement.text();
                dto.setPlot(summeryText);

            }
        }

        if (null == dto.getPlot() || dto.getPlot().isEmpty() || dto.getPlot().isBlank())
        {
            dto.setPlot("정보없음");
        }

    }

    /**
     * "D-XX" 형식의 날짜를 실제 개봉일(YYYY.MM.DD)로 변환
     */
    private String convertReleaseDate(Elements dateElements) {
        if (!dateElements.isEmpty()) {
            String remainText = dateElements.text().trim();

            // "D-XX" 형식인지 확인
            if (remainText.matches("D-\\d+")) {
                int daysRemaining = Integer.parseInt(remainText.replace("D-", ""));
                LocalDate releaseDate = LocalDate.now().plusDays(daysRemaining);
                return releaseDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
            }
        }
        return "미정";  // 개봉일 정보가 없을 경우
    }

}
