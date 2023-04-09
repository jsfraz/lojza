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
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        ;
        switch (event.getFullCommandName()) {
            /* Help commands */
            case "help": // help command
                helpCommand(event);
                break;

            /* Admin commands */
            case "delete all": // delete all command
                deleteAllCommand(event);
                break;
            case "delete count": // delete count command
                deleteCountCommand(event);
                break;

            // no match
            default:
                event.reply("I can't handle that command right now :(").queue(); // TODO localize
        }
    }

    // help
    private void helpCommand(SlashCommandInteractionEvent event) {
        // command categories
        CommandSet[] commandSets = SettingSingleton.GetInstance().getCommandSets();

        // embed
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Commands"); // TODO localize
        eb.setColor(Color.yellow);

        eb.addField("Volání příkazů",
                "Parametry označené jako `[parametr]` jsou **povinné**, parametry označené jako `(parametr)` jsou **volitelné**.",
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
            // TODO localize category name
            eb.addField("**" + commandSet.getDisplayEmoji() + " " + commandSet.getCategory() + "**", commands, true);
        }

        // replies with embed
        event.replyEmbeds(eb.build()).queue();
    }

    // delete all messages
    private void deleteAllCommand(SlashCommandInteractionEvent event) {
        // reply with button menu
        // TODO localize
        String userId = event.getUser().getId();
        event.reply("This will delete all messages.\nAre you sure you want to proceed?")
                .addActionRow(Button.primary(userId + ":deleteAll", "Hell yeah"),
                        Button.secondary(userId + ":cancel", "Nah"))
                .queue();
    }

    // delete specific number of messages
    // https://github.com/DV8FromTheWorld/JDA/blob/master/src/examples/java/SlashBotExample.java#L195
    private void deleteCountCommand(SlashCommandInteractionEvent event) {
        // count option
        OptionMapping countOption = event.getOption("count");
        int count = countOption.getAsInt();
        // reply with button menu
        // TODO localize
        String userId = event.getUser().getId();
        event.reply("This will delete " + count + " messages.\nAre you sure you want to proceed?")
                .addActionRow(Button.primary(userId + ":deleteCount:" + count, "Hell yeah"),
                        Button.secondary(userId + ":cancel", "Nah"))
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
}