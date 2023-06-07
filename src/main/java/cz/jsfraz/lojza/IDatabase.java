package cz.jsfraz.lojza;

import java.util.Date;
import java.util.List;

import com.mongodb.MongoException;

public interface IDatabase {
    public boolean testConnection();

    public void updateGuildLocale(long guildId, Locale locale) throws MongoException;

    public Locale getGuildLocale(long guildId);

    public void updateRss(long guildId, boolean value);

    public void updateRssChannel(long guildId, long rssChannel);

    public long getRssChannel(long guildId);

    public boolean rssFeedExists(long guildId, String url);

    public void updateRssFeeds(long guildId, String title, String url);

    public List<RssFeed> getRssFeeds(long guildId);

    public void removeRssFeed(long guildId, int index);

    public void clearRssFeeds(long guildId);

    public List<DiscordGuild> getGuildsRss();

    public void updateRssUpdatedDate(long guildId, String url, Date updated);
}
