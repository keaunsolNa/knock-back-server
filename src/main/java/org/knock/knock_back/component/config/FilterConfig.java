package org.knock.knock_back.component.config;

import org.knock.knock_back.component.filter.EncodingFilter;
import org.knock.knock_back.component.filter.JwtAuthenticationFilter;
import org.knock.knock_back.component.filter.JwtAuthenticationHeaderFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

/**
 * @author nks
 * @apiNote Filter 설정
 */
@Configuration
@RequiredArgsConstructor
public class FilterConfig {

	private final JwtTokenProvider jwtTokenProvider;

	/**
	 * UTF-8 인코딩 필터 설정
	 */
	@Bean
	public FilterRegistrationBean<EncodingFilter> encodingFilter() {
		FilterRegistrationBean<EncodingFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new EncodingFilter());
		registrationBean.addUrlPatterns("/*");
		registrationBean.setOrder(3);
		return registrationBean;
	}

	/**
	 * /auth controller 요청 시 거쳐가는 필터
	 * getAccessToken 과 logout 의 경우 유저 권한이 SecurityContextHolder 에 저장되어 있기에 해당 필터를 통해
	 * JWT 토큰의 유효성 검증을 시행한다
	 * 그 외 login 과 callback 관련 요청의 경우 유저 권한이 생성되지 않았기에
	 * 해당 필터를 거칠 경우 JWT 토큰의 유효성 검증을 실행하지 않고 시행된다.
	 */
	@Bean
	public FilterRegistrationBean<JwtAuthenticationFilter> authFilter() {
		FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new JwtAuthenticationFilter(jwtTokenProvider));
		registrationBean.addUrlPatterns("/auth/getAccessToken");
		registrationBean.addUrlPatterns("/auth/logout");
		registrationBean.setOrder(2);
		return registrationBean;
	}

	/**
	 * /user Controller 로 요청되는 모든 요청은 로그인 한 유저만 실행할 수 있기에
	 * 해당 컨트롤러를 거치는 모든 요청은 JWT 토큰 인증 절차를 거치도록 한다.
	 */
	@Bean
	public FilterRegistrationBean<JwtAuthenticationHeaderFilter> userFilter() {

		FilterRegistrationBean<JwtAuthenticationHeaderFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new JwtAuthenticationHeaderFilter(jwtTokenProvider));
		registrationBean.addUrlPatterns("/user/*");
		registrationBean.setOrder(1);
		return registrationBean;
	}

}
