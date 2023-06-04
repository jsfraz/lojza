package cz.jsfraz.lojza;

import com.mongodb.MongoException;

public interface IDatabase {
    public boolean testConnection();

    public void updateGuildLocale(long guildId, Locale locale) throws MongoException;

    public Locale getGuildLocale(long guildId);

    public void updateRssChannel(long guildId, long rssChannel);

    public long getRssChannel(long guildId);

    public boolean rssFeedExists(long guildId, String url);

    public void updateRssFeeds(long guildId, String url);
}
