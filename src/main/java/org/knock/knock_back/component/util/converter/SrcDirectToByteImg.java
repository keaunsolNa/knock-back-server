package org.knock.knock_back.component.util.converter;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

/**
 * @author nks
 * @apiNote Lotte 영화 포스터는 cf 로 시작하는 확장자 명으로, AWS 에 등록된 자체 파일서버의 이미지를 가져오는 것으로 판단.
 * 백엔드에서는 포스터를 가져오지만 프론트에서는 CORS 문제로 이미지를 가져오지 못 하는 문제가 발생한다.
 * 이를 해결하기 위해 크롤링 단계에서 파일 서버의 접속 위치를 확인 한 뒤,
 * 백엔드에서 파일서버의 이미지를 byte[] 타입의 원시 바이너리 파일 생성,
 * Base64로 인코딩 한 뒤 프론트로 보낸다. 
 */
@Component
public class SrcDirectToByteImg {

    /**
     * URL PATH 에서 이미지를 byte[] 타입으로 받아온 뒤 Base64 인코딩 된 바이너리 데이터를 반환한다.
     * @param srcImgPath 파일 서버 접속 위치 URL PATH
     * @return Base64 인코딩 된 원시 바이너리 데이터
     */
    public String srcImgPathToByteImg(String srcImgPath) {

        if (srcImgPath.startsWith("https:/") && !srcImgPath.startsWith("https://")) {
            srcImgPath = srcImgPath.replaceFirst("https:/", "https://");
        } else if (srcImgPath.startsWith("http:/") && !srcImgPath.startsWith("http://")) {
            srcImgPath = srcImgPath.replaceFirst("http:/", "http://");
        }

        WebClient webClient = WebClient.builder().build();

        byte[] imgByte = webClient.get()
                .uri(srcImgPath)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

        return Base64.getEncoder().encodeToString(imgByte);
    }
}
