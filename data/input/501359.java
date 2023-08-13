public class KeyboardSwitcher {
    public static final int MODE_TEXT = 1;
    public static final int MODE_SYMBOLS = 2;
    public static final int MODE_PHONE = 3;
    public static final int MODE_URL = 4;
    public static final int MODE_EMAIL = 5;
    public static final int MODE_IM = 6;
    public static final int MODE_WEB = 7;
    public static final int MODE_TEXT_QWERTY = 0;
    public static final int MODE_TEXT_ALPHA = 1;
    public static final int MODE_TEXT_COUNT = 2;
    public static final int KEYBOARDMODE_NORMAL = R.id.mode_normal;
    public static final int KEYBOARDMODE_URL = R.id.mode_url;
    public static final int KEYBOARDMODE_EMAIL = R.id.mode_email;
    public static final int KEYBOARDMODE_IM = R.id.mode_im;
    public static final int KEYBOARDMODE_WEB = R.id.mode_webentry;
    private static final int SYMBOLS_MODE_STATE_NONE = 0;
    private static final int SYMBOLS_MODE_STATE_BEGIN = 1;
    private static final int SYMBOLS_MODE_STATE_SYMBOL = 2;
    LatinKeyboardView mInputView;
    private static final int[] ALPHABET_MODES = {
        KEYBOARDMODE_NORMAL,
        KEYBOARDMODE_URL,
        KEYBOARDMODE_EMAIL,
        KEYBOARDMODE_IM,
        KEYBOARDMODE_WEB};
    Context mContext;
    InputMethodService mInputMethodService;
    private KeyboardId mSymbolsId;
    private KeyboardId mSymbolsShiftedId;
    private KeyboardId mCurrentId;
    private Map<KeyboardId, LatinKeyboard> mKeyboards;
    private int mMode; 
    private int mImeOptions;
    private int mTextMode = MODE_TEXT_QWERTY;
    private boolean mIsSymbols;
    private boolean mHasVoice;
    private boolean mVoiceOnPrimary;
    private boolean mPreferSymbols;
    private int mSymbolsModeState = SYMBOLS_MODE_STATE_NONE;
    private int mLastDisplayWidth;
    private LanguageSwitcher mLanguageSwitcher;
    private Locale mInputLocale;
    private boolean mEnableMultipleLanguages;
    KeyboardSwitcher(Context context, InputMethodService ims) {
        mContext = context;
        mKeyboards = new HashMap<KeyboardId, LatinKeyboard>();
        mSymbolsId = new KeyboardId(R.xml.kbd_symbols, false);
        mSymbolsShiftedId = new KeyboardId(R.xml.kbd_symbols_shift, false);
        mInputMethodService = ims;
    }
    void setLanguageSwitcher(LanguageSwitcher languageSwitcher) {
        mLanguageSwitcher = languageSwitcher;
        mInputLocale = mLanguageSwitcher.getInputLocale();
        mEnableMultipleLanguages = mLanguageSwitcher.getLocaleCount() > 1;
    }
    void setInputView(LatinKeyboardView inputView) {
        mInputView = inputView;
    }
    void makeKeyboards(boolean forceCreate) {
        if (forceCreate) mKeyboards.clear();
        int displayWidth = mInputMethodService.getMaxWidth();
        if (displayWidth == mLastDisplayWidth) return;
        mLastDisplayWidth = displayWidth;
        if (!forceCreate) mKeyboards.clear();
        mSymbolsId = new KeyboardId(R.xml.kbd_symbols, mHasVoice && !mVoiceOnPrimary);
        mSymbolsShiftedId = new KeyboardId(R.xml.kbd_symbols_shift,
                mHasVoice && !mVoiceOnPrimary);
    }
    private static class KeyboardId {
        public int mXml;
        public int mKeyboardMode; 
        public boolean mEnableShiftLock;
        public boolean mHasVoice;
        public KeyboardId(int xml, int mode, boolean enableShiftLock, boolean hasVoice) {
            this.mXml = xml;
            this.mKeyboardMode = mode;
            this.mEnableShiftLock = enableShiftLock;
            this.mHasVoice = hasVoice;
        }
        public KeyboardId(int xml, boolean hasVoice) {
            this(xml, 0, false, hasVoice);
        }
        public boolean equals(Object other) {
            return other instanceof KeyboardId && equals((KeyboardId) other);
        }
        public boolean equals(KeyboardId other) {
          return other.mXml == this.mXml
              && other.mKeyboardMode == this.mKeyboardMode
              && other.mEnableShiftLock == this.mEnableShiftLock;
        }
        public int hashCode() {
            return (mXml + 1) * (mKeyboardMode + 1) * (mEnableShiftLock ? 2 : 1)
                    * (mHasVoice ? 4 : 8);
        }
    }
    void setVoiceMode(boolean enableVoice, boolean voiceOnPrimary) {
        if (enableVoice != mHasVoice || voiceOnPrimary != mVoiceOnPrimary) {
            mKeyboards.clear();
        }
        mHasVoice = enableVoice;
        mVoiceOnPrimary = voiceOnPrimary;
        setKeyboardMode(mMode, mImeOptions, mHasVoice,
                mIsSymbols);
    }
    boolean hasVoiceButton(boolean isSymbols) {
        return mHasVoice && (isSymbols != mVoiceOnPrimary);
    }
    void setKeyboardMode(int mode, int imeOptions, boolean enableVoice) {
        mSymbolsModeState = SYMBOLS_MODE_STATE_NONE;
        mPreferSymbols = mode == MODE_SYMBOLS;
        setKeyboardMode(mode == MODE_SYMBOLS ? MODE_TEXT : mode, imeOptions, enableVoice,
                mPreferSymbols);
    }
    void setKeyboardMode(int mode, int imeOptions, boolean enableVoice, boolean isSymbols) {
        if (mInputView == null) return;
        mMode = mode;
        mImeOptions = imeOptions;
        if (enableVoice != mHasVoice) {
            setVoiceMode(mHasVoice, mVoiceOnPrimary);
        }
        mIsSymbols = isSymbols;
        mInputView.setPreviewEnabled(true);
        KeyboardId id = getKeyboardId(mode, imeOptions, isSymbols);
        LatinKeyboard keyboard = getKeyboard(id);
        if (mode == MODE_PHONE) {
            mInputView.setPhoneKeyboard(keyboard);
            mInputView.setPreviewEnabled(false);
        }
        mCurrentId = id;
        mInputView.setKeyboard(keyboard);
        keyboard.setShifted(false);
        keyboard.setShiftLocked(keyboard.isShiftLocked());
        keyboard.setImeOptions(mContext.getResources(), mMode, imeOptions);
    }
    private LatinKeyboard getKeyboard(KeyboardId id) {
        if (!mKeyboards.containsKey(id)) {
            Resources orig = mContext.getResources();
            Configuration conf = orig.getConfiguration();
            Locale saveLocale = conf.locale;
            conf.locale = mInputLocale;
            orig.updateConfiguration(conf, null);
            LatinKeyboard keyboard = new LatinKeyboard(
                mContext, id.mXml, id.mKeyboardMode);
            keyboard.setVoiceMode(hasVoiceButton(id.mXml == R.xml.kbd_symbols), mHasVoice);
            keyboard.setLanguageSwitcher(mLanguageSwitcher);
            if (id.mKeyboardMode == KEYBOARDMODE_NORMAL
                    || id.mKeyboardMode == KEYBOARDMODE_URL
                    || id.mKeyboardMode == KEYBOARDMODE_IM
                    || id.mKeyboardMode == KEYBOARDMODE_EMAIL
                    || id.mKeyboardMode == KEYBOARDMODE_WEB
                    ) {
                keyboard.setExtension(R.xml.kbd_extension);
            }
            if (id.mEnableShiftLock) {
                keyboard.enableShiftLock();
            }
            mKeyboards.put(id, keyboard);
            conf.locale = saveLocale;
            orig.updateConfiguration(conf, null);
        }
        return mKeyboards.get(id);
    }
    private KeyboardId getKeyboardId(int mode, int imeOptions, boolean isSymbols) {
        boolean hasVoice = hasVoiceButton(isSymbols);
        if (isSymbols) {
            return (mode == MODE_PHONE)
                ? new KeyboardId(R.xml.kbd_phone_symbols, hasVoice)
                : new KeyboardId(R.xml.kbd_symbols, hasVoice);
        }
        switch (mode) {
            case MODE_TEXT:
                if (mTextMode == MODE_TEXT_QWERTY) {
                    return new KeyboardId(R.xml.kbd_qwerty, KEYBOARDMODE_NORMAL, true, hasVoice);
                } else if (mTextMode == MODE_TEXT_ALPHA) {
                    return new KeyboardId(R.xml.kbd_alpha, KEYBOARDMODE_NORMAL, true, hasVoice);
                }
                break;
            case MODE_SYMBOLS:
                return new KeyboardId(R.xml.kbd_symbols, hasVoice);
            case MODE_PHONE:
                return new KeyboardId(R.xml.kbd_phone, hasVoice);
            case MODE_URL:
                return new KeyboardId(R.xml.kbd_qwerty, KEYBOARDMODE_URL, true, hasVoice);
            case MODE_EMAIL:
                return new KeyboardId(R.xml.kbd_qwerty, KEYBOARDMODE_EMAIL, true, hasVoice);
            case MODE_IM:
                return new KeyboardId(R.xml.kbd_qwerty, KEYBOARDMODE_IM, true, hasVoice);
            case MODE_WEB:
                return new KeyboardId(R.xml.kbd_qwerty, KEYBOARDMODE_WEB, true, hasVoice);
        }
        return null;
    }
    int getKeyboardMode() {
        return mMode;
    }
    boolean isTextMode() {
        return mMode == MODE_TEXT;
    }
    int getTextMode() {
        return mTextMode;
    }
    void setTextMode(int position) {
        if (position < MODE_TEXT_COUNT && position >= 0) {
            mTextMode = position;
        }
        if (isTextMode()) {
            setKeyboardMode(MODE_TEXT, mImeOptions, mHasVoice);
        }
    }
    int getTextModeCount() {
        return MODE_TEXT_COUNT;
    }
    boolean isAlphabetMode() {
        int currentMode = mCurrentId.mKeyboardMode;
        for (Integer mode : ALPHABET_MODES) {
            if (currentMode == mode) {
                return true;
            }
        }
        return false;
    }
    void toggleShift() {
        if (mCurrentId.equals(mSymbolsId)) {
            LatinKeyboard symbolsKeyboard = getKeyboard(mSymbolsId);
            LatinKeyboard symbolsShiftedKeyboard = getKeyboard(mSymbolsShiftedId);
            symbolsKeyboard.setShifted(true);
            mCurrentId = mSymbolsShiftedId;
            mInputView.setKeyboard(symbolsShiftedKeyboard);
            symbolsShiftedKeyboard.setShifted(true);
            symbolsShiftedKeyboard.setImeOptions(mContext.getResources(), mMode, mImeOptions);
        } else if (mCurrentId.equals(mSymbolsShiftedId)) {
            LatinKeyboard symbolsKeyboard = getKeyboard(mSymbolsId);
            LatinKeyboard symbolsShiftedKeyboard = getKeyboard(mSymbolsShiftedId);
            symbolsShiftedKeyboard.setShifted(false);
            mCurrentId = mSymbolsId;
            mInputView.setKeyboard(getKeyboard(mSymbolsId));
            symbolsKeyboard.setShifted(false);
            symbolsKeyboard.setImeOptions(mContext.getResources(), mMode, mImeOptions);
        }
    }
    void toggleSymbols() {
        setKeyboardMode(mMode, mImeOptions, mHasVoice, !mIsSymbols);
        if (mIsSymbols && !mPreferSymbols) {
            mSymbolsModeState = SYMBOLS_MODE_STATE_BEGIN;
        } else {
            mSymbolsModeState = SYMBOLS_MODE_STATE_NONE;
        }
    }
    boolean onKey(int key) {
        switch (mSymbolsModeState) {
            case SYMBOLS_MODE_STATE_BEGIN:
                if (key != LatinIME.KEYCODE_SPACE && key != LatinIME.KEYCODE_ENTER && key > 0) {
                    mSymbolsModeState = SYMBOLS_MODE_STATE_SYMBOL;
                }
                break;
            case SYMBOLS_MODE_STATE_SYMBOL:
                if (key == LatinIME.KEYCODE_ENTER || key == LatinIME.KEYCODE_SPACE) return true;
                break;
        }
        return false;
    }
}
