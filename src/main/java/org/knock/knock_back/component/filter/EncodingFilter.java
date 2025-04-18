package org.knock.knock_back.component.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author nks
 * @apiNote 모든 요청은 UTF-8 로 인코딩한다.
 */
@RequiredArgsConstructor
public class EncodingFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {

		if ("POST".equals(request.getMethod())) {
			request.setCharacterEncoding("UTF-8");
		}

		filterChain.doFilter(request, response);
	}
}
