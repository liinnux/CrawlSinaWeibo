package top.geekgao.weibo.service;

import com.thoughtworks.xstream.XStream;
import org.json.JSONArray;
import org.json.JSONObject;
import top.geekgao.weibo.po.Blog;
import top.geekgao.weibo.po.Comment;
import top.geekgao.weibo.po.PersonalInfo;
import top.geekgao.weibo.po.WeiboInfo;
import top.geekgao.weibo.utils.CrawlUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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
    public List<String> crawlFollowingOids() throws IOException {
        System.out.println("抓取关注信息...");
        int followingCount = CrawlUtils.getFollowingCount();
        String url = "http://api.weibo.cn/2/friendships/friends?trim_status=0&uicode=10000195&featurecode=10000001&c=android&i=7db11f3&s=ec4938f8&ua=Meizu-MX4%20Pro__weibo__5.6.0__android__android5.0.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzZHJbhiihd9QgLIth8-uol6qkE.&uid=" + oid + "&v_f=2&from=1056095010&gsid=_2A257WHTjDeRxGedJ7VsQ8inPyj6IHXVWTI8rrDV6PUJbrdAKLXjikWpnte1CBWpiSGvIiO29s2IRw7TMFw..&lang=zh_CN&lfid=107603" + oid + "_-_WEIBO_SECOND_PROFILE_WEIBO&page=1&skin=default&sort=1&has_pages=1&count=" + followingCount + "&oldwm=19005_0019&luicode=10000198&has_top=0&has_relation=1&has_member=1&lastmblog=1&sflag=1";
        List<String> followings = new LinkedList<String>();
        //获得关注信息
        String json = CrawlUtils.getHtml(url);

        JSONObject rootJson = new JSONObject(json);
        JSONArray users = rootJson.getJSONArray("users");
        for (Object user:users) {
            //过滤掉不是“人”的关注
            //关注的是“话题”会有这一项，关注的是人的话不会有
            if (!((JSONObject)user).has("object_type")) {
                followings.add(((JSONObject)user).getString("idstr"));
            }
        }

        System.out.println("关注信息抓取完毕.");
        return followings;
    }

    /**
     * 抓取粉丝列表，抓取个数可在配置文件中配置，最多200个
     * @return 粉丝的oid集合
     */
    public List<String> crawlFollowerOids() throws IOException {
        System.out.println("抓取粉丝信息...");
        int followersCount = CrawlUtils.getFollowersCount();
        String url = "http://api.weibo.cn/2/friendships/followers?trim_status=0&uicode=10000081&featurecode=10000001&c=android&i=7db11f3&s=ec4938f8&ua=Meizu-MX4%20Pro__weibo__5.6.0__android__android5.0.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzZHJbhiihd9QgLIth8-uol6qkE.&uid=" + oid + "&v_f=2&from=1056095010&gsid=_2A257WHTjDeRxGedJ7VsQ8inPyj6IHXVWTI8rrDV6PUJbrdAKLXjikWpnte1CBWpiSGvIiO29s2IRw7TMFw..&lang=zh_CN&lfid=107603" + oid + "_-_WEIBO_SECOND_PROFILE_WEIBO&page=1&skin=default&sort=3&has_pages=0&count=" + followersCount + "&oldwm=19005_0019&luicode=10000198&has_top=0&has_relation=1&has_member=1&lastmblog=1&sflag=1";
        List<String> followers = new LinkedList<String>();
        //获得粉丝信息
        String json = CrawlUtils.getHtml(url);

        JSONObject rootJson = new JSONObject(json);
        JSONArray users = rootJson.getJSONArray("users");
        for (Object user:users) {
            followers.add(((JSONObject)user).getString("idstr"));
        }

        System.out.println("粉丝信息抓取完毕.");
        return followers;
    }

    /**
     * 抓取微博博文内容
     * 抓取微博个数可配置
     * @return 微博内容集合
     */
    public List<Blog> crawlBlog() throws IOException {
        System.out.println("抓取博文信息...");//todo 删除全部打印语句
        List<String> weiboContentJsons = getContentJsons();
        System.out.println(1);
        List<Blog> blogs = new LinkedList<Blog>();
        //解析每一批微博
        for (String blogsJson:weiboContentJsons) {
            JSONObject rootJson = new JSONObject(blogsJson);
            JSONArray cards = rootJson.getJSONArray("cards");
            for (Object blogJson:cards) {
                String itemId = ((JSONObject)blogJson).getString("itemid");
                //此微博的id，用来获取评论，转发，赞的信息
                String blogId = itemId.substring(itemId.lastIndexOf('_') + 1);

                //获得微博内容
                String content = getContentString((JSONObject) blogJson);
                //获得微博发布时间
                String time = getWeiboTime((JSONObject) blogJson);
                //获得微博评论
                List<Comment> comments = getComments(blogId);
                //获得微博转发用户Oid
                System.out.println(2);
                List<String> fowardings = getFowardingOids(blogId);
                System.out.println(3);
                //获得微博赞的用户Oid
                System.out.println(4);
                List<String> likes = getLikeOids(blogId);
                System.out.println(5);

                Blog blog = new Blog();
                blog.setContent(content);
                blog.setTime(time);
                blog.setComments(comments);
                blog.setFowardings(fowardings);
                blog.setLikes(likes);

                blogs.add(blog);
            }
        }
        System.out.println("博文信息抓取完毕.");

        //XStream类会自动过滤null标签
        XStream xStream = new XStream();
        //重命名根节点
        xStream.alias("info",blogs.getClass());
        String result = xStream.toXML(blogs);

        BufferedWriter writer = new BufferedWriter(new FileWriter("/home/geekgao/t.xml"));
        writer.write(result);
        writer.close();

        return blogs;
    }

    /**
     *
     * @return 返回的list里面都是包含一批微博内容的json串,抓取微博个数可配置
     */
    private List<String> getContentJsons() throws IOException {
        List<String> result = new LinkedList<String>();
        //抓取pageCount页
        int pageCount = CrawlUtils.getPageCount();
        //每页blogCount条博客
        int blogCount = CrawlUtils.getBlogCount();
        //blogCount不能为0，但是大于0时会多抓一个
        if (blogCount > 1) {
            blogCount--;
        }

        //每次抓blogCount条，抓pageCount次
        for (int i = 1;i <= pageCount;i++) {
            String weiboContentJson = CrawlUtils.getHtml("http://api.weibo.cn/2/cardlist?uicode=10000198&featurecode=10000001&c=android&i=7db11f3&s=ec4938f8&ua=Meizu-MX4%20Pro__weibo__5.6.0__android__android5.0.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzZHJbhiihd9QgLIth8-uol6qkE.&fid=107603" + oid + "_-_WEIBO_SECOND_PROFILE_WEIBO&uid=1769127312&v_f=2&v_p=25&from=1056095010&gsid=_2A257WHTjDeRxGedJ7VsQ8inPyj6IHXVWTI8rrDV6PUJbrdAKLXjikWpnte1CBWpiSGvIiO29s2IRw7TMFw..&imsi=460017076390273&lang=zh_CN&page=" + i + "&skin=default&count=" + blogCount + "&oldwm=19005_0019&containerid=107603" + oid + "_-_WEIBO_SECOND_PROFILE_WEIBO&luicode=10000001&need_head_cards=0&sflag=1");
            result.add(weiboContentJson);
        }

        return result;
    }

    /**
     *
     * @param json 单条微博内容的json串
     * @return 解析出来的微博内容
     */
    private String getContentString(JSONObject json) {
        //存储微博内容
        StringBuilder result = new StringBuilder();
        JSONObject mblog = json.getJSONObject("mblog");
        //如果是转发的，那么就获取转发的内容
        if (mblog.has("retweeted_status")) {
            JSONObject retweeted_status = mblog.getJSONObject("retweeted_status");
            JSONObject user = retweeted_status.getJSONObject("user");
            result.append("@").append(user.getString("name")).append(":");
            result.append(retweeted_status.getString("text"));
        } else {
            result.append(mblog.getString("text"));
        }
        return result.toString();
    }

    /**
     *
     * @param json 单条微博内容的json串
     * @return 解析出来的微博发表时间
     */
    private String getWeiboTime(JSONObject json) {
        String time = json.getJSONObject("mblog").getString("created_at");
        return time.substring(0,time.indexOf('+') - 1);
    }

    /**
     * @param blogId 微博id
     * @return 返回此微博的转发人id
     */
    private List<String> getFowardingOids(String blogId) throws IOException {
        //转发用户id集合
        List<String> fowardingOids = new LinkedList<String>();
        //需要抓取的转发量
        int forwardingCount = CrawlUtils.getForwardingCount();
        //包含转发信息的json串
        List<String> forwardingJsons = new LinkedList<String>();

        //获取评论的json串
        //每次200个，i代表抓取哪一页，每页有200项转发信息
        for (int i = 1;i <= forwardingCount / 200;i++) {
            String json = CrawlUtils.getHtml("http://api.weibo.cn/2/statuses/repost_timeline?source=7501641714&uicode=10000002&featurecode=10000001&lcardid=1076033217179555_-_WEIBO_SECOND_PROFILE_WEIBO_-_" + blogId + "&c=android&i=7db11f3&s=ec4938f8&id=" + blogId + "&ua=Meizu-MX4%20Pro__weibo__5.6.0__android__android5.0.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzZHJbhiihd9QgLIth8-uol6qkE.&v_f=2&v_p=25&from=1056095010&gsid=_2A257WHTjDeRxGedJ7VsQ8inPyj6IHXVWTI8rrDV6PUJbrdAKLXjikWpnte1CBWpiSGvIiO29s2IRw7TMFw..&lang=zh_CN&lfid=1076033217179555_-_WEIBO_SECOND_PROFILE_WEIBO&page=" + i + "&skin=default&count=200&oldwm=19005_0019&luicode=10000198&has_member=1&sflag=1");
            forwardingJsons.add(json);
            System.out.println("转发：" + json);//todo 待删
        }

        //解析每一各json
        for (String forwardingJson:forwardingJsons) {
            JSONObject rootJson = new JSONObject(forwardingJson);
            JSONArray reposts = rootJson.getJSONArray("reposts");
            for (Object repost:reposts) {
                JSONObject user = ((JSONObject)repost).getJSONObject("user");
                String oid = user.getString("idstr");
                //加入结果链
                fowardingOids.add(oid);
            }
        }

        return fowardingOids;
    }

    /**
     * @param blogId 微博id
     * @return 返回给此微博的赞的人id
     */
    private List<String> getLikeOids(String blogId) throws IOException {
        //存储点赞的人的oid
        List<String> likeOids = new LinkedList<String>();
        //获取需要抓取的点赞人数
        int likeCount = CrawlUtils.getLikeCount();
        //包含点赞信息的json串
        List<String> likeJsons = new LinkedList<String>();

        //每次获取200个，i代表页数
        for (int i = 1; i <= likeCount / 200;i++) {
            String json = CrawlUtils.getHtml("http://api.weibo.cn/2/like/show?uicode=10000002&featurecode=10000001&lcardid=1076033217179555_-_WEIBO_SECOND_PROFILE_WEIBO_-_" + blogId + "&c=android&i=7db11f3&s=ec4938f8&id=" + blogId + "&ua=Meizu-MX4%20Pro__weibo__5.6.0__android__android5.0.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzZHJbhiihd9QgLIth8-uol6qkE.&v_f=2&v_p=25&from=1056095010&gsid=_2A257WHTjDeRxGedJ7VsQ8inPyj6IHXVWTI8rrDV6PUJbrdAKLXjikWpnte1CBWpiSGvIiO29s2IRw7TMFw..&lang=zh_CN&lfid=1076033217179555_-_WEIBO_SECOND_PROFILE_WEIBO&page=" + i + "&skin=default&type=0&count=200&oldwm=19005_0019&luicode=10000198&filter_by_author=0&filter_by_source=0&sflag=1");
            likeJsons.add(json);
            System.out.println("点赞：" + json);//todo 待删
        }

        //逐个分析每一批json串
        for (String likes:likeJsons) {
            JSONObject rootJson = new JSONObject(likes);
            JSONArray users = rootJson.getJSONArray("users");
            for (Object user:users) {
                likeOids.add(((JSONObject)user).getString("idstr"));
            }
        }

        return likeOids;
    }

    /**
     * @param blogId 微博id
     * @return 返回此微博的评论信息
     */
    private List<Comment> getComments(String blogId) {

        return null;
    }
}