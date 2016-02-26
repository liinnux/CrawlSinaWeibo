package top.geekgao.weibo.crawl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import top.geekgao.weibo.exception.StatusErrorException;
import top.geekgao.weibo.po.Blog;
import top.geekgao.weibo.po.Comment;
import top.geekgao.weibo.utils.CrawlUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by geekgao on 16-2-25.
 * 抓取单挑微博的任务类
 */
public class CrawlSingleWeibo implements Runnable{
    //本条微博的信息
    private JSONObject blogJson;
    //执行本线程的executor
    private ExecutorService executor;
    //将组装好的Blog对象放进这个list
    private List<Blog> blogs;

    public CrawlSingleWeibo(JSONObject blogJson, List<Blog> blogs,ExecutorService executor) {
        this.blogJson = blogJson;
        this.blogs = blogs;
        this.executor = executor;
    }

    public void run() {
        Blog blog = new Blog();
        String itemId;
        try {
            int card_type = blogJson.getInt("card_type");
            //9代表了自己的微博或者转发，还有11代表了最近点赞的微博
            if (card_type != 9) {
                return;
            }

            itemId = blogJson.getString("itemid");
        } catch (JSONException e) {
            return;
        }

        //此微博的id，用来获取评论，转发，赞的信息
        String blogId = itemId.substring(itemId.lastIndexOf('_') + 1);

        //获得微博内容
        String content = getContentString((JSONObject) blogJson);
        //获得微博发布时间
        String time = getWeiboTime((JSONObject) blogJson);
        //获得微博评论
        List<Comment> comments;
        //获得微博转发用户Oid
        List<String> fowardings;
        //获得微博赞的用户Oid
        List<String> likes;
        try {
            comments = getComments(blogId);
            fowardings = getFowardingIds(blogId);
            likes = getLikeIds(blogId);

            blog.setContent(content);
            blog.setTime(time);
            blog.setComments(comments);
            blog.setFowardings(fowardings);
            blog.setLikes(likes);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (StatusErrorException e) {
            //避免重复关闭线程池
            synchronized (executor) {
                if (!executor.isShutdown()) {
                    //一旦遇到无法继续抓取的情况，立即结束线程池
                    executor.shutdownNow();
                    System.err.println(e.getMessage());
                }
            }
        }
        blogs.add(blog);
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
    private List<String> getFowardingIds(String blogId) throws IOException, StatusErrorException {
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
            //登陆后的账号获取的
            String json = CrawlUtils.getHtml("http://api.weibo.cn/2/statuses/repost_timeline?source=7501641714&uicode=10000002&lcardid=102803_-_mbloglist_" + blogId + "&c=android&i=faf3db9&s=654d5841&id=" + blogId + "&ua=Meizu-MX4%20Pro__weibo__5.6.0__android__android5.0.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzZHJbhiihd9QgLIth8-uol6qkE.&v_f=2&v_p=25&from=1056095010&gsid=_2A257Ztc9DeTxGeNL41UT9yfEzTmIHXVWMm31rDV6PUJbrdANLWjnkWosLBnkO0GNRzzY8ucvOU-qtLNNvg..&lang=zh_CN&lfid=102803&page=" + i + "&skin=default&count=200&oldwm=19005_0019&luicode=10000011&has_member=1&sflag=1");
            //游客模式获取的
//            String json = CrawlUtils.getHtml("http://api.weibo.cn/2/guest/statuses_repost_timeline?networktype=wifi&source=7501641714&uicode=10000002&checktoken=800cee2ca19e61c5041491fc68478974&featurecode=10000085&lcardid=2302833217179555_" + blogId + "&c=android&i=faf3db9&s=b84a9692&id=" + blogId + "&ua=Meizu-MX4%20Pro__weibo__6.0.0__android__android5.1.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzb4Nk0i57tn_S3us9svHkMK-Cs.&did=61ccdff4c981b28ef80b9074e9c9d1429c2d950b&v_f=2&v_p=27&from=1060095010&gsid=_2AkMhkvevf8NhqwJRmPoRzWrlZYx0zg7EiebDAHrsJxI3HigX7DxnqFvTiDr5sEMqxAi0yIZRkkFkLlvc&lang=zh_CN&lfid=2302833217179555&page=" + i + "&skin=default&count=20&oldwm=9848_0009&luicode=10000198&has_member=1&sflag=1");
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
    private List<String> getLikeIds(String blogId) throws IOException, StatusErrorException {
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
    private List<Comment> getComments(String blogId) throws IOException, StatusErrorException {
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
            //登陆后的账号获取的
            json = CrawlUtils.getHtml("http://api.weibo.cn/2/comments/show?trim_level=1&uicode=10000002&featurecode=10000001&c=android&i=faf3db9&s=654d5841&id=" + blogId + "&ua=Meizu-MX4%20Pro__weibo__5.6.0__android__android5.0.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzZHJbhiihd9QgLIth8-uol6qkE.&v_f=2&v_p=25&from=1056095010&gsid=_2A257Ztc9DeTxGeNL41UT9yfEzTmIHXVWMm31rDV6PUJbrdANLWjnkWosLBnkO0GNRzzY8ucvOU-qtLNNvg..&lang=zh_CN&page=" + i + "&skin=default&trim=1&count=200&oldwm=19005_0019&luicode=10000001&with_common_cmt=1&filter_by_author=0&sflag=1");
            //游客模式获取的
//            json = CrawlUtils.getHtml("http://api.weibo.cn/2/guest/comments_show?trim_level=1&networktype=wifi&uicode=10000002&checktoken=800cee2ca19e61c5041491fc68478974&featurecode=10000085&lcardid=2302833217179555_" + blogId + "&c=android&i=faf3db9&s=b84a9692&id=" + blogId + "&ua=Meizu-MX4%20Pro__weibo__6.0.0__android__android5.1.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzb4Nk0i57tn_S3us9svHkMK-Cs.&did=61ccdff4c981b28ef80b9074e9c9d1429c2d950b&uid=1002411791346&v_f=2&v_p=27&from=1060095010&gsid=_2AkMhkvevf8NhqwJRmPoRzWrlZYx0zg7EiebDAHrsJxI3HigX7DxnqFvTiDr5sEMqxAi0yIZRkkFkLlvc&lang=zh_CN&lfid=2302833217179555&page=" + i + "&skin=default&trim=1&count=20&oldwm=9848_0009&luicode=10000198&with_common_cmt=1&filter_by_author=0&sflag=1");
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
