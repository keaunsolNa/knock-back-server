package org.knock.knock_back.component.util;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

/**
 * @author nks
 * @apiNote Crawling 중 다음 페이지가 있을 경우 Selenium 이 제어하는 웹페이지를 조작한다. method invoke 방식 동작
 */
@Component
public class NextBtnWithCssSelector {

    private static final Logger logger = LoggerFactory.getLogger(NextBtnWithCssSelector.class);

    /**
     * 다음 페이지가 버튼으로 제어될 경우
     * @param driver 제어할 WebDriver 객체
     * @param cssSelector 작동할 버튼의 cssSelector
     */
    public static void nextBtn(WebDriver driver, String cssSelector)
    {
        try {

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // 최대 10초 대기

            while (true)
            {
                WebElement nextBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssSelector)));

                if (nextBtn.isDisplayed())
                {
                    nextBtn.click();
                    wait.until(ExpectedConditions.stalenessOf(nextBtn));
                }
                else
                {
                    break;
                }
            }
        }
        catch (Exception e)
        {
            logger.debug("오류 발생: {}", e.getMessage());
        }
    }

    /**
     * 무한 스크롤 방식으로 페이지 html 요소를 로드하는 경우
     * @param driver 제어할 WebDriver 객체
     * @param cssSelector 작동할 버튼의 cssSelector
     */

    public static void scrollDownUntilElementLoaded(WebDriver driver, String cssSelector) {
        try {

            JavascriptExecutor js = (JavascriptExecutor) driver;
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            int scrollCount = 0;
            int prevHeight = ((Long) Objects.requireNonNull(js.executeScript("return document.body.scrollHeight"))).intValue();

            while (scrollCount < 100) {

                // 스크롤을 아래로 내리기
                js.executeScript("window.scrollTo(0, document.body.scrollHeight);");

                // 새로운 요소가 로드되었는지 확인
                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssSelector)));
                } catch (TimeoutException e) {
                    break;
                }

                // 페이지 높이 변경 감지 (새로운 데이터가 로드되었는지 확인)
                Thread.sleep(1000);
                int newHeight = ((Long) Objects.requireNonNull(js.executeScript("return document.body.scrollHeight"))).intValue();

                if (newHeight == prevHeight) {
                    break;
                }

                prevHeight = newHeight;
                scrollCount++;
            }

        } catch (Exception e) {
            logger.debug("스크롤링 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
