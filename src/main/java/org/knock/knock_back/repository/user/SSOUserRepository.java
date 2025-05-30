package org.knock.knock_back.repository.user;

import org.knock.knock_back.dto.document.user.SSO_USER_INDEX;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author nks
 * @apiNote SSO USER Index 위한 Repository
 */
@Repository
public interface SSOUserRepository extends ElasticsearchRepository<SSO_USER_INDEX, String> {
}
