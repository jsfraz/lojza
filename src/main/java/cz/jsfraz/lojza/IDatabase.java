package cz.jsfraz.lojza;

import java.util.Date;
import java.util.List;

public interface IDatabase {
    public boolean testConnection();

    public List<Long> getAllGuildIds();

    public DiscordGuild getGuildById(long guildId);

    public void insertGuild(DiscordGuild guild);

    public void deleteGuildById(long guildId);

    public void updateGuildLocaleById(long guildId, Locale locale) ;

    public Locale getGuildLocaleById(long guildId);

    public void updateRssById(long guildId, boolean value);

    public void updateRssChannelById(long guildId, long rssChannel);

    public long getRssChannelById(long guildId);

    public boolean rssFeedExistsById(long guildId, String url);

    public void addGuildRssFeedById(long guildId, RssFeed rssFeed);

    public List<RssFeed> getRssFeedsById(long guildId);

    public void removeRssFeedById(long guildId, int index);

    public void clearRssFeedsById(long guildId);

    public List<DiscordGuild> getGuildsRssBy();

    public void updateRssUpdatedDateById(long guildId, String url, Date updated);
}
