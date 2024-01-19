package cz.jsfraz.lojza.database.models;

import java.util.ArrayList;
import java.util.List;

// http://mongodb.github.io/mongo-java-driver/3.9/bson/pojos/

public class DiscordGuild {
    private long guildId;
    private Locale locale;
    private boolean rss;
    private long rssChannelId;
    private List<RssFeed> rssFeeds;
    private String minecraftServerAddress;
    private long minecraftWhitelistChannelId;
    private long minecraftWhitelistedRoleId;
    private boolean minecraft;
    private String minecraftRconPassword;
    private long verificationRoleId;
    private long verificationChannelId;
    private boolean verification;

    public DiscordGuild() {
    }

    public DiscordGuild(long guildId, Locale locale) {
        this.guildId = guildId;
        this.locale = locale;
        this.rss = false;
        this.rssChannelId = 0;
        this.rssFeeds = new ArrayList<RssFeed>();
        this.minecraftServerAddress = "";
        this.minecraftWhitelistChannelId = 0;
        this.minecraftWhitelistedRoleId = 0;
        this.minecraft = false;
        this.minecraftRconPassword = "";
        this.verificationRoleId = 0;
        this.verificationChannelId = 0;
        this.verification = false;
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

    public String getMinecraftServerAddress() {
        return this.minecraftServerAddress;
    }

    public void setMinecraftServerAddress(String address) {
        this.minecraftServerAddress = address;
    }

    public long getMinecraftWhitelistChannelId() {
        return this.minecraftWhitelistChannelId;
    }

    public void setMinecraftWhitelistChannelId(long id) {
        this.minecraftWhitelistChannelId = id;
    }

    public long getMinecraftWhitelistedRoleId() {
        return this.minecraftWhitelistedRoleId;
    }

    public void setMinecraftWhitelistedRoleId(long id) {
        this.minecraftWhitelistedRoleId = id;
    }

    public boolean getMinecraft() {
        return this.minecraft;
    }

    public void setMinecraft(boolean minecraft) {
        this.minecraft = minecraft;
    }

    public String getMinecraftRconPassword() {
        return this.minecraftRconPassword;
    }

    public void setMinecraftRconPassword(String password) {
        this.minecraftRconPassword = password;
    }

    public long getVerificationRoleId() {
        return this.verificationRoleId;
    }

    public void setVerificationRoleId(long id) {
        this.verificationRoleId = id;
    }

    public long getVerificationChannelId() {
        return this.verificationChannelId;
    }

    public void setVerificationChannelId(long id) {
        this.verificationChannelId = id;
    }

    public boolean getVerification() {
        return this.verification;
    }

    public void setVerification(boolean value) {
        this.verification = value;
    }
}