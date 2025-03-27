package org.knock.knock_back.component.util.converter;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.knock.knock_back.dto.Enum.SocialLoginType;

/**
 * @author nks
 * @apiNote Social Login Type(Google, NAVER, KAKAO)를 받아 각각의 enum 으로 전환한다.
 */
@Component
public class SocialLoginTypeConverter implements Converter<String, SocialLoginType> {

    /**
     * 소문자 대문자 전환
     * @param type 매개변수로 받은 Social Login Type
     * @return 대문자로 변환된 Social Login Type
     */
    @Override
    public @NonNull SocialLoginType convert(String type) {
        return SocialLoginType.valueOf(type.toUpperCase());
    }
}