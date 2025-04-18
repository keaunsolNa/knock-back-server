package org.knock.knock_back.component.config;

import javax.crypto.SecretKey;

import org.knock.knock_back.component.util.maker.KeyMaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * @author nks
 * @apiNote JwtToken Decoder
 */
@Configuration
public class KnockJwtDecoderConfig {

	/**
	 * SecurityFilterChain 이 요구하는 Decoder Bean
	 *
	 * @return decoding 된 정보
	 */
	@Bean
	public NimbusJwtDecoder jwtDecoder() {

		KeyMaker keyMaker = new KeyMaker();
		SecretKey key = keyMaker.generateKey();

		return NimbusJwtDecoder.withSecretKey(key).build();
	}
}
