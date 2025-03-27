package org.knock.knock_back.component.config;

import lombok.RequiredArgsConstructor;
import org.knock.knock_back.component.filter.EncodingFilter;
import org.knock.knock_back.component.filter.JwtAuthenticationFilter;
import org.knock.knock_back.component.filter.JwtAuthenticationHeaderFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author nks
 * @apiNote Filter 설정
 */
@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public FilterRegistrationBean<EncodingFilter> encodingFilter() {
        FilterRegistrationBean<EncodingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new EncodingFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(3);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> authFilter() {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtAuthenticationFilter(jwtTokenProvider));
        registrationBean.addUrlPatterns("/auth/getAccessToken");
        registrationBean.addUrlPatterns("/auth/logout");
        registrationBean.setOrder(2);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationHeaderFilter> userFilter() {

        FilterRegistrationBean<JwtAuthenticationHeaderFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtAuthenticationHeaderFilter(jwtTokenProvider));
        registrationBean.addUrlPatterns("/user/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

}
