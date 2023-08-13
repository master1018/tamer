public class DTMFTwelveKeyDialer implements
        SlidingDrawer.OnDrawerOpenListener,
        SlidingDrawer.OnDrawerCloseListener,
        View.OnTouchListener,
        View.OnKeyListener {
    private static final String LOG_TAG = "DTMFTwelveKeyDialer";
    private static final boolean DBG = (PhoneApp.DBG_LEVEL >= 2);
    private static final int PHONE_DISCONNECT = 100;
    private static final int DTMF_SEND_CNF = 101;
    private Phone mPhone;
    private ToneGenerator mToneGenerator;
    private Object mToneGeneratorLock = new Object();
    private boolean mDTMFToneEnabled;
    private int mDTMFToneType;
    private boolean mDTMFBurstCnfPending = false;
    private Queue<Character> mDTMFQueue = new LinkedList<Character>();
    private static final int DTMF_DURATION_MS = 120;
    private static final HashMap<Character, Integer> mToneMap =
        new HashMap<Character, Integer>();
    private static final HashMap<Integer, Character> mDisplayMap =
        new HashMap<Integer, Character>();
    static {
        mToneMap.put('1', ToneGenerator.TONE_DTMF_1);
        mToneMap.put('2', ToneGenerator.TONE_DTMF_2);
        mToneMap.put('3', ToneGenerator.TONE_DTMF_3);
        mToneMap.put('4', ToneGenerator.TONE_DTMF_4);
        mToneMap.put('5', ToneGenerator.TONE_DTMF_5);
        mToneMap.put('6', ToneGenerator.TONE_DTMF_6);
        mToneMap.put('7', ToneGenerator.TONE_DTMF_7);
        mToneMap.put('8', ToneGenerator.TONE_DTMF_8);
        mToneMap.put('9', ToneGenerator.TONE_DTMF_9);
        mToneMap.put('0', ToneGenerator.TONE_DTMF_0);
        mToneMap.put('#', ToneGenerator.TONE_DTMF_P);
        mToneMap.put('*', ToneGenerator.TONE_DTMF_S);
        mDisplayMap.put(R.id.one, '1');
        mDisplayMap.put(R.id.two, '2');
        mDisplayMap.put(R.id.three, '3');
        mDisplayMap.put(R.id.four, '4');
        mDisplayMap.put(R.id.five, '5');
        mDisplayMap.put(R.id.six, '6');
        mDisplayMap.put(R.id.seven, '7');
        mDisplayMap.put(R.id.eight, '8');
        mDisplayMap.put(R.id.nine, '9');
        mDisplayMap.put(R.id.zero, '0');
        mDisplayMap.put(R.id.pound, '#');
        mDisplayMap.put(R.id.star, '*');
    }
    private EditText mDialpadDigits;
    private InCallScreen mInCallScreen;
    private SlidingDrawer mDialerDrawer;
    private DTMFTwelveKeyDialerView mDialerView;
    private DTMFKeyListener mDialerKeyListener;
    private static class DTMFDisplayMovementMethod implements MovementMethod {
        public boolean onKeyDown(TextView widget, Spannable buffer, int keyCode, KeyEvent event) {
            return false;
        }
        public boolean onKeyUp(TextView widget, Spannable buffer, int keyCode, KeyEvent event) {
            return false;
        }
        public boolean onKeyOther(TextView view, Spannable text, KeyEvent event) {
            return false;
        }
        public boolean onTrackballEvent(TextView widget, Spannable buffer, MotionEvent event) {
            return false;
        }
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            return false;
        }
        public void initialize(TextView widget, Spannable text) {
        }
        public void onTakeFocus(TextView view, Spannable text, int dir) {
        }
        public boolean canSelectArbitrarily() {
            return false;
        }
    }
    private class DTMFKeyListener extends DialerKeyListener {
        private DTMFKeyListener() {
            super();
        }
        @Override
        protected char[] getAcceptedChars(){
            return DTMF_CHARACTERS;
        }
        @Override
        public boolean backspace(View view, Editable content, int keyCode,
                KeyEvent event) {
            return false;
        }
        private boolean isAcceptableModifierKey(int keyCode) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ALT_LEFT:
                case KeyEvent.KEYCODE_ALT_RIGHT:
                case KeyEvent.KEYCODE_SHIFT_LEFT:
                case KeyEvent.KEYCODE_SHIFT_RIGHT:
                    return true;
                default:
                    return false;
            }
        }
        @Override
        public boolean onKeyDown(View view, Editable content,
                                 int keyCode, KeyEvent event) {
            char c = (char) lookup(event, content);
            if (event.getRepeatCount() == 0 && super.onKeyDown(view, content, keyCode, event)) {
                boolean keyOK = ok(getAcceptedChars(), c);
                if (keyOK) {
                    if (DBG) log("DTMFKeyListener reading '" + c + "' from input.");
                    processDtmf(c);
                } else if (DBG) {
                    log("DTMFKeyListener rejecting '" + c + "' from input.");
                }
                return true;
            }
            return false;
        }
        @Override
        public boolean onKeyUp(View view, Editable content,
                                 int keyCode, KeyEvent event) {
            super.onKeyUp(view, content, keyCode, event);
            char c = (char) lookup(event, content);
            boolean keyOK = ok(getAcceptedChars(), c);
            if (keyOK) {
                if (DBG) log("Stopping the tone for '" + c + "'");
                stopTone();
                return true;
            }
            return false;
        }
        public boolean onKeyDown(KeyEvent event) {
            char c = lookup(event);
            if (DBG) log("DTMFKeyListener.onKeyDown: event '" + c + "'");
            if (event.getRepeatCount() == 0 && c != 0) {
                if (ok(getAcceptedChars(), c)) {
                    if (DBG) log("DTMFKeyListener reading '" + c + "' from input.");
                    processDtmf(c);
                    return true;
                } else if (DBG) {
                    log("DTMFKeyListener rejecting '" + c + "' from input.");
                }
            }
            return false;
        }
        public boolean onKeyUp(KeyEvent event) {
            if (event == null) {
                return true;
            }
            char c = lookup(event);
            if (DBG) log("DTMFKeyListener.onKeyUp: event '" + c + "'");
            if (ok(getAcceptedChars(), c)) {
                if (DBG) log("Stopping the tone for '" + c + "'");
                stopTone();
                return true;
            }
            return false;
        }
        private char lookup(KeyEvent event) {
            int meta = event.getMetaState();
            int number = event.getNumber();
            if (!((meta & (KeyEvent.META_ALT_ON | KeyEvent.META_SHIFT_ON)) == 0) || (number == 0)) {
                int match = event.getMatch(getAcceptedChars(), meta);
                number = (match != 0) ? match : number;
            }
            return (char) number;
        }
        boolean isKeyEventAcceptable (KeyEvent event) {
            return (ok(getAcceptedChars(), lookup(event)));
        }
        public final char[] DTMF_CHARACTERS = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '#', '*'
        };
    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PHONE_DISCONNECT:
                    if (DBG) log("disconnect message recieved, shutting down.");
                    mPhone.unregisterForDisconnect(this);
                    closeDialer(false);
                    break;
                case DTMF_SEND_CNF:
                    if (DBG) log("dtmf confirmation received from FW.");
                    handleBurstDtmfConfirmation();
                    break;
            }
        }
    };
    public DTMFTwelveKeyDialer(InCallScreen parent,
                               DTMFTwelveKeyDialerView dialerView,
                               SlidingDrawer dialerDrawer) {
        if (DBG) log("DTMFTwelveKeyDialer constructor... this = " + this);
        mInCallScreen = parent;
        mPhone = PhoneApp.getInstance().phone;
        if (dialerView == null) {
            Log.e(LOG_TAG, "DTMFTwelveKeyDialer: null dialerView!", new IllegalStateException());
        }
        mDialerView = dialerView;
        if (DBG) log("- Got passed-in mDialerView: " + mDialerView);
        mDialerDrawer = dialerDrawer;
        if (DBG) log("- Got passed-in mDialerDrawer: " + mDialerDrawer);
        if (mDialerView != null) {
            mDialerView.setDialer(this);
            mDialpadDigits = (EditText) mDialerView.findViewById(R.id.dtmfDialerField);
            if (mDialpadDigits != null) {
                mDialerKeyListener = new DTMFKeyListener();
                mDialpadDigits.setKeyListener(mDialerKeyListener);
                mDialpadDigits.setLongClickable(false);
            }
            setupKeypad(mDialerView);
        }
        if (mDialerDrawer != null) {
            mDialerDrawer.setOnDrawerOpenListener(this);
            mDialerDrawer.setOnDrawerCloseListener(this);
        }
    }
     void clearInCallScreenReference() {
        if (DBG) log("clearInCallScreenReference()...");
        mInCallScreen = null;
        mDialerKeyListener = null;
        if (mDialerDrawer != null) {
            mDialerDrawer.setOnDrawerOpenListener(null);
            mDialerDrawer.setOnDrawerCloseListener(null);
        }
        if (mPhone.getPhoneType() == Phone.PHONE_TYPE_CDMA) {
            mHandler.removeMessages(DTMF_SEND_CNF);
            synchronized (mDTMFQueue) {
                mDTMFBurstCnfPending = false;
                mDTMFQueue.clear();
            }
        }
        closeDialer(false);
    }
    private void onDialerOpen() {
        if (DBG) log("onDialerOpen()...");
        mPhone.registerForDisconnect(mHandler, PHONE_DISCONNECT, null);
        PhoneApp.getInstance().updateWakeState();
        mInCallScreen.onDialerOpen();
    }
    public void startDialerSession() {
        if (DBG) log("startDialerSession()... this = " + this);
        if (mPhone.getContext().getResources().getBoolean(R.bool.allow_local_dtmf_tones)) {
            mDTMFToneEnabled = Settings.System.getInt(mInCallScreen.getContentResolver(),
                    Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;
        } else {
            mDTMFToneEnabled = false;
        }
        if (DBG) log("- startDialerSession: mDTMFToneEnabled = " + mDTMFToneEnabled);
        if (mDTMFToneEnabled) {
            synchronized (mToneGeneratorLock) {
                if (mToneGenerator == null) {
                    try {
                        mToneGenerator = new ToneGenerator(AudioManager.STREAM_DTMF, 80);
                    } catch (RuntimeException e) {
                        if (DBG) log("Exception caught while creating local tone generator: " + e);
                        mToneGenerator = null;
                    }
                }
            }
        }
    }
    private void onDialerClose() {
        if (DBG) log("onDialerClose()...");
        PhoneApp app = PhoneApp.getInstance();
        app.updateWakeState();
        mPhone.unregisterForDisconnect(mHandler);
        if (mInCallScreen != null) {
            mInCallScreen.onDialerClose();
        }
    }
    public void stopDialerSession() {
        synchronized (mToneGeneratorLock) {
            if (mToneGenerator != null) {
                mToneGenerator.release();
                mToneGenerator = null;
            }
        }
    }
    public boolean onDialerKeyDown(KeyEvent event) {
        if (DBG) log("Notifying dtmf key down.");
        return mDialerKeyListener.onKeyDown(event);
    }
    public boolean onDialerKeyUp(KeyEvent event) {
        if (DBG) log("Notifying dtmf key up.");
        return mDialerKeyListener.onKeyUp(event);
    }
    private void setupKeypad(DTMFTwelveKeyDialerView dialerView) {
        View button;
        for (int viewId : mDisplayMap.keySet()) {
            button = dialerView.findViewById(viewId);
            button.setOnTouchListener(this);
            button.setClickable(true);
            button.setOnKeyListener(this);
        }
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_CALL:
                if (DBG) log("exit requested");
                closeDialer(true);  
                return true;
        }
        return mInCallScreen.onKeyDown(keyCode, event);
    }
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return mInCallScreen.onKeyUp(keyCode, event);
    }
    public boolean onTouch(View v, MotionEvent event) {
        int viewId = v.getId();
        if (mDisplayMap.containsKey(viewId)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    processDtmf(mDisplayMap.get(viewId));
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopTone();
                    break;
            }
        }
        return false;
    }
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            int viewId = v.getId();
            if (mDisplayMap.containsKey(viewId)) {
                switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    if (event.getRepeatCount() == 0) {
                        processDtmf(mDisplayMap.get(viewId));
                    }
                    break;
                case KeyEvent.ACTION_UP:
                    stopTone();
                    break;
                }
            }
        }
        return false;
    }
    public boolean isOpened() {
        if (mDialerDrawer != null) {
            return mDialerDrawer.isOpened();
        } else {
            return mDialerView.getVisibility() == View.VISIBLE;
        }
    }
    public boolean usingSlidingDrawer() {
        return (mDialerDrawer != null);
    }
    public void openDialer(boolean animate) {
        if (DBG) log("openDialer()...");
        if (!isOpened()) {
            if (mDialerDrawer != null) {
                if (animate) {
                    mDialerDrawer.animateToggle();
                } else {
                    mDialerDrawer.toggle();
                }
            } else {
                mDialerView.setVisibility(View.VISIBLE);
                onDialerOpen();
            }
        }
    }
    public void closeDialer(boolean animate) {
        if (DBG) log("closeDialer()...");
        if (isOpened()) {
            if (mDialerDrawer != null) {
                if (animate) {
                    mDialerDrawer.animateToggle();
                } else {
                    mDialerDrawer.toggle();
                }
            } else {
                mDialerView.setVisibility(View.GONE);
                onDialerClose();
            }
        }
    }
    public void setHandleVisible(boolean visible) {
        if (mDialerDrawer != null) {
            mDialerDrawer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }
    public void onDrawerOpened() {
        onDialerOpen();
    }
    public void onDrawerClosed() {
        onDialerClose();
    }
    private final void processDtmf(char c) {
        if (PhoneNumberUtils.is12Key(c)) {
            if (DBG) log("updating display and sending dtmf tone for '" + c + "'");
            if (mDialpadDigits != null) {
                mDialpadDigits.getText().append(c);
            }
            if (mToneMap.containsKey(c)) {
                startTone(c);
            }
        } else if (DBG) {
            log("ignoring dtmf request for '" + c + "'");
        }
        PhoneApp.getInstance().pokeUserActivity();
    }
    public void clearDigits() {
        if (DBG) log("clearDigits()...");
        if (mDialpadDigits != null) {
            mDialpadDigits.setText("");
        }
    }
     void startDtmfTone(char tone) {
        if (DBG) log("startDtmfTone()...");
        mPhone.startDtmf(tone);
        if (mDTMFToneEnabled) {
            synchronized (mToneGeneratorLock) {
                if (mToneGenerator == null) {
                    if (DBG) log("startDtmfTone: mToneGenerator == null, tone: " + tone);
                } else {
                    if (DBG) log("starting local tone " + tone);
                    mToneGenerator.startTone(mToneMap.get(tone));
                }
            }
        }
    }
     void stopDtmfTone() {
        if (DBG) log("stopDtmfTone()...");
        mPhone.stopDtmf();
        if (DBG) log("trying to stop local tone...");
        if (mDTMFToneEnabled) {
            synchronized (mToneGeneratorLock) {
                if (mToneGenerator == null) {
                    if (DBG) log("stopDtmfTone: mToneGenerator == null");
                } else {
                    if (DBG) log("stopping local tone.");
                    mToneGenerator.stopTone();
                }
            }
        }
    }
    boolean isKeyEventAcceptable (KeyEvent event) {
        return (mDialerKeyListener != null && mDialerKeyListener.isKeyEventAcceptable(event));
    }
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    private void startTone(char c) {
        int phoneType = mPhone.getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_GSM) {
            startDtmfTone(c);
        } else if (phoneType == Phone.PHONE_TYPE_CDMA) {
            startToneCdma(c);
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
    }
    private void stopTone() {
        int phoneType = mPhone.getPhoneType();
        if (phoneType == Phone.PHONE_TYPE_GSM) {
            stopDtmfTone();
        } else if (phoneType == Phone.PHONE_TYPE_CDMA) {
            if (mDTMFToneType == CallFeaturesSetting.DTMF_TONE_TYPE_LONG) {
                stopToneCdma();
            }
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
    }
    void startToneCdma(char tone) {
        if (DBG) log("startToneCdma('" + tone + "')...");
        mDTMFToneType = Settings.System.getInt(mInCallScreen.getContentResolver(),
                Settings.System.DTMF_TONE_TYPE_WHEN_DIALING,
                CallFeaturesSetting.DTMF_TONE_TYPE_NORMAL);
        if (mDTMFToneType == CallFeaturesSetting.DTMF_TONE_TYPE_NORMAL) {
            sendShortDtmfToNetwork (tone);
        } else {
            Log.i(LOG_TAG, "send long dtmf for " + tone);
            mPhone.startDtmf(tone);
        }
        startLocalToneCdma(tone);
    }
    void startLocalToneCdma(char tone) {
        if (DBG) log("startLocalToneCdma('" + tone + "')..."
                     + " mDTMFToneEnabled = " + mDTMFToneEnabled + " this = " + this);
        if (mDTMFToneEnabled) {
            synchronized (mToneGeneratorLock) {
                if (mToneGenerator == null) {
                    if (DBG) log("startToneCdma: mToneGenerator == null, tone: " + tone);
                } else {
                    if (DBG) log("starting local tone " + tone);
                    int toneDuration = -1;
                    if (mDTMFToneType == CallFeaturesSetting.DTMF_TONE_TYPE_NORMAL) {
                        toneDuration = DTMF_DURATION_MS;
                    }
                    mToneGenerator.startTone(mToneMap.get(tone), toneDuration);
                }
            }
        }
    }
    private void sendShortDtmfToNetwork(char dtmfDigit) {
        synchronized (mDTMFQueue) {
            if (mDTMFBurstCnfPending == true) {
                mDTMFQueue.add(new Character(dtmfDigit));
            } else {
                String dtmfStr = Character.toString(dtmfDigit);
                Log.i(LOG_TAG, "dtmfsent = " + dtmfStr);
                mPhone.sendBurstDtmf(dtmfStr, 0, 0, mHandler.obtainMessage(DTMF_SEND_CNF));
                mDTMFBurstCnfPending = true;
            }
        }
    }
    private void stopToneCdma() {
        if (DBG) log("stopping remote tone.");
        mPhone.stopDtmf();
        stopLocalToneCdma();
    }
    void stopLocalToneCdma() {
        if (DBG) log("trying to stop local tone...");
        if (mDTMFToneEnabled) {
            synchronized (mToneGeneratorLock) {
                if (mToneGenerator == null) {
                    if (DBG) log("stopLocalToneCdma: mToneGenerator == null");
                } else {
                    if (DBG) log("stopping local tone.");
                    mToneGenerator.stopTone();
                }
            }
        }
    }
    void handleBurstDtmfConfirmation() {
        Character dtmfChar = null;
        synchronized (mDTMFQueue) {
            mDTMFBurstCnfPending = false;
            if (!mDTMFQueue.isEmpty()) {
                dtmfChar = mDTMFQueue.remove();
                Log.i(LOG_TAG, "The dtmf character removed from queue" + dtmfChar);
            }
        }
        if (dtmfChar != null) {
            sendShortDtmfToNetwork(dtmfChar);
        }
    }
}
