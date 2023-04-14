package cz.jsfraz.lojza;

import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.mongodb.MongoException;

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
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class SlashCommandListener extends ListenerAdapter {
    private ILocalizationManager lm;
    private IDatabase db;

    public SlashCommandListener() {
        this.lm = new LocalizationManager();
        this.db = new Database();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // server locale
        Locale locale = Locale.en;
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

        // TODO future support for SubcommandGroups
        for (CommandSet commandSet : commandSets) {
            String commands = "";
            for (CommandData commandData : commandSet.commands) {
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
                        commands += command + "\n";
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
                            commands += command + "\n";
                        }
                    }
                }
            }

            eb.addField("**" + commandSet.getDisplayEmoji() + " " + lm.getText(locale, commandSet.getCategory().name())
                    + "**", commands, true);
        }

        // reply with embed
        event.replyEmbeds(eb.build()).queue();
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
        if (option == SetupOption.locale) {
            StringSelectMenu.Builder localeMenuBuilder = StringSelectMenu.create(userId + ":localeMenu");
            SettingSingleton settings = SettingSingleton.GetInstance();
            Map<String, String> languagues = settings.getLanguagueNames();
            settings.getLocalization().keySet().forEach(x -> {
                localeMenuBuilder.addOption(languagues.get(x), x);
            });
            localeMenuBuilder.setDefaultValues(locale.name());
            components.add(localeMenuBuilder.build());
        } else {
            Button[] buttons = getSetupEnableDisableButtons(locale, userId, option);
            for (Button button : buttons) {
                components.add(button);
            }
        }

        event.getHook()
                .editMessageComponentsById("@original",
                        ActionRow.of(getSetupSelectMenu(locale, userId, option)),
                        ActionRow.of(components))
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
        Button[] components = new Button[] { Button.success(userId + ":enable",
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
        }
    }

    // string select interaction
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        // server locale
        Locale locale = Locale.en;
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
                                            MessageCreateData.fromContent(lm.getText(Locale.en, "textDbErrorUser"))))
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
            event.reply(lm.getText(locale, "textDbOk")).queue();
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