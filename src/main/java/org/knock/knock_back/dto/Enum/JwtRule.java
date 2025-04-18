package org.knock.knock_back.dto.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author nks
 * @apiNote JWT TOKEN 권한 관리 위한 ENUM
 */
@RequiredArgsConstructor
@Getter
public enum JwtRule {

	TYPE("type"),
	RESOURCE_ACCESS("resource_access"),
	ACCOUNT("account"),
	ROLES("roles"),
	ROLE_PREFIX("ROLE_");

	private final String value;
}
