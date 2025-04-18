package org.knock.knock_back.component.util.maker;

import java.util.Random;

import org.knock.knock_back.dto.dictionary.Dictionary;
import org.springframework.stereotype.Component;

/**
 * @author nks
 * @apiNote 임시 닉네임을 만들기 위한 클래스
 */
@Component
public class RandomNickNameMaker {

	/**
	 * 임시 닉네임을 만든다.
	 * 명명규칙 : [형용사] [동물이름] [3자리숫자]
	 */
	public String makeRandomNickName() {

		Dictionary dictionary = new Dictionary();

		Random random = new Random();

		String adjective = dictionary.adjective[random.nextInt(dictionary.adjective.length - 1)];
		String animal = dictionary.animal[random.nextInt(dictionary.animal.length - 1)];
		String number = String.valueOf(random.nextInt(999));

		return adjective + " " + animal + number;
	}
}
