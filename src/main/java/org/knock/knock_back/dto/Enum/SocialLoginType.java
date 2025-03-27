package org.knock.knock_back.dto.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * @author nks
 * @apiNote SSO 로그인 진입 경로 관리 위한 ENUM
 */
@Getter
@RequiredArgsConstructor
public enum SocialLoginType {
    GOOGLE,
    KAKAO,
    NAVER,
    GUEST
}
