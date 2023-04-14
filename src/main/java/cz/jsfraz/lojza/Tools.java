package cz.jsfraz.lojza;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import net.dv8tion.jda.api.interactions.commands.Command.Choice;

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

        // keeps removing items from set until 25 are left
        Random rand = new Random();
        while (keySet.size() > 25) {
            keySet.remove(keySet.toArray()[rand.nextInt(0, keySet.size() - 1)]);
        }

        for (String key : keySet) {
            choices.add(new Choice(key, key));
        }
        return choices;
    }
}
