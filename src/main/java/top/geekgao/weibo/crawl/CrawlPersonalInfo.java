package top.geekgao.weibo.crawl;

import com.thoughtworks.xstream.XStream;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import top.geekgao.weibo.po.PersonalInfo;
import top.geekgao.weibo.service.CrawlPersonalInfoService;
import top.geekgao.weibo.utils.CrawlUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by geekgao on 15-11-30.
 * 抓取博主信息
 * 这个类提供三个公共方法
 * 开始抓取(crawl())和返回抓取到的信息(getPersonalInfo())，还有写入文件(write())
 */
public class CrawlPersonalInfo {

    //网页url上面显示的用户的id
    private String id;
    //用户真实的id
    private String oid;

    private CrawlPersonalInfoService crawlService;

    public CrawlPersonalInfo(String id) throws IOException {
        this.id = id;
        setOid();
        crawlService = new CrawlPersonalInfoService("http://api.weibo.cn/2/cardlist?uicode=10000011&featurecode=10000001&lcardid=more_web&c=android&i=faf3db9&s=ec4938f8&ua=Meizu-MX4%20Pro__weibo__5.6.0__android__android5.0.1&wm=9848_0009&aid=01AlUdIfLWEqtqXPlIra_FKzZHJbhiihd9QgLIth8-uol6qkE.&fid=230283" + oid + "_-_INFO&uid=1769127312&v_f=2&v_p=25&from=1056095010&gsid=_2A257XvBeDeRxGedJ7VsQ8inPyj6IHXVWSgSWrDV6PUJbrdANLVf2kWqXUOhtuyQouzVv8ATwTRBWwvO4hQ..&imsi=460017076390273&lang=zh_CN&lfid=230283" + oid + "&skin=default&count=20&oldwm=19005_0019&containerid=230283" + oid + "_-_INFO&luicode=10000198&need_head_cards=1&sflag=1");
    }

    /**
     * 调用后开始抓取
     */
    public void crawl() {
        crawlService.crawl();

    }

    /**
     *
     * @return 得到代表博主信息的类
     */
    public PersonalInfo getPersonalInfo() {
        return crawlService.getPersonalInfo();
    }

    /**
     * 默认冲配置文件读取写入的路径然后写入
     */
    public void write() throws IOException {
        if (crawlService == null) {
            return;
        }

        String path = CrawlUtils.getPersonalInfoPath();
        File pathFile = new File(path);
        //尝试创建文件夹，已经存在时不会创建
        pathFile.mkdirs();

        //XStream类会自动过滤null标签
        XStream xStream = new XStream();
        //重命名根节点
        xStream.alias("info",PersonalInfo.class);
        String result = xStream.toXML(getPersonalInfo());

        BufferedWriter writer = new BufferedWriter(new FileWriter(path + id + ".xml"));
        writer.write(result);
        writer.close();
    }

    private void setOid() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet getOid = new HttpGet("http://weibo.cn/" + id);
        try {
            CloseableHttpResponse response = client.execute(getOid);
            String html = EntityUtils.toString(response.getEntity());
            oid = html.split("uid=")[1].split("&")[0];
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.close();
    }

    public void setId(String id) {
        this.id = id;
    }
}
