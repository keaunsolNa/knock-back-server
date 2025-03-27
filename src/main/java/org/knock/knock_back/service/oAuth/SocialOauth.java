package org.knock.knock_back.service.oAuth;

import org.knock.knock_back.dto.Enum.SocialLoginType;

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
