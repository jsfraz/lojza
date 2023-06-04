package cz.jsfraz.lojza;

import java.io.IOException;
import java.time.LocalDateTime;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.commands.Command;
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
                LocalDateTime started = LocalDateTime.now();

                // settings singleton
                SettingSingleton settings = SettingSingleton.GetInstance();
                // start time
                settings.setStarted(started);
                // text localization
                try {
                        settings.setLocalization(Tools.getLocalization());
                        settings.setLanguagueNames(Tools.getLanguagueNames());
                } catch (IOException e) {
                        e.printStackTrace();
                }
                // environment variables
                if (System.getenv("DEFAULT_LOCALE") != null) {
                        settings.setDefaultLocale(Locale.valueOf(System.getenv("DEFAULT_LOCALE")));
                }
                settings.setDiscordToken(System.getenv("DISCORD_TOKEN"));
                if (System.getenv("MONGO_USER") != null) {
                        settings.setMongoUser(System.getenv("MONGO_USER"));
                }
                settings.setMongoPassword(System.getenv("MONGO_PASSWORD"));
                settings.setMongoServer(System.getenv("MONGO_SERVER"));
                if (System.getenv("MONGO_PORT") != null) {
                        settings.setMongoPort(Integer.parseInt(System.getenv("MONGO_PORT")));
                }
                if (System.getenv("MONGO_DATABASE") != null) {
                        settings.setMongoDatabase(System.getenv("MONGO_DATABASE"));
                }
                if (System.getenv("MONGO_TIMEOUT") != null) {
                        settings.setMongoTimeoutMS(Integer.parseInt(System.getenv("MONGO_TIMEOUT")));
                }
                if (System.getenv("RSS_REFRESH") != null) {
                        settings.setRssRefreshMinutes(Integer.parseInt(System.getenv("RSS_REFRESH")));
                }

                // localization function for command descriptions
                final LocalizationFunction localizationFunction = ResourceBundleLocalizationFunction
                                .fromBundles("commandLocalization/slashCommands", DiscordLocale.CZECH)
                                .build();

                // FIXME subcommand option localization doesnt work
                // slash commands
                settings.setCommandSets(new CommandSet[] {
                                // help commands
                                new CommandSet(CommandCategory.categoryHelp, ":information_source:", new CommandData[] {
                                                // help command
                                                Commands.slash("help", "Shows available commands for this bot.")
                                                                .setLocalizationFunction(localizationFunction)
                                }),
                                // admin commands
                                new CommandSet(CommandCategory.categoryAdmin, ":shield:", new CommandData[] {
                                                // delete command
                                                Commands.slash("delete", "Deletes messages from the text channel.")
                                                                .setLocalizationFunction(localizationFunction)
                                                                // subcommands
                                                                .addSubcommands(
                                                                                // delete all messages
                                                                                new SubcommandData("all",
                                                                                                "Deletes all messages from the text channel.")
                                                                                                .addOptions(
                                                                                                                // whether
                                                                                                                // or
                                                                                                                // not
                                                                                                                // to
                                                                                                                // display
                                                                                                                // prompt
                                                                                                                new OptionData(
                                                                                                                                OptionType.STRING,
                                                                                                                                "force",
                                                                                                                                "Deletes messages without showing warning.")
                                                                                                                                .addChoices(new Command.Choice(
                                                                                                                                                "true",
                                                                                                                                                "true"),
                                                                                                                                                new Command.Choice(
                                                                                                                                                                "false",
                                                                                                                                                                "false"))),
                                                                                // deletes specific number of messages
                                                                                new SubcommandData("count",
                                                                                                "Deletes specific number of messages from the text channel.")
                                                                                                .addOptions(
                                                                                                                // messages
                                                                                                                // to
                                                                                                                // delete
                                                                                                                new OptionData(
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
                                                                                .enabledFor(Permission.ADMINISTRATOR)),
                                                // setup command
                                                Commands.slash("setup", "Setup Lojza for your server.")
                                                                .setLocalizationFunction(localizationFunction)
                                                                // guild-only command
                                                                .setGuildOnly(true)
                                                                // admin-only command
                                                                .setDefaultPermissions(DefaultMemberPermissions
                                                                                .enabledFor(Permission.ADMINISTRATOR)),
                                                // rss commands
                                                Commands.slash("rss", "Manage RSS channels.")
                                                                .setLocalizationFunction(localizationFunction)
                                                                .addSubcommands(
                                                                                // sets channel to send annoucements
                                                                                new SubcommandData("channel",
                                                                                                "Sets current text channel as channel to send RSS annoucements to."),
                                                                                // add RSS channel
                                                                                new SubcommandData("add",
                                                                                                "Adds new RSS channel.")
                                                                                                .addOptions(new OptionData(
                                                                                                                OptionType.STRING,
                                                                                                                "url",
                                                                                                                "URL of RSS channel.")
                                                                                                                .setRequired(true)),
                                                                                // get rss channel and list of urls
                                                                                new SubcommandData("list",
                                                                                                "Shows text channel for sending annoucements and list of RSS channels.")

                                                                )

                                                                // guild-only command
                                                                .setGuildOnly(true)
                                                                // admin-only command
                                                                .setDefaultPermissions(DefaultMemberPermissions
                                                                                .enabledFor(Permission.ADMINISTRATOR))
                                }),
                                // fun commands (it's difficult, because fun is subjective thing...)
                                new CommandSet(CommandCategory.categoryFun, ":zany_face:", new CommandData[] {
                                                // greet command
                                                Commands.slash("greet",
                                                                "He just says hi, like I don't know what else to write here.")
                                                                .setLocalizationFunction(localizationFunction)
                                }),
                                // developer commands
                                new CommandSet(CommandCategory.categoryDev, ":technologist:", new CommandData[] {
                                                // test command
                                                Commands.slash("test", "Command for testing purposes.")
                                                                .setLocalizationFunction(localizationFunction)
                                                                // subcommands
                                                                .addSubcommands(
                                                                                // test localization
                                                                                new SubcommandData("localization",
                                                                                                "For testing localiztion.")
                                                                                                .addOptions(
                                                                                                                // locale
                                                                                                                // to
                                                                                                                // choose
                                                                                                                new OptionData(OptionType.STRING,
                                                                                                                                "locale",
                                                                                                                                "Locale for testing.")
                                                                                                                                // locale
                                                                                                                                // chocies
                                                                                                                                .addChoices(Tools
                                                                                                                                                .getChoicesFromKeys(
                                                                                                                                                                settings.getLocalization()
                                                                                                                                                                                .keySet()))
                                                                                                                                .setRequired(true),
                                                                                                                // name
                                                                                                                // of
                                                                                                                // string
                                                                                                                new OptionData(OptionType.STRING,
                                                                                                                                "text",
                                                                                                                                "Name of string.")
                                                                                                                                // name
                                                                                                                                // choices
                                                                                                                                .addChoices(Tools
                                                                                                                                                .getChoicesFromKeys(
                                                                                                                                                                settings.getLocalization()
                                                                                                                                                                                .get(settings.getLocalization()
                                                                                                                                                                                                .entrySet()
                                                                                                                                                                                                .iterator()
                                                                                                                                                                                                .next()
                                                                                                                                                                                                .getKey())
                                                                                                                                                                                .keySet()))
                                                                                                                                // required
                                                                                                                                .setRequired(true)),
                                                                                // test database
                                                                                new SubcommandData("database",
                                                                                                "Tests database connection."),
                                                                                // test rss
                                                                                new SubcommandData("rss",
                                                                                                "Tests RSS feed.")
                                                                                                .addOptions(
                                                                                                                // url
                                                                                                                // of
                                                                                                                // rss
                                                                                                                // channel
                                                                                                                new OptionData(
                                                                                                                                OptionType.STRING,
                                                                                                                                "url",
                                                                                                                                "URL of RSS channel.")
                                                                                                                                .setRequired(true)))
                                                                // guild-only command
                                                                .setGuildOnly(true)
                                                                // admin-only command
                                                                .setDefaultPermissions(DefaultMemberPermissions
                                                                                .enabledFor(Permission.ADMINISTRATOR)),
                                                // info command
                                                Commands.slash("info",
                                                                "Gets system info.")
                                                                .setLocalizationFunction(localizationFunction)
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
