package org.knock.knock_back.dto.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author nks
 * @apiNote User 권한 관리용 ENUM
 */
@Getter
@RequiredArgsConstructor
public enum Role {

    GUEST("ROLE_GUEST", "Not Login User"),
    USER("ROLE_USER", "Regular User"),
    ADMIN("ROLE_ADMIN", "Administrator"),
    INVALID("ROLE_INVALID", "User Who Banned");

    private final String key;
    private final String title;
}