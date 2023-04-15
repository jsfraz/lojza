package cz.jsfraz.lojza;

import java.util.ArrayList;
import java.util.List;

// http://mongodb.github.io/mongo-java-driver/3.9/bson/pojos/

public class DiscordGuild {
    public long guildId;
    public Locale locale;
    public List<String> rssFeeds;
    public long rssChannel;

    public DiscordGuild() {
    }

    public DiscordGuild(long guildId, Locale locale) {
        this.guildId = guildId;
        this.locale = locale;
        this.rssFeeds = new ArrayList<String>();
        this.rssChannel = 0;
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

    /* Setters */

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void setRssChannel(long rssChannel) {
        this.rssChannel = rssChannel;
    }
}
