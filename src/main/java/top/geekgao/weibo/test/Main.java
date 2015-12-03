package top.geekgao.weibo.test;

import top.geekgao.weibo.crawl.CrawlPersonalInfo;
import top.geekgao.weibo.crawl.CrawlWeiboInfo;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by geekgao on 15-11-30.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String id = scanner.next();

        try {
            CrawlWeiboInfo crawlWeiboInfo = new CrawlWeiboInfo(id);
            crawlWeiboInfo.crawl();
            crawlWeiboInfo.write();

            CrawlPersonalInfo crawlPersonalInfo = new CrawlPersonalInfo(id);
            crawlPersonalInfo.crawl();
            crawlPersonalInfo.write();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
