package org.knock.knock_back.service.crawling.common;

import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.knock.knock_back.component.util.maker.WebDriverUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Objects;

/**
 * @author nks
 * @apiNote 영화 크롤링을 위한 Interface, 추상 메서드를 정의한다.
 * 각각의 크롤링 서비스들은 반드시 해당 클래스를 상속받아 구현해야 한다.
 * 해당 클래스를 통해 WebDriver 와 같은 무거운 객체를 싱글턴 패턴으로 생성하고 재활용한다. 
 * synchronized 를 통해 멀티스레드 방식 유지
 */
@Service
public interface CrawlingInterface {

    Logger logger = LoggerFactory.getLogger(CrawlingInterface.class);

    /**
     * 모든 크롤링 클래스는 addNewIndex() 메서드 오버로딩 필요
     */
    void addNewIndex();

    /**
     * 크롤링할 주소 (urlPath), 크롤링할 주소에 있는 css 선택자 (cssQuery), WebDriver 객체를 받아 Element 를 생성한다.
     * 멀티 스레드 방식을 위해 WebDriver 객체는 ThreadLocal 
     * Getter 통해 크롤링한 html 페이지의 특정 요소들과 driver 객체를 가져올 수 있다.
     */
    class ElementExtractor implements Runnable {

        private final String urlPath;
        private final String cssQuery;
        private final ThreadLocal<WebDriver> driverThreadLocal = ThreadLocal.withInitial(WebDriverUtil::getChromeDriver);

        @Getter
        private Elements elements;
        @Getter
        private WebDriver driver;

        public ElementExtractor(String urlPath, String cssQuery) {
            this.urlPath = urlPath;
            this.cssQuery = cssQuery;
        }

        /**
         * Webdriver 생성
         */
        public void setUpDriver()
        {
            driver = driverThreadLocal.get();
            if (urlPath == null) {
                logger.warn("urlPath is null");
                return;
            }

            driver.navigate().to(urlPath);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        }

        /**
         * urlPath 에 있는 html 요소들을 페이지의 형식 (다음 페이지가 있는 경우, Infinity Scroll 인 경우에 따라
         * 모든 HTML 요소가 로딩될 수 있도록 웹 페이지를 제어한다. 
         *  편의를 위해 method invoke 방식 사용
         */
        public void preparePage(WebDriver driver, String[] names)
        {
            try {

                Class<?> clazz = Class.forName("org.knock.knock_back.component.util.NextBtnWithCssSelector");
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                Object instance = constructor.newInstance();

                Method method = clazz.getMethod(names[0], WebDriver.class, String.class);

                method.invoke(instance, driver, names[1]);

            } catch (Exception e) {
                logger.warn("Error in preparePage: {}", e.getMessage());
            }
        }

        /**
         * HTML 요소를 가져온 뒤 synchronized 방식으로 요소들을 Document 타입으로 파싱한다.
         */
        @Override
        public void run() {

            Document urlDoc;
            synchronized (this) {
                urlDoc = Jsoup.parse(Objects.requireNonNull(driver.getPageSource()));
            }

            synchronized (this) {
                elements = urlDoc.select(cssQuery);
            }
        }
    }
}
