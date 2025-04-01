package org.knock.knock_back.dto.document.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CurrentTimestamp;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.knock.knock_back.dto.Enum.AlarmTiming;
import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.Enum.Role;
import org.knock.knock_back.dto.Enum.SocialLoginType;

import java.sql.Timestamp;
import java.util.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Document(indexName = "sso-user-index")
public class SSO_USER_INDEX {

    @Id
    @Enumerated(EnumType.STRING)
    private String id;                                                  // 고객 ID

    @Field(type = FieldType.Text, analyzer = "nori", fielddata = true)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private String name;                                                // 고객 이름

    @Field(type = FieldType.Text, analyzer = "nori", fielddata = true)
    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
    //TODO : KAKAO 비즈 앱 동의 이후 nullable = false
    private String email;                                              // 고객 email 주소

    @Field(type = FieldType.Text, analyzer = "nori", fielddata = true)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private String nickName;                                            // 고객 nickname

    @Field(type = FieldType.Text)
    @Column
    @Enumerated(EnumType.STRING)
    private String picture;                                             // 고객 프로필 사진

    @Field(type = FieldType.Text)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialLoginType loginType;                                  // 고객 로그인 타입 (EX; KAKAO, GOOGLE, NAVER)

    @Field(type = FieldType.Text)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;                                                  // 고객 권한 (GUEST, USER, ADMIN, INVALID)

    @Field(type = FieldType.Text)
    @Enumerated(EnumType.STRING)
    private CategoryLevelOne favoriteLevelOne;                          // 선호 카테고리 ( MOVIE, PERFORMING_ARTS, EXHIBITION, MY_PAGE, BOARD)

    @Field(type = FieldType.Text)
    @Enumerated(EnumType.STRING)
    private AlarmTiming[] alarmTimings;                                 // 알람 발송 시간

    @CurrentTimestamp
    @Column(nullable = false)
    private Date lastLoginTime;                                         // 마지막 로그인 시간

    @Field(type = FieldType.Object)
    @Enumerated(EnumType.STRING)
    private Map<CategoryLevelOne, Set<String>> subscribeList;    // 구독 목록

    @Field(type = FieldType.Text)
    private Set<String> deviceToken;

    @Builder
    public SSO_USER_INDEX(String id, String name, String email, String nickName, String picture, SocialLoginType loginType, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.nickName = nickName;
        this.picture = picture;
        this.loginType = loginType;
        this.role = role;
        this.favoriteLevelOne = CategoryLevelOne.MOVIE;
        this.alarmTimings = new AlarmTiming[] { AlarmTiming.NONE, AlarmTiming.NONE, AlarmTiming.NONE, AlarmTiming.NONE };
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
