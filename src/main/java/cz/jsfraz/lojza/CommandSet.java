package cz.jsfraz.lojza;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class CommandSet {
    private CommandCategory category;
    private String displayEmoji;
    public CommandData[] commands;

    public CommandSet(CommandCategory category, String displayEmoji, CommandData[] commands) {
        this.category = category;
        this.displayEmoji = displayEmoji;
        this.commands = commands;
    }

    public CommandCategory getCategory() {
        return this.category;
    }

    public String getDisplayEmoji() {
        return this.displayEmoji;
    }
}
