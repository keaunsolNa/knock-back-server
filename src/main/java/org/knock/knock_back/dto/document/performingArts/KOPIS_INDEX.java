package org.knock.knock_back.dto.document.performingArts;

import java.util.Date;
import java.util.Set;

import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.Enum.PrfState;
import org.knock.knock_back.dto.document.category.CATEGORY_LEVEL_TWO_INDEX;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(indexName = "kopis-index")
public class KOPIS_INDEX {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private String id;                                                                  // 공연예술 ID

	private String code;                                                                // 공연예술 KOPIS ID 코드

	@Field(type = FieldType.Text, analyzer = "nori", fielddata = true)
	private String name;                                                                // 공연예술 이름

	@Field(type = FieldType.Date, format = DateFormat.epoch_millis)
	private Date from;                                                                  // 공연예술 개봉일

	@Field(type = FieldType.Date, format = DateFormat.epoch_millis)
	private Date to;                                                                    // 공연예술 마감일

	private String[] directors;                                                         // 공연예술 제작자

	private String[] actors;                                                            // 공연예술 출연진

	private String[] companyNm;                                                         // 공연예술 회사

	private String holeNm;                                                              // 공연예술 공연장

	private String poster;                                                              // 공연예술 포스터

	private String story;                                                               // 공연예술 스토리

	private String[] styurls;                                                           // 공연예술 소개 포스터

	private String area;                                                                // 공연예술 공연 지역

	private PrfState prfState;                                                          // 공연예술 상태

	private String[] dtguidance;                                                        // 공연예술 공연시간

	private String[] relates;                                                           // 공연예술 예매처

	@Field(type = FieldType.Long, format = DateFormat.epoch_millis)
	private Long runningTime;                                                           // 공연예술 상영시간

	@Enumerated(EnumType.STRING)
	private CategoryLevelOne categoryLevelOne;                                          // 공연예술 구분 (PERFORMING_ARTS)

	@Enumerated(EnumType.STRING)
	private CATEGORY_LEVEL_TWO_INDEX categoryLevelTwo;                                  // 공연예술 장르

	private Set<String> favorites;                                                      // 공연예술 구독자
}
