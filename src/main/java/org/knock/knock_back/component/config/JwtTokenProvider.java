package org.knock.knock_back.component.config;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.knock.knock_back.component.util.maker.KeyMaker;
import org.knock.knock_back.dto.document.user.SSO_USER_INDEX;
import org.knock.knock_back.repository.user.SSOUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * @author nks
 * @apiNote JwtToken 생성기
 */
@RequiredArgsConstructor
@Configuration
public class JwtTokenProvider {

	private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
	private static final KeyMaker keyMaker = new KeyMaker();
	private static final SecretKey KEY = keyMaker.generateKey();

	private final SSOUserRepository ssoUserRepository;

	@Value("${spring.security.oauth2.authorizationserver.issuer}")
	private String issuer;

	/**
	 * user 정보를 받아 AccessToken 생성한다.
	 * 유효 시간은 생성 시점으로부터 4시간
	 * 사용 알고리즘은 HmacSHA256
	 *
	 * @param user 매개변수로 받은 SSO_USER_INDEX
	 * @return 생성된 토큰
	 */
	public String generateAccessToken(SSO_USER_INDEX user) {

		long now = System.currentTimeMillis();
		long ACCESS_EXPIRATION = 31 * 24 * 60 * 10000;

		return Jwts.builder()
			.header().add("typ", "JWT").add("alg", "HmacSHA256").and()
			.issuer(issuer)
			.claims(createClaims(user))
			.subject(String.valueOf(user.getId()))
			.expiration(new Date(now + ACCESS_EXPIRATION))
			.signWith(KEY)
			.compact();

	}

	/**
	 * user 정보를 받아 AccessToken 갱신
	 * 유효 시간은 생성 시점으로부터 4시간
	 * 사용 알고리즘은 HmacSHA256
	 *
	 * @param user 매개변수로 받은 SSO_USER_INDEX
	 * @return 재생성된 토큰
	 */
	public String generateRefreshToken(SSO_USER_INDEX user) {

		long now = System.currentTimeMillis();
		long REFRESH_EXPIRATION = 7 * 24 * 60 * 10000;

		return Jwts.builder()
			.header().add("typ", "JWT").add("alg", "HmacSHA256").and()
			.issuer(issuer)
			.claims(createClaims(user))
			.subject(String.valueOf(user.getId()))
			.expiration(new Date(now + REFRESH_EXPIRATION))
			.signWith(KEY)
			.compact();

	}

	/**
	 * user 정보를 받아 Map<String, Object> 형태의 인증 정보 생성
	 * identifier : 유저 권한
	 *
	 * @param user 매개변수로 받은 SSO_USER_INDEX
	 * @return 생성된 Claims 타입
	 */
	private Map<String, Object> createClaims(SSO_USER_INDEX user) {

		Map<String, Object> claims = new HashMap<>();
		claims.put("Identifier", user.getRoleKey());
		claims.put("Role", user.getRole());
		return claims;

	}

	/**
	 * request 요청 시 헤더에서 Refresh 토큰 정보를 가져온다.
	 *
	 * @param request request 요청 시 header 의 Refresh Token 가져오기
	 * @return 생성된 Refresh JWT 토큰
	 */
	public String resolveToken(HttpServletRequest request) {

		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {

				if (cookie.getName().equals("refreshTokenForKnock")) {
					logger.info("[{}]", cookie.getName() + ": " + cookie.getValue());
					return cookie.getValue();
				}
			}
		}

		return null;

	}

	/**
	 * request 요청 시 헤더에서 토큰 정보를 가져온다.
	 *
	 * @param request request 요청 시 header 의 Access Token 가져오기
	 * @return 생성된 Access JWT 토큰
	 */
	public String resolveAccessToken(HttpServletRequest request) {

		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {

				if (cookie.getName().equals("accessToken")) {
					return cookie.getValue();
				}
			}
		}

		return null;

	}

	/**
	 * Token 에서 유저 정보를 가져온다.
	 *
	 * @param token : 매개변수로 받은 JWT 토큰 (As Expect)
	 * @return (토큰이 유효할 경우) 유저 ID
	 */
	public String getUserPk(String token) {

		try {
			if (null == token || token.isEmpty())
				return null;
			Claims claims = Jwts.parser()
				.verifyWith(KEY)
				.build()
				.parseSignedClaims(token)
				.getPayload();

			return claims.getSubject();
		} catch (Exception e) {
			logger.warn("유저 정보 추출 중 에러, {}", e.getMessage());
			return null;
		}
	}

	/**
	 * Token 권한 정보를 가져온다.
	 *
	 * @param token : 매개변수로 받은 JWT 토큰 (As Expect)
	 * @return 권한 정보
	 */
	public Authentication getAuthentication(String token) {

		SSO_USER_INDEX userDetails = ssoUserRepository.findById(this.getUserPk(token)).orElseThrow();
		return new UsernamePasswordAuthenticationToken(userDetails, "");
	}

	/**
	 * refreshToken 통해 USER_SSO_INDEX 정보를 가져온다.
	 *
	 * @param token : 대상 refreshToken
	 * @return 유저 정보
	 */
	public SSO_USER_INDEX getUserDetails(String token) {
		return ssoUserRepository.findById(this.getUserPk(token)).orElseThrow();
	}

	/**
	 * Token 유효성 여부를 판별한다.
	 *
	 * @param jwtToken : 매개변수로 받은 JWT 토큰 (As Expect)
	 * @return 유효성 여부
	 */
	public boolean validateToken(String jwtToken) {
		try {

			if (null == jwtToken || jwtToken.isEmpty())
				return false;

			Jwts.parser()
				.verifyWith(KEY)
				.build()
				.parseSignedClaims(jwtToken)
				.getPayload();

			return true;
		} catch (ExpiredJwtException exception) {
			logger.warn("ExpiredJwtException, {}", exception.getMessage());
			return false;
		} catch (JwtException exception) {
			logger.warn("JwtException, {}", exception.getMessage());
			return false;
		} catch (NullPointerException exception) {
			logger.warn("NullPointerException, {}", exception.getMessage());
			return false;
		} catch (IllegalArgumentException exception) {
			logger.warn("IllegalArgumentException, {}", exception.getMessage());
			return false;
		}
	}
}