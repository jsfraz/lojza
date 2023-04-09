package cz.jsfraz.lojza;

import java.util.Map;

public interface ILocalization {
    void setLocalization(Map<String, Map<String, String>> localization);

    String getTranslation(String locale, String name);
}
