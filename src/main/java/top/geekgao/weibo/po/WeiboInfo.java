package top.geekgao.weibo.po;

import java.util.List;

/**
 * Created by geekgao on 15-12-1.
 */
public class WeiboInfo {
    private String id;
    private List<String> followingOids;
    private List<String> followerOids;
    private List<Blog> blogs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getFollowingOids() {
        return followingOids;
    }

    public void setFollowingOids(List<String> followingOids) {
        this.followingOids = followingOids;
    }

    public List<String> getFollowerOids() {
        return followerOids;
    }

    public void setFollowerOids(List<String> followerOids) {
        this.followerOids = followerOids;
    }

    public List<Blog> getBlogs() {
        return blogs;
    }

    public void setBlogs(List<Blog> blogs) {
        this.blogs = blogs;
    }
}
