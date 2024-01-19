package cz.jsfraz.lojza.database;

import java.util.Date;
import java.util.List;

import cz.jsfraz.lojza.database.models.DiscordGuild;
import cz.jsfraz.lojza.database.models.Locale;
import cz.jsfraz.lojza.database.models.RssFeed;

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

    public String getMinecraftServerAddressById(long guildId);

    public void updateMinecraftServerAddressById(long guildId, String address);

    public long getMinecraftChannelById(long guildId);

    public void updateMinecraftChannelById(long guildId, long channelId);

    public long getMinecraftRoleById(long guildId);

    public void updateMinecraftRoleById(long guildId, long roleId);

    public void updateMinecraftById(long guildId, boolean value);

    public DiscordGuild getDiscordGuildWithMinecraftInfo(long guildId);

    public void updateMinecraftRconPasswordById(long guildId, String password);

    public long getVerificationRoleById(long guildId);

    public void updateVerificationRoleById(long guildId, long roleId);

    public long getVerificationChannelById(long guildId);

    public void updateVerificationChannelById(long guildId, long channelId);

    public void updateVerificationById(long guildId, boolean value);

    public DiscordGuild getDiscordGuildWithVerificationInfo(long guildId);
}
