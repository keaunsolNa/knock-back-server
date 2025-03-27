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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FcmService {

    // topic 이름 환경 변수
    @Value("${fcm.topic-name}")
    private String topicName;

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

    // 해당 지정된 topic에 fcm를 보내는 메서드
    public void sendMessageByTopic(String title, String body) throws FirebaseMessagingException {
        FirebaseMessaging.getInstance().send(Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setTopic(topicName)
                .build());

    }
    // 받은 token을 이용하여 fcm를 보내는 메서드
    public void sendMessageByToken(String title, String body,String token) throws FirebaseMessagingException{
        FirebaseMessaging.getInstance().send(Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setToken(token)
                .build());
    }

}