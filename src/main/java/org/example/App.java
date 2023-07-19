package org.example;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        String s = SerialGenerator.makeSerialNum(BusinessEnum.QQ);
        System.out.println( s );
    }
}
