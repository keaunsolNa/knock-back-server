package org.knock.knock_back.component.util.converter;

import org.knock.knock_back.dto.Enum.CategoryLevelOne;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import lombok.NonNull;

/**
 * @author nks
 * @apiNote CategoryLevelOne(MOVIE, OPERA, MY_PAGE 등)를 받아 각각의 enum 으로 전환한다.
 */
@Component
public class CategoryLevelOneConverter implements Converter<String, CategoryLevelOne> {

	/**
	 * 소문자 대문자 변환
	 *
	 * @param type 매개변수로 받은 CategoryLevelOne
	 * @return 대문자로 변환된 CategoryLevelOne
	 */
	@Override
	public @NonNull CategoryLevelOne convert(String type) {

		return CategoryLevelOne.valueOf(type.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase());
	}
}