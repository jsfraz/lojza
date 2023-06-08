package cz.jsfraz.lojza;

import java.util.Date;

public class RssFeed {
    private String title;
    private String url;
    private Date updated;

    public RssFeed() {
    }

    public RssFeed(String title, String url) {
        this.title = title;
        this.url = url;
        this.updated = new Date(
                System.currentTimeMillis() - SettingSingleton.GetInstance().getRssRefreshMinutes() * 60 * 1000);
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getUpdated() {
        return this.updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
