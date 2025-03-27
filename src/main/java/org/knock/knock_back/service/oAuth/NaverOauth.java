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
import org.knock.knock_back.component.util.maker.RandomNickNameMaker;
import org.knock.knock_back.dto.Enum.Role;
import org.knock.knock_back.dto.Enum.SocialLoginType;
import org.knock.knock_back.dto.document.user.SSO_USER_INDEX;
import org.knock.knock_back.repository.user.SSOUserRepository;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author nks
 * @apiNote Naver SSO Login API
 */
@Component
@RequiredArgsConstructor
public class NaverOauth implements SocialOauth {

    // application.yml
    @Value("${spring.security.oauth2.client.provider.naver.authorization-uri}")
    private String NAVER_BASE_URL;
    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String NAVER_CLIENT_ID;
    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String NAVER_CALLBACK_URL;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String NAVER_CLIENT_SECRET;
    @Value("${spring.security.oauth2.client.provider.naver.token-uri}")
    private String NAVER_TOKEN_URI;
    @Value("${spring.security.oauth2.client.registration.naver.authorization-grant-type}")
    private String NAVER_GRANT_TYPE;
    @Value("${spring.security.oauth2.client.provider.naver.user-info-uri}")
    private String NAVER_USER_INFO_URI;

    private final SSOUserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(NaverOauth.class);
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * controller 에서 요청을 받을 경우 Naver SSO 요청을 하는 페이지 GET 방식 이동 한다.
     * @return Request URI
     */
    @Override
    public String getOauthRedirectURL() {

        Map<String, Object> params = new HashMap<>();
        params.put("response_type", "code");
        params.put("client_id", NAVER_CLIENT_ID);
        params.put("redirect_uri", NAVER_CALLBACK_URL);
        SecureRandom random = new SecureRandom();
        String state = new BigInteger(130, random).toString();
        params.put("state", state);
        params.put("grant_type", NAVER_GRANT_TYPE);

        String parameterString = params.entrySet().stream()
                .map(x -> x.getKey() + "=" + x.getValue())
                .collect(Collectors.joining("&"));

        return NAVER_BASE_URL + "?" + parameterString;
    }

    /**
     * Get 요청 이후 유저가 로그인 한 후, callback page 에서 받은 verify code 통해 accessToken 요청한다.
     * @param code : verify code
     * @return AccessToken / RuntimeException
     */
    @Override
    public String requestAccessToken(String code) {

        RestTemplate restTemplate = new RestTemplate();

        String apiURL = NAVER_TOKEN_URI +
                "?grant_type=" + NAVER_GRANT_TYPE +
                "&client_id=" + NAVER_CLIENT_ID +
                "&client_secret=" + NAVER_CLIENT_SECRET +
                "&redirect_uri=" + NAVER_CALLBACK_URL +
                "&code=" + code;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try
        {
            ResponseEntity<String> responseEntity = restTemplate.exchange(
                    apiURL,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            if (responseEntity.getStatusCode() == HttpStatus.OK)
            {
                return responseEntity.getBody();
            }
            else
            {
                logger.error("Failed to retrieve access token: {}", responseEntity.getBody());
                throw new RuntimeException("Failed to retrieve naver token");
            }

        }
        catch (Exception e)
        {
            logger.error("Error during access token request: {}", e.getMessage());
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

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        final HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        String userInfo = restTemplate.exchange(NAVER_USER_INFO_URI, HttpMethod.GET, httpEntity, String.class)
                .getBody();

        JsonNode jsonNode;

        try
        {

            // GET 요청 전송
            ResponseEntity<String> responseEntity = restTemplate.exchange(NAVER_USER_INFO_URI, HttpMethod.GET, httpEntity, String.class);

            if (responseEntity.getStatusCode() == HttpStatus.OK)
            {
                jsonNode = mapper.readTree(userInfo).get("response");
            }
            else
            {
                logger.error("Failed to retrieve Naver user info: {}", responseEntity.getStatusCode());
                throw new RuntimeException("Failed to retrieve Naver user info");
            }

        }
        catch (Exception e)
        {
            logger.error("Exception during Naver user info retrieval: ", e);
            throw new RuntimeException("Exception during Naver user info retrieval", e);
        }


        assert jsonNode != null;
        String id = jsonNode.get("id").asText();

        if (userRepository.findById(id).isEmpty())
        {

            RandomNickNameMaker randomNickNameMaker = new RandomNickNameMaker();

            SSO_USER_INDEX ssoUserIndex = SSO_USER_INDEX.builder()
                    .id(id)
                    .name(jsonNode.get("name").asText())
                    .email(jsonNode.get("email").asText())
                    .nickName(randomNickNameMaker.makeRandomNickName())
                    .picture(jsonNode.get("profile_image").asText())
                    .loginType(SocialLoginType.NAVER)
                    .role(Role.USER)
                    .build();

            userRepository.save(ssoUserIndex);

        }
        else
        {
            SSO_USER_INDEX existingUser = userRepository.findById(id).get();

            SSO_USER_INDEX updatedUser = existingUser.update(
                    jsonNode.get("name").asText(),
                    jsonNode.get("email").asText(),
                    jsonNode.get("profile_image").asText()
            );

            userRepository.save(updatedUser);
        }

        String userRefreshToken = jwtTokenProvider.generateRefreshToken(userRepository.findById(id).get());
        String userAccessToken = jwtTokenProvider.generateAccessToken(userRepository.findById(id).get());

        logger.info("LOGIN : [{}]", userRepository.findById(id).get().getName());

        return new String[] { userRefreshToken, userAccessToken };
    }
}
