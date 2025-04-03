package org.knock.knock_back.component.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.knock.knock_back.component.config.JwtTokenProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
/**
 * @author nks
 * @apiNote Jwt Token Redirect Filter
 */
@RequiredArgsConstructor
public class JwtAuthenticationHeaderFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null) {
            if (!response.isCommitted()) {
                response.sendError(401);
            }
        }

        // "Bearer " 이후의 토큰 값만 추출
        if (!jwtTokenProvider.validateToken(authorizationHeader)) {
            if (!response.isCommitted()) {
                response.sendError(401);
            }
        }

        else
        {
            Authentication authentication = jwtTokenProvider.getAuthentication(authorizationHeader);

            // SecurityContext 에 Authentication 객체를 저장.
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        }
    }
}
