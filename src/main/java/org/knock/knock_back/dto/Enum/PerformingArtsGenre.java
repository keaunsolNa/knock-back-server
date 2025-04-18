package org.knock.knock_back.dto.Enum;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * @author nks
 * @apiNote 공연예술 장르 ENUM
 */
@Getter
public enum PerformingArtsGenre {

	THEATER("연극"),
	MUSICAL("뮤지컬"),
	CLASSICAL("클래식"),
	KOREAN_TRADITIONAL("국악"),
	POPULAR_MUSIC("대중 음악"),
	WESTERN_KOREAN_DANCE("서양, 한국무용"),
	POPULAR_DANCE("대중 무용"),
	CIRCUS_MAGIC("서커스, 마술"),
	COMPLEX("복합"),
	UNKNOWN("알 수 없음"); // 예외적인 값 처리

	private static final Map<String, PerformingArtsGenre> lookupEn = new HashMap<>();

	// 한글 장르명을 ENUM 값으로 매핑
	static {
		for (PerformingArtsGenre genre : PerformingArtsGenre.values()) {
			lookupEn.put(genre.name().replaceAll("_", "").toUpperCase(), genre);

			String upperSnakeCase = toUpperSnakeCase(genre.name());
			lookupEn.put(upperSnakeCase, genre);
		}
	}

	private final String korean;

	PerformingArtsGenre(String korean) {
		this.korean = korean;
	}

	public static String toUpperSnakeCase(String input) {
		if (input == null || input.isBlank()) {
			return "";
		}

		return input.replaceAll("([a-z])([A-Z])", "$1_$2") // 소문자 + 대문자 조합을 언더스코어로 분리
			.toUpperCase(); // 대문자로 변환
	}

	// 한글 장르명을 ENUM 변환하는 메서드
	public static String fromEng(String eng) {
		if (eng == null || eng.isBlank()) {
			return UNKNOWN.getKorean();
		}

		PerformingArtsGenre genre = lookupEn.get(eng.toUpperCase());
		return (genre != null) ? genre.getKorean() : UNKNOWN.getKorean();
	}
}
