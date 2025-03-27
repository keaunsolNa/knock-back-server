package org.knock.knock_back.controller.fcm;

import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.extern.slf4j.Slf4j;
import org.knock.knock_back.dto.dto.fcm.MessageRequestDTO;
import org.knock.knock_back.service.fcm.FcmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/message/fcm")
public class FcmController {

    private final FcmService fcmService;

    public FcmController(FcmService fcmService) {
        this.fcmService = fcmService;
    }

    // fcm를 보낸다 ( topic )
    @PostMapping("/topic")
    public ResponseEntity<Object> sendMessageTopic(@RequestBody MessageRequestDTO requestDTO) throws FirebaseMessagingException{
        fcmService.sendMessageByTopic(requestDTO.getTitle(), requestDTO.getBody());
        return ResponseEntity.ok().build();
    }
    // fcm를 보낸다 ( token )
    @PostMapping("/token")
    public ResponseEntity<Object> sendMessageToken(@RequestBody Map<String, String> valueMap) throws FirebaseMessagingException{

        System.out.println(valueMap);
        System.out.println(valueMap.get("targetToken"));
        log.info(valueMap.get("targetToken"));
        log.info("[{}]", valueMap);
        fcmService.sendMessageByToken( valueMap.get("targetToken ") );
        return ResponseEntity.ok().build();
    }

}