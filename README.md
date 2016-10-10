SpeechToText
============

A sample Android project which uses machine learning techniques powered by [Microsoft Cognitive Services APIs](https://www.microsoft.com/cognitive-services).

## Requirements

- Android OS must be Android 4.1 or higher (API Level 16 or higher)
- The project requires Microsoft Speech to Text API keys. Go to [Microsoft Cognitive Services](https://www.microsoft.com/cognitive-services) and subscribe "Bing Speech" plan. Then, you'll get two API keys. Replace `TBA` with your own keys in `app/src/main/java/me/jmchen/Constants.java`.
```java
    public static final String PRIMARY_SUBSCRIPTION_KEY = "TBA";
    public static final String SECONDARY_SUBSCRIPTION_KEY = "TBA";
```
- Bing Text-To-Speech API key. Register an account at [Microsoft DataMarket](https://datamarket.azure.com/home/). Then subscribe the [Microsoft Translator](https://datamarket.azure.com/dataset/bing/microsofttranslator) data. Registered your own application and get the client ID and client secret. Change the following definitions in `app/src/main/java/me/jmchen/Constants.java`.
```java
    public static final String CLIENT_ID_VALUE = "me_jmchen_speechtotext";
    public static final String CLIENT_SECRET_VALUE = "TBA";
```


## Credits

SpeechToText was created by [Jianming Chen](http://jmchen.me/), under the guidance of [Tim Buchalka's course](https://www.udemy.com/android-marshmallow-java-app-development-course/) (Section 16).


## License

SpeechToText is available under the MIT license. See the [LICENSE](./LICENSE) file for more info.
