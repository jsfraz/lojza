package cz.jsfraz.lojza;

import java.util.Map;

public class Localization implements ILocalization {
    private Map<String, Map<String, String>> localization;

    public void setLocalization(Map<String, Map<String, String>> localization) {
        this.localization = localization;
    }

    public String getTranslation(String locale, String name) {
        String text = "UNABLE TO FETCH TEXT";
        Map<String, String> strings = this.localization.getOrDefault(locale, null);
        if (strings != null) {
            String result = strings.getOrDefault(name, null);
            if (result != null) {
                text = result;
            }
        }
        return text;
    }
}
