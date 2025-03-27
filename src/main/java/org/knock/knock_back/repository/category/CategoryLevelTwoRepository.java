package org.knock.knock_back.repository.category;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.knock.knock_back.dto.document.category.CATEGORY_LEVEL_TWO_INDEX;

import java.util.Optional;
import java.util.Set;

/**
 * @author nks
 * @apiNote CategoryLevelOne Index 위한 Repository
 */
@Repository
public interface CategoryLevelTwoRepository extends ElasticsearchRepository<CATEGORY_LEVEL_TWO_INDEX, String>
{
    Optional<Set<CATEGORY_LEVEL_TWO_INDEX>> findAllByParentNm(CategoryLevelOne parentNm);
    CATEGORY_LEVEL_TWO_INDEX findByNmAndParentNm(String nm, CategoryLevelOne parentNm);
}

