package cz.jsfraz.lojza;

import java.util.Map;

public interface ILocalizationManager {
    void setLocalization(Map<String, Map<String, String>> localization);

    String getText(String locale, String name);
}
