package org.knock.knock_back.service.crawling.common;

import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import org.knock.knock_back.dto.dto.movie.MOVIE_DTO;
import org.knock.knock_back.service.layerClass.Movie;

import java.util.HashSet;
import java.util.Set;

/**
 * @author nks
 * @apiNote Crawling 작업을 실행 할 때 사용할 추상 클래스
 */
public abstract class AbstractCrawlingService implements CrawlingInterface {

    protected final Movie movieService;
    protected AbstractCrawlingService(Movie movieService) {
        this.movieService = movieService;
    }

    protected abstract String getUrlPath();
    protected abstract String getCssQuery();
    protected abstract String[] prepareCss();
    protected abstract void processElement(Element element, Set<MOVIE_DTO> dtos);

    /**
     * driver 설정, page 준비 (스크롤, 다음 페이지 버튼 등), 크롤링 작업을 실행 한 뒤
     * Elements 를 가져와 구체 클래스에서 DTO 로 만들고
     * 만든 정보를 INDEX 에 저장한다.
     */
    @Override
    public void addNewIndex() {

        ElementExtractor extractor = new ElementExtractor(getUrlPath(), getCssQuery());
        extractor.setUpDriver();
        extractor.preparePage(extractor.getDriver(), prepareCss());

        extractor.run();

        Elements elements = extractor.getElements();
        logger.info(elements.toString());
        Set<MOVIE_DTO> dtos = new HashSet<>();
        for (Element element : elements) {
            processElement(element, dtos);
        }

        logger.info(dtos.toString());
        saveData(dtos);
    }

    /**
     * @param dtos : 저장할 대상의 DTO
     * DTO 정보를 받아 인덱스에 저장한다.
     */
    protected void saveData(Set<MOVIE_DTO> dtos) {

        movieService.createMovie(dtos);
    }

}
