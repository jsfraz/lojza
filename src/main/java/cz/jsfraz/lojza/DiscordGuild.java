package cz.jsfraz.lojza;

import java.util.ArrayList;
import java.util.List;

// http://mongodb.github.io/mongo-java-driver/3.9/bson/pojos/

public class DiscordGuild {
    private long guildId;
    private Locale locale;
    private boolean rss;
    private long rssChannelId;
    private List<RssFeed> rssFeeds;

    public DiscordGuild() {
    }

    public DiscordGuild(long guildId, Locale locale) {
        this.guildId = guildId;
        this.locale = locale;
        this.rss = true;
        this.rssChannelId = 0;
        this.rssFeeds = new ArrayList<RssFeed>();
    }

    public long getGuildId() {
        return this.guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public boolean getRss() {
        return this.rss;
    }

    public void setRss(boolean rss) {
        this.rss = rss;
    }

    public long getRssChannelId() {
        return this.rssChannelId;
    }

    public void setRssChannelId(long rssChannelId) {
        this.rssChannelId = rssChannelId;
    }

    public List<RssFeed> getRssFeeds() {
        return this.rssFeeds;
    }

    public void setRssFeeds(List<RssFeed> rssFeeds) {
        this.rssFeeds = rssFeeds;
    }    
}