package cz.jsfraz.lojza;

import java.util.Map;

public class LocalizationManager implements ILocalizationManager {
    private Map<String, Map<String, String>> localization;

    public LocalizationManager() {
        this.localization = SettingSingleton.GetInstance().getLocalization();
    }

    // gets loclized text or returns original name
    public String getText(Locale locale, String name) {
        Map<String, String> strings = this.localization.getOrDefault(locale.name(), null);
        // check if locale exists
        if (strings != null) {
            String result = strings.getOrDefault(name, null);
            // check if name exists
            if (result != null) {
                return result;
            } else {
                return name;
            }
        } else {
            return name;
        }
    }
}
