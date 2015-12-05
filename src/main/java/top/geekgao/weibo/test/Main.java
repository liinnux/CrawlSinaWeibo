package top.geekgao.weibo.test;

import top.geekgao.weibo.crawl.CrawlPersonalInfo;
import top.geekgao.weibo.crawl.CrawlWeiboInfo;

import java.io.IOException;

/**
 * Created by geekgao on 15-11-30.
 */
public class Main {
    public static void main(String[] args) {
        //-i代表抓取个人信息
        //-c代表抓取博文信息
        String mode = args[0];
        String id = args[1];

        try {
            if (mode.equals("-i")) {
                CrawlPersonalInfo crawlPersonalInfo = new CrawlPersonalInfo(id);
                crawlPersonalInfo.crawl();
                crawlPersonalInfo.write();
            } else if (mode.equals("-c")) {
                CrawlWeiboInfo crawlWeiboInfo = new CrawlWeiboInfo(id);
                crawlWeiboInfo.crawl();
                crawlWeiboInfo.write();
            } else {
                System.out.println("运行格式：java -jar CrawlWeibo.jar [-i/-c] [id]");
                System.out.println("\t-i代表抓取个人信息");
                System.out.println("\t-c代表抓取博文信息");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
