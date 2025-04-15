package org.knock.knock_back.dto.document.movie;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.document.category.CATEGORY_LEVEL_TWO_INDEX;

import java.util.List;

@Getter
@Setter
@ToString
@Document(indexName = "kofic-index")
public class KOFIC_INDEX {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String movieId;                                                 // KOFIC Index ID

    private String KOFICCode;                                               // KOFIC 자체 ID

    @Field(type = FieldType.Text, analyzer = "nori", fielddata = true)
    private String movieNm;                                                 // KOFIC 영화 이름

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Long prdtYear;                                                  // KOFIC 영화 제작 년도

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Long openingTime;                                               // KOFIC 영화 개봉연도

    private List<String> directors;                                             // KOFIC 영화 감독

    private List<String> actors;                                                // KOFIC 영화 배우

    private List<String> companyNm;                                             // KOFIC 영화 제작사

    @Enumerated(EnumType.STRING)
    private CategoryLevelOne categoryLevelOne;                              // 카테고리 (MOVIE)

    @OneToMany
    private Iterable<CATEGORY_LEVEL_TWO_INDEX> categoryLevelTwo;            // 장르 (EX; 공포, 미스테리 등)

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Long runningTime;                                               // 상영시간

    @Builder
    public KOFIC_INDEX
            (String KOFICCode, String movieNm,
             Long prdtYear, Long openingTime, List<String> directors,
             List<String> companyNm, CategoryLevelOne categoryLevelOne,
             Iterable<CATEGORY_LEVEL_TWO_INDEX> categoryLevelTwo)
    {
        this.KOFICCode = KOFICCode;
        this.movieNm = movieNm;
        this.prdtYear = prdtYear;
        this.openingTime = openingTime;
        this.directors = directors;
        this.companyNm = companyNm;
        this.categoryLevelOne = categoryLevelOne;
        this.categoryLevelTwo = categoryLevelTwo;
    }

}
