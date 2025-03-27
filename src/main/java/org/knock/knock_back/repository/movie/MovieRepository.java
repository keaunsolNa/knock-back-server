package org.knock.knock_back.repository.movie;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import org.knock.knock_back.dto.document.movie.MOVIE_INDEX;

import java.util.Optional;

/**
 * @author nks
 * @apiNote MOVIE Index 위한 Repository
 */
@Repository
public interface MovieRepository extends ElasticsearchRepository<MOVIE_INDEX, String> {

    Optional<MOVIE_INDEX> findByMovieNm(String movieNm);
    void deleteMOVIE_INDEXByOpeningTimeBefore(Long openingTime);
}
