package org.knock.knock_back.repository.movie;

import org.knock.knock_back.dto.document.movie.MOVIE_INDEX;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author nks
 * @apiNote MOVIE Index 위한 Repository
 */
@Repository
public interface MovieRepository extends ElasticsearchRepository<MOVIE_INDEX, String> {

}
