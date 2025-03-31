package org.knock.knock_back.component.util.converter;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class SrcDirectToByteImg {

    public byte[] srcImgPathToByteImg(String srcImgPath) {

        WebClient webClient = WebClient.builder().build();

        return webClient.get()
                .uri(srcImgPath)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }
}
