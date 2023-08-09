public class VoiceDialerActivity extends Activity {
    private static final String TAG = "VoiceDialerActivity";
    private static final String MICROPHONE_EXTRA = "microphone";
    private static final String CONTACTS_EXTRA = "contacts";
    private static final String SAMPLE_RATE_EXTRA = "samplerate";
    private static final String INTENTS_KEY = "intents";
    private static final int FAIL_PAUSE_MSEC = 5000;
    private static final int SAMPLE_RATE = 11025;
    private static final int DIALOG_ID = 1;
    private final static CommandRecognizerEngine mCommandEngine =
            new CommandRecognizerEngine();
    private CommandRecognizerClient mCommandClient;
    private VoiceDialerTester mVoiceDialerTester;
    private Handler mHandler;
    private Thread mRecognizerThread = null;
    private AudioManager mAudioManager;
    private ToneGenerator mToneGenerator;
    private AlertDialog mAlertDialog;
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
        if (Config.LOGD) Log.d(TAG, "onStart "  + getIntent());
        super.onStart();
        mAudioManager.requestAudioFocus(
                null, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        mCommandEngine.setContactsFile(newFile(getArg(CONTACTS_EXTRA)));
        mCommandClient = new CommandRecognizerClient();
        mCommandEngine.setMinimizeResults(false);
        mCommandEngine.setAllowOpenEntries(true);
        setTheme(android.R.style.Theme_Dialog);
        setTitle(R.string.title);
        setContentView(R.layout.voice_dialing);
        findViewById(R.id.microphone_view).setVisibility(View.INVISIBLE);
        findViewById(R.id.retry_view).setVisibility(View.INVISIBLE);
        findViewById(R.id.microphone_loading_view).setVisibility(View.VISIBLE);
        if (RecognizerLogger.isEnabled(this)) {
            ((TextView)findViewById(R.id.substate)).setText(R.string.logging_enabled);
        }
        mVoiceDialerTester = null;
        File micDir = newFile(getArg(MICROPHONE_EXTRA));
        if (micDir != null && micDir.isDirectory()) {
            mVoiceDialerTester = new VoiceDialerTester(micDir);
            startNextTest();
            return;
        }
        startWork();
    }
    private void startWork() {
        mRecognizerThread = new Thread() {
            public void run() {
                if (Config.LOGD) Log.d(TAG, "onCreate.Runnable.run");
                String sampleRateStr = getArg(SAMPLE_RATE_EXTRA);
                int sampleRate = SAMPLE_RATE;
                if (sampleRateStr != null) {
                    sampleRate = Integer.parseInt(sampleRateStr);
                }
                mCommandEngine.recognize(mCommandClient, VoiceDialerActivity.this,
                        newFile(getArg(MICROPHONE_EXTRA)),
                        sampleRate);
            }
        };
        mRecognizerThread.start();
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
    private void startNextTest() {
        mHandler.postDelayed(new Runnable() {
            public void run() {
                if (mVoiceDialerTester == null) {
                    return;
                }
                if (!mVoiceDialerTester.stepToNextTest()) {
                    mVoiceDialerTester.report();
                    notifyText("Test completed!");
                    finish();
                    return;
                }
                File microphone = mVoiceDialerTester.getWavFile();
                File contacts = newFile(getArg(CONTACTS_EXTRA));
                notifyText("Testing\n" + microphone + "\n" + contacts);
                mCommandEngine.recognize(mCommandClient, VoiceDialerActivity.this,
                        microphone, SAMPLE_RATE);
            }
        }, 2000);
    }
    private int playSound(int toneType) {
        int msecDelay = 1;
        if (mToneGenerator != null) {
            mToneGenerator.startTone(toneType);
            msecDelay = StrictMath.max(msecDelay, 300);
        }
        if ((mAudioManager != null) &&
                (mAudioManager.shouldVibrate(AudioManager.VIBRATE_TYPE_RINGER))) {
            final int VIBRATOR_TIME = 150;
            final int VIBRATOR_GUARD_TIME = 150;
            Vibrator vibrator = new Vibrator();
            vibrator.vibrate(VIBRATOR_TIME);
            msecDelay = StrictMath.max(msecDelay,
                    VIBRATOR_TIME + VIBRATOR_GUARD_TIME);
        }
        return msecDelay;
    }
    @Override
    protected void onStop() {
        if (Config.LOGD) Log.d(TAG, "onStop");
        mAudioManager.abandonAudioFocus(null);
        mVoiceDialerTester = null;
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
        if (mToneGenerator != null) {
            mToneGenerator.release();
            mToneGenerator = null;
        }
        super.onStop();
        finish();
    }
    private void notifyText(final CharSequence msg) {
        Toast.makeText(VoiceDialerActivity.this, msg, Toast.LENGTH_SHORT).show();
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
    protected Dialog onCreateDialog(int id, Bundle args) {
        final Intent intents[] = (Intent[])args.getParcelableArray(INTENTS_KEY);
        DialogInterface.OnClickListener clickListener =
            new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (Config.LOGD) Log.d(TAG, "clickListener.onClick " + which);
                startActivityHelp(intents[which]);
                dismissDialog(DIALOG_ID);
                mAlertDialog = null;
                finish();
            }
        };
        DialogInterface.OnCancelListener cancelListener =
            new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                if (Config.LOGD) Log.d(TAG, "cancelListener.onCancel");
                dismissDialog(DIALOG_ID);
                mAlertDialog = null;
                finish();
            }
        };
        DialogInterface.OnClickListener positiveListener =
            new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (Config.LOGD) Log.d(TAG, "positiveListener.onClick " + which);
                if (intents.length == 1 && which == -1) which = 0;
                startActivityHelp(intents[which]);
                dismissDialog(DIALOG_ID);
                mAlertDialog = null;
                finish();
            }
        };
        DialogInterface.OnClickListener negativeListener =
            new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (Config.LOGD) Log.d(TAG, "negativeListener.onClick " + which);
                dismissDialog(DIALOG_ID);
                mAlertDialog = null;
                finish();
            }
        };
        String[] sentences = new String[intents.length];
        for (int i = 0; i < intents.length; i++) {
            sentences[i] = intents[i].getStringExtra(
                    RecognizerEngine.SENTENCE_EXTRA);
        }
        mAlertDialog = intents.length > 1 ?
                new AlertDialog.Builder(VoiceDialerActivity.this)
                .setTitle(R.string.title)
                .setItems(sentences, clickListener)
                .setOnCancelListener(cancelListener)
                .setNegativeButton(android.R.string.cancel, negativeListener)
                .show()
                :
                new AlertDialog.Builder(VoiceDialerActivity.this)
                .setTitle(R.string.title)
                .setItems(sentences, clickListener)
                .setOnCancelListener(cancelListener)
                .setPositiveButton(android.R.string.ok, positiveListener)
                .setNegativeButton(android.R.string.cancel, negativeListener)
                .show();
        return mAlertDialog;
    }
    private class CommandRecognizerClient implements RecognizerClient {
        static final int MIN_VOLUME_TO_SKIP = 2;
        public void onMicrophoneStart(InputStream mic) {
            if (Config.LOGD) Log.d(TAG, "onMicrophoneStart");
            playSound(ToneGenerator.TONE_PROP_BEEP);
            int ringVolume = mAudioManager.getStreamVolume(
                    AudioManager.STREAM_RING);
            Log.d(TAG, "ringVolume " + ringVolume);
            if (ringVolume >= MIN_VOLUME_TO_SKIP) {
                try {
                    skipBeep(mic);
                } catch (java.io.IOException e) {
                    Log.e(TAG, "IOException " + e);
                }
            } else {
                Log.d(TAG, "no tone");
            }
            if (mVoiceDialerTester != null) return;
            mHandler.post(new Runnable() {
                public void run() {
                    findViewById(R.id.microphone_loading_view).setVisibility(View.INVISIBLE);
                    ((TextView)findViewById(R.id.state)).setText(R.string.listening);
                    mHandler.post(mMicFlasher);
                }
            });
        }
        private static final int START_WINDOW_MS = 500;  
        private static final int SINE_FREQ = 400;        
        private static final int NUM_PERIODS_BLOCK = 10; 
        private static final int THRESHOLD = 8;          
        private static final int START = 0;              
        private static final int RISING = 1;             
        private static final int TOP = 2;                
        void skipBeep(InputStream is) throws IOException {
            int sampleCount = ((SAMPLE_RATE / SINE_FREQ) * NUM_PERIODS_BLOCK);
            int blockSize = 2 * sampleCount; 
            if (is == null || blockSize == 0) {
                return;
            }
            byte[] buf = new byte[blockSize];
            int maxBytes = 2 * ((START_WINDOW_MS * SAMPLE_RATE) / 1000);
            maxBytes = ((maxBytes-1) / blockSize + 1) * blockSize;
            int count = 0;
            int state = START;  
            long prevE = 0; 
            long peak = 0;
            int threshold =  THRESHOLD*sampleCount;  
            Log.d(TAG, "blockSize " + blockSize);
            while (count < maxBytes) {
                int cnt = 0;
                while (cnt < blockSize) {
                    int n = is.read(buf, cnt, blockSize-cnt);
                    if (n < 0) {
                        throw new java.io.IOException();
                    }
                    cnt += n;
                }
                cnt = blockSize;
                long sumx = 0;
                long sumxx = 0;
                while (cnt >= 2) {
                    short smp = (short)((buf[cnt - 1] << 8) + (buf[cnt - 2] & 0xFF));
                    sumx += smp;
                    sumxx += smp*smp;
                    cnt -= 2;
                }
                long energy = (sumxx*sampleCount - sumx*sumx)/(sampleCount*sampleCount);
                Log.d(TAG, "sumx " + sumx + " sumxx " + sumxx + " ee " + energy);
                switch (state) {
                    case START:
                        if (energy > threshold && energy > (prevE * 2) && prevE != 0) {
                            state = RISING;
                            if (Config.LOGD) Log.d(TAG, "start RISING: " + count +" time: "+ (((1000*count)/2)/SAMPLE_RATE));
                        }
                        break;
                    case RISING:
                        if (energy < threshold || energy < (prevE / 2)){
                            if (Config.LOGD) Log.d(TAG, "back to START: " + count +" time: "+ (((1000*count)/2)/SAMPLE_RATE));
                            peak = 0;
                            state = START;
                        } else if (energy > (prevE / 2) && energy < (prevE * 2)) {
                            if (Config.LOGD) Log.d(TAG, "start TOP: " + count +" time: "+ (((1000*count)/2)/SAMPLE_RATE));
                            if (peak < energy) {
                                peak = energy;
                            }
                            state = TOP;
                        }
                        break;
                    case TOP:
                        if (energy < threshold || energy < (peak / 2)) {
                            if (Config.LOGD) Log.d(TAG, "end TOP: " + count +" time: "+ (((1000*count)/2)/SAMPLE_RATE));
                            return;
                        }
                        break;
                    }
                prevE = energy;
                count += blockSize;
            }
            if (Config.LOGD) Log.d(TAG, "no beep detected, timed out");
        }
        public void onRecognitionFailure(final String msg) {
            if (Config.LOGD) Log.d(TAG, "onRecognitionFailure " + msg);
            mHandler.post(new Runnable() {
                public void run() {
                    playSound(ToneGenerator.TONE_PROP_NACK);
                    mHandler.removeCallbacks(mMicFlasher);
                    ((TextView)findViewById(R.id.state)).setText(R.string.please_try_again);
                    findViewById(R.id.state).setVisibility(View.VISIBLE);
                    findViewById(R.id.microphone_view).setVisibility(View.INVISIBLE);
                    findViewById(R.id.retry_view).setVisibility(View.VISIBLE);
                    if (mVoiceDialerTester != null) {
                        mVoiceDialerTester.onRecognitionFailure(msg);
                        startNextTest();
                        return;
                    }
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, FAIL_PAUSE_MSEC);
                }
            });
        }
        public void onRecognitionError(final String msg) {
            if (Config.LOGD) Log.d(TAG, "onRecognitionError " + msg);
            mHandler.post(new Runnable() {
                public void run() {
                    playSound(ToneGenerator.TONE_PROP_NACK);
                    mHandler.removeCallbacks(mMicFlasher);
                    ((TextView)findViewById(R.id.state)).setText(R.string.please_try_again);
                    ((TextView)findViewById(R.id.substate)).setText(R.string.recognition_error);
                    findViewById(R.id.state).setVisibility(View.VISIBLE);
                    findViewById(R.id.microphone_view).setVisibility(View.INVISIBLE);
                    findViewById(R.id.retry_view).setVisibility(View.VISIBLE);
                    if (mVoiceDialerTester != null) {
                        mVoiceDialerTester.onRecognitionError(msg);
                        startNextTest();
                        return;
                    }
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, FAIL_PAUSE_MSEC);
                }
            });
        }
        public void onRecognitionSuccess(final Intent[] intents) {
            if (Config.LOGD) Log.d(TAG, "onRecognitionSuccess " + intents.length);
            final Bundle args = new Bundle();
            args.putParcelableArray(INTENTS_KEY, intents);
            mHandler.post(new Runnable() {
                public void run() {
                    playSound(ToneGenerator.TONE_PROP_ACK);
                    mHandler.removeCallbacks(mMicFlasher);
                    showDialog(DIALOG_ID, args);
                    if (mVoiceDialerTester != null) {
                        mVoiceDialerTester.onRecognitionSuccess(intents);
                        startNextTest();
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                dismissDialog(DIALOG_ID);
                                mAlertDialog = null;
                            }
                        }, 2000);
                    }
                }
            });
        }
    }
    private void startActivityHelp(Intent intent) {
        if (getArg(MICROPHONE_EXTRA) == null &&
                getArg(CONTACTS_EXTRA) == null) {
            startActivity(intent);
        } else {
            notifyText(intent.
                    getStringExtra(RecognizerEngine.SENTENCE_EXTRA) +
                    "\n" + intent.toString());
        }
    }
    @Override
    protected void onDestroy() {
        if (Config.LOGD) Log.d(TAG, "onDestroy");
        super.onDestroy();
    }
}
