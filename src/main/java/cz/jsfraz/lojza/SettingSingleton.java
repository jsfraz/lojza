package cz.jsfraz.lojza;

import java.time.LocalDateTime;
import java.util.Map;

public class SettingSingleton {
    private static SettingSingleton instance;
    private LocalDateTime started;
    private Map<String, Map<String, String>> localization;
    private Map<String, String> languagueNames;
    private CommandSet[] commandSets;
    private String discordToken; // required
    private String mongoUser = "lojza";
    private String mongoPassword; // required
    private String mongoServer; // required
    private int mongoPort = 27017;
    private String mongoDatabase = "lojza";
    private int mongoTimeoutMS = 100;
    private int rssRefreshMinutes = 3600;

    private SettingSingleton() {
    }

    public static SettingSingleton GetInstance() {
        if (instance == null) {
            instance = new SettingSingleton();
        }
        return instance;
    }

    /* Getters */

    public LocalDateTime getStarted() {
        return this.started;
    }

    public Map<String, Map<String, String>> getLocalization() {
        return this.localization;
    }

    public Map<String, String> getLanguagueNames() {
        return this.languagueNames;
    }

    public CommandSet[] getCommandSets() {
        return this.commandSets;
    }

    public String getDiscordToken() {
        return this.discordToken;
    }

    public String getMongoUser() {
        return this.mongoUser;
    }

    public String getMongoPassword() {
        return this.mongoPassword;
    }

    public String getMongoServer() {
        return this.mongoServer;
    }

    public int getMongoPort() {
        return this.mongoPort;
    }

    public String getMongoDatabase() {
        return this.mongoDatabase;
    }

    public int getMongoTimeoutMS() {
        return this.mongoTimeoutMS;
    }

    public int getRssRefreshMinutes() {
        return this.rssRefreshMinutes;
    }

    /* Setters */

    public void setStarted(LocalDateTime started) {
        this.started = started;
    }

    public void setLocalization(Map<String, Map<String, String>> localization) {
        this.localization = localization;
    }

    public void setLanguagueNames(Map<String, String> languages) {
        this.languagueNames = languages;
    }

    public void setCommandSets(CommandSet[] commandCategories) {
        this.commandSets = commandCategories;
    }

    public void setDiscordToken(String token) {
        this.discordToken = token;
    }

    public void setMongoUser(String mongoUser) {
        this.mongoUser = mongoUser;
    }

    public void setMongoPassword(String mongoPassword) {
        this.mongoPassword = mongoPassword;
    }

    public void setMongoServer(String mongoServer) {
        this.mongoServer = mongoServer;
    }

    public void setMongoPort(int mongoPort) {
        this.mongoPort = mongoPort;
    }

    public void setMongoDatabase(String mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    public void setMongoTimeoutMS(int timeout) {
        this.mongoTimeoutMS = timeout;
    }

    public void setRssRefreshMinutes(int minutes) {
        this.rssRefreshMinutes = minutes;
    }
}
