package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SerialGenerator
{
    private static JedisUtil redis = JedisUtil.getInstance();
    public static String makeSerialNum(BusinessEnum businessEnum){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(businessEnum.getDatePattern());
        String today = formatter.format(LocalDateTime.now());
        StringBuffer prefix = new StringBuffer();
        prefix.append(businessEnum.getPrefix());
        prefix.append(today);
        //生成号码池
        CreateSerialPool(prefix.toString(),businessEnum,today);
        //取号
        String SerialNum = FetchCodeFromRedis(businessEnum.getPrefix());
        return SerialNum;
    }

    private static void CreateSerialPool(String prefix,BusinessEnum businessEnum,String currentDate)
    {
        String key = businessEnum.getPrefix();
        String currentLast = "";
        int beginNum = businessEnum.getStartNum();
        List list = new ArrayList();
        try {
            //清空旧数据
            if (redis.exists(key)) {
                list = redis.getObjectByKey(key, ArrayList.class);
                //每天第一次使用会把前一天的编号池清空
                if (!list.get(list.size() - 1).toString().contains(currentDate)) {
                    list.clear();
                    System.out.println("前一天的数据正在清理...");
                }
            }

            int initCacheLength = list.size();
            //判断是否需要生成code
            if (IsCreate(key, businessEnum, list)) {
                //如果今天的序号已经生成
                if (list.size() > 0) {
                    //计算当前序号
                    currentLast = list.get(list.size() - 1).toString();
                    //计算下一个序号
                    beginNum = Integer.parseInt(currentLast.substring(businessEnum.getSerialNumberIndex())) + 1;
                }
                //保证号码池有max个号码
                for (int i = beginNum; i < beginNum + (businessEnum.getMaxPoolSize() - initCacheLength); ++i) {
                    StringBuffer buffer = new StringBuffer(prefix);
                    //用0补全位数
                    buffer.append(String.format("%0" + businessEnum.getSerialLength() + "d", i));
                    list.add(buffer.toString());
                }
                redis.setObjectByKey(key,list,1000*60*60*24);
            }
            System.out.println("号码池：" + redis.getObjectByKey(key, ArrayList.class));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static String FetchCodeFromRedis(String key)
    {
        String code = "";
        try{
            List list = redis.getObjectByKey(key, ArrayList.class);
            code = list.get(0).toString();
            list.remove(0);
            redis.setObjectByKey(key,list,1000*60*60*24);
            System.out.println("取走第一个，剩余："+redis.getObjectByKey(key, ArrayList.class));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return code;
    }

    private static boolean IsCreate(String key,BusinessEnum businessEnum,List list)
    {
        boolean flag = false;
        if(!redis.exists(key) || list.size() < businessEnum.getMinPoolSize())
        {
            flag = true;
        }
        return flag;
    }
}
