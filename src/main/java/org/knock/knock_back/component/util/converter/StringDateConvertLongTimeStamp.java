package org.knock.knock_back.component.util.converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author nks
 * @apiNote 여러 종류의 String Type 문자열을 받아 Date Parsing 한 후, epochTime 반환
 */
@Component
public class StringDateConvertLongTimeStamp {

	private static final Logger logger = LoggerFactory.getLogger(StringDateConvertLongTimeStamp.class);

	/**
	 * epochTime 받아 yyyy.MM.dd 형태 문자열로 반환
	 *
	 * @param time 변환할 epochTime
	 * @return yyyy.MM.dd 문자열
	 */
	public String Converter(long time) {

		if (time == 0) {
			logger.warn("parameter is null");
			return "개봉 예정";
		}

		Instant instant = Instant.ofEpochMilli(time);

		LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

		return date.format(formatter);

	}

}
