package top.geekgao.weibo.utils;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by geekgao on 15-11-30.
 * 工具类
 */
public class CrawlUtils {

    private static Properties properties = new Properties();
    static {
        try {
            properties.load(CrawlUtils.class.getResourceAsStream("/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return 个人信息保存的路径
     * @throws IOException
     */
    public static String getPersonalInfoPath() {
        return properties.getProperty("personalInfoPath");
    }

    /**
     *
     * @return 微博的具体信息的保存路径
     * @throws IOException
     */
    public static String getWeiboContentInfoPath() {
        return properties.getProperty("weiboContentInfoPath");
    }

    /**
     *
     * @return 微博的抓取个数
     */
    public static Integer getBlogCount() {
        return Integer.valueOf(properties.getProperty("weiboContentInfoPath"));
    }

    /**
     *
     * @return 点赞的抓取个数
     */
    public static Integer getLikeCount() {
        return Integer.valueOf(properties.getProperty("weiboContentInfoPath"));
    }

    /**
     *
     * @return 转发的抓取个数
     */
    public static Integer getForwardingCount() {
        return Integer.valueOf(properties.getProperty("weiboContentInfoPath"));
    }

    /**
     *
     * @return 评论的抓取个数
     */
    public static Integer getcommentCount() {
        return Integer.valueOf(properties.getProperty("weiboContentInfoPath"));
    }
}
