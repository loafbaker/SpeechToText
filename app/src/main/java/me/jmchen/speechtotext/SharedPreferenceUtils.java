package me.jmchen.speechtotext;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Think on 2016/8/20.
 */
public class SharedPreferenceUtils {
    public static int getSpeechModeIndex(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.SPEECH_TO_TEXT_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Constants.SPEECH_MODE_INDEX, 0);
    }

    public static void updateSpeechModeIndex(Context context, int speechModeIndex) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.SPEECH_TO_TEXT_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.SPEECH_MODE_INDEX, speechModeIndex);
        editor.apply();
    }

    public static int getBaseLanguageIndex(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.SPEECH_TO_TEXT_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Constants.BASE_LANGUAGE_INDEX, 0);
    }

    public static void updateBaseLanguageIndex(Context context, int languageIndex) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.SPEECH_TO_TEXT_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.BASE_LANGUAGE_INDEX, languageIndex);
        editor.apply();
    }

    public static int getConvertLanguageIndex(Context context) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.SPEECH_TO_TEXT_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(Constants.CONVERT_LANGUAGE_INDEX, 0);
    }

    public static void updateConvertLanguageIndex(Context context, int languageIndex) {
        SharedPreferences sharedPreferences =
                context.getSharedPreferences(Constants.SPEECH_TO_TEXT_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.CONVERT_LANGUAGE_INDEX, languageIndex);
        editor.apply();
    }
}
