package me.jmchen.speechtotext;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import com.microsoft.projectoxford.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.projectoxford.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.projectoxford.speechrecognition.RecognitionResult;
import com.microsoft.projectoxford.speechrecognition.RecognitionStatus;
import com.microsoft.projectoxford.speechrecognition.SpeechRecognitionMode;
import com.microsoft.projectoxford.speechrecognition.SpeechRecognitionServiceFactory;
import com.microsoft.speech.tts.Synthesizer;
import com.microsoft.speech.tts.Voice;

public class MainActivity extends AppCompatActivity implements ISpeechRecognitionServerEvents {

    public static final String LOG_TAG = "SpeechToText";
    private MicrophoneRecognitionClient mMicClient;
    private SpeechRecognitionMode mSpeechMode = SpeechRecognitionMode.ShortPhrase;

    private String mLanguageCode = Constants.LANGUAGE_CODES[0];
    private Language mLanguageTranslation = Constants.LANGUAGES[0];
    private String mKey = Constants.PRIMARY_SUBSCRIPTION_KEY;

    private TextView mResultText;
    private FloatingActionButton mFab;

    private ItemAdapter mItemAdapter = new ItemAdapter(this);
    private View mSuggestionLayout;

    private int onlineIcon;
    private int busyIcon;

    private boolean mHasStartedRecording = false;
    private boolean mHasOptionChanged = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mResultText = (TextView) findViewById(R.id.resultText);
        mSuggestionLayout = findViewById(R.id.suggestionLayout);

        onlineIcon = getResources().getIdentifier("@android:drawable/presence_audio_online", null, null);
        busyIcon = getResources().getIdentifier("@android:drawable/ic_voice_search", null, null);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasInternetConnection()) {
                    mResultText.setText("");
                    mSuggestionLayout.setVisibility(View.GONE);
                    initRecording();
                    if (mMicClient != null) {
                        if (mSpeechMode.equals(SpeechRecognitionMode.ShortPhrase)) {
                            if (!mHasStartedRecording) {
                                mMicClient.startMicAndRecognition();
                            }
                        } else {
                            if (!mHasStartedRecording) {
                                mMicClient.startMicAndRecognition();
                            } else {
                                mMicClient.endMicAndRecognition();
                            }
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.check_connection), Toast.LENGTH_LONG).show();
                }
            }
        });

        initLanguageSpinner();
        initSpeechModeSpinner();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mResultText", mResultText.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mResultText.setText(savedInstanceState.getString("mResultText"));
    }

    @Override
    public void onPartialResponseReceived(String s) {
        mResultText.append("PARTIAL RESULT:\n");
        mResultText.append(s + "\n");
    }

    @Override
    public void onFinalResponseReceived(RecognitionResult recognitionResult) {
        // explanation of results at https://msdn.microsoft.com/en-us/library/mt613453.aspx
        mResultText.setText("");
        boolean isFinalDictationMessage = (
                mSpeechMode == SpeechRecognitionMode.LongDictation &&
                        (recognitionResult.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                                recognitionResult.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout ||
                                recognitionResult.RecognitionStatus == RecognitionStatus.RecognitionSuccess)
        );
        if (mSpeechMode == SpeechRecognitionMode.ShortPhrase || isFinalDictationMessage) {
            if (mMicClient != null) {
                mMicClient.endMicAndRecognition();
            }
            mFab.setEnabled(true);
            mFab.setImageResource(onlineIcon);
        }

        if (recognitionResult.Results.length > 0) {
            ListView listView = (ListView) findViewById(R.id.resultList);
            listView.setAdapter(mItemAdapter);
            mSuggestionLayout.setVisibility(View.VISIBLE);
            for (int i = 0; i < recognitionResult.Results.length; i++) {
                mItemAdapter.addItem(recognitionResult.Results[i].DisplayText);
            }

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    final Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.dialog_content);
                    ListView translationList = (ListView) dialog.findViewById(R.id.translation_list);
                    final ItemAdapter translationAdapter = new ItemAdapter(MainActivity.this);
                    translationAdapter.setItems(getResources().getStringArray(R.array.languages));
                    translationList.setAdapter(translationAdapter);
                    translationAdapter.setSelected(SharedPreferenceUtils.getConvertLanguageIndex(MainActivity.this));
                    // Initialise the translation language to store the preference
                    mLanguageTranslation = Constants.LANGUAGES[SharedPreferenceUtils.getConvertLanguageIndex(MainActivity.this)];

                    translationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            mLanguageTranslation = Constants.LANGUAGES[position];
                            SharedPreferenceUtils.updateConvertLanguageIndex(MainActivity.this, position);
                            translationAdapter.setSelected(position);
                        }
                    });

                    dialog.findViewById(R.id.translate_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            mResultText.setText("");
                            new TranslationTask(
                                    Constants.LANGUAGES[SharedPreferenceUtils.getBaseLanguageIndex(MainActivity.this)],
                                    mLanguageTranslation,
                                    (String) mItemAdapter.getItem(position)
                            ).execute();
                        }
                    });

                    dialog.findViewById(R.id.cancel_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.setCancelable(true);
                    dialog.setTitle(getString(R.string.dialog_title));
                    dialog.show();
                }
            });
        }
    }

    @Override
    public void onIntentReceived(String s) {

    }

    @Override
    public void onError(int i, String s) {
        mFab.setEnabled(true);
        mFab.setImageResource(onlineIcon);
        Toast.makeText(this, getString(R.string.internet_error_text), Toast.LENGTH_LONG).show();
        mResultText.append("Error " + i + ": " + s + "\n");
        mMicClient = null;  // force an initialization when recording next time
        mKey = Constants.SECONDARY_SUBSCRIPTION_KEY;
    }

    @Override
    public void onAudioEvent(boolean b) {
        mHasStartedRecording = b;
        if (!b) {
            if (mMicClient != null) {
                mMicClient.endMicAndRecognition();
            }
            mFab.setEnabled(true);
            mFab.setImageResource(onlineIcon);
        } else {
            if (mSpeechMode == SpeechRecognitionMode.ShortPhrase) {
                mFab.setEnabled(false);
            }
            mFab.setImageResource(busyIcon);
        }
        mResultText.append(b ? getString(R.string.recording_start) : getString(R.string.recording_end));

    }

    private boolean hasInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager != null &&
                connectivityManager.getActiveNetworkInfo() != null &&
                connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void initLanguageSpinner() {
        final Spinner spinner = (Spinner) findViewById(R.id.language_spinner);
        spinner.setSaveEnabled(true);
        spinner.setSelection(SharedPreferenceUtils.getBaseLanguageIndex(this));
        mLanguageCode = Constants.LANGUAGE_CODES[SharedPreferenceUtils.getBaseLanguageIndex(this)];

        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(LOG_TAG, "in Language Spinner onItemSelected");
                        mLanguageCode = Constants.LANGUAGE_CODES[position];
                        mHasOptionChanged = true;
                        SharedPreferenceUtils.updateBaseLanguageIndex(MainActivity.this, position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });
    }

    private void initSpeechModeSpinner() {
        final Spinner spinner = (Spinner) findViewById(R.id.speech_mode_spinner);
        spinner.setSaveEnabled(true);
        int pref = SharedPreferenceUtils.getSpeechModeIndex(this);
        spinner.setSelection(pref);
        mSpeechMode = (pref == 0) ? SpeechRecognitionMode.ShortPhrase : SpeechRecognitionMode.LongDictation;

        spinner.post(new Runnable() {
            @Override
            public void run() {
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(LOG_TAG, "In Speech Mode onItemSelected");
                        mSpeechMode = (position == 0) ? SpeechRecognitionMode.ShortPhrase : SpeechRecognitionMode.LongDictation;
                        mHasOptionChanged = true;
                        SharedPreferenceUtils.updateSpeechModeIndex(MainActivity.this, position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });
    }

    private void initRecording() {
        if (mHasOptionChanged || (mMicClient == null)) {
            Log.d(LOG_TAG, "Language is " + mLanguageCode + "\nSpeech mode is " + mSpeechMode);
            if (mKey.equals(Constants.PRIMARY_SUBSCRIPTION_KEY)) {
                mResultText.append(getString(R.string.primary_connect));
            } else {
                mResultText.append(getString(R.string.secondary_connect));
            }
            mMicClient = SpeechRecognitionServiceFactory.createMicrophoneClient(this, mSpeechMode, mLanguageCode,this, mKey);
            mHasOptionChanged = false;
        }
        // Discard previous items
        mItemAdapter.clear();
        // And hid the speaker button
        ImageButton speakButton = (ImageButton) findViewById(R.id.speak_button);
        if (speakButton != null) {
            speakButton.setVisibility(View.GONE);
        }
    }

    private class TranslationTask extends AsyncTask<Void, Void, Void> {
        private final Language baseLanguage;
        private final Language convertLanguage;
        private final String word;
        private String translatedText = "";

        public TranslationTask(Language baseLanguage, Language convertLanguage, String word) {
            this.baseLanguage = baseLanguage;
            this.convertLanguage = convertLanguage;
            this.word = word;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mResultText.append("Word Selected: " + word);
            mResultText.append(getString(R.string.translation_start));
        }

        @Override
        protected Void doInBackground(Void... params) {
            Translate.setClientId(Constants.CLIENT_ID_VALUE);
            Translate.setClientSecret(Constants.CLIENT_SECRET_VALUE);
            try {
                translatedText = Translate.execute(word, baseLanguage, convertLanguage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mResultText.setText(getString(R.string.translation_heading));
            mResultText.append(translatedText);

            // Set up the click listener for the speak button
            ImageButton speakButton = (ImageButton) findViewById(R.id.speak_button);
            if (speakButton != null) {
                speakButton.setVisibility(View.VISIBLE);
                speakButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Get the language code that the translation is in
                        String speechLanguage = Constants.LANGUAGE_CODES[SharedPreferenceUtils.getConvertLanguageIndex(MainActivity.this)];
                        Log.d(LOG_TAG, "Speech language is " + speechLanguage);
                        Synthesizer synthesizer = new Synthesizer(getString(R.string.app_name), Constants.PRIMARY_SUBSCRIPTION_KEY);
                        Voice voice = Voices.getVoice(speechLanguage, 0);
                        if (voice != null) {
                            Log.d(LOG_TAG, voice.voiceName);
                            synthesizer.SetVoice(voice, voice);
                            Log.d(LOG_TAG, "Speaking: " + translatedText);
                            synthesizer.SpeakToAudio(translatedText);
                        }
                    }
                });
            }
        }
    }
}
