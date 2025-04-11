package org.knock.knock_back.component.util.maker;

import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;

/**
 * @author nks
 * @apiNote WebDriver 설정을 제어하고 생성된 객체를 반환한다.
 * Heroku 배포 환경의 경우, 메모리 제한으로 인해 크롤링을 실행하지 않는다.
 * 크롤링은 로컬 환경에서 생성하며, 추후 파이썬으로 별도 서버 올릴 예정
 * 그렇기에 해당 컴포넌트는 Bean 등록하지 않는다.
 */
public class WebDriverUtil {

    private static final Logger logger = LoggerFactory.getLogger(WebDriverUtil.class);
    /**
     * ChromeDriver 옵션 지정 및 생성
     * @return 생성된 WebDriver 객체
     */
    public static WebDriver getChromeDriver() {
        String uniqueTempDir = "/tmp/chrome_user_data_" + UUID.randomUUID();
        Path tempDirPath = Paths.get(uniqueTempDir);
        try {
            Files.createDirectories(tempDirPath);
            logger.info("Created user-data-dir: {}", uniqueTempDir);  // 디렉토리 생성 로그
        } catch (Exception e) {
            logger.warn("Failed to create temp directory for Chrome user data: {}", e.getMessage());
        }

        // 기존 크롬 프로세스 종료 (충돌 방지)
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("pkill", "-f", "chrome");
            processBuilder.start();
            logger.info("Attempted to kill existing Chrome processes.");
        } catch (Exception e) {
            logger.warn("Failed to kill existing Chrome processes: {}", e.getMessage());
        }

        ChromeOptions options = getChromeOptions(uniqueTempDir);
        logger.info("Chrome option set: --user-data-dir={}", uniqueTempDir);

        // Heroku ChromeDriver 실행 경로 설정
        File driverExecutable = new File("/app/.chrome-for-testing/chromedriver-linux64/chromedriver");
        if (!driverExecutable.exists()) {
            logger.warn("❌ ChromeDriver not found at: {}", driverExecutable.getAbsolutePath());
            throw new RuntimeException("ChromeDriver not found!");
        }

        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingDriverExecutable(driverExecutable)
                .usingAnyFreePort()
                .build();

        try {
            service.start();
        } catch (Exception e) {
            logger.warn("❌ Failed to start ChromeDriver service: {}", e.getMessage());
            throw new RuntimeException("ChromeDriver service failed to start");
        }

        WebDriver driver = new ChromeDriver(service, options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(100));

        logger.info("ChromeDriver started successfully!");

        return driver;

    }

    @NotNull
    private static ChromeOptions getChromeOptions(String uniqueTempDir) {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--log-level=3");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--blink-settings=imagesEnabled=false");
        options.addArguments("--disable-notifications");

        // CDP 버전 경고 무시 옵션 추가
        options.addArguments("--disable-build-check");

        // 고유한 user-data-dir 설정
        options.addArguments("--user-data-dir=" + uniqueTempDir);
        return options;
    }

}
