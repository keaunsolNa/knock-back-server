package org.knock.knock_back.component.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.knock.knock_back.component.util.maker.TokenMaker;
import org.knock.knock_back.dto.document.user.SSO_USER_INDEX;
import org.knock.knock_back.component.config.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author nks
 * @apiNote Jwt Token Filter
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final TokenMaker tokenMaker = new TokenMaker();

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        // 헤더에서 Refresh Token 가쟈오기
        String token = jwtTokenProvider.resolveToken(request);

        // Refresh Token 이 없거나 만료되었다면
        if (token == null || !jwtTokenProvider.validateToken(token)) {

            // AccessToken 탐색
            token = jwtTokenProvider.resolveAccessToken(request);

            // AccessToken 도 없거나 만료되었다면 login 창으로 redirect
            if (token == null || !jwtTokenProvider.validateToken(token)) {
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Expired token");
                }
            }

            // AccessToken 만료되지 않았다면
            else
            {
                // AccessToken 에서 유저 정보 가져온 뒤 RefreshToken 재 발급
                SSO_USER_INDEX userIndex = null;
                try
                {
                    userIndex = jwtTokenProvider.getUserDetails(token);
                }
                // NPE 시 계정 정보가 없으므로 재 로그인 요청
                catch (NullPointerException e)
                {
                    response.resetBuffer();
                    if (!response.isCommitted()) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Expired token");
                    }
                }

                String refreshTokenValue = jwtTokenProvider.generateRefreshToken(userIndex);

                Cookie refreshTokenForKnock = new Cookie("refreshTokenForKnock", refreshTokenValue);

                tokenMaker.makeRefreshToken(response, refreshTokenForKnock);

                response.addCookie(refreshTokenForKnock);

            }
        }

        // 유효한 토큰인지 확인.
        if (jwtTokenProvider.validateToken(token)) {

            // 토큰이 유효하면 토큰으로부터 유저 정보를 받아오기.
            Authentication authentication = jwtTokenProvider.getAuthentication(token);

            // SecurityContext 에 Authentication 객체를 저장.
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

            return;
        }

        logger.debug("send Error");
        if (!response.isCommitted()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Expired token");
        }
    }
}