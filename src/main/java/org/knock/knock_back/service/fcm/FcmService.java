package org.knock.knock_back.service.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.knock.knock_back.dto.Enum.AlarmTiming;
import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.document.movie.MOVIE_INDEX;
import org.knock.knock_back.dto.document.performingArts.KOPIS_INDEX;
import org.knock.knock_back.dto.document.user.SSO_USER_INDEX;
import org.knock.knock_back.repository.movie.MovieRepository;
import org.knock.knock_back.repository.performingArts.KOPISRepository;
import org.knock.knock_back.repository.user.SSOUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FcmService {

    private static final Logger logger = LoggerFactory.getLogger(FcmService.class);
    private final MovieRepository movieRepository;
    private final KOPISRepository kopisRepository;
    private final SSOUserRepository ssoUserRepository;

    // 프로젝트 아이디 환경 변수 ( 필수 )
    @Value("${fcm.project-id}")
    private String projectId;


    // 의존성 주입이 이루어진 후 초기화를 수행한다.
    @PostConstruct
    public void initialize() throws IOException {

        String firebaseJson = System.getenv("FCM_CREDENTIALS_JSON");
        if (firebaseJson == null) {
            throw new IllegalStateException("Missing FCM_CREDENTIALS_JSON env var");
        }
        File tempFile = File.createTempFile("firebase", ".json");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(firebaseJson);
        }

        //Firebase 프로젝트 정보를 FireBaseOptions에 입력해준다.
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(new FileInputStream(tempFile)))
                .setProjectId(projectId)
                .build();

        //입력한 정보를 이용하여 initialze 해준다.
        FirebaseApp.initializeApp(options);
    }

    // 받은 token을 이용하여 fcm를 보내는 메서드
    public void sendMessageByToken(String token, String title, String msg) {

        try
        {
            FirebaseMessaging.getInstance().send(Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(msg)
                            .build())
                    .setToken(token)
                    .build());
        }
        catch (FirebaseMessagingException e)
        {
            logger.debug(e.getMessage());
        }

    }

    /**
     * 모든 유저의 구독 정보를 가져온 뒤, 해당 작품의 개봉시간이 알림 시간 이하일 경우 FCM 알림 발송
     *
     */
    public void pushMsg() {

        Iterable<SSO_USER_INDEX> allUser = ssoUserRepository.findAll();

        LocalDate today = LocalDate.now();
        ZonedDateTime zonedDateTime = today.atStartOfDay(ZoneId.systemDefault());
        long epochMillis = zonedDateTime.toInstant().toEpochMilli();

        for (SSO_USER_INDEX user : allUser)
        {

            AlarmTiming[] usersAlarmTimings = user.getAlarmTimings();

            for (int i = 0; i < usersAlarmTimings.length; i++)
            {

                if (!usersAlarmTimings[i].equals(AlarmTiming.NONE))
                {

                    long minusTime = 0;

                    switch (usersAlarmTimings[i])
                    {
                        case AlarmTiming.ONE_DAY -> minusTime = 86400000L;
                        case AlarmTiming.THR_DAY -> minusTime = 86400000L * 2;
                        case AlarmTiming.SEV_DAY -> minusTime = 86400000L * 7;
                        case AlarmTiming.TEN_DAY -> minusTime = 86400000L * 10;
                    }

                    Set<String> idList = new HashSet<>();
                    switch (i)
                    {
                        case 1 ->
                        {
                            idList.addAll(user.getSubscribeList().get(CategoryLevelOne.MOVIE));

                            for (String id : idList)
                            {
                                MOVIE_INDEX movie = movieRepository.findById(id).orElseThrow();

                                if (movie.getOpeningTime() - minusTime >= epochMillis)
                                {
                                    int remainTime = (int) ((movie.getOpeningTime() - minusTime) / 86400000L);

                                    logger.info("[{}} remainTime", remainTime);
                                    for (String token : user.getDeviceToken())
                                    {
                                        sendMessageByToken(token, movie.getMovieNm() + " 개봉 D-" + remainTime +"!", "기다리셨던 그 컨텐츠 지금 확인해보세요!");
                                    }


                                }
                            }
                        }

                        case 2 ->
                        {
                            idList.addAll(user.getSubscribeList().get(CategoryLevelOne.PERFORMING_ARTS));
                            for (String id : idList)
                            {
                                KOPIS_INDEX kofisIndex = kopisRepository.findById(id).orElseThrow();

                                Date fromTime = kofisIndex.getFrom();
                                LocalDate localDate = fromTime.toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate();

                                ZonedDateTime koficZonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());

                                long koficEpochMillis = koficZonedDateTime.toInstant().toEpochMilli();

                                if (koficEpochMillis - minusTime >= epochMillis)
                                {
                                    int remainTime = (int) ((koficEpochMillis - minusTime) / 86400000L);

                                    for (String token : user.getDeviceToken())
                                    {
                                        sendMessageByToken(token, kofisIndex.getName() + " 개봉 D-" + remainTime +"!", "기다리셨던 그 컨텐츠 지금 확인해보세요!");
                                    }
                                }
                            }
                        }

                    }

                }
            }
        }
    }

}