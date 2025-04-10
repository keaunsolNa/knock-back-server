package org.knock.knock_back.controller.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.knock.knock_back.component.config.JwtTokenProvider;
import org.knock.knock_back.component.util.maker.TokenMaker;
import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.document.user.SSO_USER_INDEX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.knock.knock_back.dto.Enum.SocialLoginType;
import org.knock.knock_back.service.layerClass.OauthService;

/**
 * @author nks
 * @apiNote SSO Login 시 인입되는 페이지
 */
@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/auth")
public class OauthController {

    private final OauthService oauthService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenMaker tokenMaker;
    private static final Logger logger = LoggerFactory.getLogger(OauthController.class);

    /**
     * SSO LOGIN 시도 시 인입되는 페이지. 각 요청 별 enum 타입으로 Service request 시행
     * @param socialLoginType : 로그인할 SSO Type (Google, NAVER, KAKAO)
     */
    @GetMapping(value = "/{socialLoginType}")
    public ResponseEntity<Map<String, String>> socialLoginType(@PathVariable(name = "socialLoginType") SocialLoginType socialLoginType) {

        return ResponseEntity.ok()
                .body(oauthService.request(socialLoginType));
    }

    /**
     * SSO 요청 후 Refresh 받는 callback Controller
     * 각 SocialLoginType 별 반환 값을 받은 뒤 service 계층에 전달한다.
     * @param socialLoginType : 로그인할 SSO Type (Google, NAVER, KAKAO)
     * @param authorizationCode : SSO 요청 후 받은 반환 값인 verify code
     * @param httpServletResponse : 반환 될 response 객체
     * @return token : response 객체에 refresh 토큰 담아 반환
     */
    @CrossOrigin
    @PostMapping(value = "/login/{socialLoginType}/callback")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> callback(@PathVariable(name = "socialLoginType") SocialLoginType socialLoginType,
                                                        @RequestBody Map<String, String> authorizationCode, HttpServletResponse httpServletResponse) {

        String[] tokens;
        if (socialLoginType.equals(SocialLoginType.GOOGLE)) {
            tokens = oauthService.requestUserInfo(authorizationCode.get("authorizationCode"));
        }
        else
        {
            tokens = oauthService.requestAccessToken(socialLoginType, authorizationCode.get("authorizationCode"));
        }

        // refreshToken
        String refreshTokenValue = tokens[0];
        String accessTokenValue = tokens[1];

        Cookie refreshTokenForKnock = new Cookie("refreshTokenForKnock", refreshTokenValue);
        Cookie accessToken = new Cookie("accessToken", accessTokenValue);

        tokenMaker.makeRefreshToken(httpServletResponse, refreshTokenForKnock);
        tokenMaker.makeAccessToken(accessToken);

        SSO_USER_INDEX user = jwtTokenProvider.getUserDetails(accessTokenValue);
        String redirectUrl = "";

        switch (user.getFavoriteLevelOne())
        {
            case CategoryLevelOne.MOVIE -> redirectUrl = "/movie";
            case CategoryLevelOne.PERFORMING_ARTS -> redirectUrl = "/performingArts";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("redirectUrl", redirectUrl);
        response.put("accessToken", accessToken);

        return ResponseEntity.ok(response);

    }

    /**
     * 프론트로부터 refreshToken 받아 accessToken 반환한다.
     * @return token : response 객체에 access 토큰 담아 반환
     */
    @PostMapping(value = "/getAccessToken")
    @ResponseBody
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<Map<String, String>> getAccessToken( HttpServletRequest request ) {

        String token = jwtTokenProvider.resolveToken(request);

        try
        {
            SSO_USER_INDEX user = jwtTokenProvider.getUserDetails(token);
            String accessToken = jwtTokenProvider.generateAccessToken(user);

            Cookie accessTokenForKnock = new Cookie("accessToken", accessToken);

            tokenMaker.makeAccessToken(accessTokenForKnock);

            String redirectUrl = "";


            switch (user.getFavoriteLevelOne())
            {
                case CategoryLevelOne.MOVIE -> redirectUrl = "/movie";
                case CategoryLevelOne.PERFORMING_ARTS -> redirectUrl = "/performingArts";
            }

            Map<String, String> response = new HashMap<>();
            response.put("redirectUrl", redirectUrl);
            response.put("accessToken", accessToken);

            return ResponseEntity.ok(response);

        }
        catch (Exception e)
        {
            logger.warn("AccessToken 생성 중 에러 발생 {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }

    }

    /**
     * 프론트로부터 refreshToken 받아 제거한다.
     */
    @PostMapping(value = "/logout")
    @ResponseBody
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<Map<String, String>> logout( HttpServletRequest request, HttpServletResponse response ) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        try
        {
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }

            Cookie[] cookies = request.getCookies();
            if (cookies != null)
            {
                for (Cookie cookie : cookies)
                {
                    if (cookie.getName().equals("refreshTokenForKnock"))
                    {
                        tokenMaker.makeTokenValidOut(cookie);
                        response.addCookie(cookie);
                    }
                }
            }

            return ResponseEntity.ok().build();

        }
        catch (Exception e)
        {
            logger.warn("토큰 제거 중 에러 발생 {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }

    }
}
