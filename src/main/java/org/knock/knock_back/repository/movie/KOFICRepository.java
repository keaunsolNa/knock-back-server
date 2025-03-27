package org.knock.knock_back.repository.movie;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import org.knock.knock_back.dto.document.movie.KOFIC_INDEX;

/**
 * @author nks
 * @apiNote KOFIC Index 위한 Repository
 */
@Repository
public interface KOFICRepository extends ElasticsearchRepository<KOFIC_INDEX, String>
{
    KOFIC_INDEX findByKOFICCode(String cd);
}
