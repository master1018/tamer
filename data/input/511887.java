public class DefaultSoftKeyboard implements InputViewManager, KeyboardView.OnKeyboardActionListener {
    public static final int KEYCODE_CHANGE_LANG = -500;
    public static final int KEYCODE_JP12_1 = -201;
    public static final int KEYCODE_JP12_2 = -202;
    public static final int KEYCODE_JP12_3 = -203;
    public static final int KEYCODE_JP12_4 = -204;
    public static final int KEYCODE_JP12_5 = -205;
    public static final int KEYCODE_JP12_6 = -206;
    public static final int KEYCODE_JP12_7 = -207;
    public static final int KEYCODE_JP12_8 = -208;
    public static final int KEYCODE_JP12_9 = -209;
    public static final int KEYCODE_JP12_0 = -210;
    public static final int KEYCODE_JP12_SHARP = -211;
    public static final int KEYCODE_JP12_ASTER = -213;
    public static final int KEYCODE_JP12_BACKSPACE = -214;
    public static final int KEYCODE_JP12_SPACE = -215;
    public static final int KEYCODE_JP12_ENTER = -216;
    public static final int KEYCODE_JP12_RIGHT = -217;
    public static final int KEYCODE_JP12_LEFT = -218;
    public static final int KEYCODE_JP12_REVERSE = -219;
    public static final int KEYCODE_JP12_CLOSE   = -220;
    public static final int KEYCODE_JP12_KBD   = -221;
    public static final int KEYCODE_JP12_EMOJI      = -222;
    public static final int KEYCODE_JP12_ZEN_HIRA   = -223;
    public static final int KEYCODE_JP12_ZEN_NUM    = -224;
    public static final int KEYCODE_JP12_ZEN_ALPHA  = -225;
    public static final int KEYCODE_JP12_ZEN_KATA   = -226;
    public static final int KEYCODE_JP12_HAN_KATA   = -227;
    public static final int KEYCODE_JP12_HAN_NUM    = -228;
    public static final int KEYCODE_JP12_HAN_ALPHA  = -229;
    public static final int KEYCODE_JP12_TOGGLE_MODE = -230;
    public static final int KEYCODE_QWERTY_BACKSPACE = -100;
    public static final int KEYCODE_QWERTY_ENTER = -101;
    public static final int KEYCODE_QWERTY_SHIFT = Keyboard.KEYCODE_SHIFT;
    public static final int KEYCODE_QWERTY_ALT   = -103;
    public static final int KEYCODE_QWERTY_KBD   = -104;
    public static final int KEYCODE_QWERTY_CLOSE = -105;
    public static final int KEYCODE_QWERTY_EMOJI = -106;
    public static final int KEYCODE_QWERTY_ZEN_HIRA   = -107;
    public static final int KEYCODE_QWERTY_ZEN_NUM    = -108;
    public static final int KEYCODE_QWERTY_ZEN_ALPHA  = -109;
    public static final int KEYCODE_QWERTY_ZEN_KATA   = -110;
    public static final int KEYCODE_QWERTY_HAN_KATA   = -111;
    public static final int KEYCODE_QWERTY_HAN_NUM    = -112;
    public static final int KEYCODE_QWERTY_HAN_ALPHA  = -113;
    public static final int KEYCODE_QWERTY_TOGGLE_MODE = -114;
    public static final int KEYCODE_QWERTY_PINYIN  = -115;
    protected OpenWnn      mWnn;
    protected KeyboardView mKeyboardView;
    protected ViewGroup mMainView;
    protected ViewGroup mSubView;
    protected Keyboard mCurrentKeyboard;
    protected boolean mCapsLock;
    protected boolean mDisableKeyInput = true;
    protected Keyboard[][][][][][] mKeyboard;
    protected int mCurrentLanguage;
    public static final int LANG_EN  = 0;
    public static final int LANG_JA  = 1;
    public static final int LANG_CN  = 2;
    protected int mDisplayMode = 0;
    public static final int PORTRAIT  = 0;
    public static final int LANDSCAPE = 1;
    protected int mCurrentKeyboardType;
    public static final int KEYBOARD_QWERTY  = 0;
    public static final int KEYBOARD_12KEY   = 1;
    protected int mShiftOn = 0;
    public static final int KEYBOARD_SHIFT_OFF = 0;
    public static final int KEYBOARD_SHIFT_ON  = 1;
    protected int mCurrentKeyMode;
    public static final int KEYMODE_EN_ALPHABET = 0;
    public static final int KEYMODE_EN_NUMBER   = 1;
    public static final int KEYMODE_EN_PHONE    = 2;
    public static final int KEYMODE_JA_FULL_HIRAGANA = 0;
    public static final int KEYMODE_JA_FULL_ALPHABET = 1;
    public static final int KEYMODE_JA_FULL_NUMBER   = 2;
    public static final int KEYMODE_JA_FULL_KATAKANA = 3;
    public static final int KEYMODE_JA_HALF_ALPHABET = 4;
    public static final int KEYMODE_JA_HALF_NUMBER   = 5;
    public static final int KEYMODE_JA_HALF_KATAKANA = 6;
    public static final int KEYMODE_JA_HALF_PHONE    = 7;
    public static final int KEYMODE_CN_PINYIN   = 0;
    public static final int KEYMODE_CN_FULL_NUMBER   = 1;
    public static final int KEYMODE_CN_ALPHABET = 2;
    public static final int KEYMODE_CN_PHONE    = 3;
    public static final int KEYMODE_CN_HALF_NUMBER   = 4;
    public static final int HARD_KEYMODE_SHIFT_OFF_ALT_OFF     = 2;
    public static final int HARD_KEYMODE_SHIFT_ON_ALT_OFF      = 3;
    public static final int HARD_KEYMODE_SHIFT_OFF_ALT_ON      = 4;
    public static final int HARD_KEYMODE_SHIFT_ON_ALT_ON       = 5;
    public static final int HARD_KEYMODE_SHIFT_LOCK_ALT_OFF    = 6;
    public static final int HARD_KEYMODE_SHIFT_LOCK_ALT_ON     = 7;
    public static final int HARD_KEYMODE_SHIFT_LOCK_ALT_LOCK   = 8;
    public static final int HARD_KEYMODE_SHIFT_OFF_ALT_LOCK    = 9;
    public static final int HARD_KEYMODE_SHIFT_ON_ALT_LOCK     = 10;
    protected boolean mHardKeyboardHidden = true;
    protected boolean mNoInput = true;
    protected Vibrator mVibrator = null;
    protected MediaPlayer mSound = null;
    protected String[] mCurrentCycleTable;
    public DefaultSoftKeyboard() { }
    protected void createKeyboards(OpenWnn parent) {
        mKeyboard = new Keyboard[3][2][4][2][7][2];
    }
    protected Keyboard getShiftChangeKeyboard(int shift) {
        try {
            Keyboard[] kbd = mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][shift][mCurrentKeyMode];
            if (!mNoInput && kbd[1] != null) {
                return kbd[1];
            }
            return kbd[0];
        } catch (Exception ex) {
            return null;
        }
    }
    protected Keyboard getModeChangeKeyboard(int mode) {
        try {
            Keyboard[] kbd = mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mode];
            if (!mNoInput && kbd[1] != null) {
                return kbd[1];
            }
            return kbd[0];
        } catch (Exception ex) {
            return null;
        }
    }
    protected Keyboard getTypeChangeKeyboard(int type) {
        try {
            Keyboard[] kbd = mKeyboard[mCurrentLanguage][mDisplayMode][type][mShiftOn][mCurrentKeyMode];
            if (!mNoInput && kbd[1] != null) {
                return kbd[1];
            }
            return kbd[0];
        } catch (Exception ex) {
            return null;
        }
    }
    protected Keyboard getKeyboardInputed(boolean inputed) {
        try {
            Keyboard[] kbd = mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mCurrentKeyMode];
            if (inputed && kbd[1] != null) {
                return kbd[1];
            }
            return kbd[0];
        } catch (Exception ex) {
            return null;
        }
    }
    protected void toggleKeyMode() {
        mShiftOn = KEYBOARD_SHIFT_OFF;
        Keyboard[][] keyboardList = mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn];
        do {
            if (++mCurrentKeyMode >= keyboardList.length) {
                mCurrentKeyMode = 0;
            }
        } while (keyboardList[mCurrentKeyMode][0] == null);
        Keyboard kbd;
        if (!mNoInput && keyboardList[mCurrentKeyMode][1] != null) {
            kbd = keyboardList[mCurrentKeyMode][1];
        } else {
            kbd = keyboardList[mCurrentKeyMode][0];
        }
        changeKeyboard(kbd);
        mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                      OpenWnnEvent.Mode.DEFAULT));
    }
    protected void toggleShiftLock() {
        if (mShiftOn == 0) {
            Keyboard newKeyboard = getShiftChangeKeyboard(KEYBOARD_SHIFT_ON);
            if (newKeyboard != null) {
                mShiftOn = 1;
                changeKeyboard(newKeyboard);
            }
            mCapsLock = true;
        } else {
            Keyboard newKeyboard = getShiftChangeKeyboard(KEYBOARD_SHIFT_OFF);
            if (newKeyboard != null) {
                mShiftOn = 0;
                changeKeyboard(newKeyboard);
            }
            mCapsLock = false;
        }
    }
    protected void processAltKey() {
        if (mCurrentKeyboardType != KEYBOARD_QWERTY) {
            return;
        }
        int mode = -1;
        int keymode = mCurrentKeyMode;
        switch (mCurrentLanguage) {
        case LANG_EN:
            if (keymode == KEYMODE_EN_ALPHABET) {
                mode = KEYMODE_EN_NUMBER;
            } else if (keymode == KEYMODE_EN_NUMBER) {
                mode = KEYMODE_EN_ALPHABET;
            }
            break;
        case LANG_JA:
            if (keymode == KEYMODE_JA_HALF_ALPHABET) {
                mode = KEYMODE_JA_HALF_NUMBER;
            } else if (keymode == KEYMODE_JA_HALF_NUMBER) {
                mode = KEYMODE_JA_HALF_ALPHABET;
            } else if (keymode == KEYMODE_JA_FULL_ALPHABET) {
                mode = KEYMODE_JA_FULL_NUMBER;
            } else if (keymode == KEYMODE_JA_FULL_NUMBER) {
                mode = KEYMODE_JA_FULL_ALPHABET;
            }
            break;
        default:
        }
        if (mode >= 0) {
            Keyboard kbd = getModeChangeKeyboard(mode);
            if (kbd != null) {
                mCurrentKeyMode = mode;
                changeKeyboard(kbd);
            }
        }
    }
    public void changeKeyboardType(int type) {
        if (type != KEYBOARD_QWERTY && type != KEYBOARD_12KEY) {
            return;
        }
        Keyboard kbd = getTypeChangeKeyboard(type);
        if (kbd != null) {
            mCurrentKeyboardType = type;
            changeKeyboard(kbd);
        }
        mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.CHANGE_MODE,
                                      OpenWnnEvent.Mode.DEFAULT));
    }
    protected boolean changeKeyboard(Keyboard keyboard) {
        if (keyboard == null) {
            return false;
        }
        if (mCurrentKeyboard != keyboard) {
            mKeyboardView.setKeyboard(keyboard);
            mKeyboardView.setShifted((mShiftOn == 0) ? false : true);
            mCurrentKeyboard = keyboard;
            return true;
        } else {
            mKeyboardView.setShifted((mShiftOn == 0) ? false : true);
            return false;
        }
    }
    public View initView(OpenWnn parent, int width, int height) {
        mWnn = parent;
        mDisplayMode = 
            (parent.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            ? LANDSCAPE : PORTRAIT;
        createKeyboards(parent);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(parent);
        String skin = pref.getString("keyboard_skin",
                                     mWnn.getResources().getString(R.string.keyboard_skin_id_default));
        int id = parent.getResources().getIdentifier(skin, "layout", "jp.co.omronsoft.openwnn");
        mKeyboardView = (KeyboardView) mWnn.getLayoutInflater().inflate(id, null);
        mKeyboardView.setOnKeyboardActionListener(this);
        mCurrentKeyboard = null;
        mMainView = (ViewGroup) parent.getLayoutInflater().inflate(R.layout.keyboard_default_main, null);
        mSubView = (ViewGroup) parent.getLayoutInflater().inflate(R.layout.keyboard_default_sub, null);
        if (mDisplayMode == LANDSCAPE || !mHardKeyboardHidden) { 
            mMainView.addView(mSubView);
        } else if (mKeyboardView != null) {
            mMainView.addView(mKeyboardView);
        }
        return mMainView;
    }
    public void updateIndicator(int mode) {
        Resources res = mWnn.getResources();
        TextView text1 = (TextView)mSubView.findViewById(R.id.shift);
        TextView text2 = (TextView)mSubView.findViewById(R.id.alt);
        switch (mode) {
        case HARD_KEYMODE_SHIFT_OFF_ALT_OFF:
            text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_off));
            text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_off));
            text1.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
            text2.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
            break;
        case HARD_KEYMODE_SHIFT_ON_ALT_OFF:
            text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_on));
            text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_off));
            text1.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
            text2.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
            break;
        case HARD_KEYMODE_SHIFT_LOCK_ALT_OFF:
            text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_lock));
            text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_off));
            text1.setBackgroundColor(res.getColor(R.color.indicator_background_lock_caps));
            text2.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
            break;
        case HARD_KEYMODE_SHIFT_OFF_ALT_ON:
            text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_off));
            text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_on));
            text1.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
            text2.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
            break;
        case HARD_KEYMODE_SHIFT_OFF_ALT_LOCK:
            text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_off));
            text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_lock));
            text1.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
            text2.setBackgroundColor(res.getColor(R.color.indicator_background_lock_alt));
            break;
        case HARD_KEYMODE_SHIFT_ON_ALT_ON:
            text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_on));
            text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_on));
            text1.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
            text2.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
            break;
        case HARD_KEYMODE_SHIFT_ON_ALT_LOCK:
            text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_on));
            text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_lock));
            text1.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
            text2.setBackgroundColor(res.getColor(R.color.indicator_background_lock_alt));
            break;
        case HARD_KEYMODE_SHIFT_LOCK_ALT_ON:
            text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_lock));
            text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_on));
            text1.setBackgroundColor(res.getColor(R.color.indicator_background_lock_caps));
            text2.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
            break;
        case HARD_KEYMODE_SHIFT_LOCK_ALT_LOCK:
            text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_lock));
            text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_lock));
            text1.setBackgroundColor(res.getColor(R.color.indicator_background_lock_caps));
            text2.setBackgroundColor(res.getColor(R.color.indicator_background_lock_alt));
            break;
        default:
            text1.setTextColor(res.getColor(R.color.indicator_textcolor_caps_off));
            text2.setTextColor(res.getColor(R.color.indicator_textcolor_alt_off));
            text1.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
            text2.setBackgroundColor(res.getColor(R.color.indicator_textbackground_default));
            break;
        }
        return;
    }
    public View getCurrentView() {
        if (mCurrentKeyboard == null) {
            return null;
        }
        return mMainView;
    }
    public void onUpdateState(OpenWnn parent) {
        try {
            if (parent.mComposingText.size(1) == 0) {
                if (!mNoInput) {
                    mNoInput = true;
                    Keyboard newKeyboard = getKeyboardInputed(false);
                    if (mCurrentKeyboard != newKeyboard) {
                        changeKeyboard(newKeyboard);
                    }
                }
            } else {
                if (mNoInput) {
                    mNoInput = false;
                    Keyboard newKeyboard = getKeyboardInputed(true);
                    if (mCurrentKeyboard != newKeyboard) {
                        changeKeyboard(newKeyboard);
                    }
                }
            }
        } catch (Exception ex) {
        }
    }
    public void setPreferences(SharedPreferences pref, EditorInfo editor) {
        try {
            if (pref.getBoolean("key_vibration", false)) {
                mVibrator = (Vibrator)mWnn.getSystemService(Context.VIBRATOR_SERVICE);
            } else {
                mVibrator = null;
            }
        } catch (Exception ex) {
            Log.d("OpenWnn", "NO VIBRATOR");
        }
        try {
            if (pref.getBoolean("key_sound", false)) {
                mSound = MediaPlayer.create(mWnn, R.raw.type);
            } else {
                mSound = null;
            }
        } catch (Exception ex) {
            Log.d("OpenWnn", "NO SOUND");
        }
        mKeyboardView.setPreviewEnabled(pref.getBoolean("popup_preview", true));
    }
    public void closing() {
        if (mKeyboardView != null) {
            mKeyboardView.closing();
        }
        mDisableKeyInput = true;
    }
    public void onKey(int primaryCode, int[] keyCodes) { }
    public void swipeRight() { }
    public void swipeLeft() { }
    public void swipeDown() { }
    public void swipeUp() { }
    public void onRelease(int x) { }
    public void onPress(int x) {
        if (mVibrator != null) {
            try { mVibrator.vibrate(30); } catch (Exception ex) { }
        }
        if (mSound != null) {
            try { mSound.seekTo(0); mSound.start(); } catch (Exception ex) { }
        }
    }
    public void onText(CharSequence text) {}
    public int getKeyMode() {
        return mCurrentKeyMode;
    }
    public int getKeyboardType() {
        return mCurrentKeyboardType;
    }
    public void setHardKeyboardHidden(boolean hidden) {
        mHardKeyboardHidden = hidden;
    }
    public View getKeyboardView() {
        return mKeyboardView;
    }
    public void resetCurrentKeyboard() {
        closing();
        Keyboard keyboard = mCurrentKeyboard;
        mCurrentKeyboard = null;
        changeKeyboard(keyboard);
    }
}
