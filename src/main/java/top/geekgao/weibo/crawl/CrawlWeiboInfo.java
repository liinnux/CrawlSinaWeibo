package top.geekgao.weibo.crawl;

import com.thoughtworks.xstream.XStream;
import top.geekgao.weibo.exception.StatusErrorException;
import top.geekgao.weibo.po.Blog;
import top.geekgao.weibo.po.Comment;
import top.geekgao.weibo.po.WeiboInfo;
import top.geekgao.weibo.service.CrawlWeiboInfoService;
import top.geekgao.weibo.utils.CrawlUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by geekgao on 15-11-30.
 * 抓取博主的博文相关信息
 * 开放三个共有接口crawl(),write(),getWeiboInfo()
 */
public class CrawlWeiboInfo {
    //用户昵称
    private String id;
    //用户真实的id
    private String oid;
    //包含微博信息的对象
    private WeiboInfo weiboInfo;
    //抓取信息的服务类
    private CrawlWeiboInfoService crawlService;
    //出现错误后对写入操作进行加锁
    private final Object lock = new Object();
    //标记是否已经将结果写入过
    private boolean isWrited = false;

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
        //打印即将发送的请求次数
        int count = 0;
        count += CrawlUtils.getPageCount();
        int blogCount = CrawlUtils.getPageCount() * CrawlUtils.getBlogCount();
        count += blogCount * ((CrawlUtils.getLikeCount() + CrawlUtils.getForwardingCount() + CrawlUtils.getCommentCount()) / 200);
        //获取粉丝和关注各一次
        count += 2;
        System.out.println("【即将抓取[" + id + "]的微博信息，共需抓取[" + blogCount + "]条微博，至少需要发送[" + count + "]次请求】");
        if (count > 1000) {
            System.err.println("每次发送请求次数不能超过1000次，否则可能会被禁止访问导致抓取失败，请修改.");
            return null;
        }

        weiboInfo = new WeiboInfo();
        weiboInfo.setId(id);

        //三条线程，分别执行“抓取博主关注信息”，“抓取博主粉丝信息”，“抓取博主微博信息”
        final ExecutorService executorService = Executors.newFixedThreadPool(3);

        //分为3各线程个字抓取“关注”，“粉丝”，“微博内容”
        Runnable crawlFollowing = new Runnable() {
            public void run() {
                try {
                    weiboInfo.setFollowingOids(crawlService.crawlFollowingIds());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (StatusErrorException e) {
                    System.err.println("【关注信息抓取失败.】");
                    //因为有三条线程，所以必须加锁，防止重复将线程池关闭
                    synchronized (lock) {
                        if (!isWrited) {
                            System.err.println(e.getMessage());
                            executorService.shutdownNow();
                            try {
                                write();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable crawlFollower = new Runnable() {
            public void run() {
                try {
                    weiboInfo.setFollowerOids(crawlService.crawlFollowerIds());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (StatusErrorException e) {
                    System.err.println("【粉丝信息抓取失败.】");
                    synchronized (lock) {
                        if (!isWrited) {
                            System.err.println(e.getMessage());
                            executorService.shutdownNow();
                            try {
                                write();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
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
                } catch (StatusErrorException e) {
                    System.err.println("【博文信息抓取失败.】");
                    synchronized (lock) {
                        if (!isWrited) {
                            System.err.println(e.getMessage());
                            executorService.shutdownNow();
                            try {
                                write();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        executorService.execute(crawlFollowing);
        executorService.execute(crawlFollower);
        executorService.execute(crawlBlog);
        executorService.shutdown();

        //等代所有线程任务结束
        long startTime = System.currentTimeMillis();
        long lastTime = System.currentTimeMillis();

        //线程池没有停止就一直计时
        while (!executorService.isTerminated()) {
            long endTime = System.currentTimeMillis();
            //定时输出正在抓取的状态信息
            if (endTime - lastTime >= 5000) {
                System.out.println("已运行" + (endTime - startTime) / 1000 + "秒");
                lastTime = System.currentTimeMillis();
            }

            Thread.sleep(200);
        }

        try {
            if (!isWrited) {
                write();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return weiboInfo;
    }

    /**
     * 将内容写入文件
     * 路径采取配置文件中的路径
     */
    private void write() throws IOException {
        System.out.println("【信息正在写入文件.】");
        isWrited = true;
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
        BufferedWriter writer = new BufferedWriter(new FileWriter(path + id + System.currentTimeMillis() + ".xml"));
        writer.write(result);
        writer.close();
        System.out.println("【信息写入文件完毕.】");
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
