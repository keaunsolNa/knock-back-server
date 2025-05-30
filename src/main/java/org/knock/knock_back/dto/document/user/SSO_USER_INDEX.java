package org.knock.knock_back.dto.document.user;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.annotations.CurrentTimestamp;
import org.knock.knock_back.dto.Enum.AlarmTiming;
import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.Enum.Role;
import org.knock.knock_back.dto.Enum.SocialLoginType;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Document(indexName = "sso-user-index")
public class SSO_USER_INDEX {

	@Id
	private String id;                                                  // 고객 ID

	@Field(type = FieldType.Text, analyzer = "nori", fielddata = true)
	private String name;                                                // 고객 이름

	@Field(type = FieldType.Text, analyzer = "nori", fielddata = true)
	private String email;                                              // 고객 email 주소

	@Field(type = FieldType.Text, analyzer = "nori", fielddata = true)
	private String nickName;                                            // 고객 nickname

	@Field(type = FieldType.Text)
	private String picture;                                             // 고객 프로필 사진

	@Field(type = FieldType.Text)
	private SocialLoginType loginType;                                  // 고객 로그인 타입 (EX; KAKAO, GOOGLE, NAVER)

	@Field(type = FieldType.Text)
	private Role role;                                                  // 고객 권한 (GUEST, USER, ADMIN, INVALID)

	@Field(type = FieldType.Text)
	private CategoryLevelOne favoriteLevelOne;                          // 선호 카테고리 ( MOVIE, PERFORMING_ARTS, EXHIBITION, MY_PAGE, BOARD)

	@Field(type = FieldType.Text)
	private AlarmTiming[] alarmTimings;                                 // 알람 발송 시간

	@CurrentTimestamp
	private Date lastLoginTime;                                         // 마지막 로그인 시간

	@Field(type = FieldType.Object)
	private Map<CategoryLevelOne, Set<String>> subscribeList;    // 구독 목록

	@Field(type = FieldType.Text)
	private List<String> deviceToken;

	@Builder
	public SSO_USER_INDEX(String id, String name, String email, String nickName, String picture,
		SocialLoginType loginType, Role role) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.nickName = nickName;
		this.picture = picture;
		this.loginType = loginType;
		this.role = role;
		this.favoriteLevelOne = CategoryLevelOne.MOVIE;
		this.alarmTimings = new AlarmTiming[] {AlarmTiming.NONE, AlarmTiming.NONE, AlarmTiming.NONE, AlarmTiming.NONE};
		this.lastLoginTime = new Timestamp(System.currentTimeMillis());
		this.subscribeList = new HashMap<>();
		subscribeList.put(CategoryLevelOne.MOVIE, new HashSet<>());
		subscribeList.put(CategoryLevelOne.EXHIBITION, new HashSet<>());
		subscribeList.put(CategoryLevelOne.PERFORMING_ARTS, new HashSet<>());
	}

	public SSO_USER_INDEX update(String name, String email, String picture) {
		this.name = name;
		this.email = email;
		this.picture = picture;
		this.lastLoginTime = new Timestamp(System.currentTimeMillis());

		return this;
	}

	public void updateNickName(String nickName) {
		this.nickName = nickName;
	}

	public void updateFavoriteLevelOne(CategoryLevelOne favoriteLevelOne) {
		this.favoriteLevelOne = favoriteLevelOne;
	}

	public void updateAlarmTimings(AlarmTiming[] alarmTimings) {
		this.alarmTimings = alarmTimings;
	}

	public String getRoleKey() {
		return this.role.getKey();
	}
}
