package com.example.eggxtremeflight;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {
    private static final String prefName = "GameSharedPreferences";
    private static final String keyLang = "selected_language";
    public static Context setLocale(Context context, String language){
        SharedPreferences sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(keyLang, language).apply();
        return updateResources(context, language);
    }

    public static Context loadLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        String language = prefs.getString(keyLang, "en");
        return updateResources(context, language);
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);

        return context.createConfigurationContext(configuration);
    }
}
