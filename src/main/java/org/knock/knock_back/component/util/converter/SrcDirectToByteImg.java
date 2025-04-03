package org.knock.knock_back.component.util.converter;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class SrcDirectToByteImg {

    public byte[] srcImgPathToByteImg(String srcImgPath) {

        if (srcImgPath.startsWith("https:/") && !srcImgPath.startsWith("https://")) {
            srcImgPath = srcImgPath.replaceFirst("https:/", "https://");
        } else if (srcImgPath.startsWith("http:/") && !srcImgPath.startsWith("http://")) {
            srcImgPath = srcImgPath.replaceFirst("http:/", "http://");
        }

        WebClient webClient = WebClient.builder().build();

        return webClient.get()
                .uri(srcImgPath)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }
}
