package cz.jsfraz.lojza;

public interface IDatabase {
    public void testConnection() throws Exception;

    public void updateGuildLocale(long guildId, Locale locale);

    public Locale getGuildLocale(long guildId);
}
