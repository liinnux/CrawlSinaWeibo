package top.geekgao.weibo.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import top.geekgao.weibo.crawl.CrawlSingleWeibo;
import top.geekgao.weibo.exception.StatusErrorException;
import top.geekgao.weibo.po.Blog;
import top.geekgao.weibo.po.Comment;
import top.geekgao.weibo.utils.CrawlUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by geekgao on 15-12-1.
 */
public class CrawlWeiboInfoService {
    //待抓取的用户oid
    private String oid;

    public CrawlWeiboInfoService(String oid) {
        this.oid = oid;
    }

    /**
     * 抓取关注人列表,抓取个数可在配置文件中配置，最多200个
     * @return 关注人的oid集合
     */
    public List<String> crawlFollowingIds() throws IOException, StatusErrorException {
        System.out.println("抓取关注信息...");
        int followingCount = CrawlUtils.getFollowingCount();
        String url = "http://api.weibo.cn/2/friendships/friends?trim_status=0&uicode=10000195&featurecode=10000001&c=android&i=faf3db9&s=654d5841&ua=Meizu-MX4%20Pro__weibo__5.6.0__android__android5.0.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzZHJbhiihd9QgLIth8-uol6qkE.&uid=" + oid + "&v_f=2&from=1056095010&gsid=_2A257Ztc9DeTxGeNL41UT9yfEzTmIHXVWMm31rDV6PUJbrdANLWjnkWosLBnkO0GNRzzY8ucvOU-qtLNNvg..&lang=zh_CN&lfid=230283" + oid + "&page=1&skin=default&sort=1&has_pages=1&count=" + followingCount + "&oldwm=19005_0019&luicode=10000198&has_top=0&has_relation=1&has_member=1&lastmblog=1&sflag=1";
        List<String> followings = new LinkedList<String>();
        //获得关注信息
        String json;
        int count = 0;
        while (true) {
            json = CrawlUtils.getHtml(url);
            if (!json.contains("errmsg")) {
                count++;
                //超过5此尝试就不再尝试
                if (count > 5) {
                    System.err.println("【抓取关注信息失败】！");
                    return followings;
                }
                break;
            }
        }

        JSONArray users;
        try {
            JSONObject rootJson = new JSONObject(json);
            users = rootJson.getJSONArray("users");
        } catch (JSONException e) {
            System.out.println("【关注信息抓取完毕.】");
            return followings;
        }

        for (Object user:users) {
            try {
                //过滤掉不是“人”的关注
                //关注的是“话题”会有这一项，关注的是人的话不会有
                if (!((JSONObject)user).has("object_type")) {
                    followings.add(((JSONObject)user).getString("name"));
                }
            } catch (JSONException ignored) {
            }
        }

        System.out.println("【关注信息抓取完毕.】");
        return followings;
    }

    /**
     * 抓取粉丝列表，抓取个数可在配置文件中配置，最多200个
     * @return 粉丝的oid集合
     */
    public List<String> crawlFollowerIds() throws IOException, StatusErrorException {
        System.out.println("抓取粉丝信息...");
        int followersCount = CrawlUtils.getFollowersCount();
        String url = "http://api.weibo.cn/2/friendships/followers?trim_status=0&uicode=10000081&featurecode=10000001&c=android&i=faf3db9&s=654d5841&ua=Meizu-MX4%20Pro__weibo__5.6.0__android__android5.0.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzZHJbhiihd9QgLIth8-uol6qkE.&uid=" + oid + "&v_f=2&from=1056095010&gsid=_2A257Ztc9DeTxGeNL41UT9yfEzTmIHXVWMm31rDV6PUJbrdANLWjnkWosLBnkO0GNRzzY8ucvOU-qtLNNvg..&lang=zh_CN&lfid=230283" + oid + "&page=1&skin=default&sort=3&has_pages=0&count=" + followersCount + "&oldwm=19005_0019&luicode=10000198&has_top=0&has_relation=1&has_member=1&lastmblog=1&sflag=1";
        List<String> followers = new LinkedList<String>();
        //获得粉丝信息
        String json;
        int count = 0;
        while (true) {
            json = CrawlUtils.getHtml(url);
            if (!json.contains("errmsg")) {
                count++;
                //超过5此尝试就不再尝试
                if (count > 5) {
                    System.err.println("【抓取粉丝信息失败】！");
                    return followers;
                }
                break;
            }
        }

        JSONArray users;
        try {
            JSONObject rootJson = new JSONObject(json);
            users = rootJson.getJSONArray("users");
        } catch (JSONException e) {
            System.out.println("【粉丝信息抓取完毕.】");
            return followers;
        }

        for (Object user:users) {
            try {
                followers.add(((JSONObject)user).getString("name"));
            } catch (JSONException ignored) {
            }
        }

        System.out.println("【粉丝信息抓取完毕.】");
        return followers;
    }

    /**
     * 抓取微博博文内容
     * 抓取微博个数可配置
     * @return 微博内容集合
     */
    public List<Blog> crawlBlog() throws IOException, StatusErrorException {
        System.out.println("抓取博文信息...");
        List<String> weiboContentJsons = getContentJsons();
        List<Blog> blogs = new LinkedList<Blog>();

        //抓取每条微博是一个单任务，每条线程执行一个这样的任务
        ExecutorService executor = Executors.newFixedThreadPool(10);

        //解析每一批微博
        for (String blogsJson:weiboContentJsons) {
            JSONArray cards;
            try {
                JSONObject rootJson = new JSONObject(blogsJson);
                cards = rootJson.getJSONArray("cards");
            } catch (JSONException e) {
                continue;
            }

            //抓取每一条微博的评论，转发，赞
            for (Object blogJson:cards) {

                //线程池没有关闭就往里面添加任务，正在执行的线程池可能会因为StatusErrorException异常而终止
                //在这里检测出了已经关闭就是因为StatusErrorException异常而终止，所以必须退出
                if (!executor.isShutdown()) {
                    executor.execute(new CrawlSingleWeibo((JSONObject) blogJson,blogs,executor));
                } else {//中途关闭只可能是在线程中与到了问题将线程池关闭了
                    return blogs;
                }
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("【博文信息抓取完毕.】");

        return blogs;
    }

    /**
     *
     * @return 返回的list里面都是包含一批微博内容的json串,抓取微博个数可配置
     */
    private List<String> getContentJsons() throws IOException, StatusErrorException {
        List<String> result = new LinkedList<String>();
        //抓取pageCount页
        int pageCount = CrawlUtils.getPageCount();
        //每页blogCount条博客
        int blogCount = CrawlUtils.getBlogCount();

        int startPage = CrawlUtils.getStartPage();
        int count = 0;
        //每次抓blogCount条，抓pageCount次
        for (int i = 1;i <= pageCount;i++,startPage++) {
            String weiboContentJson = CrawlUtils.getHtml("http://api.weibo.cn/2/cardlist?uicode=10000198&featurecode=10000001&c=android&i=faf3db9&s=654d5841&ua=Meizu-MX4%20Pro__weibo__5.6.0__android__android5.0.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzZHJbhiihd9QgLIth8-uol6qkE.&fid=107603" + oid + "_-_WEIBO_SECOND_PROFILE_WEIBO&uid=5587279865&v_f=2&v_p=25&from=1056095010&gsid=_2A257Ztc9DeTxGeNL41UT9yfEzTmIHXVWMm31rDV6PUJbrdANLWjnkWosLBnkO0GNRzzY8ucvOU-qtLNNvg..&imsi=460017076390273&lang=zh_CN&page=" + startPage + "&skin=default&count=" + blogCount + "&oldwm=19005_0019&containerid=107603" + oid + "_-_WEIBO_SECOND_PROFILE_WEIBO&luicode=10000001&need_head_cards=0&sflag=1");
            if (weiboContentJson.contains("errmsg")) {
                count++;
                //超过5此尝试就不再尝试
                if (count > 5) {
                    System.err.println("抓取完整博文信息失败");
                    return result;
                }

                i--;
                startPage--;
                continue;
            }
            result.add(weiboContentJson);
        }

        return result;
    }
}
