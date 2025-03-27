package org.knock.knock_back.dto.dto.fcm;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class MessageRequestDTO {
    private String title;
    private String body;
    private String targetToken;
}