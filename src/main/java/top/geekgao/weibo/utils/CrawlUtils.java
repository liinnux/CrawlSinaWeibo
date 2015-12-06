package top.geekgao.weibo.utils;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Proxy;
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
        System.out.println("获取[" + id + "]的真实id...");
        String json;

        int count = 0;
        while (true) {
            try {
                //尝试超过5此就退出
                if (count++ > 6) {
                    throw new IllegalStateException("获取用户oid失败");
                }

                json = getHtml("http://s.weibo.com/ajax/topsuggest.php?key=" + id).split("try\\{window\\.&\\(")[1].split("\\);}catch")[0];
                break;
            } catch (IOException e) {
                System.out.println("获取[" + id + "]的真实id出错!");
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("获取[" + id + "]的真实id出错!");
            }
        }

        JSONObject rootJson = new JSONObject(json);
        JSONObject data = rootJson.getJSONObject("data");

        JSONArray user = data.getJSONArray("user");
        if (user.length() == 0) {
            throw new IllegalStateException("没有找到此用户");
        }

        //第一个就是完全匹配的
        String oid = String.valueOf(user.getJSONObject(0).getBigInteger("u_id"));
        System.out.println("[" + id + "]的真实id => " + oid);

        return oid;
    }
}
