package cz.jsfraz.lojza;

public class RssFeed {
    private String title;
    private String url;

    public RssFeed() {
    }

    public RssFeed(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return this.title;
    }

    public String getUrl() {
        return this.url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
