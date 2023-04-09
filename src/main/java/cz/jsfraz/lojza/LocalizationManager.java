package cz.jsfraz.lojza;

import java.util.Map;

public class LocalizationManager implements ILocalizationManager {
    private Map<String, Map<String, String>> localization;

    public LocalizationManager() {
        this.localization = SettingSingleton.GetInstance().getLocalization();
    }

    public String getText(String locale, String name) {
        String text = "ERROR";
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
