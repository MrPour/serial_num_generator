package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SerialGenerator
{
    public static String makeSerialNum(BusinessEnum businessEnum){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(businessEnum.getDatePattern());
        String currentDate = formatter.format(LocalDateTime.now());
        StringBuffer prefix = new StringBuffer();
        prefix.append(businessEnum.getPrefix());
        prefix.append(currentDate);
        //生成号码池
        CreateNumPool(prefix.toString(),businessEnum,currentDate);
        //取号
        String SerialNum = FetchCodeFromRedis(businessEnum.getPrefix());
        return SerialNum;
    }

    private static void CreateNumPool(String prefix,BusinessEnum businessEnum,String currentDate)
    {
        String key = businessEnum.getPrefix();

    }

    private static String FetchCodeFromRedis(String key)
    {
        String code = "";
        return code;
    }
}
