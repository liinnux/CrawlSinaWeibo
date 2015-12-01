package top.geekgao.weibo.po;

import java.util.List;

/**
 * Created by geekgao on 15-12-1.
 */
public class Blog {
    //微博内容
    private String content;
    //发表时间
    private String time;
    //所有评论
    private List<Comment> comments;
    //所有转发人的id
    private List<String> fowardings;
    //所有赞的人的id
    private List<String> likes;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<String> getFowardings() {
        return fowardings;
    }

    public void setFowardings(List<String> fowardings) {
        this.fowardings = fowardings;
    }

    public List<String> getLikes() {
        return likes;
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }
}
