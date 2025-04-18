package org.knock.knock_back.dto.Enum;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

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

	private static final Map<String, PrfState> lookup = new HashMap<>();

	// 한글 상태를 ENUM 값으로 매핑
	static {
		for (PrfState state : PrfState.values()) {
			lookup.put(state.korean, state);
		}
	}

	private final String korean;

	PrfState(String korean) {
		this.korean = korean;
	}

}
