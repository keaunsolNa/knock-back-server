package org.knock.knock_back.component.util.maker;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author nks
 * @apiNote JWT 토큰에 사용할 Private key 생성기
 * 요청 시 마다 서로 다른 Key 값을 생성한다.
 */
@Component
public class KeyMaker {

	private static final Logger logger = LoggerFactory.getLogger(KeyMaker.class);

	/**
	 * 일회용 비밀키 생성
	 * HmacSHA256 알고리즘과 KeyGenerator, 난수값 SALT 를 활용하여 비밀키를 생성한다.
	 */
	public SecretKey generateKey() {

		KeyGenerator keyGen = null;

		String KEY_ALGORITHM = "HmacSHA256";
		try {
			keyGen = KeyGenerator.getInstance(KEY_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			logger.info(e.getMessage());
		}

		assert keyGen != null;
		keyGen.init(256); // 256비트 키
		SecretKey key = keyGen.generateKey();

		byte[] salt = makeSalt();
		byte[] combinedKey = new byte[key.getEncoded().length + salt.length];

		System.arraycopy(key.getEncoded(), 0, combinedKey, 0, key.getEncoded().length);
		System.arraycopy(salt, 0, combinedKey, key.getEncoded().length, salt.length);

		return new SecretKeySpec(combinedKey, KEY_ALGORITHM);
	}

	/**
	 * 일회용 SALT 값 생성
	 * 15 ~ 31 자의 랜덤한 길이의 SALT 값 생성
	 */
	public byte[] makeSalt() {

		int length = ThreadLocalRandom.current().nextInt(15, 31); // 15(포함) ~ 31(미포함)
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[length];
		random.nextBytes(salt);

		return salt;
	}
}
