package org.knock.knock_back.component.config;

import lombok.RequiredArgsConstructor;
import org.knock.knock_back.service.fcm.FcmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author nks
 * @apiNote Scheduler 로 제어되는 설정들
 */
@Configuration
@RequiredArgsConstructor
public class SchedulerConfig {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final FcmService fcmService;

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
}
