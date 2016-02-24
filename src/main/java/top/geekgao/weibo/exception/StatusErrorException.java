package top.geekgao.weibo.exception;

/**
 * Created by geekgao on 16-2-25.
 * 错误导致无法继续抓取，则抛出此异常
 */
public class StatusErrorException extends Exception{
    public StatusErrorException(String s) {
        super(s);
    }
}
