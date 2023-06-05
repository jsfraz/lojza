package cz.jsfraz.lojza;

import java.util.ArrayList;
import java.util.List;

// http://mongodb.github.io/mongo-java-driver/3.9/bson/pojos/

public class DiscordGuild {
    private long guildId;
    private Locale locale;
    private boolean rss;
    private long rssChannel;
    private List<RssFeed> rssFeeds;

    private static final long defaultRssChannel = 0;

    public DiscordGuild() {
    }

    public DiscordGuild(long guildId, Locale locale) {
        this.guildId = guildId;
        this.locale = locale;
        this.rss = true;
        this.rssChannel = defaultRssChannel;
        this.rssFeeds = new ArrayList<RssFeed>();
    }

    /* Getters */

    public long getGuildId() {
        return this.guildId;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public boolean getRss() {
        return this.rss;
    }

    public long getRssChannel() {
        return this.rssChannel;
    }

    public List<RssFeed> getRssFeeds() {
        return this.rssFeeds;
    }
    
    /* Static methods */

    public static long getDefaultRssChannel() {
        return defaultRssChannel;
    }

    /* Setters */

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setRss(boolean enable) {
        this.rss = enable;
    }

    public void setRssChannel(long rssChannel) {
        this.rssChannel = rssChannel;
    }

    public void setRssFeeds(List<RssFeed> rssFeeds) {
        this.rssFeeds = rssFeeds;
    }
}
