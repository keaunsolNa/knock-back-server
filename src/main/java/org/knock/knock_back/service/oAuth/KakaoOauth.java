package org.knock.knock_back.service.oAuth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.knock.knock_back.component.config.JwtTokenProvider;
import org.knock.knock_back.dto.Enum.Role;
import org.knock.knock_back.dto.Enum.SocialLoginType;
import org.knock.knock_back.dto.document.user.SSO_USER_INDEX;
import org.knock.knock_back.repository.user.SSOUserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author nks
 * @apiNote Kakao SSO Login API
 */
@Component
@RequiredArgsConstructor
public class KakaoOauth implements SocialOauth {

    // application.yml
    @Value("${spring.security.oauth2.client.provider.kakao.authorization-uri}")
    private String KAKAO_BASE_URL;
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String KAKAO_CLIENT_ID;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String KAKAO_CALLBACK_URL;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String KAKAO_CLIENT_SECRET;
    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String KAKAO_TOKEN_URI;
    @Value("${spring.security.oauth2.client.registration.kakao.authorization-grant-type}")
    private String KAKAO_GRANT_TYPE;
    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String KAKAO_USER_INFO_URI;

    private final SSOUserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(KakaoOauth.class);
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * controller 에서 요청을 받을 경우 Kakao SSO 요청을 하는 페이지 GET 방식 이동 한다.
     * @return Request URI
     */
    @Override
    public String getOauthRedirectURL() {

        return KAKAO_BASE_URL + "?response_type=code&client_id=" + KAKAO_CLIENT_ID + "&redirect_uri=" + KAKAO_CALLBACK_URL;
    }

    /**
     * Get 요청 이후 유저가 로그인 한 후, callback page 에서 받은 verify code 통해 accessToken 요청한다.
     * @param code : verify code
     * @return AccessToken / RuntimeException
     */
    @Override
    public String requestAccessToken(String code) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", KAKAO_GRANT_TYPE);
        params.put("client_id", KAKAO_CLIENT_ID);
        params.put("code", code);
        params.put("client_secret", KAKAO_CLIENT_SECRET);

        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));

        HttpEntity<String> requestEntity = new HttpEntity<>(parameterString, headers);

        try
        {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(KAKAO_TOKEN_URI, requestEntity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK)
            {
                return responseEntity.getBody();
            }
            else
            {
                logger.error("Failed to retrieve Kakao token: {}", responseEntity.getStatusCode());
                throw new RuntimeException("Failed to retrieve Kakao token");
            }
        }
        catch (Exception e)
        {
            logger.error("Exception during Kakao token retrieval: ", e);
            throw new RuntimeException("Exception during Kakao token retrieval", e);
        }

    }

    /**
     * AccessToken 받은 후 user 정보를 요청하는 API
     * userInfo 를 받은 경우, 해당하는 id가 sso-user-index 에 있다면 update, 없다면 insert 수행
     * @param accessToken : 전달받은 AccessToken
     * @return 반환될 JWT Token
     */
    @Override
    public String[] requestUserInfo(String accessToken) {

        ObjectMapper mapper = new ObjectMapper();
        RestTemplate restTemplate = new RestTemplate();
        final HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        JsonNode jsonNode;

        try {

            // GET 요청 전송
            ResponseEntity<String> responseEntity = restTemplate.exchange(KAKAO_USER_INFO_URI, HttpMethod.GET, requestEntity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK)
            {
                jsonNode = mapper.readTree(responseEntity.getBody());
            }
            else
            {
                logger.error("Failed to retrieve Kakao user info: {}", responseEntity.getStatusCode());
                throw new RuntimeException("Failed to retrieve Kakao user info");
            }

        }
        catch (Exception e)
        {
            logger.error("Exception during Kakao user info retrieval: ", e);
            throw new RuntimeException("Exception during Kakao user info retrieval", e);
        }

        assert jsonNode != null;
        String id = jsonNode.get("id").asText();

        if (userRepository.findById(id).isEmpty())
        {

            SSO_USER_INDEX ssoUserIndex = SSO_USER_INDEX.builder()
                    .id(id)
                    .name(jsonNode.get("properties").get("nickname").asText())
                    .email("익명")
                    .nickName(jsonNode.get("properties").get("nickname").asText())
                    .picture(jsonNode.get("properties").get("profile_image").asText())
                    .loginType(SocialLoginType.KAKAO)
                    .role(Role.USER)
                    .build();

            userRepository.save(ssoUserIndex);
        }
        else
        {
            SSO_USER_INDEX existingUser = userRepository.findById(id).get();

            SSO_USER_INDEX updatedUser = existingUser.update(
                    jsonNode.get("properties").get("nickname").asText(),
                    "익명",
                    jsonNode.get("properties").get("profile_image").asText()
            );

            userRepository.save(updatedUser);
        }

        String userRefreshToken = jwtTokenProvider.generateRefreshToken(userRepository.findById(id).get());
        String userAccessToken = jwtTokenProvider.generateAccessToken(userRepository.findById(id).get());

        logger.info("LOGIN : [{}]", userRepository.findById(id).get().getName());

        return new String[] { userRefreshToken, userAccessToken };

    }
}
