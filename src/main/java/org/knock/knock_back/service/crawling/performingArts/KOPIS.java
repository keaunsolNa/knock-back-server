package org.knock.knock_back.service.crawling.performingArts;

import lombok.RequiredArgsConstructor;
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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class KOPIS {

    private static final Logger logger = LoggerFactory.getLogger(KOPIS.class);
    private final StringDateConvertLongTimeStamp SDCLTS = new StringDateConvertLongTimeStamp();
    private final CategoryLevelTwoRepository categoryLevelTwoRepository;
    private final KOPISRepository kopisRepository;
    private final WebClient webClient = WebClient.create();
    private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    @Value("${api.kopis.url}")
    private String REQUEST_URL;

    @Value("${api.kopis.key}")
    private String AUTH_KEY;

    private Map<String, CATEGORY_LEVEL_TWO_INDEX> categoryLevelTwoList;
    private final Map<String, Boolean> processedCodes = Collections.synchronizedMap(
            new LinkedHashMap<>(10000, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
                    return size() > 10000;
                }
            }
    );

    private String makeQueryString(Map<String, String> paramMap) {
        StringBuilder sb = new StringBuilder();
        paramMap.forEach((key, value) -> {
            if (!sb.isEmpty()) sb.append('&');
            sb.append(key).append('=').append(value);
        });
        return sb.toString();
    }

    @Async
    public void requestAPIAsync() {
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

        List<KOPIS_INDEX> buffer = Collections.synchronizedList(new ArrayList<>());
        final int batchSize = 100;
        List<Future<?>> futures;
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            futures = new ArrayList<>();

            while (true) {
                int currentPage = Integer.parseInt(paramMap.get("cpage"));
                if (currentPage > 100) break;

                String url = REQUEST_URL + "?" + makeQueryString(paramMap);
                List<String> mt20idList = fetchMt20idListFromXml(url);

                if (mt20idList.isEmpty()) {
                    logger.warn("Page {} 응답 실패 또는 결과 없음. 종료.", currentPage);
                    break;
                }

                for (String mt20id : mt20idList) {
                    if (kopisRepository.existsByCode(mt20id) || processedCodes.containsKey(mt20id)) continue;
                    processedCodes.put(mt20id, true);

                    futures.add(executor.submit(() -> {
                        try {
                            KOPIS_INDEX performance = new KOPIS_INDEX();
                            performance.setCode(mt20id);
                            fetchPerformanceDetails(performance);
                            synchronized (buffer) {
                                buffer.add(performance);
                                if (buffer.size() >= batchSize) {
                                    List<KOPIS_INDEX> tempBatch = new ArrayList<>(buffer);
                                    buffer.clear();
                                    kopisRepository.saveAll(tempBatch);
                                }
                            }
                        } catch (Exception e) {
                            logger.warn("병렬 처리 중 예외 발생 ({}): {}", mt20id, e.getMessage());
                        }
                    }));
                }

                paramMap.put("cpage", String.valueOf(currentPage + 1));
            }

            executor.shutdown();
        }
        try {
            for (Future<?> future : futures) future.get();
        } catch (Exception e) {
            logger.error("스레드 작업 대기 중 오류: {}", e.getMessage());
        }

        if (!buffer.isEmpty()) {
            kopisRepository.saveAll(buffer);
        }
    }


    private List<String> fetchMt20idListFromXml(String url) {
        List<String> mt20ids = new ArrayList<>();
        try {
            String xml = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (xml == null || xml.isBlank()) return mt20ids;

            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new StringReader(xml));

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT && "mt20id".equals(reader.getLocalName())) {
                    mt20ids.add(reader.getElementText());
                }
            }
            reader.close();
        } catch (Exception e) {
            logger.warn("StAX XML 파싱 실패: {}", e.getMessage());
        }
        return mt20ids;
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

            XMLStreamReader reader = xmlInputFactory.createXMLStreamReader(new StringReader(xml));
            Map<String, String> detailMap = new HashMap<>();
            List<String> relates = new ArrayList<>();
            List<String> styurls = new ArrayList<>();
            String currentRelateNm = null;
            String currentRelateUrl;

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    String tag = reader.getLocalName();
                    String value = reader.getElementText();
                    switch (tag) {
                        case "relatenm" -> currentRelateNm = value;
                        case "relateurl" -> {
                            currentRelateUrl = value;
                            if (currentRelateNm != null) {
                                relates.add(currentRelateNm + " : " + currentRelateUrl);
                                currentRelateNm = null;
                            }
                        }
                        case "styurl" -> {
                            if (value != null && !value.isBlank()) styurls.add(value);
                        }
                        default -> detailMap.put(tag, value);
                    }
                }
            }
            reader.close();

            String genre = detailMap.getOrDefault("genrenm", "기타").toUpperCase();
            CATEGORY_LEVEL_TWO_INDEX category;
            synchronized (categoryLevelTwoList) {
                category = categoryLevelTwoList.get(genre);
                if (category == null) {
                    category = new CATEGORY_LEVEL_TWO_INDEX(genre, CategoryLevelOne.PERFORMING_ARTS);
                    categoryLevelTwoRepository.save(category);
                    categoryLevelTwoList.put(genre, category);
                }
            }

            performance.setCategoryLevelOne(CategoryLevelOne.PERFORMING_ARTS);
            performance.setCategoryLevelTwo(category);
            performance.setName(detailMap.get("prfnm"));
            performance.setFrom(new Date(SDCLTS.Converter(detailMap.get("prfpdfrom"))));
            performance.setTo(new Date(SDCLTS.Converter(detailMap.get("prfpdto"))));
            performance.setDirectors(splitComma(detailMap.get("prfcrew")));
            performance.setActors(splitComma(detailMap.get("prfcast")));
            performance.setCompanyNm(splitComma(detailMap.get("entrpsnmP")));
            performance.setHoleNm(detailMap.get("fcltynm"));
            performance.setPoster(detailMap.get("poster"));
            performance.setStory(detailMap.get("sty"));
            performance.setArea(detailMap.get("area"));
            performance.setPrfState(PrfState.fromKorean(detailMap.get("prfstate")));
            performance.setDtguidance(splitComma(detailMap.get("dtguidance")));
            performance.setRunningTime(parseRuntime(detailMap.getOrDefault("prfruntime", "")));
            performance.setRelates(relates.toArray(new String[0]));
            performance.setStyurls(styurls.toArray(new String[0]));

        } catch (Exception e) {
            logger.warn("상세정보 StAX 파싱 실패 ({}): {}", performance.getCode(), e.getMessage());
        }
    }

    private String[] splitComma(String s) {
        return (s == null || s.isBlank()) ? new String[0] : s.split("\\s*,\\s*");
    }

    private long parseRuntime(String runTime) {
        int time = 0;
        if (runTime.contains("시간")) time = Integer.parseInt(runTime.split("시간")[0].trim()) * 60;
        if (runTime.contains("분")) time += Integer.parseInt(runTime.replaceAll(".*시간", "").replace("분", "").trim());
        return time;
    }
}
