package org.knock.knock_back.dto.Enum;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author nks
 * @apiNote 공연예술의 공연상태 관리하기 위한 ENUM
 */
@Getter
public enum PrfState {

    UPCOMING("공연예정"),
    ONGOING("공연중"),
    COMPLETED("공연완료"),
    OPEN_RUN("오픈런"),
    LIMITED_RUN("리미티드런"),
    CLOSING_SOON("마감임박"),
    UNKNOWN("알 수 없음"); // 예외적인 값 처리

    private final String korean;
    private static final Map<String, PrfState> lookup = new HashMap<>();

    // 한글 상태를 ENUM 값으로 매핑
    static {
        for (PrfState state : PrfState.values()) {
            lookup.put(state.korean, state);
        }
    }

    PrfState(String korean) {
        this.korean = korean;
    }

    // 한글 상태를 ENUM 변환하는 메서드
    public static PrfState fromKorean(String korean) {
        if (korean == null || korean.isBlank()) {
            return null;
        }
        return lookup.getOrDefault(korean, null); // 매칭되지 않으면 null 반환
    }
}
