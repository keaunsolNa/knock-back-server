package org.knock.knock_back.repository.performingArts;

import org.knock.knock_back.dto.Enum.PrfState;
import org.knock.knock_back.dto.document.performingArts.KOPIS_INDEX;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author nks
 * @apiNote KOPIS Index 위한 Repository
 */
@Repository
public interface KOPISRepository extends ElasticsearchRepository<KOPIS_INDEX, String>
{
    Iterable<KOPIS_INDEX> findByPrfState(PrfState prfState);
    boolean existsByCode(String code);
}
