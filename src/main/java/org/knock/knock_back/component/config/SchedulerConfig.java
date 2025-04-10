package org.knock.knock_back.component.config;

import lombok.RequiredArgsConstructor;
import org.knock.knock_back.service.crawling.common.CrawlingService;
import org.knock.knock_back.service.crawling.performingArts.KOPIS;
import org.knock.knock_back.service.fcm.FcmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.knock.knock_back.service.crawling.movie.KOFIC;

/**
 * @author nks
 * @apiNote Scheduler 로 제어되는 설정들
 */
@Configuration
@RequiredArgsConstructor
public class SchedulerConfig {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final KOFIC kofic;
    private final KOPIS kopis;
    private final CrawlingService crawlingService;
    private final FcmService fcmService;

    @Value("${schedule.kofic.use}")
    private boolean useScheduleKOFIC;

    @Value("${schedule.kopis.use}")
    private boolean useScheduleKOPIS;

    @Value("${schedule.megabox.use}")
    private boolean useScheduleMegaBox;

    @Value("${schedule.cgv.use}")
    private boolean useScheduleCGV;

    @Value("${schedule.lotte.use}")
    private boolean useScheduleLotte;

    @Value("${schedule.fcm.use}")
    private boolean useScheduleFCM;

    /**
     * 주기적으로 KOFIC 에서 영화 정보를 받아온다.
     * @apiNote cronTab = 1시간에 1번, 정시
     */
    @Scheduled(cron = "${schedule.fcm.cron}")
    public void fcmJob() {

        try
        {
            if (useScheduleFCM)
            {
                fcmService.pushMsg();
            }
        }
        catch (Exception e)
        {
            logger.warn("FCM JOB 에러, {}", e.getMessage());
        }
    }

    /**
     * 주기적으로 KOFIC 에서 영화 정보를 받아온다.
     * @apiNote cronTab = 1시간에 1번, 정시
     */
    @Async
    @Scheduled(cron = "${schedule.kofic.cron}")
    public void koficJob() {

        try
        {
            if (useScheduleKOFIC)
            {
                kofic.requestAPI();
            }
        }
        catch (Exception e)
        {
            logger.warn("KOFIC 크롤링 중 에러, {}", e.getMessage());
        }
    }

    /**
     * 주기적으로 KOPIS 에서 영화 정보를 받아온다.
     * @apiNote cronTab = 1시간에 1번, 정시
     */
    @Async
    @Scheduled(cron = "${schedule.kopis.cron}")
    public void kopisJob() {

        try
        {
            if (useScheduleKOPIS)
            {
                kopis.requestAPIAsync();
            }
        }
        catch (Exception e)
        {
            logger.warn("KOPIS 크롤링 중 에러, {}", e.getMessage());
        }
    }


    /**
     * 주기적으로 MegaBox 에서 상영 예정작 정보를 받아온다.
     * @apiNote cronTab = 매일 오전 3시
     */
    @Async
    @Scheduled(cron = "${schedule.megabox.cron}")
    public void megaBoxJob() {

        try
        {
            if (useScheduleMegaBox)
            {
                crawlingService.startCrawling("MEGABOX");
            }
        }
        catch (Exception e)
        {
            logger.warn("MegaBox 크롤링 중 에러, {}", e.getMessage());
        }
    }

    /**
     * 주기적으로 CGV 에서 상영 예정작 정보를 받아온다.
     * @apiNote cronTab = 매일 오전 4시
     */
    @Async
    @Scheduled(cron = "${schedule.cgv.cron}")
    public void CGVJob() {

        try
        {
            if (useScheduleCGV)
            {
                crawlingService.startCrawling("CGV");
            }
        }
        catch (Exception e)
        {
            logger.warn("CGV 크롤링 중 에러, {}", e.getMessage());
        }
    }

    /**
     * 주기적으로 Lotte 에서 상영 예정작 정보를 받아온다.
     * @apiNote cronTab = 매일 오전 4시
     */
    @Async
    @Scheduled(cron = "${schedule.lotte.cron}")
    public void LotteJob() {

        try
        {
            if (useScheduleLotte)
            {
                crawlingService.startCrawling("LOTTE");
            }
        }
        catch (Exception e)
        {
            logger.warn("Lotte 크롤링 중 에러, {}", e.getMessage());
        }
    }
}
