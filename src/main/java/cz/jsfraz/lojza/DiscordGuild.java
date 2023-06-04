package cz.jsfraz.lojza;

import java.util.ArrayList;
import java.util.List;

// http://mongodb.github.io/mongo-java-driver/3.9/bson/pojos/

public class DiscordGuild {
    private long guildId;
    private Locale locale;
    // TODO enabling/disabling RSS
    private long rssChannel;
    private List<String> rssFeeds;

    public DiscordGuild() {
    }

    public DiscordGuild(long guildId, Locale locale) {
        this.guildId = guildId;
        this.locale = locale;
        this.rssChannel = 0;
        this.rssFeeds = new ArrayList<String>();
    }

    /* Getters */

    public long getGuildId() {
        return this.guildId;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public long getRssChannel() {
        return this.rssChannel;
    }

    public List<String> getRssFeeds() {
        return this.rssFeeds;
    }

    /* Setters */

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setRssChannel(long rssChannel) {
        this.rssChannel = rssChannel;
    }

    public void setRssFeeds(List<String> rssFeeds) {
        this.rssFeeds = rssFeeds;
    }
}
