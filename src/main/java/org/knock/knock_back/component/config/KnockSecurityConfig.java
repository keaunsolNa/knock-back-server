package org.knock.knock_back.component.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.knock.knock_back.component.util.converter.JwtKeyConverter;

import java.util.List;

/**
 * @author nks
 * @apiNote Spring Security Filter Chain
 * - 인증 및 인가, 세션 정책, CORS, CSRF, JWT 설정 포함
 */
@Configuration
public class KnockSecurityConfig {

    /**
     * Spring Security Filter Chain 정의
     * - JWT 인증 기반이므로 Stateless 세션 유지
     * - /auth, /user, /api 등은 인증 없이 접근 가능
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // HTTP Basic 인증 비활성화 및 401 에러 리턴 설정 (HTTPS 설정)
                .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                // CSRF 보호 비활성화 (JWT 방식이므로 서버에 세션 상태를 저장하지 않기 때문)
                .csrf((AbstractHttpConfigurer::disable))
                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 세션을 설정하지 않도록(Stateless) (매 요청마다 JWT 검증)
                .sessionManagement((sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 엔드포인트별 접근 권한 설정
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/auth/**").permitAll()            // 로그인, 토큰 인증 관련
                                .requestMatchers("/user/**").permitAll()            // 사용자 관련
                                .requestMatchers("/api/**").permitAll()             // API 관련
                                .requestMatchers("/message/**").permitAll()         // FCM 관련
                                .requestMatchers("/favicon.ico").permitAll()        // favicon
                                .anyRequest().authenticated()
                )
                // OAuth2 리소스 서버 설정 (JWT 인증 방식 사용)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new JwtKeyConverter()))
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                // 인증 실패 시 401 반환
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );


        return http.build();
    }

    /**
     * CORS 설정 정의
     * - 프론트엔드와 도메인이 다른 경우에도 요청 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "https://localhost:3000", "https://localhost:53287",   // 로컬 환경
                                                "https://knock-six.vercel.app",                                                 // 프론트 vercel 배포 환경
                                                "https://knock-f24a348e0f1e.herokuapp.com/"                                     // 백엔드 heroku 배포 환경
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));       // 허용할 HTTP 메서드
        configuration.setAllowedHeaders(List.of("Content-Type", "Authorization"));      // 허용할 요청 헤더
        configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));        // 클라이언트에서 접근 가능한 응답 헤더
        configuration.setAllowCredentials(true); // 쿠키와 인증정보 포함 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
