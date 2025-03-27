package org.knock.knock_back.component.util.converter;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.knock.knock_back.dto.Enum.JwtRule;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author nks
 * @apiNote JwtKey Converter
 * jwt 토큰을 통해 유저 정보, 권한을 가져온다.
 */
@Component
public class JwtKeyConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    /**
     * Jwt 토큰으로부터 유저 권한 목록을 추출한 뒤 반환한다.
     * @param jwt : Jwt 토큰
     */
    @Override
    public @NonNull AbstractAuthenticationToken convert(Jwt jwt) {
        Object username = jwt.getClaims().get("preferred_username");
        Set<GrantedAuthority> authorities = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt, authorities, username == null ? null : String.valueOf(username));
    }

    /**
     * Jwt 토큰으로부터 유저 권한 목록을 추출한 뒤 반환한다.
     * @param jwt : Jwt 토큰
     */
    private Set<GrantedAuthority> extractAuthorities(Jwt jwt) {

        Set<String> roleSet = new HashSet<>();
        Map<String, Object> claims = jwt.getClaims();

        for (Map.Entry<String, Object> entry : claims.entrySet())
        {
            String key = entry.getKey();

            if (key.equals(JwtRule.TYPE.getValue()))
            {
                roleSet.add(String.valueOf(entry.getValue()));
            }
            else if (key.equals(JwtRule.RESOURCE_ACCESS.getValue()))
            {
                @SuppressWarnings("unchecked")
                Map<String, List<String>> resourceAccess = (Map<String, List<String>>) entry.getValue();
                if (resourceAccess.containsKey(JwtRule.ACCOUNT.getValue()))
                {
                    @SuppressWarnings("unchecked")
                    Map<String, List<String>> account = (Map<String, List<String>>) resourceAccess.get("account");
                    if (account.containsKey(JwtRule.ROLES.getValue()))
                    {
                        roleSet.addAll(account.get(JwtRule.ROLES.getValue()));
                    }
                }
            }
        }

        return roleSet.stream()
                .map(role -> new SimpleGrantedAuthority(JwtRule.ROLE_PREFIX.getValue() + role))
                .collect(Collectors.toSet());
    }
}