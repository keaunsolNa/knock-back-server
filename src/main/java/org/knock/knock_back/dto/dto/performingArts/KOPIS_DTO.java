package org.knock.knock_back.dto.dto.performingArts;

import java.util.Date;
import java.util.Set;

import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.Enum.PrfState;
import org.knock.knock_back.dto.dto.category.CATEGORY_LEVEL_TWO_DTO;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class KOPIS_DTO {

	private String id;
	private String code;
	private String name;
	private Date from;
	private Date to;
	private String[] directors;
	private String[] actors;
	private String[] companyNm;
	private String holeNm;
	private String poster;
	private String story;
	private String[] styurls;
	private String area;
	@Enumerated(EnumType.STRING)
	private PrfState prfState;
	private String[] dtguidance;
	private String[] relates;
	private Long runningTime;
	@Enumerated(EnumType.STRING)
	private CategoryLevelOne categoryLevelOne;
	private CATEGORY_LEVEL_TWO_DTO categoryLevelTwo;
	private Set<String> favorites;
	private Integer favoritesCount;
}
