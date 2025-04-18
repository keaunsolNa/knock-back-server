package org.knock.knock_back.dto.dto.category;

import java.util.List;

import org.knock.knock_back.dto.Enum.CategoryLevelOne;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class CATEGORY_LEVEL_TWO_DTO {

	private String id;
	private String nm;
	@Enumerated(EnumType.STRING)
	private CategoryLevelOne parentNm;
	private List<String> favoriteUsers;

}
