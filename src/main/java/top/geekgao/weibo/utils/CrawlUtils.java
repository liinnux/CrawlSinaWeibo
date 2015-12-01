package top.geekgao.weibo.utils;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

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
        return Integer.valueOf(properties.getProperty("blogCount"));
    }

    /**
     *
     * @return 点赞的抓取个数
     */
    public static Integer getLikeCount() {
        return Integer.valueOf(properties.getProperty("likeCount"));
    }

    /**
     *
     * @return 转发的抓取个数
     */
    public static Integer getForwardingCount() {
        return Integer.valueOf(properties.getProperty("forwardingCount"));
    }

    /**
     *
     * @return 评论的抓取个数
     */
    public static Integer getCommentCount() {
        return Integer.valueOf(properties.getProperty("commentCount"));
    }

    /**
     *
     * @return 关注的抓取个数
     */
    public static Integer getFollowingCount() {
        return Integer.valueOf(properties.getProperty("followingCount"));
    }

    /**
     *
     * @return 粉丝的抓取个数
     */
    public static Integer getFollowersCount() {
        return Integer.valueOf(properties.getProperty("followersCount"));
    }

    public static String getHtml(String url) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response = client.execute(get);
        String result = EntityUtils.toString(response.getEntity());
        client.close();
        return result;
    }

    /**
     * 根据输入的用户id返回用户真实oid
     */
    public static String getOid(String id) throws IOException {
        String html;
        html = getHtml("http://weibo.cn/" + id);
        return html.split("uid=")[1].split("&")[0];
    }
}
