package cz.jsfraz.lojza;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction;
import net.dv8tion.jda.api.interactions.commands.localization.ResourceBundleLocalizationFunction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

// https://github.com/DV8FromTheWorld/JDA/tree/master/src/examples/java

public class App {
        public static void main(String[] args) {
                // settings singleton
                SettingSingleton settings = SettingSingleton.GetInstance();

                // discord token
                settings.setDiscordToken(System.getenv("DISCORD_TOKEN"));
                // TODO other ENVs to singleton setttings

                // localization function for command descriptions
                final LocalizationFunction localizationFunction = ResourceBundleLocalizationFunction
                                .fromBundles("slashCommands", DiscordLocale.CZECH)
                                .build();
                // slash commands
                settings.setCommandSets(new CommandSet[] {
                                // help commands
                                new CommandSet(CommandCategory.categoryHelp, ":information_source:", new CommandData[] {
                                                // /help command
                                                Commands.slash("help", "Shows available commands for this bot.")
                                                                .setLocalizationFunction(localizationFunction)
                                }),
                                // admin commands
                                new CommandSet(CommandCategory.categoryAdmin, ":wrench:", new CommandData[] {
                                                // /clear command
                                                Commands.slash("delete", "Deletes messages from the text channel.")
                                                                // TODO localize
                                                                .setLocalizationFunction(localizationFunction)
                                                                // subcommands
                                                                .addSubcommands(
                                                                                // delete all messages
                                                                                new SubcommandData("all",
                                                                                                "Deletes all messages from the text channel."),
                                                                                // deletes specific number of messages
                                                                                new SubcommandData("count",
                                                                                                "Deletes specific number of messages from the text channel.")
                                                                                                // number of messages to
                                                                                                // delete
                                                                                                .addOptions(new OptionData(
                                                                                                                OptionType.INTEGER,
                                                                                                                "count",
                                                                                                                "Number of messages to delete.")
                                                                                                                // required
                                                                                                                .setRequired(true)
                                                                                                                // valid
                                                                                                                // range
                                                                                                                .setRequiredRange(
                                                                                                                                2,
                                                                                                                                1000)))
                                                                // guild-only command
                                                                .setGuildOnly(true)
                                                                // admin-only command
                                                                .setDefaultPermissions(DefaultMemberPermissions
                                                                                .enabledFor(Permission.ADMINISTRATOR))
                                })
                });

                // JDA instance
                JDABuilder builder = JDABuilder.createDefault(settings.getDiscordToken());
                JDA jda = builder.addEventListeners(new SlashCommandListener()).build();

                // updating bot commands (might take a few minutes to be applied)
                CommandListUpdateAction commands = jda.updateCommands();
                for (CommandSet commandCategory : settings.getCommandSets()) {
                        commands.addCommands(commandCategory.commands);
                }

                // send the new set of commands to discord
                commands.queue();
        }
}
