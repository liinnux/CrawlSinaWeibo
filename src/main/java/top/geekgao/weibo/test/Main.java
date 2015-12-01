package top.geekgao.weibo.test;

import top.geekgao.weibo.crawl.CrawlPersonalInfo;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by geekgao on 15-11-30.
 */
public class Main {
    public static void main(String[] args) {
        String id;
        Scanner scanner = new Scanner(System.in);
        id = scanner.next();

        try {
            CrawlPersonalInfo crawl = new CrawlPersonalInfo(id);
            crawl.crawl();
            crawl.write();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
