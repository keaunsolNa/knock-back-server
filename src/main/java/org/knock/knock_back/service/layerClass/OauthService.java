package org.knock.knock_back.service.layerClass;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.knock.knock_back.dto.Enum.SocialLoginType;
import org.knock.knock_back.service.oAuth.SocialOauth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nks
 * @apiNote Oauth 관련 요청을 수행하는 Service
 */
@Service
@RequiredArgsConstructor
public class OauthService {

    private final List<SocialOauth> socialOauthList;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(OauthService.class);

    public Map<String, String> request(SocialLoginType socialLoginType)
    {
        SocialOauth socialOauth = this.findSocialOauthByType(socialLoginType);
        String redirectURL = socialOauth.getOauthRedirectURL();

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("redirect_url", redirectURL);

        return responseBody;
    }

    public String[] requestAccessToken(SocialLoginType socialLoginType, String code) {
        SocialOauth socialOauth = this.findSocialOauthByType(socialLoginType);

        String callBackResponse = socialOauth.requestAccessToken(code);
        // JSON 파싱
        JsonNode jsonNode = null;
        try
        {
            jsonNode = mapper.readTree(callBackResponse);
        }
        catch (JsonProcessingException e)
        {
            logger.warn("accessToken 파싱 중 에러 , {}", e.getMessage());
        }

        // access_token 추출
        assert jsonNode != null;
        String accessToken = jsonNode.get("access_token").asText();

        return socialOauth.requestUserInfo(accessToken);
    }

    public String[] requestUserInfo(String accessToken) {

        SocialOauth socialOauth = this.findSocialOauthByType(SocialLoginType.GOOGLE);

        return socialOauth.requestUserInfo(accessToken);

    }
    private SocialOauth findSocialOauthByType(SocialLoginType socialLoginType) {
        return socialOauthList.stream()
                .filter(x -> x.type() == socialLoginType)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 SocialLoginType 입니다."));
    }

}
