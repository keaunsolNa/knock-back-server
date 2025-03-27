package org.knock.knock_back.dto.dto.movie;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.document.category.CATEGORY_LEVEL_TWO_INDEX;

@Data
public class KOFIC_DTO {

    private String movieId;

    private String KOFICCode;

    private String movieNm;

    private Long prdtYear;

    private Long openingTime;

    private String[] directors;

    private String[] actors;

    private String[] companyNm;

    @Enumerated(EnumType.STRING)
    private CategoryLevelOne categoryLevelOne;

    private Iterable<CATEGORY_LEVEL_TWO_INDEX> categoryLevelTwo;

    private Long runningTime;

}
