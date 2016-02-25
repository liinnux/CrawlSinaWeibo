package top.geekgao.weibo.test;

import top.geekgao.weibo.crawl.CrawlPersonalInfo;
import top.geekgao.weibo.crawl.CrawlWeiboInfo;
import top.geekgao.weibo.exception.StatusErrorException;

import java.io.IOException;

/**
 * Created by geekgao on 15-11-30.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("运行格式：java -jar CrawlWeibo.jar [-i/-c] [id]");
            System.out.println("\t-i代表抓取个人信息");
            System.out.println("\t-c代表抓取博文信息");
            return;
        }
        //-i代表抓取个人信息
        //-c代表抓取博文信息
        String mode = args[0];
        String id = args[1];

        if (mode.equals("-i")) {
            CrawlPersonalInfo crawlPersonalInfo = null;
            try {
                crawlPersonalInfo = new CrawlPersonalInfo(id);
                crawlPersonalInfo.crawl();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (StatusErrorException e) {
                e.printStackTrace();
            }

        } else if (mode.equals("-c")) {
            CrawlWeiboInfo crawlWeiboInfo = new CrawlWeiboInfo(id);
            try {
                crawlWeiboInfo.crawl();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("运行格式：java -jar CrawlWeibo.jar [-i/-c] [id]");
            System.out.println("\t-i代表抓取个人信息");
            System.out.println("\t-c代表抓取博文信息");
            }
    }
}
