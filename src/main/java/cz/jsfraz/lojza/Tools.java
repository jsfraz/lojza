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
            // reading json from resource folder
            InputStream inputStream = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("textLocalization/" + locale.name() + ".json");
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(
                    new InputStreamReader(inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                }
            }
            String json = textBuilder.toString();

            // deserializing with jackson
            // https://stackoverflow.com/questions/18002132/deserializing-into-a-hashmap-of-custom-objects-with-jackson
            ObjectMapper mapper = new ObjectMapper();
            TypeFactory typeFactory = mapper.getTypeFactory();
            MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);
            HashMap<String, String> map = mapper.readValue(json, mapType);

            // adding to localization map
            localization.put(locale.name(), map);
        }

        return localization;
    }

    // returns list of choices based on key set
    public static List<Choice> getChoicesFromKeys(Set<String> keySet) {
        List<Choice> choices = new ArrayList<Choice>();
        for (String key : keySet) {
            choices.add(new Choice(key, key));
        }
        return choices;
    }
}
