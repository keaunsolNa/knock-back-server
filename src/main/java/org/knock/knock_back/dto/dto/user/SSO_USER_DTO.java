package org.knock.knock_back.dto.dto.user;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import org.knock.knock_back.dto.Enum.AlarmTiming;
import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.Enum.Role;
import org.knock.knock_back.dto.Enum.SocialLoginType;

import java.util.Date;
import java.util.Map;
import java.util.Set;

@Data
public class SSO_USER_DTO {

    @Enumerated(EnumType.STRING)
    private String id;

    @Enumerated(EnumType.STRING)
    private String name;

    @Enumerated(EnumType.STRING)
    private String email;

    @Enumerated(EnumType.STRING)
    private String nickName;

    @Enumerated(EnumType.STRING)
    private String picture;

    @Enumerated(EnumType.STRING)
    private SocialLoginType loginType;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private CategoryLevelOne favoriteLevelOne;

    @Enumerated(EnumType.STRING)
    private AlarmTiming[] alarmTimings;

    @Enumerated(EnumType.STRING)
    private Date lastLoginTime;

    @Enumerated(EnumType.STRING)
    private Map<CategoryLevelOne, Set<String>> subscribeList;
}
