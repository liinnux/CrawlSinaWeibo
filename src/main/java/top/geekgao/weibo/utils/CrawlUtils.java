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
     * @return 需要抓取的微博页数
     */
    public static Integer getPageCount() {
        return Integer.valueOf(properties.getProperty("PageCount"));
    }

    /**
     *
     * @return 每页微博的个数，搭配需要抓取的页面数量使用
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
        get.setHeader("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36");
        get.setHeader("Upgrade-Insecure-Requests","1");
        CloseableHttpResponse response = client.execute(get);
        String result = EntityUtils.toString(response.getEntity());
        client.close();
        return result;
    }

    /**
     * 根据输入的用户id返回用户真实oid
     */
    public static String getOid(String id)  {
        System.out.println("获取用户真实id...");
        String html;

        while (true) {
            try {
                html = getHtml("http://weibo.cn/" + id).split("uid=")[1].split("&")[0];
                break;
            } catch (IOException e) {
                System.out.println("获取用户真实id出错!");
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("获取用户真实id出错!");
            }
        }
        System.out.println("[" + id + "]的真实id => " + html);

        return html;
    }
}
