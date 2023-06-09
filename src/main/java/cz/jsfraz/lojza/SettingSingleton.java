package cz.jsfraz.lojza;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;

import net.dv8tion.jda.api.JDA;

public class SettingSingleton {
    private static SettingSingleton instance;
    private Properties properties;      // https://stackoverflow.com/a/26573884/19371130
    private LocalDateTime started;
    private Map<String, Map<String, String>> localization;
    private Map<String, String> languagueNames;
    private Locale defaultLocale = Locale.en;
    private CommandSet[] commandSets;
    private String discordToken; // required
    private JDA jdaInstance;
    private String mongoUser = "lojza";
    private String mongoPassword; // required
    private String mongoServer; // required
    private int mongoPort = 27017;
    private String mongoDatabase = "lojza";
    private int mongoTimeoutMS = 100;
    private int rssRefreshMinutes = 60;
    private int rssMaxFeedCount = 5;
    private String urlRemovedText = "[URL REMOVED]";

    private SettingSingleton() {
    }

    public static SettingSingleton GetInstance() {
        if (instance == null) {
            instance = new SettingSingleton();
        }
        return instance;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public LocalDateTime getStarted() {
        return this.started;
    }

    public void setStarted(LocalDateTime started) {
        this.started = started;
    }

    public Map<String,Map<String,String>> getLocalization() {
        return this.localization;
    }

    public void setLocalization(Map<String,Map<String,String>> localization) {
        this.localization = localization;
    }

    public Map<String,String> getLanguagueNames() {
        return this.languagueNames;
    }

    public void setLanguagueNames(Map<String,String> languagueNames) {
        this.languagueNames = languagueNames;
    }

    public Locale getDefaultLocale() {
        return this.defaultLocale;
    }

    public CommandSet[] getCommandSets() {
        return this.commandSets;
    }

    public void setCommandSets(CommandSet[] commandSets) {
        this.commandSets = commandSets;
    }

    public String getDiscordToken() {
        return this.discordToken;
    }

    public void setDiscordToken(String discordToken) {
        this.discordToken = discordToken;
    }

    public JDA getJdaInstance() {
        return this.jdaInstance;
    }

    public void setJdaInstance(JDA jdaInstance) {
        this.jdaInstance = jdaInstance;
    }

    public String getMongoUser() {
        return this.mongoUser;
    }

    public void setMongoUser(String mongoUser) {
        this.mongoUser = mongoUser;
    }

    public String getMongoPassword() {
        return this.mongoPassword;
    }

    public void setMongoPassword(String mongoPassword) {
        this.mongoPassword = mongoPassword;
    }

    public String getMongoServer() {
        return this.mongoServer;
    }

    public void setMongoServer(String mongoServer) {
        this.mongoServer = mongoServer;
    }

    public int getMongoPort() {
        return this.mongoPort;
    }

    public void setMongoPort(int mongoPort) {
        this.mongoPort = mongoPort;
    }

    public String getMongoDatabase() {
        return this.mongoDatabase;
    }

    public void setMongoDatabase(String mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    public int getMongoTimeoutMS() {
        return this.mongoTimeoutMS;
    }

    public void setMongoTimeoutMS(int mongoTimeoutMS) {
        this.mongoTimeoutMS = mongoTimeoutMS;
    }

    public int getRssRefreshMinutes() {
        return this.rssRefreshMinutes;
    }

    public void setRssRefreshMinutes(int rssRefreshMinutes) {
        this.rssRefreshMinutes = rssRefreshMinutes;
    }

    public int getRssMaxFeedCount() {
        return this.rssMaxFeedCount;
    }

    public String getUrlRemovedText() {
        return this.urlRemovedText;
    }
}
