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
import java.util.Objects;

@Getter
@Setter
@ToString
@Document(indexName = "movie-index")
public class MOVIE_INDEX {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;                                                         // 영화 ID

    @Field(type = FieldType.Text, analyzer = "nori", fielddata = true)
    private String movieNm;                                                         // 영화 제목

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Long openingTime;                                                       // 영화 개봉시간

    private String KOFICCode;                                                       // 영화 KOFIC 코드

    private List<String> reservationLink;                                               // 영화 예매 링크

    private String posterBase64;                                                    // 영화 포스터

    private List<String> directors;                                                     // 영화 감독

    private List<String> actors;                                                        // 영화 배우

    private List<String> companyNm;                                                     // 영화 제작사

    @Enumerated(EnumType.STRING)
    private CategoryLevelOne categoryLevelOne;                                      // 상위 장르 (MOVIE)

    @OneToMany
    private Iterable<CATEGORY_LEVEL_TWO_INDEX> categoryLevelTwo;                    // 하위 장르 (EX; 공포, 미스테리)

    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Long runningTime;                                                       // 영화 상영시간
    
    private String plot;                                                            // 영화 줄거리

    private List<String> favorites;                                                  // 영화 구독자

    @Builder
    public MOVIE_INDEX
            (String id, String movieNm, Long openingTime, String KOFICCode,
             List<String> reservationLink, String posterBase64, List<String> directors,
             List<String> actors, List<String> companyNm,  CategoryLevelOne categoryLevelOne,
             Iterable<CATEGORY_LEVEL_TWO_INDEX> categoryLevelTwo, Long runningTime,
             String plot, List<String> favorites )
    {
        this.id = id;
        this.movieNm = movieNm;
        this.openingTime = openingTime;
        this.KOFICCode = KOFICCode;
        this.reservationLink = reservationLink;
        this.posterBase64 = posterBase64;
        this.directors = directors;
        this.actors = actors;
        this.companyNm = companyNm;
        this.categoryLevelOne = categoryLevelOne;
        this.categoryLevelTwo = categoryLevelTwo;
        this.runningTime = runningTime;
        this.plot = plot;
        this.favorites = favorites;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MOVIE_INDEX movieINDEX)) return false;
        return Objects.equals(KOFICCode, movieINDEX.KOFICCode) || Objects.equals(id, movieINDEX.id);
    }
}
