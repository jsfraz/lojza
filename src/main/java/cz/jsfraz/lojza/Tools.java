package cz.jsfraz.lojza;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

public class Tools {
    // gets localization from resource folder
    public static Map<String, Map<String, String>> getLocalization() throws IOException {
        Map<String, Map<String, String>> localization = new HashMap<String, Map<String, String>>();
        for (Locale locale : EnumSet.allOf(Locale.class)) {
            // reading json file
            String json = readJsonFile("textLocalization/" + locale.name() + ".json");
            // adding to localization map
            localization.put(locale.name(), deserializeMap(json));
        }
        return localization;
    }

    public static Map<String, String> getLanguagueNames() throws IOException {
        String json = readJsonFile("ISO-693-1.json");
        return deserializeMap(json);
    }

    // https://stackoverflow.com/questions/15749192/how-do-i-load-a-file-from-resource-folder
    // https://www.baeldung.com/convert-input-stream-to-string
    private static String readJsonFile(String path) throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(path);
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(
                new InputStreamReader(inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        }
        return textBuilder.toString();
    }

    // deserializing with jackson
    // https://stackoverflow.com/questions/18002132/deserializing-into-a-hashmap-of-custom-objects-with-jackson
    private static HashMap<String, String> deserializeMap(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory typeFactory = mapper.getTypeFactory();
        MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);
        return mapper.readValue(json, mapType);
    }

    // returns list of choices based on key set
    // Cannot have more than 25 choices for one option!
    public static List<Choice> getChoicesFromKeys(Set<String> keySet) {
        List<Choice> choices = new ArrayList<Choice>();

        List<String> keys = new ArrayList<String>(keySet);
        // keeps removing items from set until 25 are left
        while (keys.size() > 25) {
            keys.remove(keys.toArray()[keys.size() - 1]);
        }

        for (String key : keys) {
            choices.add(new Choice(key, key));
        }
        return choices;
    }

    // gets rss feed
    public static SyndFeed getRssFeed(String url) throws Exception {
        URL feedSource = new URL(url);
        SyndFeedInput input = new SyndFeedInput();
        return input.build(new XmlReader(feedSource.openStream()));
    }

    public static MessageEmbed getHelpEmbed(ILocalizationManager lm, Locale locale) {
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

        return eb.build();
    }

    // I forgot how this works
    private static String getCommandHelp(CommandData[] commands) {
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

    // setup embed
    public static MessageEmbed getSetupEmbed(ILocalizationManager lm, Locale locale) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(Color.decode("#2b2d31"));
        eb.addField(lm.getText(locale, "textSetupTitle"), lm.getText(locale, "textSetupDesc"), false);
        return eb.build();
    }

    // setup select menu
    public static StringSelectMenu getSetupSelectMenu(ILocalizationManager lm, Locale locale, String userId, SetupOption option) {
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
}
