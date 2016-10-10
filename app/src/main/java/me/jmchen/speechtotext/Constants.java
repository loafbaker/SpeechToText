package me.jmchen.speechtotext;

import com.memetix.mst.language.Language;

/**
 * Created by Think on 2016/8/20.
 */
public class Constants {
    public static final String PRIMARY_SUBSCRIPTION_KEY = "TBA";
    public static final String SECONDARY_SUBSCRIPTION_KEY = "TBA";

    public static final String[] LANGUAGE_CODES = {"en-us", "en-gb", "fr-fr", "de-de", "it-it", "zh-cn", "zh-hk", "es-es"};
    public static final String SPEECH_TO_TEXT_PREFERENCES = "SpeechToTextPreferences";
    public static final String SPEECH_MODE_INDEX = "SpeechModeIndex";
    public static final String BASE_LANGUAGE_INDEX = "BaseLanguageIndex";

    public static final String CONVERT_LANGUAGE_INDEX = "ConvertLanguageIndex";

    public static final String CLIENT_ID_VALUE = "me_jmchen_speechtotext";
    public static final String CLIENT_SECRET_VALUE = "TBA";

    public static final Language[] LANGUAGES = {
            Language.ENGLISH,
            Language.ENGLISH,
            Language.FRENCH,
            Language.GERMAN,
            Language.ITALIAN,
            Language.CHINESE_SIMPLIFIED,
            Language.CHINESE_TRADITIONAL,
            Language.SPANISH
    };
}
