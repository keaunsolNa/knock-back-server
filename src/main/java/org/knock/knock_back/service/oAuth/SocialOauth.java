package org.knock.knock_back.service.oAuth;

import org.knock.knock_back.dto.Enum.SocialLoginType;

/**
 * @author nks
 * @apiNote Social 로그인 관련 인터페이스, 타입별 정의하고 리다이렉트 될 서비스 맵핑
 */
public interface SocialOauth {
    String getOauthRedirectURL();
    String requestAccessToken(String code);
    String[] requestUserInfo(String accessToken);

    default SocialLoginType type() {
        return switch (this) {
            case GoogleOauth ignored -> SocialLoginType.GOOGLE;
            case NaverOauth ignored -> SocialLoginType.NAVER;
            case KakaoOauth ignored -> SocialLoginType.KAKAO;
            default -> null;
        };
    }
}
