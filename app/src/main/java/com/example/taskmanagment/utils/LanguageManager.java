package com.example.taskmanagment.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

/**
 * Gestionnaire de langue pour l'application
 * Supporte le français et l'arabe avec support RTL
 */
public class LanguageManager {
    private static final String PREF_NAME = "app_preferences";
    private static final String KEY_LANGUAGE = "selected_language";

    public static final String LANGUAGE_FRENCH = "fr";
    public static final String LANGUAGE_ARABIC = "ar";

    /**
     * Définit la langue de l'application
     */
    public static void setLocale(Context context, String languageCode) {
        // Sauvegarder la langue sélectionnée
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();

        // Appliquer la nouvelle locale
        updateResources(context, languageCode);
    }

    /**
     * Récupère la langue actuellement sélectionnée
     */
    public static String getLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_FRENCH);
    }

    /**
     * Met à jour les ressources de l'application avec la nouvelle langue
     */
    private static void updateResources(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            config.setLayoutDirection(locale);
        } else {
            config.locale = locale;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLayoutDirection(locale);
        }

        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    /**
     * Applique la langue sauvegardée au démarrage de l'application
     * Cette méthode doit être appelée dans onCreate() de chaque activité
     */
    public static Context applyLanguage(Context context) {
        String languageCode = getLanguage(context);
        updateResources(context, languageCode);
        return context;
    }

    /**
     * Vérifie si la langue actuelle est l'arabe
     */
    public static boolean isArabic(Context context) {
        return getLanguage(context).equals(LANGUAGE_ARABIC);
    }

    /**
     * Vérifie si la langue actuelle utilise RTL (Right-To-Left)
     */
    public static boolean isRTL(Context context) {
        return isArabic(context);
    }

    /**
     * Obtient le nom d'affichage de la langue
     */
    public static String getLanguageDisplayName(String languageCode) {
        switch (languageCode) {
            case LANGUAGE_FRENCH:
                return "Français";
            case LANGUAGE_ARABIC:
                return "العربية";
            default:
                return "Français";
        }
    }
}