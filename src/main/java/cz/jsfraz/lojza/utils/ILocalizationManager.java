package cz.jsfraz.lojza.utils;

import cz.jsfraz.lojza.database.models.Locale;

public interface ILocalizationManager {
    String getText(Locale locale, String name);
}
