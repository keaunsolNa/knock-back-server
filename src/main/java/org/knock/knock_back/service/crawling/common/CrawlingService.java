package org.knock.knock_back.service.crawling.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.knock.knock_back.component.util.converter.ConvertDTOAndIndex;
import org.knock.knock_back.dto.document.movie.KOFIC_INDEX;
import org.knock.knock_back.dto.document.movie.MOVIE_INDEX;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CrawlingService extends AbstractCrawlingService {

    private final ConvertDTOAndIndex movieDtoToIndex;
    private final Map<String, CrawlingConfig> sourceConfigMap;
    private CrawlingConfig currentConfig;
    private final Logger logger = LoggerFactory.getLogger(CrawlingService.class);

    protected CrawlingService(Movie movieService, ConvertDTOAndIndex movieDtoToIndex,
                              CrawlingProperties crawlingProperties) {
        super(movieService);
        this.movieDtoToIndex = movieDtoToIndex;
        this.sourceConfigMap = crawlingProperties.getSources();
    }

    public void startCrawling(String sourceName) {

        logger.info("Current config: {}", sourceConfigMap.get(sourceName.toLowerCase()));
        this.currentConfig = sourceConfigMap.get(sourceName.toLowerCase());
        if (this.currentConfig == null) {
            throw new IllegalArgumentException("Invalid source name: " + sourceName);
        }
        addNewIndex();
    }

    @Override
    protected String getUrlPath() {
        return currentConfig.getUrl();
    }

    @Override
    protected String getCssQuery() {
        return currentConfig.getCssQuery1();
    }

    @Override
    protected String[] prepareCss() {
        return new String[]{currentConfig.getCssQuery2(), currentConfig.getCssQuery3()};
    }

    @Override
    protected void processElement(Element element, Set<MOVIE_DTO> dtos) {
        String title = extractTitle(element);
        if (title == null || title.isBlank()) return;

        MOVIE_DTO dto;
        Optional<MOVIE_INDEX> existing = movieService.checkMovie(title);
        if (existing.isPresent()) {
            dto = movieDtoToIndex.MovieIndexToDTO(existing.get());
            updatePartialMovieData(element, dto);
            dtos.add(dto);
            return;
        }

        KOFIC_INDEX kofic = findKoficBySimilarity(title, element);
        dto = (kofic != null) ? movieDtoToIndex.koficIndexToMovieDTO(kofic) : new MOVIE_DTO();
        dto.setOpeningTime(extractOpeningDate(element));

        setReservationLink(element, dto);
        setPoster(element, dto);
        setPlot(element, dto);

        dtos.add(dto);
    }

    private String extractTitle(Element element) {
        try {
            Elements titleElements = element.select(currentConfig.getTitleQuery());
            return Objects.requireNonNull(titleElements.first()).text();
        } catch (Exception e) {
            logger.debug("제목 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    private void updatePartialMovieData(Element element, MOVIE_DTO dto) {
        setReservationLink(element, dto);
        if (null == dto.getPosterBase64() || dto.getPosterBase64().isEmpty()) setPoster(element, dto);
        if (dto.getPlot() == null || dto.getPlot().isBlank() || dto.getPlot().equals("정보없음")) setPlot(element, dto);
    }

    private KOFIC_INDEX findKoficBySimilarity(String title, Element element) {
        try {
            return movieService.similaritySearch(title);
        } catch (Exception e) {
            String directorName = extractDirectorNameFromDetailPage(element);
            return movieService.similaritySearch(title, directorName);
        }
    }

    private String extractDirectorNameFromDetailPage(Element element) {
        // Simplified & unified director extraction for brevity and maintenance
        try {
            String detailUrl = extractDetailUrl(element);

            ElementExtractor extractor = new ElementExtractor(detailUrl, currentConfig.getPlotQuery());
            extractor.setUpDriver();
            extractor.run();
            WebDriver driver = extractor.getDriver();
            driver.get(detailUrl);

            WebElement directorElement = switch (currentConfig.getName()) {
                case "MEGABOX" -> driver.findElement(By.xpath("//div[@class='botInfo']/li[span[contains(text(), '감독')]]"));
                case "LOTTE" -> driver.findElement(By.xpath("//em[contains(text(), '감독')]/following-sibling::span[contains(@class, 'line_type')]/a"));
                case "CGV" -> driver.findElement(By.xpath("//div[@class='spec']//dt[contains(text(), '감독')]/following-sibling::dd[1]/a"));
                default -> null;
            };
            return (directorElement != null) ? directorElement.getText().trim() : "";
        } catch (Exception e) {
            logger.warn("감독 이름 추출 실패: {}", e.getMessage());
            return "";
        }
    }

    private String extractDetailUrl(Element element) {
        Elements reservationElement = element.select(currentConfig.getReservationQuery());
        return currentConfig.getReservationPrefix() + reservationElement.attr(currentConfig.getReservationExtract());
    }

    private String extractOpeningDate(Element element) {
        Elements dateElements = element.select(currentConfig.getDateQuery());
        if (dateElements.isEmpty()) return "미정";

        String rawDate = dateElements.text().trim();
        if (currentConfig.getName().equals("LOTTE") && rawDate.matches("D-\\d+")) {
            int daysRemaining = Integer.parseInt(rawDate.replace("D-", ""));
            return LocalDate.now().plusDays(daysRemaining).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        }
        return rawDate.replace(currentConfig.getDateExtract(), "").trim();
    }

    private void setReservationLink(Element element, MOVIE_DTO dto) {
        Elements reservationElement = element.select(currentConfig.getReservationQuery());
        if (reservationElement.isEmpty()) return;

        String link = reservationElement.attr(currentConfig.getReservationExtract());
        if (currentConfig.getName().equals("CGV")) {
            Matcher matcher = Pattern.compile("fnQuickReserve\\('(\\d+)'").matcher(link);
            if (matcher.find()) link = matcher.group(1);
            else {
                logger.warn("CGV 예매 링크 파싱 실패: {}", link);
                return;
            }
        }

        if (!currentConfig.getName().equals("LOTTE")) {
            link = currentConfig.getReservationPrefix() + link;
        }

        String[] reservationLinks = dto.getReservationLink() != null ? dto.getReservationLink() : new String[3];
        int idx = switch (currentConfig.getName()) {
            case "MEGABOX" -> 0;
            case "CGV" -> 1;
            case "LOTTE" -> 2;
            default -> -1;
        };
        if (idx >= 0) reservationLinks[idx] = link;
        dto.setReservationLink(reservationLinks);
    }

    private void setPoster(Element element, MOVIE_DTO dto) {
        Elements imgElement = element.select(currentConfig.getPosterQuery());

        if (imgElement.isEmpty()) return;

        String srcPath = Objects.requireNonNull(imgElement.first()).attr(currentConfig.getPosterExtract());
        if (srcPath.contains("//") && currentConfig.getName().equals("LOTTE")) {
            srcPath = srcPath.replace("//", "/");
        }
//            InputStream in = URI.create(srcPath).toURL().openStream();
//
//            byte[] imageBytes = in.readAllBytes();
//            String base64 = Base64.getEncoder().encodeToString(imageBytes);
        dto.setPosterBase64(srcPath);

    }

    private void setPlot(Element element, MOVIE_DTO dto) {
        try {
            String plot = switch (currentConfig.getName()) {
                case "CGV" -> Objects.requireNonNull(Jsoup.connect(currentConfig.getDetailPrefix() + element.className().replaceAll("\\D+", ""))
                        .get().selectFirst(currentConfig.getPlotQuery())).attr("content");
                case "LOTTE" -> extractLottePlot(element);
                default -> element.select(currentConfig.getPlotQuery()).text();
            };
            dto.setPlot((plot == null || plot.isBlank()) ? "정보없음" : plot);
        } catch (Exception e) {
            logger.warn("줄거리 추출 실패: {}", e.getMessage());
            dto.setPlot("정보없음");
        }
    }

    private String extractLottePlot(Element element) {
        Elements detailLinks = element.select("a.btn_col3.ty3");
        for (Element link : detailLinks) {
            Matcher matcher = Pattern.compile("movie=(\\d+)(?:&|$)").matcher(link.attr("href"));
            if (matcher.find()) {
                String idValue = matcher.group(1);
                ElementExtractor extractor = new ElementExtractor(currentConfig.getDetailPrefix() + idValue, currentConfig.getPlotQuery());
                extractor.setUpDriver();
                extractor.preparePage(extractor.getDriver(), new String[]{currentConfig.getCssQuery2(), currentConfig.getPlotQuery()});
                extractor.run();
                return extractor.getElements().stream().findFirst().map(e -> e.attr("content")).orElse("정보없음");
            }
        }
        return "정보없음";
    }
}
