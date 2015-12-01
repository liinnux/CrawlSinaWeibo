package top.geekgao.weibo.service;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import top.geekgao.weibo.po.PersonalInfo;
import top.geekgao.weibo.utils.CrawlUtils;

import java.io.IOException;

/**
 * Created by geekgao on 15-11-30.
 */
public class CrawlPersonalInfoService {
    //待抓取的链接
    private String url;
    //包含博主信息的json串
    private String json;
    //保存博主信息的类
    private PersonalInfo personalInfo;

    public CrawlPersonalInfoService(String url) {
        this.url = url;
    }

    /**
     * 执行后立即开始抓取用户信息
     */
    public void crawl() throws IOException {
        personalInfo = new PersonalInfo();
        json = CrawlUtils.getHtml(url);
        analysis();
    }

    /**
     * 将json信息解析到PersonalInfo类的实例中
     */
    private void analysis() {
        JSONObject rootJson = new JSONObject(json);

        //获取id
        JSONObject cardlistInfo = rootJson.getJSONObject("cardlistInfo");
        String id = cardlistInfo.getString("containerid").substring(6,16);
        setInfo("id",id);

        //获取除id外的其他信息
        for (Object card:rootJson.getJSONArray("cards")) {
            try {
                for (Object group:((JSONObject) card).getJSONArray("card_group")) {
                    String item_name;
                    try {
                        item_name = ((JSONObject) group).getString("item_name");
                    } catch (org.json.JSONException e) {
                        //微博认证那块没有item_name这个条目,有的是item_type条目
                        item_name = ((JSONObject) group).getString("item_type");
                    }
                    String item_content = ((JSONObject) group).getString("item_content");

                    setInfo(item_name,item_content);

                    //这个是个人信息的最后一条,后面的多数是空的,不重要的
                    if (item_name.equals("注册时间")) {
                        return;
                    }
                }
            } catch (org.json.JSONException ignored) {
            }
        }
    }

    /**
     * 将博主信息放置到PersonalInfo类的实例中
     * @param item_name
     * @param item_content
     */
    private void setInfo(String item_name, String item_content) {
        if (item_name.equals("id")) {
            personalInfo.setId(item_content);
        } else if (item_name.equals("昵称")) {
            personalInfo.setNickname(item_content);
        } else if (item_name.equals("性别")) {
            personalInfo.setSex(item_content);
        } else if (item_name.equals("所在地")) {
            personalInfo.setLocale(item_content);
        } else if (item_name.equals("简介")) {
            personalInfo.setDescribe(item_content);
        } else if (item_name.equals("学校")) {
            personalInfo.setSchool(item_content);
        } else if (item_name.equals("等级")) {
            personalInfo.setLevel(item_content);
        } else if (item_name.equals("注册时间")) {
            personalInfo.setTime(item_content);
        } else if (item_name.equals("标签")) {
            personalInfo.setTag(item_content);
        } else if (item_name.startsWith("verify_")) {
            personalInfo.setCertified(item_content);
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getJson() {
        return json;
    }

    public PersonalInfo getPersonalInfo() {
        return personalInfo;
    }
}
