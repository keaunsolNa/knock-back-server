package org.knock.knock_back.dto.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * @author nks
 * @apiNote JWT TOKEN 상태 관리 위한 ENUM
 */
@RequiredArgsConstructor
@Getter
public enum TokenStatus {
    AUTHENTICATED,
    EXPIRED,
    INVALID
}