package top.geekgao.weibo.po;

/**
 * Created by geekgao on 15-12-1.
 */
public class Comment {
    //用户id
    private String id;
    //发表时间
    private String time;
    //评论内容
    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
