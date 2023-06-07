package cz.jsfraz.lojza;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.mongodb.MongoException;
import com.rometools.rome.feed.synd.SyndFeed;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class SlashCommandListener extends ListenerAdapter {
    private ILocalizationManager lm;
    private IDatabase db;
    private SettingSingleton settings;

    public SlashCommandListener() {
        this.lm = new LocalizationManager();
        this.db = new Database();
        this.settings = SettingSingleton.GetInstance();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // server locale
        Locale locale = settings.getDefaultLocale();
        if (event.isFromGuild()) {
            locale = db.getGuildLocale(event.getGuild().getIdLong());
        }

        switch (event.getFullCommandName()) {
            /* Help commands */
            case "help": // help command
                helpCommand(event, locale);
                break;

            /* Admin commands */
            case "delete all": // delete all command
                deleteAllCommand(event, locale);
                break;
            case "delete count": // delete count command
                deleteCountCommand(event, locale);
                break;

            case "setup": // setup command
                setupCommand(event, locale);
                break;

            case "rss channel": // set rss channel command
                rssChannelCommand(event, locale);
                break;

            case "rss add": // add rss feed command
                rssAddCommand(event, locale);
                break;

            case "rss list": // list rss feeds command
                rssListCommand(event, locale);
                break;

            case "rss remove": // remove rss feed by index command
                rssRemoveCommand(event, locale);
                break;

            case "rss clear": // clear rss feed list command
                rssClearCommand(event, locale);
                break;

            /* Fun commands */
            case "greet": // greet user
                greetCommand(event, locale);
                break;

            /* Developer commands */
            case "test localization": // test localization command
                testLocalization(event);
                break;

            case "test database": // test database
                testDatabase(event, locale);
                break;

            case "test rss": // test rss
                testRss(event, locale);
                break;

            case "info": // gets system info
                infoCommand(event, locale);
                break;

            // no match
            default:
                event.reply(lm.getText(locale, "errorCommandHandle")).queue();
        }
    }

    /* Help commands */

    // help
    private void helpCommand(SlashCommandInteractionEvent event, Locale locale) {
        // command categories
        CommandSet[] commandSets = SettingSingleton.GetInstance().getCommandSets();

        // embed
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(lm.getText(locale, "textHelpTitle"));
        eb.setColor(Color.yellow);
        eb.addField(lm.getText(locale, "textHelpDescTitle"),
                lm.getText(locale, "textHelpDesc"),
                false);

        // gets commands as string
        for (CommandSet commandSet : commandSets) {
            String commands = getCommandHelp(commandSet.commands);

            eb.addField("**" + commandSet.getDisplayEmoji() + " " + lm.getText(locale, commandSet.getCategory().name())
                    + "**", commands, true);
        }

        // reply with embed
        event.replyEmbeds(eb.build()).queue();
    }

    // I forgot how this works
    private String getCommandHelp(CommandData[] commands) {
        // TODO support for SubcommandGroups
        String text = "";
        for (CommandData commandData : commands) {
            if (commandData.getType() == Type.SLASH) {
                SlashCommandData slash = (SlashCommandData) commandData;
                List<SubcommandData> subcommands = slash.getSubcommands();
                if (subcommands.isEmpty()) {
                    List<OptionData> options = slash.getOptions();
                    String command = "`/" + slash.getName();
                    for (OptionData option : options) {
                        if (option.isRequired()) {
                            command += " " + option.getName();
                        } else {
                            command += " [" + option.getName() + "]";
                        }
                    }
                    command += "`";
                    text += command + "\n";
                } else {
                    for (SubcommandData subcommand : subcommands) {
                        List<OptionData> options = subcommand.getOptions();
                        String command = "`/" + slash.getName() + " " + subcommand.getName();
                        for (OptionData option : options) {
                            if (option.isRequired()) {
                                command += " [" + option.getName() + "]";
                            } else {
                                command += " (" + option.getName() + ")";
                            }
                        }
                        command += "`";
                        text += command + "\n";
                    }
                }
            }
        }
        return text;
    }

    /* Admin commands */

    // delete all messages
    private void deleteAllCommand(SlashCommandInteractionEvent event, Locale locale) {
        OptionMapping forceOption = event.getOption("force");
        if (forceOption != null) {
            if (Boolean.parseBoolean(forceOption.getAsString())) {
                deleteAllMessages((TextChannel) event.getChannel());
                return;
            }
        }

        String userId = event.getUser().getId();
        // reply with button menu
        event.reply(lm.getText(locale, "textDeleteMessagesAll"))
                .addActionRow(Button.primary(userId + ":deleteAll", lm.getText(locale, "textYes")),
                        Button.secondary(userId + ":cancel", lm.getText(locale, "textNo")))
                .setEphemeral(true).queue();
    }

    // delete specific number of messages
    // https://github.com/DV8FromTheWorld/JDA/blob/master/src/examples/java/SlashBotExample.java#L195
    private void deleteCountCommand(SlashCommandInteractionEvent event, Locale locale) {
        // count option
        OptionMapping countOption = event.getOption("count");

        // reply with button menu
        String userId = event.getUser().getId();
        event.reply(String.format(lm.getText(locale, "textDeleteMessagesCount"), countOption.getAsInt()))
                .addActionRow(Button.primary(userId + ":deleteCount:" + countOption.getAsInt(),
                        lm.getText(locale, "textYes")),
                        Button.secondary(userId + ":cancel", lm.getText(locale, "textNo")))
                .setEphemeral(true).queue();
    }

    // setup command
    private void setupCommand(SlashCommandInteractionEvent event, Locale locale) {
        String userId = event.getUser().getId();
        event.replyEmbeds(getSetupEmbed(locale)).addActionRow(getSetupSelectMenu(locale, userId, null))
                .setEphemeral(true).queue();
    }

    // guild setup
    private void setup(StringSelectInteractionEvent event, Locale locale) {
        String userId = event.getUser().getId();
        SetupOption option = SetupOption.valueOf(event.getValues().get(0));

        List<ItemComponent> components = new ArrayList<ItemComponent>();

        switch (option) {
            case locale: // locale menu
                StringSelectMenu.Builder localeMenuBuilder = StringSelectMenu.create(userId + ":localeMenu");
                SettingSingleton settings = SettingSingleton.GetInstance();
                Map<String, String> languagues = settings.getLanguagueNames();
                settings.getLocalization().keySet().forEach(x -> {
                    localeMenuBuilder.addOption(languagues.get(x), x);
                });
                localeMenuBuilder.setDefaultValues(locale.name());
                components.add(localeMenuBuilder.build());
                break;

            default: // anything else (enable/disable buttons)
                Button[] buttons = getSetupEnableDisableButtons(locale, userId, option);
                for (Button button : buttons) {
                    components.add(button);
                }
                break;
        }

        // componenets to send
        List<LayoutComponent> layoutComponents = new ArrayList<LayoutComponent>();
        layoutComponents.add(ActionRow.of(getSetupSelectMenu(locale, userId, option)));
        if (!components.isEmpty()) {
            layoutComponents.add(ActionRow.of(components));
        }

        event.getHook()
                .editMessageComponentsById("@original", layoutComponents)
                .queue();
    }

    // setup embed
    private MessageEmbed getSetupEmbed(Locale locale) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.decode("#2b2d31"));
        eb.addField(lm.getText(locale, "textSetupTitle"), lm.getText(locale, "textSetupDesc"), false);
        return eb.build();
    }

    // setup select menu
    private StringSelectMenu getSetupSelectMenu(Locale locale, String userId, SetupOption option) {
        // https://stackoverflow.com/questions/74833816/how-to-send-dropdown-menu-in-java-discord-api
        StringSelectMenu.Builder selectMenuBuilder = StringSelectMenu.create(userId + ":setupMenu");
        for (SetupOption o : EnumSet.allOf(SetupOption.class)) {
            selectMenuBuilder.addOption(
                    lm.getText(locale, "option" + o.name().substring(0, 1).toUpperCase() + o.name().substring(1)),
                    o.name());
            // setting default option
            if (o == option) {
                selectMenuBuilder.setDefaultValues(o.name());
            }
        }
        return selectMenuBuilder.build();
    }

    // setup enable/disable buttons
    private Button[] getSetupEnableDisableButtons(Locale locale, String userId, SetupOption option) {
        String o = "";
        if (option != null) {
            o = ":" + option.name();
        }
        Button[] components = new Button[] { Button.success(userId + ":enable" + o,
                lm.getText(locale, "textEnable")),
                Button.danger(userId + ":disable" + o, lm.getText(locale, "textDisable")) };
        return components;
    }

    // button interaction
    // https://github.com/DV8FromTheWorld/JDA/blob/master/src/examples/java/SlashBotExample.java#L116
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] ids = event.getComponentId().split(":");

        // check if the right user clicked, otherwise just ignore
        if (!ids[0].equals(event.getUser().getId()))
            return;

        // acknowledge the button was clicked, otherwise the interaction will fail
        event.deferEdit().queue();

        TextChannel channel = (TextChannel) event.getChannel();

        switch (ids[1]) {
            // delete all messages
            case "deleteAll":
                // delete the prompt message
                event.getHook().deleteOriginal().queue();

                deleteAllMessages(channel);
                break;

            // delete specific count of images
            case "deleteCount":
                // delete the prompt message
                event.getHook().deleteOriginal().queue();

                String count = ids[2]; // number of messages to delete
                deleteMessageCount(event.getMessageIdLong(), channel, Integer.parseInt(count));
                break;

            // delete the prompt message
            case "cancel":
                event.getHook().deleteOriginal().queue();
                break;

            // enabling
            case "enable":
                switch (ids[2]) {

                    // rss
                    case "rss":
                        db.updateRss(event.getGuild().getIdLong(), true);
                        break;
                }
                break;

            // disabling
            case "disable":
                switch (ids[2]) {

                    // rss
                    case "rss":
                        db.updateRss(event.getGuild().getIdLong(), false);
                        break;
                }
                break;
        }
    }

    // string select interaction
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        // server locale
        Locale locale = settings.getDefaultLocale();
        ;
        if (event.isFromGuild()) {
            locale = db.getGuildLocale(event.getGuild().getIdLong());
        }

        String[] ids = event.getComponentId().split(":");

        // check if the right user clicked, otherwise just ignore
        if (!ids[0].equals(event.getUser().getId()))
            return;

        // acknowledge the button was clicked, otherwise the interaction will fail
        event.deferEdit().queue();

        switch (ids[1]) {
            case "setupMenu": // guild setup
                setup(event, locale);
                break;

            case "localeMenu": // guild languague
                try {
                    db.updateGuildLocale(event.getGuild().getIdLong(), Locale.valueOf(event.getValues().get(0)));
                } catch (MongoException e) {
                    event.getUser().openPrivateChannel()
                            .flatMap(x -> x
                                    .sendMessage(
                                            MessageCreateData.fromContent(
                                                    lm.getText(settings.getDefaultLocale(), "textDbErrorUser"))))
                            .queue();
                }
                break;
        }
    }

    // delete all messages in channel
    private void deleteAllMessages(TextChannel channel) {
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // clones channel and deletes the original
        int postion = channel.getPosition() - 1;
        channel.createCopy().setPosition(postion).queue();
        channel.delete().queue();
    }

    // delete specific number of messages in cahnnel
    private void deleteMessageCount(long messageId, TextChannel channel, int count) {
        // delete messages
        channel.getIterableHistory()
                .skipTo(messageId)
                .takeAsync(count)
                .thenAccept(channel::purgeMessages);
    }

    // rss channel command
    private void rssChannelCommand(SlashCommandInteractionEvent event, Locale locale) {
        long guildRssChannel = db.getRssChannel(event.getGuild().getIdLong());

        if (guildRssChannel != event.getGuildChannel().getIdLong()) {
            db.updateRssChannel(event.getGuild().getIdLong(), event.getGuildChannel().getIdLong());
            event.reply(lm.getText(locale, "textRssChannelSet")).setEphemeral(true).queue();
        } else {
            event.reply(lm.getText(locale, "textRssChannelAlreadySet")).setEphemeral(true).queue();
        }
    }

    // rss add url command
    private void rssAddCommand(SlashCommandInteractionEvent event, Locale locale) {
        // url option
        OptionMapping urlOption = event.getOption("url");
        // Discord guild rss feed count
        int rssCount = db.getRssFeeds(event.getGuild().getIdLong()).size();

        if (rssCount < settings.getRssMaxFeedCount()) {
            try {
                // check url
                new URL(urlOption.getAsString());

                // check if url is already in list
                boolean exists = db.rssFeedExists(event.getGuild().getIdLong(), urlOption.getAsString());

                if (exists) {
                    event.reply(lm.getText(locale, "textRssAlreadyExists")).setEphemeral(true).queue();
                } else {
                    // test RSS url
                    try {
                        SyndFeed feed = Tools.getRssFeed(urlOption.getAsString());

                        db.updateRssFeeds(event.getGuild().getIdLong(), feed.getTitle(), urlOption.getAsString());
                        event.reply(lm.getText(locale, "textRssAdded")).setEphemeral(true).queue();
                    } catch (Exception e) {
                        event.reply(lm.getText(locale, "textInvalidRssSource")).setEphemeral(true).queue();
                    }
                }
            } catch (MalformedURLException e) {
                event.reply(lm.getText(locale, "textInvalidRssUrl")).setEphemeral(true).queue();
            }
        } else {
            event.reply(String.format(lm.getText(locale, "textRssFeedLimit"), settings.getRssMaxFeedCount()))
                    .setEphemeral(true).queue();
        }
    }

    // rss list command
    private void rssListCommand(SlashCommandInteractionEvent event, Locale locale) {
        long channelId = db.getRssChannel(event.getGuild().getIdLong());

        // check if channel is set
        if (channelId != DiscordGuild.getDefaultRssChannelId()) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(lm.getText(locale, "textRssConfig"));
            eb.setColor(Color.yellow);
            eb.addField(lm.getText(locale, "textRssChannel"), String.format("<#%s>", Long.toString(channelId)), false);
            List<RssFeed> guildUrls = db.getRssFeeds(event.getGuild().getIdLong());
            String urls = "";
            if (!guildUrls.isEmpty()) {
                for (int i = 0; i < guildUrls.size(); i++) {
                    urls += "`" + (i + 1) + ")`  [" + guildUrls.get(i).getTitle() + "](" + guildUrls.get(i).getUrl()
                            + ")";
                    if (i != guildUrls.size() - 1) {
                        urls += "\n";
                    }
                }
            } else {
                urls += "*" + lm.getText(locale, "rssListEmpty") + "*";
            }
            eb.addField(lm.getText(locale, "textRssSources"), urls, false);

            // reply with embed
            event.replyEmbeds(eb.build()).setEphemeral(true).queue();
        } else {
            event.reply(lm.getText(locale, "textRssChannelNotSet")).setEphemeral(true).queue();
        }
    }

    // rss remove command
    private void rssRemoveCommand(SlashCommandInteractionEvent event, Locale locale) {
        // index option
        OptionMapping indexOption = event.getOption("index");
        // Discord guild rss feed count
        int rssCount = db.getRssFeeds(event.getGuild().getIdLong()).size();

        // check index range
        if (indexOption.getAsInt() > 0 && indexOption.getAsInt() <= rssCount) {
            db.removeRssFeed(event.getGuild().getIdLong(), indexOption.getAsInt() - 1);
            event.reply(lm.getText(locale, "textRssRemoved")).setEphemeral(true).queue();
        } else {
            event.reply(lm.getText(locale, "textRssInvalidRange")).setEphemeral(true).queue();
        }
    }

    // rss clear command
    private void rssClearCommand(SlashCommandInteractionEvent event, Locale locale) {
        db.clearRssFeeds(event.getGuild().getIdLong());
        event.reply(lm.getText(locale, "textRssCleared")).setEphemeral(true).queue();
    }

    /* Fun commands */

    // greet command
    private void greetCommand(SlashCommandInteractionEvent event, Locale locale) {
        event.reply(lm.getText(locale, "textGreet")).queue();
    }

    /* Developer commands */

    // test localization command
    private void testLocalization(SlashCommandInteractionEvent event) {
        // locale option
        OptionMapping localeOption = event.getOption("locale");
        // name option
        OptionMapping nameOption = event.getOption("text");

        // reply
        event.reply("`" + localeOption.getAsString() + "`, `" + nameOption.getAsString() + "`: "
                + lm.getText(Locale.valueOf(localeOption.getAsString()), nameOption.getAsString())).setEphemeral(true)
                .queue();
    }

    // test database connection
    private void testDatabase(SlashCommandInteractionEvent event, Locale locale) {
        if (db.testConnection()) {
            event.reply(lm.getText(locale, "textDbOk")).setEphemeral(true).queue();
        } else {
            event.reply(lm.getText(locale, "textDbError")).setEphemeral(true).queue();
        }
    }

    // test rss feed
    private void testRss(SlashCommandInteractionEvent event, Locale locale) {
        // url option
        OptionMapping urlOption = event.getOption("url");

        boolean ok = false;
        try {
            Tools.getRssFeed(urlOption.getAsString());
            ok = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ok) {
            event.reply(lm.getText(locale, "textDbOk")).setEphemeral(true).queue();
        } else {
            event.reply(lm.getText(locale, "textDbError")).setEphemeral(true).queue();
        }
    }

    // info command
    private void infoCommand(SlashCommandInteractionEvent event, Locale locale) {
        // os info
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");
        // java version
        String javaVersion = System.getProperty("java.version");
        // uptime
        LocalDateTime started = SettingSingleton.GetInstance().getStarted();
        Duration uptime = Duration.between(started, LocalDateTime.now());
        // https://www.baeldung.com/java-ms-to-hhmmss
        long HH = uptime.toHours();
        long MM = uptime.toMinutesPart();
        long SS = uptime.toSecondsPart();

        // embed
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(lm.getText(locale, "textStatusTitle"));
        eb.setColor(Color.yellow);
        eb.addField(lm.getText(locale, "textOsTitle"), osName + " " + osVersion + " (" + osArch + ")", false);
        eb.addField(lm.getText(locale, "textJavaVerTitle"), javaVersion, false);
        eb.addField(lm.getText(locale, "textUptimeTitle"), String.format("%02d:%02d:%02d", HH, MM, SS), false);

        // reply with embed
        event.replyEmbeds(eb.build()).setEphemeral(true).queue();
    }
}