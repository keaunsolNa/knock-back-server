package org.knock.knock_back.dto.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author nks
 * @apiNote User 알림 받는 주기를 관리하기 위한 ENUM
 */
@Getter
@RequiredArgsConstructor
public enum AlarmTiming {
    NONE,
    ZERO_DAY,
    ONE_DAY,
    THR_DAY,
    SEV_DAY,
    TEN_DAY
}
