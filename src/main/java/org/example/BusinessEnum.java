package org.example;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BusinessEnum
{
    QQ("QQ","yyyyMMdd",5,1,100,30),;
    /**
     * 单号前缀
     * 为空时填""
     */
    private String prefix;

    /**
     * 时间格式表达式
     * 例如：yyyyMMdd
     */
    private String datePattern;

    /**
     * 流水号长度
     */
    private Integer serialLength;

    /**
     * 每日起排号
     */
    private Integer startNum;

    /**
     * 号码池最大容量
     */
    private Integer maxPoolSize;

    /**
     * 号码池容量的临界值
     */
    private Integer minPoolSize;
    /**
     * 获取流水号的索引
     */
    public int getSerialNumberIndex()
    {
        return this.getPrefix().length() + this.getDatePattern().length();
    }
}
