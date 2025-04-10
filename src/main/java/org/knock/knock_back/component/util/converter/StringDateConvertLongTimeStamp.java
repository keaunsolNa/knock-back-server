package org.knock.knock_back.component.util.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author nks
 * @apiNote 여러 종류의 String Type 문자열을 받아 Date Parsing 한 후, epochTime 반환
 */
@Component
public class StringDateConvertLongTimeStamp {

    private static final Logger logger = LoggerFactory.getLogger(StringDateConvertLongTimeStamp.class);

    /**
     * String 형태의 문자열을 epochTime 반환
     * @param dateString 변환할 String
     * @return epochTime
     */
    public Long Converter(String dateString) {

        if (dateString == null || dateString.isEmpty())
        {
            logger.warn("[{}]", "parameter is null");
            return 0L;
        }

        /*
         * 공연예술의 경우, 마감 일자가 날짜 형식이 아닌 오픈런으로 되어 있는 경우가 있어
         * 해당 케이스는 99991231 로 치환함.
         */
        if (dateString.equals("오픈런"))
        {
            dateString = "99991231";
        }

        SimpleDateFormat dateFormat;
        switch (dateString.length())
        {
            case 4 : dateFormat = new SimpleDateFormat("yyyy"); break;
            case 6 : dateFormat = new SimpleDateFormat("yyyyMM"); break;
            case 7 : dateFormat = new SimpleDateFormat("yyyy.MM"); break;
            case 8 : dateFormat = new SimpleDateFormat("yyyyMMdd"); break;
            case 10 : dateFormat = new SimpleDateFormat("yyyy.MM.dd"); break;

            default:
                logger.warn("parameter is Illegal  {}", dateString + "\t " + dateString.length());

                return 0L;
        }

        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("KST"));
        long result = 0;

        try
        {
            Date date = dateFormat.parse(dateString);
            result = date.getTime();
        }
        catch (ParseException e)
        {
            logger.warn(" 파싱 중 에러 발생 {}", e.getMessage());
        }

        return result;
    }

    /**
     * epochTime 받아 yyyy.MM.dd 형태 문자열로 반환
     * @param time 변환할 epochTime
     * @return yyyy.MM.dd 문자열
     */
    public String Converter(long time)
    {

        if (time == 0)
        {
            logger.warn("parameter is null");
            return "개봉 예정";
        }

        Instant instant = Instant.ofEpochMilli(time);

        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        return date.format(formatter);

    }

}
