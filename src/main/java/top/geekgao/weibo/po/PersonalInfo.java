package top.geekgao.weibo.po;

/**
 * Created by geekgao on 15-11-30.
 * 个人信息类，包含个性信息的各个字段
 */
public class PersonalInfo {
    private String nickname;
    private String certified;
    private String tag;
    private String sex;
    private String locale;
    private String describe;
    private String school;
    private String level;
    private String time;


    @Override
    public String toString() {
        return "PersonalInfo{" +
                "nickname='" + nickname + '\'' +
                ", certified='" + certified + '\'' +
                ", tag='" + tag + '\'' +
                ", sex='" + sex + '\'' +
                ", locale='" + locale + '\'' +
                ", describe='" + describe + '\'' +
                ", school='" + school + '\'' +
                ", level='" + level + '\'' +
                ", time='" + time + '\'' +
                '}';
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCertified() {
        return certified;
    }

    public void setCertified(String certified) {
        this.certified = certified;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
