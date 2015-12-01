package top.geekgao.weibo.test;

import top.geekgao.weibo.service.CrawlWeiboInfoService;

import java.io.IOException;
import java.util.List;

/**
 * Created by geekgao on 15-11-30.
 */
public class Main {
    public static void main(String[] args) {

        try {
            CrawlWeiboInfoService service = new CrawlWeiboInfoService("3217179555");
            List<String> result = service.getFowardingOids("3915361818719126");
            System.out.println(result.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
