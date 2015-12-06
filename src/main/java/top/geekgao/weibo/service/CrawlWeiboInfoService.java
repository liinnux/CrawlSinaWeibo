package top.geekgao.weibo.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import top.geekgao.weibo.po.Blog;
import top.geekgao.weibo.po.Comment;
import top.geekgao.weibo.utils.CrawlUtils;

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
    public List<String> crawlFollowingIds() throws IOException {
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
                    System.err.println("抓取关注信息失败");
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
            System.out.println("关注信息抓取完毕.");
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

        System.out.println("关注信息抓取完毕.");
        return followings;
    }

    /**
     * 抓取粉丝列表，抓取个数可在配置文件中配置，最多200个
     * @return 粉丝的oid集合
     */
    public List<String> crawlFollowerIds() throws IOException {
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
                    System.err.println("抓取粉丝信息失败");
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
            System.out.println("粉丝信息抓取完毕.");
            return followers;
        }

        for (Object user:users) {
            try {
                followers.add(((JSONObject)user).getString("name"));
            } catch (JSONException ignored) {
            }
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
        System.out.println("抓取博文信息...");
        List<String> weiboContentJsons = getContentJsons();
        List<Blog> blogs = new LinkedList<Blog>();
        //解析每一批微博
        int i = 0;
        for (String blogsJson:weiboContentJsons) {
            JSONArray cards;
            try {
                JSONObject rootJson = new JSONObject(blogsJson);
                cards = rootJson.getJSONArray("cards");
            } catch (JSONException e) {
                continue;
            }

            for (Object blogJson:cards) {
                System.out.println(i++);
                String itemId;
                try {
                    int card_type = ((JSONObject)blogJson).getInt("card_type");
                    //9代表了自己的微博或者转发，还有11代表了最近点赞的微博
                    if (card_type != 9) {
                        continue;
                    }

                    itemId = ((JSONObject)blogJson).getString("itemid");
                } catch (JSONException e) {
                    continue;
                }

                //此微博的id，用来获取评论，转发，赞的信息
                String blogId = itemId.substring(itemId.lastIndexOf('_') + 1);

                //获得微博内容
                System.out.print("a");
                String content = getContentString((JSONObject) blogJson);
                //获得微博发布时间
                System.out.print("b");
                String time = getWeiboTime((JSONObject) blogJson);
                //获得微博评论
                System.out.print("c");
                List<Comment> comments = getComments(blogId);
                //获得微博转发用户Oid
                System.out.print("d");
                List<String> fowardings = getFowardingIds(blogId);
                //获得微博赞的用户Oid
                System.out.print("e");
                List<String> likes = getLikeIds(blogId);
                System.out.print("f");

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

        int count = 0;
        //每次抓blogCount条，抓pageCount次
        for (int i = 1;i <= pageCount;i++) {
            String weiboContentJson = CrawlUtils.getHtml("http://api.weibo.cn/2/cardlist?uicode=10000198&featurecode=10000001&c=android&i=faf3db9&s=654d5841&ua=Meizu-MX4%20Pro__weibo__5.6.0__android__android5.0.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzZHJbhiihd9QgLIth8-uol6qkE.&fid=107603" + oid + "_-_WEIBO_SECOND_PROFILE_WEIBO&uid=5587279865&v_f=2&v_p=25&from=1056095010&gsid=_2A257Ztc9DeTxGeNL41UT9yfEzTmIHXVWMm31rDV6PUJbrdANLWjnkWosLBnkO0GNRzzY8ucvOU-qtLNNvg..&imsi=460017076390273&lang=zh_CN&page=" + i + "&skin=default&count=" + blogCount + "&oldwm=19005_0019&containerid=107603" + oid + "_-_WEIBO_SECOND_PROFILE_WEIBO&luicode=10000001&need_head_cards=0&sflag=1");
            if (weiboContentJson.contains("errmsg")) {
                count++;
                //超过5此尝试就不再尝试
                if (count > 5) {
                    System.err.println("抓取完整博文信息失败");
                    return result;
                }

                i--;
                continue;
            }
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
        try {
            JSONObject mblog = json.getJSONObject("mblog");
            //如果是转发的，那么就获取转发的内容
            if (mblog.has("retweeted_status")) {
                JSONObject retweeted_status = mblog.getJSONObject("retweeted_status");

                //转发的微博被删除会有这个标记
                //删除后就没有了user标记
                if (retweeted_status.has("deleted")) {
                    result.append(retweeted_status.getString("text"));
                } else {
                    JSONObject user = retweeted_status.getJSONObject("user");
                    result.append("@").append(user.getString("name")).append(":");
                    result.append(retweeted_status.getString("text"));
                }
            } else {
                result.append(mblog.getString("text"));
            }
        } catch (JSONException ignored) {
        }
        return result.toString();
    }

    /**
     *
     * @param json 单条微博内容的json串
     * @return 解析出来的微博发表时间
     */
    private String getWeiboTime(JSONObject json) {
        String time = null;
        try {
            time = json.getJSONObject("mblog").getString("created_at");
        } catch (JSONException ignored) {
        }
        return time.substring(0,time.indexOf('+') - 1);
    }

    /**
     * @param blogId 微博id
     * @return 返回此微博的转发人id
     */
    private List<String> getFowardingIds(String blogId) throws IOException {
        //转发用户id集合
        List<String> fowardingIds = new LinkedList<String>();
        //需要抓取的转发量
        int forwardingCount = CrawlUtils.getForwardingCount();
        //包含转发信息的json串
        List<String> forwardingJsons = new LinkedList<String>();

        int count = 0;
        //获取转发的json串
        //每次200个，i代表抓取哪一页，每页有200项转发信息
        for (int i = 1;i <= forwardingCount / 200;i++) {
            String json = CrawlUtils.getHtml("http://api.weibo.cn/2/statuses/repost_timeline?source=7501641714&uicode=10000002&lcardid=102803_-_mbloglist_" + blogId + "&c=android&i=faf3db9&s=654d5841&id=" + blogId + "&ua=Meizu-MX4%20Pro__weibo__5.6.0__android__android5.0.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzZHJbhiihd9QgLIth8-uol6qkE.&v_f=2&v_p=25&from=1056095010&gsid=_2A257Ztc9DeTxGeNL41UT9yfEzTmIHXVWMm31rDV6PUJbrdANLWjnkWosLBnkO0GNRzzY8ucvOU-qtLNNvg..&lang=zh_CN&lfid=102803&page=" + i + "&skin=default&count=200&oldwm=19005_0019&luicode=10000011&has_member=1&sflag=1");
            if (json.contains("errmsg")) {
                count++;
                //超过5此尝试就不再尝试
                if (count > 5) {
                    System.err.println("抓取转发信息失败");
                    return fowardingIds;
                }

                i--;
                continue;
            } else if (json.equals("{\"reposts\":null}")) {
                return fowardingIds;
            }
            forwardingJsons.add(json);
        }

        //解析每一个json
        for (String forwardingJson:forwardingJsons) {
            JSONArray reposts;
            try {
                JSONObject rootJson = new JSONObject(forwardingJson);
                reposts = rootJson.getJSONArray("reposts");
            } catch (JSONException e) {
                continue;
            }

            for (Object repost:reposts) {
                JSONObject user;
                try {
                    user = ((JSONObject)repost).getJSONObject("user");
                } catch (JSONException e) {
                    continue;
                }

                String oid = user.getString("name");
                //加入结果链
                fowardingIds.add(oid);
            }
        }

        return fowardingIds;
    }

    /**
     * @param blogId 微博id
     * @return 返回给此微博的赞的人id
     */
    private List<String> getLikeIds(String blogId) throws IOException {
        //存储点赞的人的oid
        List<String> likeIds = new LinkedList<String>();
        //获取需要抓取的点赞人数
        int likeCount = CrawlUtils.getLikeCount();
        //包含点赞信息的json串
        List<String> likeJsons = new LinkedList<String>();

        int count = 0;
        //每次获取200个，i代表页数
        for (int i = 1; i <= likeCount / 200;i++) {
            String json = CrawlUtils.getHtml("http://api.weibo.cn/2/like/show?uicode=10000002&featurecode=10000001&c=android&i=faf3db9&s=654d5841&id=" + blogId + "&ua=Meizu-MX4%20Pro__weibo__5.6.0__android__android5.0.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzZHJbhiihd9QgLIth8-uol6qkE.&v_f=2&v_p=25&from=1056095010&gsid=_2A257Ztc9DeTxGeNL41UT9yfEzTmIHXVWMm31rDV6PUJbrdANLWjnkWosLBnkO0GNRzzY8ucvOU-qtLNNvg..&lang=zh_CN&page=" + i + "&skin=default&type=0&count=200&oldwm=19005_0019&luicode=10000001&filter_by_author=0&filter_by_source=0&sflag=1");
            if (json.contains("errmsg")) {
                count++;
                //超过5此尝试就不再尝试
                if (count > 5) {
                    System.err.println("抓取点赞信息失败");
                    return likeIds;
                }

                i--;
                continue;
            } else if (json.equals("{\"users\":null}")) {
                return likeIds;
            }
            likeJsons.add(json);
        }

        //逐个分析每一批json串
        for (String likes:likeJsons) {
            JSONArray users;
            try {
                JSONObject rootJson = new JSONObject(likes);
                users = rootJson.getJSONArray("users");
            } catch (JSONException e) {
                continue;
            }

            for (Object user:users) {
                try {
                    likeIds.add(((JSONObject)user).getString("name"));
                } catch (JSONException ignored) {
                }
            }
        }

        return likeIds;
    }

    /**
     * @param blogId 微博id
     * @return 返回此微博的评论信息
     */
    private List<Comment> getComments(String blogId) throws IOException {
        //存储结果集
        List<Comment> commentList = new LinkedList<Comment>();
        //评论的抓取个数
        int commentCount = CrawlUtils.getCommentCount();
        //包含评论信息的json串
        List<String> commentJsons = new LinkedList<String>();

        int count = 0;
        //每次获取200个，i代表页数
        for (int i = 1; i <= commentCount / 200;i++) {
            String json;
            json = CrawlUtils.getHtml("http://api.weibo.cn/2/comments/show?trim_level=1&uicode=10000002&featurecode=10000001&c=android&i=faf3db9&s=654d5841&id=" + blogId + "&ua=Meizu-MX4%20Pro__weibo__5.6.0__android__android5.0.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzZHJbhiihd9QgLIth8-uol6qkE.&v_f=2&v_p=25&from=1056095010&gsid=_2A257Ztc9DeTxGeNL41UT9yfEzTmIHXVWMm31rDV6PUJbrdANLWjnkWosLBnkO0GNRzzY8ucvOU-qtLNNvg..&lang=zh_CN&page=" + i + "&skin=default&trim=1&count=200&oldwm=19005_0019&luicode=10000001&with_common_cmt=1&filter_by_author=0&sflag=1");
            if (json.contains("errmsg")) {
                count++;
                //超过5此尝试就不再尝试
                if (count > 5) {
                    System.err.println("抓取评论信息失败");
                    return commentList;
                }

                i--;
                continue;
            } else if (json.equals("{\"comments\":null}")) {
                return commentList;
            }
            commentJsons.add(json);
        }

        for (String json:commentJsons) {
            JSONArray comments;
            try {
                JSONObject rootJson = new JSONObject(json);
                comments = rootJson.getJSONArray("comments");
            } catch (JSONException e) {
                continue;
            }

            //遍历每一条评论
            for (Object comment:comments) {
                String id;
                String time;
                String content;
                try {
                    id = ((JSONObject)comment).getJSONObject("user").getString("screen_name");
                    time = ((JSONObject)comment).getString("created_at").split(" \\+")[0];
                    content = ((JSONObject)comment).getString("text");
                } catch (JSONException e) {
                    continue;
                }

                Comment c = new Comment();
                c.setId(id);
                c.setTime(time);
                c.setContent(content);
                //加入结果链
                commentList.add(c);
            }
        }

        return commentList;
    }
}
