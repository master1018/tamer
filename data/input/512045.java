public class BluetoothVoiceDialerActivity extends Activity {
    private static final String TAG = "VoiceDialerActivity";
    private static final String MICROPHONE_EXTRA = "microphone";
    private static final String CONTACTS_EXTRA = "contacts";
    private static final String SPEAK_NOW_UTTERANCE = "speak_now";
    private static final String TRY_AGAIN_UTTERANCE = "try_again";
    private static final String CHOSEN_ACTION_UTTERANCE = "chose_action";
    private static final String GOODBYE_UTTERANCE = "goodbye";
    private static final String CHOICES_UTTERANCE = "choices";
    private static final int FIRST_UTTERANCE_DELAY = 300;
    private static final int MAX_TTS_DELAY = 6000;
    private static final int SAMPLE_RATE = 8000;
    private static final int INITIALIZING = 0;
    private static final int SPEAKING_GREETING = 1;
    private static final int WAITING_FOR_COMMAND = 2;
    private static final int SPEAKING_TRY_AGAIN = 3;
    private static final int SPEAKING_CHOICES = 4;
    private static final int WAITING_FOR_CHOICE = 5;
    private static final int SPEAKING_CHOSEN_ACTION = 6;
    private static final int SPEAKING_GOODBYE = 7;
    private static final int EXITING = 8;
    private static final CommandRecognizerEngine mCommandEngine =
            new CommandRecognizerEngine();
    private static final PhoneTypeChoiceRecognizerEngine mPhoneTypeChoiceEngine =
            new PhoneTypeChoiceRecognizerEngine();
    private CommandRecognizerClient mCommandClient;
    private ChoiceRecognizerClient mChoiceClient;
    private ToneGenerator mToneGenerator;
    private Handler mHandler;
    private Thread mRecognizerThread = null;
    private AudioManager mAudioManager;
    private BluetoothHeadset mBluetoothHeadset;
    private TextToSpeech mTts;
    private HashMap<String, String> mTtsParams;
    private VoiceDialerBroadcastReceiver mReceiver;
    private int mBluetoothAudioState;
    private boolean mWaitingForTts;
    private boolean mWaitingForScoConnection;
    private Intent[] mAvailableChoices;
    private Intent mChosenAction;
    private int mBluetoothVoiceVolume;
    private int mState;
    private AlertDialog mAlertDialog;
    private Runnable mFallbackRunnable;
    private WakeLock mWakeLock;
    @Override
    protected void onCreate(Bundle icicle) {
        if (Config.LOGD) Log.d(TAG, "onCreate");
        super.onCreate(icicle);
        mHandler = new Handler();
        mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        mToneGenerator = new ToneGenerator(AudioManager.STREAM_RING,
                ToneGenerator.MAX_VOLUME);
    }
    protected void onStart() {
        if (Config.LOGD) Log.d(TAG, "onStart " + getIntent());
        super.onStart();
        acquireWakeLock(this);
        mState = INITIALIZING;
        mChosenAction = null;
        mAudioManager.requestAudioFocus(
                null, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        int flags = WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        getWindow().addFlags(flags);
        setTheme(android.R.style.Theme_Dialog);
        setTitle(R.string.bluetooth_title);
        setContentView(R.layout.voice_dialing);
        findViewById(R.id.microphone_view).setVisibility(View.INVISIBLE);
        findViewById(R.id.retry_view).setVisibility(View.INVISIBLE);
        findViewById(R.id.microphone_loading_view).setVisibility(View.VISIBLE);
        if (RecognizerLogger.isEnabled(this)) {
            ((TextView) findViewById(R.id.substate)).setText(R.string.logging_enabled);
        }
        IntentFilter audioStateFilter;
        audioStateFilter = new IntentFilter();
        audioStateFilter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        mReceiver = new VoiceDialerBroadcastReceiver();
        registerReceiver(mReceiver, audioStateFilter);
        mCommandEngine.setContactsFile(newFile(getArg(CONTACTS_EXTRA)));
        mCommandEngine.setMinimizeResults(true);
        mCommandEngine.setAllowOpenEntries(false);
        mCommandClient = new CommandRecognizerClient();
        mChoiceClient = new ChoiceRecognizerClient();
        mBluetoothAudioState = BluetoothHeadset.STATE_ERROR;
        if (BluetoothHeadset.isBluetoothVoiceDialingEnabled(this)) {
            mWaitingForScoConnection = true;
            mBluetoothHeadset = new BluetoothHeadset(this,
                    mBluetoothHeadsetServiceListener);
            mWaitingForTts = true;
            mTts = new TextToSpeech(this, new TtsInitListener());
            mTtsParams = new HashMap<String, String>();
            mTtsParams.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                    String.valueOf(AudioManager.STREAM_BLUETOOTH_SCO));
        } else {
            finish();
        }
    }
    class ErrorRunnable implements Runnable {
        private int mErrorMsg;
        public ErrorRunnable(int errorMsg) {
            mErrorMsg = errorMsg;
        }
        public void run() {
            mHandler.removeCallbacks(mMicFlasher);
            ((TextView)findViewById(R.id.state)).setText(R.string.failure);
            ((TextView)findViewById(R.id.substate)).setText(mErrorMsg);
            ((TextView)findViewById(R.id.substate)).setText(
                    R.string.headset_connection_lost);
            findViewById(R.id.microphone_view).setVisibility(View.INVISIBLE);
            findViewById(R.id.retry_view).setVisibility(View.VISIBLE);
            playSound(ToneGenerator.TONE_PROP_NACK);
        }
    }
    class FallbackRunnable implements Runnable {
        public void run() {
            Log.e(TAG, "utterance completion not delivered, using fallback");
            onSpeechCompletion();
        }
    }
    class GreetingRunnable implements Runnable {
        public void run() {
            mState = SPEAKING_GREETING;
            mTtsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                    SPEAK_NOW_UTTERANCE);
            mTts.speak(getString(R.string.speak_now_tts),
                TextToSpeech.QUEUE_FLUSH,
                mTtsParams);
            mFallbackRunnable = new FallbackRunnable();
            mHandler.postDelayed(mFallbackRunnable, MAX_TTS_DELAY);
        }
    }
    class TtsInitListener implements TextToSpeech.OnInitListener {
        public void onInit(int status) {
            if (Config.LOGD) Log.d(TAG, "onInit for tts");
            if (status != TextToSpeech.SUCCESS) {
                Log.e(TAG, "Could not initialize TextToSpeech.");
                mHandler.post(new ErrorRunnable(R.string.recognition_error));
                exitActivity();
                return;
            }
            if (mTts == null) {
                Log.e(TAG, "null tts");
                mHandler.post(new ErrorRunnable(R.string.recognition_error));
                exitActivity();
                return;
            }
            mWaitingForTts = false;
            mTts.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener());
            mBluetoothVoiceVolume = mAudioManager.getStreamVolume(
                    AudioManager.STREAM_BLUETOOTH_SCO);
            int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_BLUETOOTH_SCO);
            int volume = maxVolume - ((18 / (50/maxVolume)) + 1);
            if (mBluetoothVoiceVolume > volume) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO, volume, 0);
            }
            if (mWaitingForScoConnection) {
            } else {
                mHandler.postDelayed(new GreetingRunnable(), FIRST_UTTERANCE_DELAY);
            }
        }
    }
    class OnUtteranceCompletedListener
            implements TextToSpeech.OnUtteranceCompletedListener {
        public void onUtteranceCompleted(String utteranceId) {
            Log.d(TAG, "onUtteranceCompleted " + utteranceId);
            mHandler.removeCallbacks(mFallbackRunnable);
            mFallbackRunnable = null;
            mHandler.post(new Runnable() {
                public void run() {
                    onSpeechCompletion();
                }
            });
        }
    }
    private void onSpeechCompletion() {
        if (mState == SPEAKING_GREETING || mState == SPEAKING_TRY_AGAIN) {
            listenForCommand();
        } else if (mState == SPEAKING_CHOICES) {
            listenForChoice();
        } else if (mState == SPEAKING_GOODBYE) {
            mState = EXITING;
            finish();
        } else if (mState == SPEAKING_CHOSEN_ACTION) {
            mState = EXITING;
            startActivityHelp(mChosenAction);
            finish();
        }
    }
    private BluetoothHeadset.ServiceListener mBluetoothHeadsetServiceListener =
            new BluetoothHeadset.ServiceListener() {
        public void onServiceConnected() {
            if (mBluetoothHeadset != null &&
                    mBluetoothHeadset.getState() == BluetoothHeadset.STATE_CONNECTED) {
                mBluetoothHeadset.startVoiceRecognition();
            }
            if (Config.LOGD) Log.d(TAG, "onServiceConnected");
        }
        public void onServiceDisconnected() {}
    };
    private class VoiceDialerBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
                mBluetoothAudioState = intent.getIntExtra(
                        BluetoothHeadset.EXTRA_AUDIO_STATE,
                        BluetoothHeadset.STATE_ERROR);
                if (Config.LOGD) Log.d(TAG, "HEADSET AUDIO_STATE_CHANGED -> " +
                        mBluetoothAudioState);
                if (mBluetoothAudioState == BluetoothHeadset.AUDIO_STATE_CONNECTED &&
                    mWaitingForScoConnection) {
                    mWaitingForScoConnection = false;
                    if (mWaitingForTts) {
                    } else {
                        mHandler.postDelayed(new GreetingRunnable(), FIRST_UTTERANCE_DELAY);
                    }
                } else {
                    if (!mWaitingForScoConnection) {
                        if (Config.LOGD) Log.d(TAG, "lost sco connection");
                        mHandler.post(new ErrorRunnable(
                                R.string.headset_connection_lost));
                        exitActivity();
                    }
                }
            }
        }
    }
    private class CommandRecognizerClient implements RecognizerClient {
        public void onMicrophoneStart(InputStream mic) {
            if (Config.LOGD) Log.d(TAG, "onMicrophoneStart");
            mHandler.post(new Runnable() {
                public void run() {
                    findViewById(R.id.retry_view).setVisibility(View.INVISIBLE);
                    findViewById(R.id.microphone_loading_view).setVisibility(
                            View.INVISIBLE);
                    ((TextView)findViewById(R.id.state)).setText(R.string.listening);
                    mHandler.post(mMicFlasher);
                }
            });
        }
        public void onRecognitionFailure(final String msg) {
            if (Config.LOGD) Log.d(TAG, "onRecognitionFailure " + msg);
            askToTryAgain();
        }
        public void onRecognitionError(final String msg) {
            if (Config.LOGD) Log.d(TAG, "onRecognitionError " + msg);
            mHandler.post(new ErrorRunnable(R.string.recognition_error));
            exitActivity();
        }
        private void askToTryAgain() {
            mHandler.post(new Runnable() {
                public void run() {
                    if (mAlertDialog != null) {
                        mAlertDialog.dismiss();
                    }
                    mState = SPEAKING_TRY_AGAIN;
                    mTtsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                            TRY_AGAIN_UTTERANCE);
                    mTts.speak(getString(R.string.no_results_tts),
                        TextToSpeech.QUEUE_FLUSH,
                        mTtsParams);
                    mHandler.removeCallbacks(mMicFlasher);
                    ((TextView)findViewById(R.id.state)).setText(R.string.please_try_again);
                    findViewById(R.id.state).setVisibility(View.VISIBLE);
                    findViewById(R.id.microphone_view).setVisibility(View.INVISIBLE);
                    findViewById(R.id.retry_view).setVisibility(View.VISIBLE);
                }
            });
        }
        public void onRecognitionSuccess(final Intent[] intents) {
            if (Config.LOGD) Log.d(TAG, "onRecognitionSuccess " + intents.length);
            mAvailableChoices = intents;
            mHandler.post(new Runnable() {
                public void run() {
                    mHandler.removeCallbacks(mMicFlasher);
                    String[] sentences = new String[intents.length];
                    for (int i = 0; i < intents.length; i++) {
                        sentences[i] = intents[i].getStringExtra(
                                RecognizerEngine.SENTENCE_EXTRA);
                    }
                    if (intents.length == 0) {
                        onRecognitionFailure("zero intents");
                        return;
                    }
                    if (intents.length > 0) {
                        String value = intents[0].getStringExtra(
                            RecognizerEngine.SEMANTIC_EXTRA);
                        if (Config.LOGD) Log.d(TAG, "value " + value);
                        if ("X".equals(value)) {
                            exitActivity();
                            return;
                        }
                    }
                    if ((intents.length == 1) ||
                            (!Intent.ACTION_CALL_PRIVILEGED.equals(
                                    intents[0].getAction()))) {
                        String sentenceSpoken = spaceOutDigits(
                                mAvailableChoices[0].getStringExtra(
                                    RecognizerEngine.SENTENCE_EXTRA));
                        mState = SPEAKING_CHOSEN_ACTION;
                        mTtsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                                CHOSEN_ACTION_UTTERANCE);
                        mTts.speak(sentenceSpoken,
                            TextToSpeech.QUEUE_FLUSH,
                            mTtsParams);
                        mChosenAction = intents[0];
                        mFallbackRunnable = new FallbackRunnable();
                        mHandler.postDelayed(mFallbackRunnable, MAX_TTS_DELAY);
                        return;
                    } else {
                        speakChoices();
                        mFallbackRunnable = new FallbackRunnable();
                        mHandler.postDelayed(mFallbackRunnable, MAX_TTS_DELAY);
                        DialogInterface.OnCancelListener cancelListener =
                            new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                if (Config.LOGD) {
                                    Log.d(TAG, "cancelListener.onCancel");
                                }
                                dialog.dismiss();
                                finish();
                            }
                       };
                        DialogInterface.OnClickListener clickListener =
                            new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (Config.LOGD) {
                                    Log.d(TAG, "clickListener.onClick " + which);
                                }
                                startActivityHelp(intents[which]);
                                dialog.dismiss();
                                finish();
                            }
                        };
                        DialogInterface.OnClickListener negativeListener =
                            new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (Config.LOGD) {
                                    Log.d(TAG, "negativeListener.onClick " +
                                        which);
                                }
                                dialog.dismiss();
                                finish();
                            }
                        };
                        mAlertDialog =
                                new AlertDialog.Builder(
                                        BluetoothVoiceDialerActivity.this)
                                .setTitle(R.string.title)
                                .setItems(sentences, clickListener)
                                .setOnCancelListener(cancelListener)
                                .setNegativeButton(android.R.string.cancel,
                                        negativeListener)
                                .show();
                    }
                }
            });
        }
    }
    private class ChoiceRecognizerClient implements RecognizerClient {
        public void onRecognitionSuccess(final Intent[] intents) {
            if (Config.LOGD) Log.d(TAG, "ChoiceRecognizerClient onRecognitionSuccess");
            if (mAlertDialog != null) {
                mAlertDialog.dismiss();
            }
            if (intents.length > 0) {
                String value = intents[0].getStringExtra(
                    RecognizerEngine.SEMANTIC_EXTRA);
                if (Config.LOGD) Log.d(TAG, "value " + value);
                if ("R".equals(value)) {
                    mHandler.post(new GreetingRunnable());
                } else if ("X".equals(value)) {
                    exitActivity();
                } else {
                    mChosenAction = null;
                    for (int i = 0; i < mAvailableChoices.length; i++) {
                        if (value.equalsIgnoreCase(
                                mAvailableChoices[i].getStringExtra(
                                        CommandRecognizerEngine.PHONE_TYPE_EXTRA))) {
                            mChosenAction = mAvailableChoices[i];
                        }
                    }
                    if (mChosenAction != null) {
                        mState = SPEAKING_CHOSEN_ACTION;
                        mTtsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                                CHOSEN_ACTION_UTTERANCE);
                        mTts.speak(mChosenAction.getStringExtra(
                                RecognizerEngine.SENTENCE_EXTRA),
                            TextToSpeech.QUEUE_FLUSH,
                            mTtsParams);
                        mFallbackRunnable = new FallbackRunnable();
                        mHandler.postDelayed(mFallbackRunnable, MAX_TTS_DELAY);
                    } else {
                        if (Config.LOGD) Log.d(TAG, "invalid choice" + value);
                        mTtsParams.remove(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                        mTts.speak(getString(R.string.invalid_choice_tts),
                            TextToSpeech.QUEUE_FLUSH,
                            mTtsParams);
                        speakChoices();
                        mFallbackRunnable = new FallbackRunnable();
                        mHandler.postDelayed(mFallbackRunnable, MAX_TTS_DELAY);
                    }
                }
            }
        }
        public void onRecognitionFailure(String msg) {
            if (Config.LOGD) Log.d(TAG, "ChoiceRecognizerClient onRecognitionFailure");
            exitActivity();
        }
        public void onRecognitionError(String err) {
            if (Config.LOGD) Log.d(TAG, "ChoiceRecognizerClient onRecognitionError");
            mHandler.post(new ErrorRunnable(R.string.recognition_error));
            exitActivity();
        }
        public void onMicrophoneStart(InputStream mic) {
            if (Config.LOGD) Log.d(TAG, "ChoiceRecognizerClient onMicrophoneStart");
        }
    }
    private void speakChoices() {
        if (Config.LOGD) Log.d(TAG, "speakChoices");
        mState = SPEAKING_CHOICES;
        String sentenceSpoken = spaceOutDigits(
                mAvailableChoices[0].getStringExtra(
                    RecognizerEngine.SENTENCE_EXTRA));
        StringBuilder builder = new StringBuilder();
        builder.append(sentenceSpoken);
        int count = mAvailableChoices.length;
        for (int i=1; i < count; i++) {
            if (i == count-1) {
                builder.append(" or ");
            } else {
                builder.append(" ");
            }
            String tmpSentence = mAvailableChoices[i].getStringExtra(
                    RecognizerEngine.SENTENCE_EXTRA);
            String[] words = tmpSentence.trim().split(" ");
            builder.append(words[words.length-1]);
        }
        mTtsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                CHOICES_UTTERANCE);
        mTts.speak(builder.toString(),
            TextToSpeech.QUEUE_ADD,
            mTtsParams);
    }
    private static String spaceOutDigits(String sentenceDisplay) {
        char buffer[] = sentenceDisplay.toCharArray();
        StringBuilder builder = new StringBuilder();
        boolean buildingNumber = false;
        int l = sentenceDisplay.length();
        for (int index = 0; index < l; index++) {
            char c = buffer[index];
            if (Character.isDigit(c)) {
                if (buildingNumber) {
                    builder.append(" ");
                }
                buildingNumber = true;
                builder.append(c);
            } else if (c == ' ') {
                if (buildingNumber) {
                    builder.append(",");
                } else {
                    builder.append(" ");
                }
            } else {
                buildingNumber = false;
                builder.append(c);
            }
        }
        return builder.toString();
    }
    private void startActivityHelp(Intent intent) {
        startActivity(intent);
    }
    private void listenForCommand() {
        if (Config.LOGD) Log.d(TAG, ""
                + "Command(): MICROPHONE_EXTRA: "+getArg(MICROPHONE_EXTRA)+
                ", CONTACTS_EXTRA: "+getArg(CONTACTS_EXTRA));
        mState = WAITING_FOR_COMMAND;
        mRecognizerThread = new Thread() {
            public void run() {
                mCommandEngine.recognize(mCommandClient,
                        BluetoothVoiceDialerActivity.this,
                        newFile(getArg(MICROPHONE_EXTRA)),
                        SAMPLE_RATE);
            }
        };
        mRecognizerThread.start();
    }
    private void listenForChoice() {
        if (Config.LOGD) Log.d(TAG, "listenForChoice(): MICROPHONE_EXTRA: " +
                getArg(MICROPHONE_EXTRA));
        mState = WAITING_FOR_CHOICE;
        mRecognizerThread = new Thread() {
            public void run() {
                mPhoneTypeChoiceEngine.recognize(mChoiceClient,
                        BluetoothVoiceDialerActivity.this,
                        newFile(getArg(MICROPHONE_EXTRA)), SAMPLE_RATE);
            }
        };
        mRecognizerThread.start();
    }
    private void exitActivity() {
        synchronized(this) {
            if (mState != EXITING) {
                if (Config.LOGD) Log.d(TAG, "exitActivity");
                mState = SPEAKING_GOODBYE;
                mTtsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                        GOODBYE_UTTERANCE);
                mTts.speak(getString(R.string.goodbye_tts),
                    TextToSpeech.QUEUE_FLUSH,
                    mTtsParams);
                mFallbackRunnable = new FallbackRunnable();
                mHandler.postDelayed(mFallbackRunnable, MAX_TTS_DELAY);
            }
        }
    }
    private String getArg(String name) {
        if (name == null) return null;
        String arg = getIntent().getStringExtra(name);
        if (arg != null) return arg;
        arg = SystemProperties.get("app.voicedialer." + name);
        return arg != null && arg.length() > 0 ? arg : null;
    }
    private static File newFile(String name) {
        return name != null ? new File(name) : null;
    }
    private int playSound(int toneType) {
        int msecDelay = 1;
        if (mToneGenerator != null) {
            mToneGenerator.startTone(toneType);
            msecDelay = StrictMath.max(msecDelay, 300);
        }
        if ((mAudioManager != null) && (mAudioManager.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER))) {
            final int VIBRATOR_TIME = 150;
            final int VIBRATOR_GUARD_TIME = 150;
            Vibrator vibrator = new Vibrator();
            vibrator.vibrate(VIBRATOR_TIME);
            msecDelay = StrictMath.max(msecDelay,
                    VIBRATOR_TIME + VIBRATOR_GUARD_TIME);
        }
        return msecDelay;
    }
    protected void onStop() {
        if (Config.LOGD) Log.d(TAG, "onStop");
        synchronized(this) {
            mState = EXITING;
        }
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO,
                                      mBluetoothVoiceVolume, 0);
        mAudioManager.abandonAudioFocus(null);
        if (mBluetoothHeadset != null) {
            mBluetoothHeadset.stopVoiceRecognition();
            mBluetoothHeadset.close();
            mBluetoothHeadset = null;
        }
        if (mRecognizerThread !=  null) {
            mRecognizerThread.interrupt();
            try {
                mRecognizerThread.join();
            } catch (InterruptedException e) {
                if (Config.LOGD) Log.d(TAG, "onStop mRecognizerThread.join exception " + e);
            }
            mRecognizerThread = null;
        }
        mHandler.removeCallbacks(mMicFlasher);
        mHandler.removeMessages(0);
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
            mTts = null;
        }
        unregisterReceiver(mReceiver);
        super.onStop();
        releaseWakeLock();
        finish();
    }
    private void acquireWakeLock(Context context) {
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BluetoothVoiceDialer");
            mWakeLock.acquire();
        }
    }
    private void releaseWakeLock() {
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
    private Runnable mMicFlasher = new Runnable() {
        int visible = View.VISIBLE;
        public void run() {
            findViewById(R.id.microphone_view).setVisibility(visible);
            findViewById(R.id.state).setVisibility(visible);
            visible = visible == View.VISIBLE ? View.INVISIBLE : View.VISIBLE;
            mHandler.postDelayed(this, 750);
        }
    };
    @Override
    protected void onDestroy() {
        if (Config.LOGD) Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
