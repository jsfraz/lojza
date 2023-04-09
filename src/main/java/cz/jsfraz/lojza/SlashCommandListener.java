package cz.jsfraz.lojza;

import java.awt.Color;
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

    public SlashCommandListener() {
        this.lm = new LocalizationManager();
        this.lm.setLocalization(SettingSingleton.GetInstance().getLocalization());
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

            case "test localization": // test localization command
                testLocalization(event, locale);
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

        int fieldCount = 0;
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

            Boolean inline = true;
            if (fieldCount != 0 && fieldCount % 2 == 0) {
                inline = false;
            }
            eb.addField("**" + commandSet.getDisplayEmoji() + " " + lm.getText(locale, commandSet.getCategory().name())
                    + "**", commands, inline);
            fieldCount++;
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
                .queue();
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
                .queue();
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

    // test localization
    private void testLocalization(SlashCommandInteractionEvent event, String locale) {
        // locale option
        OptionMapping localeOption = event.getOption("locale");
        // name option
        OptionMapping nameOption = event.getOption("text");

        // reply
        event.reply("`" + localeOption.getAsString() + "`, `" + nameOption.getAsString() + "`: "
                + lm.getText(localeOption.getAsString(), nameOption.getAsString())).queue();
    }
}