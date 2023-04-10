package cz.jsfraz.lojza;

import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class SlashCommandListener extends ListenerAdapter {
    private ILocalizationManager lm;
    private IDatabase db;

    public SlashCommandListener() {
        this.lm = new LocalizationManager();
        this.db = new Database();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // TODO get server locale
        // server locale
        final String locale = "en_US";

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

            /* Fun commands */
            case "greet": // greet user
                greetCommand(event, locale);
                break;

            /* Developer commands */
            case "test localization": // test localization command
                testLocalization(event, locale);
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

    // help
    private void helpCommand(SlashCommandInteractionEvent event, String locale) {
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

    // delete all messages
    private void deleteAllCommand(SlashCommandInteractionEvent event, String locale) {
        String userId = event.getUser().getId();
        // reply with button menu
        event.reply(lm.getText(locale, "textDeleteMessagesAll"))
                .addActionRow(Button.primary(userId + ":deleteAll", lm.getText(locale, "textYes")),
                        Button.secondary(userId + ":cancel", lm.getText(locale, "textNo")))
                .setEphemeral(true).queue();
    }

    // delete specific number of messages
    // https://github.com/DV8FromTheWorld/JDA/blob/master/src/examples/java/SlashBotExample.java#L195
    private void deleteCountCommand(SlashCommandInteractionEvent event, String locale) {
        // count option
        OptionMapping countOption = event.getOption("count");

        // reply with button menu
        String userId = event.getUser().getId();
        event.reply(String.format(lm.getText(locale, "textDeleteMessagesCount"), countOption.getAsInt()))
                .addActionRow(
                        Button.primary(userId + ":deleteCount:" + countOption.getAsInt(),
                                lm.getText(locale, "textYes")),
                        Button.secondary(userId + ":cancel", lm.getText(locale, "textNo")))
                .setEphemeral(true).queue();
    }

    // button interaction
    // https://github.com/DV8FromTheWorld/JDA/blob/master/src/examples/java/SlashBotExample.java#L116
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] ids = event.getComponentId().split(":");
        String authorId = ids[0];
        String type = ids[1];

        // check if the right user clicked, otherwise just ignore
        if (!authorId.equals(event.getUser().getId()))
            return;
        // acknowledge the button was clicked, otherwise the interaction will fail
        event.deferEdit().queue();

        TextChannel channel = (TextChannel) event.getChannel();
        switch (type) {
            // delete all messages
            case "deleteAll":
                // delete the prompt message
                event.getHook().deleteOriginal().queue();
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // clones channel and deletes the original
                int postion = channel.getPosition() - 1;
                channel.createCopy().setPosition(postion).queue();
                channel.delete().queue();
                break;

            // delete specific count of images
            case "deleteCount":
                // delete the prompt message
                event.getHook().deleteOriginal().queue();
                // delete messages
                String count = ids[2]; // number of messages to delete
                channel.getIterableHistory()
                        .skipTo(event.getMessageIdLong())
                        .takeAsync(Integer.parseInt(count))
                        .thenAccept(channel::purgeMessages);
                break;

            // delete the prompt message
            case "cancel":
                event.getHook().deleteOriginal().queue();
                break;
        }
    }

    // greet command
    private void greetCommand(SlashCommandInteractionEvent event, String locale) {
        event.reply(lm.getText(locale, "textGreet")).queue();
    }

    // test localization command
    private void testLocalization(SlashCommandInteractionEvent event, String locale) {
        // locale option
        OptionMapping localeOption = event.getOption("locale");
        // name option
        OptionMapping nameOption = event.getOption("text");

        // reply
        event.reply("`" + localeOption.getAsString() + "`, `" + nameOption.getAsString() + "`: "
                + lm.getText(localeOption.getAsString(), nameOption.getAsString())).queue();
    }

    // test database connection
    private void testDatabase(SlashCommandInteractionEvent event, String locale) {
        try {
            db.testConnection();
            event.reply(lm.getText(locale, "textDbOk")).queue();
        } catch (Exception e) {
            e.printStackTrace();
            event.reply(lm.getText(locale, "textDbError") + " *" + e.getMessage() + "*").queue();
        }
    }

    // info command
    private void infoCommand(SlashCommandInteractionEvent event, String locale) {
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
        event.replyEmbeds(eb.build()).queue();
    }
}