package cz.jsfraz.lojza;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import net.dv8tion.jda.api.interactions.commands.Command.Choice;

public class Tools {
    // get files from folder in resource directory
    // https://www.logicbig.com/how-to/java/list-all-files-in-resouce-folder.html
    private static File[] getResourceFolderFiles(String folder) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource(folder);
        String path = url.getPath();
        return new File(path).listFiles();
    }

    // gets localization from resource folder
    public static Map<String, Map<String, String>> getLocalization() throws IOException {
        Map<String, Map<String, String>> localization = new HashMap<String, Map<String, String>>();
        for (File file : getResourceFolderFiles("textLocalization")) {
            String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);

            // https://stackoverflow.com/questions/18002132/deserializing-into-a-hashmap-of-custom-objects-with-jackson
            ObjectMapper mapper = new ObjectMapper();
            TypeFactory typeFactory = mapper.getTypeFactory();
            MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);
            HashMap<String, String> map = mapper.readValue(json, mapType);

            localization.put(file.getName().replaceAll(".json", ""), map);
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
