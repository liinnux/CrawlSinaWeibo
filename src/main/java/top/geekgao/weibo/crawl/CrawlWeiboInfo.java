package top.geekgao.weibo.crawl;

import com.thoughtworks.xstream.XStream;
import top.geekgao.weibo.po.Blog;
import top.geekgao.weibo.po.Comment;
import top.geekgao.weibo.po.WeiboInfo;
import top.geekgao.weibo.service.CrawlWeiboInfoService;
import top.geekgao.weibo.utils.CrawlUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by geekgao on 15-11-30.
 * 抓取博主的博文相关信息
 * 开放三个共有接口crawl(),write(),getWeiboInfo()
 */
public class CrawlWeiboInfo {
    //网页url上面显示的用户的id
    private String id;
    //用户真实的id
    private String oid;
    //包含微博信息的对象
    private WeiboInfo weiboInfo;
    //抓取信息的服务类
    private CrawlWeiboInfoService crawlService;

    public CrawlWeiboInfo(String id) throws IOException {
        this.id = id;
        oid = CrawlUtils.getOid(id);
        crawlService = new CrawlWeiboInfoService(oid);
    }

    /**
     * 开始抓取这个人的微博信息
     * 各种内容的抓取数量可以在配置文件中配置
     */
    public WeiboInfo crawl() throws InterruptedException {
        weiboInfo = new WeiboInfo();
        weiboInfo.setId(id);

        //分为3各线程个字抓取“关注”，“粉丝”，“微博内容”
        Runnable crawlFollowing = new Runnable() {
            public void run() {
                try {
                    weiboInfo.setFollowingOids(crawlService.crawlFollowingOids());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable crawlFollower = new Runnable() {
            public void run() {
                try {
                    weiboInfo.setFollowerOids(crawlService.crawlFollowerOids());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable crawlBlog = new Runnable() {
            public void run() {
                try {
                    weiboInfo.setBlogs(crawlService.crawlBlog());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.submit(crawlFollowing);
        executorService.submit(crawlFollower);
        executorService.submit(crawlBlog);
        executorService.shutdown();

        //等代所有线程任务结束
        long startTime = System.currentTimeMillis();
        long lastTime = System.currentTimeMillis();

        while (true) {
            long endTime = System.currentTimeMillis();
            //定时输出正在抓取的状态信息
            if (endTime - lastTime >= 5000) {
                System.out.println("已运行" + (endTime - startTime) / 1000 + "秒");
                lastTime = System.currentTimeMillis();
            }

            if (executorService.isTerminated()) {
                break;
            }
            Thread.sleep(200);
        }
        return weiboInfo;
    }

    /**
     * 将内容写入文件
     * 路径采取配置文件中的路径
     */
    public void write() throws IOException {
        if (weiboInfo == null) {
            throw new IllegalStateException("您可能没有先调用crawl()方法");
        }

        String path = CrawlUtils.getWeiboContentInfoPath();
        File pathFile = new File(path);
        //尝试创建文件夹，已经存在时不会创建
        pathFile.mkdirs();

        //XStream类会自动过滤null标签
        XStream xStream = new XStream();
        //重命名根节点
        xStream.alias("info",WeiboInfo.class);
        xStream.alias("blog", Blog.class);
        xStream.alias("comment", Comment.class);
        xStream.alias("id", String.class);

        String result = xStream.toXML(getWeiboInfo());
        BufferedWriter writer = new BufferedWriter(new FileWriter(path + id + ".xml"));
        writer.write(result);
        writer.close();
    }

    /**
     * 返回包含抓取信息的对象
     * @return
     */
    public WeiboInfo getWeiboInfo() {
        if (weiboInfo == null) {
            throw new IllegalStateException("您可能没有先调用crawl()方法");
        }
        return weiboInfo;
    }
}
