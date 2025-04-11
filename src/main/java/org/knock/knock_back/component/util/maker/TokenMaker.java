package org.knock.knock_back.component.util.maker;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author nks
 * @apiNote Token 생성기
 * Access, Refresh Token 생성 및 재발급
 */
@Component
public class TokenMaker {

    @Value("${server.reactive.session.cors.domain}")
    private String domain;

    /**
     * Cookie 를 전달 받아 설정 값 붙이기
     * Refresh Token 용
     */
    public void makeRefreshToken(HttpServletResponse httpServletResponse, Cookie refreshTokenForKnock)
    {
        refreshTokenForKnock.setPath("/");
        refreshTokenForKnock.setHttpOnly(true);
        refreshTokenForKnock.setSecure(true);
        refreshTokenForKnock.setDomain(domain);
        refreshTokenForKnock.setAttribute("SameSite", "None");

        httpServletResponse.addCookie(refreshTokenForKnock);
    }

    /**
     * Cookie 를 전달 받아 설정 값 붙이기
     * Refresh Token 용
     */
    public void makeAccessToken(Cookie accessTokenForKnock)
    {
        accessTokenForKnock.setPath("/");
        accessTokenForKnock.setHttpOnly(true);
        accessTokenForKnock.setSecure(true);
        accessTokenForKnock.setDomain(domain);
        accessTokenForKnock.setAttribute("SameSite", "None");
    }

    public void makeTokenValidOut(Cookie refreshTokenForKnock)
    {
        refreshTokenForKnock.setPath("/");
        refreshTokenForKnock.setHttpOnly(true);
        refreshTokenForKnock.setSecure(true);
        refreshTokenForKnock.setDomain(domain);
        refreshTokenForKnock.setAttribute("SameSite", "None");
        refreshTokenForKnock.setMaxAge(0);
    }
}
