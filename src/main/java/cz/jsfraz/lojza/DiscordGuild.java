package cz.jsfraz.lojza;

// http://mongodb.github.io/mongo-java-driver/3.9/bson/pojos/

public class DiscordGuild {
    public long guildId;
    public Locale locale;

    public DiscordGuild() {
    }

    public DiscordGuild(long guildId, Locale locale) {
        this.guildId = guildId;
        this.locale = locale;
    }

    /* Getters */

    public long getGuildId() {
        return this.guildId;
    }

    public Locale getLocale() {
        return this.locale;
    }

    /* Setters */

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
