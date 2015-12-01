package top.geekgao.weibo.crawl;

import top.geekgao.weibo.utils.CrawlUtils;

import java.io.IOException;

/**
 * Created by geekgao on 15-11-30.
 * 抓取博主的博文相关信息
 */
public class CrawlWeiboInfo {
    //网页url上面显示的用户的id
    private String id;
    //用户真实的id
    private String oid;

    public CrawlWeiboInfo(String id) throws IOException {
        this.id = id;
        oid = CrawlUtils.getOid(id);
    }
}
