package com.smartbite.utils;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
public class ThemeManager {
    private static final String PREF_DARK_MODE="dark_mode";
    private static SharedPreferences prefs;
    public static void init(Context context){
        prefs=context.getSharedPreferences("ThemePrefs",Context.MODE_PRIVATE);
        applyTheme(isDarkMode());
    }
    public static void toggleTheme(){setDarkMode(!isDarkMode());}
    public static void setDarkMode(boolean dark){
        prefs.edit().putBoolean(PREF_DARK_MODE,dark).apply();
        applyTheme(dark);
    }
    public static boolean isDarkMode(){return prefs!=null&&prefs.getBoolean(PREF_DARK_MODE,false);}
    private static void applyTheme(boolean dark){
        AppCompatDelegate.setDefaultNightMode(
            dark?AppCompatDelegate.MODE_NIGHT_YES:AppCompatDelegate.MODE_NIGHT_NO);
    }
}