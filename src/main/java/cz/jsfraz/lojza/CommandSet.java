package cz.jsfraz.lojza;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class CommandSet {
    private CommandCategory category;
    private String displayEmoji;
    public CommandData[] commands;

    public CommandSet(CommandCategory name, String emoji, CommandData[] commands) {
        this.category = name;
        this.displayEmoji = emoji;
        this.commands = commands;
    }

    public CommandCategory getCategory() {
        return this.category;
    }

    public String getDisplayEmoji() {
        return this.displayEmoji;
    }
}
