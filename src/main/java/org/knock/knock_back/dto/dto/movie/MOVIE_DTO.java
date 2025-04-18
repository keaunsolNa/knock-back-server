package org.knock.knock_back.dto.dto.movie;

import java.util.List;

import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.dto.category.CATEGORY_LEVEL_TWO_DTO;

import lombok.Data;

@Data
public class MOVIE_DTO {

	private String movieId;
	private String movieNm;
	private String openingTime;
	private String KOFICCode;
	private List<String> reservationLink;
	private String posterBase64;
	private List<String> directors;
	private List<String> actors;
	private List<String> companyNm;
	private CategoryLevelOne categoryLevelOne;
	private Iterable<CATEGORY_LEVEL_TWO_DTO> categoryLevelTwo;
	private Long runningTime;
	private String plot;
	private List<String> favorites;
	private Integer favoritesCount;
	private String img;

}
