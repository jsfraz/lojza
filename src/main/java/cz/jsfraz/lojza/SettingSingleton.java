package cz.jsfraz.lojza;

public class SettingSingleton {
    private static SettingSingleton instance;
    private CommandSet[] commandSets;
    private String discordToken;

    private SettingSingleton() {}

    public static SettingSingleton GetInstance() {
        if (instance == null) {
            instance = new SettingSingleton();
        }
        return instance;
    }

    /* Getters */

    public CommandSet[] getCommandSets() {
        return this.commandSets;
    }

    public String getDiscordToken() {
        return this.discordToken;
    }

    /* Setters */

    public void setCommandSets(CommandSet[] commandCategories) {
        this.commandSets = commandCategories;
    }

    public void setDiscordToken(String token)
    {
        this.discordToken = token;
    }
}
