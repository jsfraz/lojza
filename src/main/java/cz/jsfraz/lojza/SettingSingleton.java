package cz.jsfraz.lojza;

import java.time.LocalDateTime;
import java.util.Map;

public class SettingSingleton {
    private static SettingSingleton instance;
    private LocalDateTime started;
    private Map<String, Map<String, String>> localization;
    private CommandSet[] commandSets;
    private String discordToken;

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

    public CommandSet[] getCommandSets() {
        return this.commandSets;
    }

    public String getDiscordToken() {
        return this.discordToken;
    }

    /* Setters */

    public void setStarted(LocalDateTime started) {
        this.started = started;
    }

    public void setLocalization(Map<String, Map<String, String>> localization) {
        this.localization = localization;
    }

    public void setCommandSets(CommandSet[] commandCategories) {
        this.commandSets = commandCategories;
    }

    public void setDiscordToken(String token) {
        this.discordToken = token;
    }
}
