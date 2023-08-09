public class OpenWnnJAJP extends OpenWnn {
    public static final int ENGINE_MODE_FULL_KATAKANA = 101;
    public static final int ENGINE_MODE_HALF_KATAKANA = 102;
    public static final int ENGINE_MODE_EISU_KANA = 103;
    public static final int ENGINE_MODE_SYMBOL = 104;
    public static final int ENGINE_MODE_OPT_TYPE_QWERTY = 105;
    public static final int ENGINE_MODE_OPT_TYPE_12KEY = 106;
    private static final boolean FIX_CURSOR_TEXT_END = true;
    private static final CharacterStyle SPAN_CONVERT_BGCOLOR_HL   = new BackgroundColorSpan(0xFF8888FF);
    private static final CharacterStyle SPAN_EXACT_BGCOLOR_HL     = new BackgroundColorSpan(0xFF66CDAA);
    private static final CharacterStyle SPAN_EISUKANA_BGCOLOR_HL  = new BackgroundColorSpan(0xFF9FB6CD);
    private static final CharacterStyle SPAN_REMAIN_BGCOLOR_HL    = new BackgroundColorSpan(0xFFF0FFFF);
    private static final CharacterStyle SPAN_TEXTCOLOR  = new ForegroundColorSpan(0xFF000000);
    private static final CharacterStyle SPAN_UNDERLINE            = new UnderlineSpan();
    private static final int STATUS_INIT            = 0x0000;
    private static final int STATUS_INPUT           = 0x0001;
    private static final int STATUS_INPUT_EDIT      = 0x0003;
    private static final int STATUS_CANDIDATE_FULL  = 0x0010;
    private static final Pattern ENGLISH_CHARACTER_LAST = Pattern.compile(".*[a-zA-Z]$");
    private static final int PRIVATE_AREA_CODE = 61184;
    private static final int LIMIT_INPUT_NUMBER = 30;
    private static final int AUTO_COMMIT_ENGLISH_ON      = 0x0000;
    private static final int AUTO_COMMIT_ENGLISH_OFF     = 0x0001;
    private static final int AUTO_COMMIT_ENGLISH_SYMBOL  = 0x0010;
    private static final int MSG_PREDICTION = 0;
    private static final int MSG_START_TUTORIAL = 1;
    private static final int MSG_CLOSE = 2;
    private static final int PREDICTION_DELAY_MS_1ST = 200;
    private static final int PREDICTION_DELAY_MS_SHOWING_CANDIDATE = 200;
    private class EngineState {
        public static final int INVALID = -1;
        public static final int DICTIONARYSET_JP = 0;
        public static final int DICTIONARYSET_EN = 1;
        public static final int CONVERT_TYPE_NONE = 0;
        public static final int CONVERT_TYPE_RENBUN = 1;
        public static final int CONVERT_TYPE_EISU_KANA = 2;
        public static final int TEMPORARY_DICTIONARY_MODE_NONE = 0;
        public static final int TEMPORARY_DICTIONARY_MODE_SYMBOL = 1;
        public static final int TEMPORARY_DICTIONARY_MODE_USER = 2;
        public static final int PREFERENCE_DICTIONARY_NONE = 0;
        public static final int PREFERENCE_DICTIONARY_PERSON_NAME = 1;
        public static final int PREFERENCE_DICTIONARY_POSTAL_ADDRESS = 2;
        public static final int PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI = 3;
        public static final int KEYBOARD_UNDEF = 0;
        public static final int KEYBOARD_QWERTY = 1;
        public static final int KEYBOARD_12KEY  = 2;
        public int dictionarySet = INVALID;
        public int convertType = INVALID;
        public int temporaryMode = INVALID;
        public int preferenceDictionary = INVALID;
        public int keyboard = INVALID;
        public boolean isRenbun() {
            return convertType == CONVERT_TYPE_RENBUN;
        }
        public boolean isEisuKana() {
            return convertType == CONVERT_TYPE_EISU_KANA;
        }
        public boolean isConvertState() {
            return convertType != CONVERT_TYPE_NONE;
        }
        public boolean isSymbolList() {
            return temporaryMode == TEMPORARY_DICTIONARY_MODE_SYMBOL;
        }
        public boolean isEnglish() {
            return dictionarySet == DICTIONARYSET_EN;
        }
    }
    protected int mStatus = STATUS_INIT;
    protected boolean mExactMatchMode = false;
    protected SpannableStringBuilder mDisplayText;
    private static OpenWnnJAJP mSelf = null;
    private WnnEngine mConverterBack;
    private LetterConverter mPreConverterBack;
    private OpenWnnEngineJAJP mConverterJAJP;
    private OpenWnnEngineEN mConverterEN;
    private SymbolList mConverterSymbolEngineBack;
    private static final String[] SYMBOL_LISTS = {
        SymbolList.SYMBOL_JAPANESE_FACE, SymbolList.SYMBOL_JAPANESE, SymbolList.SYMBOL_ENGLISH
    };
    private int mCurrentSymbol = 0;
    private Romkan mPreConverterHiragana;
    private RomkanFullKatakana mPreConverterFullKatakana;
    private RomkanHalfKatakana mPreConverterHalfKatakana;
    private EngineState mEngineState = new EngineState();
    private boolean mEnableLearning = true;
    private boolean mEnablePrediction = true;
    private boolean mEnableConverter = true;
    private boolean mEnableSymbolList = true;
    private boolean mEnableSymbolListNonHalf = true;
    private boolean mEnableSpellCorrection = true;
    private int mDisableAutoCommitEnglishMask = AUTO_COMMIT_ENGLISH_ON;
    private boolean mEnableAutoDeleteSpace = false;
    private boolean mEnableAutoInsertSpace = true;
    private boolean mEnableAutoHideKeyboard = true;
    private int mCommitCount = 0;
    private int mTargetLayer = 1;
    private int mOrientation = Configuration.ORIENTATION_UNDEFINED;
    private int mPrevDictionarySet = OpenWnnEngineJAJP.DIC_LANG_INIT;
    private  Pattern mEnglishAutoCommitDelimiter = null;
    private int mComposingStartCursor = 0;
    private int mCommitStartCursor = 0;
    private StringBuffer mPrevCommitText = null;
    private int mPrevCommitCount = 0;
    private int mHardShift;
    private boolean mShiftPressing;
    private int mHardAlt;
    private boolean mAltPressing;
    private static final int[] mShiftKeyToggle = {0, MetaKeyKeyListener.META_SHIFT_ON, MetaKeyKeyListener.META_CAP_LOCKED};
    private static final int[] mAltKeyToggle = {0, MetaKeyKeyListener.META_ALT_ON, MetaKeyKeyListener.META_ALT_LOCKED};
    private boolean mAutoCaps = false;
    private WnnWord[] mUserDictionaryWords = null;
    private TutorialJAJP mTutorial;
    private boolean mEnableTutorial;
    private boolean mHasContinuedPrediction = false;
    private boolean mHasStartedTextSelection = true;
    Handler mHandler = new Handler() {
            @Override
                public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_PREDICTION:
                    updatePrediction();
                    break;
                case MSG_START_TUTORIAL:
                    if (mTutorial == null) {
                        if (isInputViewShown()) {
                            DefaultSoftKeyboardJAJP inputManager = ((DefaultSoftKeyboardJAJP) mInputViewManager);
                            View v = inputManager.getKeyboardView();
                            mTutorial = new TutorialJAJP(OpenWnnJAJP.this, v, inputManager);
                            mTutorial.start();
                        } else {
                            sendMessageDelayed(obtainMessage(MSG_START_TUTORIAL), 100);
                        }
                    }
                    break;
                case MSG_CLOSE:
                    if (mConverterJAJP != null) mConverterJAJP.close();
                    if (mConverterEN != null) mConverterEN.close();
                    if (mConverterSymbolEngineBack != null) mConverterSymbolEngineBack.close();
                    break;
                }
            }
        };
    private CandidateFilter mFilter;
    public OpenWnnJAJP() {
        super();
        mSelf = this;
        mComposingText = new ComposingText();
        mCandidatesViewManager = new TextCandidatesViewManager(-1);
        mInputViewManager  = new DefaultSoftKeyboardJAJP();
        mConverter = mConverterJAJP = new OpenWnnEngineJAJP("/data/data/jp.co.omronsoft.openwnn/writableJAJP.dic");
        mConverterEN = new OpenWnnEngineEN("/data/data/jp.co.omronsoft.openwnn/writableEN.dic");
        mPreConverter = mPreConverterHiragana = new Romkan();
        mPreConverterFullKatakana = new RomkanFullKatakana();
        mPreConverterHalfKatakana = new RomkanHalfKatakana();
        mFilter = new CandidateFilter();
        mDisplayText = new SpannableStringBuilder();
        mAutoHideMode = false;
        mPrevCommitText = new StringBuffer();
    }
    public OpenWnnJAJP(Context context) {
        this();
        attachBaseContext(context);
    }
    @Override public void onCreate() {
        super.onCreate();
        String delimiter = Pattern.quote(getResources().getString(R.string.en_word_separators));
        mEnglishAutoCommitDelimiter = Pattern.compile(".*[" + delimiter + "]$");
        if (mConverterSymbolEngineBack == null) {
            mConverterSymbolEngineBack = new SymbolList(this, SymbolList.LANG_JA);
        }
    }
    @Override public View onCreateInputView() {
        int hiddenState = getResources().getConfiguration().hardKeyboardHidden;
        boolean hidden = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
        ((DefaultSoftKeyboardJAJP) mInputViewManager).setHardKeyboardHidden(hidden);
        mEnableTutorial = hidden;
        return super.onCreateInputView();
    }
    @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
        if (restarting) {
            super.onStartInputView(attribute, restarting);
        } else {
            EngineState state = new EngineState();
            state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
            updateEngineState(state);
            mPrevCommitCount = 0;
            clearCommitInfo();
            ((DefaultSoftKeyboard) mInputViewManager).resetCurrentKeyboard();
            super.onStartInputView(attribute, restarting);
            mCandidatesViewManager.clearCandidates();
            mStatus = STATUS_INIT;
            mExactMatchMode = false;       
            mHardShift = 0;
            mHardAlt   = 0;
            updateMetaKeyStateDisplay();
        }
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        fitInputType(pref, attribute);
        ((TextCandidatesViewManager)mCandidatesViewManager).setAutoHide(true);
        if (isEnableL2Converter()) {
            breakSequence();
        }
    }
    @Override public void hideWindow() {
        mComposingText.clear();
        mInputViewManager.onUpdateState(this);
        clearCommitInfo();
        mHandler.removeMessages(MSG_START_TUTORIAL);
        mInputViewManager.closing();
        if (mTutorial != null) {
            mTutorial.close();
            mTutorial = null;
        }
        super.hideWindow();
    }
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        mComposingStartCursor = (candidatesStart < 0) ? newSelEnd : candidatesStart;
        boolean prevSelection = mHasStartedTextSelection;
        if (newSelStart != newSelEnd) {
            clearCommitInfo();
            mHasStartedTextSelection = true;
        } else {
            mHasStartedTextSelection = false;
        }
        if (mHasContinuedPrediction) {
            mHasContinuedPrediction = false;
            if (0 < mPrevCommitCount) {
                mPrevCommitCount--;
            }
            return;
        }
        boolean isNotComposing = ((candidatesStart < 0) && (candidatesEnd < 0));
        if ((mComposingText.size(ComposingText.LAYER1) != 0)
            && !isNotComposing) {
            updateViewStatus(mTargetLayer, false, true);
        } else {
            if (0 < mPrevCommitCount) {
                mPrevCommitCount--;
            } else {
                int commitEnd = mCommitStartCursor + mPrevCommitText.length();
                if ((((newSelEnd < oldSelEnd) || (commitEnd < newSelEnd)) && clearCommitInfo())
                    || isNotComposing) {
                    if (isEnableL2Converter()) {
                        breakSequence();
                    }
                    if (mInputConnection != null) {
                        if (isNotComposing && (mComposingText.size(ComposingText.LAYER1) != 0)) {
                            mInputConnection.finishComposingText();
                        }
                    }
                    if ((prevSelection != mHasStartedTextSelection) || !mHasStartedTextSelection) {
                        initializeScreen();
                    }
                }
            }
        }
    }
    @Override public void onConfigurationChanged(Configuration newConfig) {
        try {
            super.onConfigurationChanged(newConfig);
            if (mInputConnection != null) {
                if (super.isInputViewShown()) {
                    updateViewStatus(mTargetLayer, true, true);
                }
                if (mOrientation != newConfig.orientation) {
                    mOrientation = newConfig.orientation;
                    commitConvertingText();
                    initializeScreen();
                }
                int hiddenState = newConfig.hardKeyboardHidden;
                boolean hidden = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
                ((DefaultSoftKeyboardJAJP) mInputViewManager).setHardKeyboardHidden(hidden);
                mEnableTutorial = hidden;
            }
        } catch (Exception ex) {
        }
    }
    @Override synchronized public boolean onEvent(OpenWnnEvent ev) {
        EngineState state;
        switch (ev.code) {
        case OpenWnnEvent.KEYUP:
            onKeyUpEvent(ev.keyEvent);
            return true;
        case OpenWnnEvent.INITIALIZE_LEARNING_DICTIONARY:
            mConverterEN.initializeDictionary(WnnEngine.DICTIONARY_TYPE_LEARN);
            mConverterJAJP.initializeDictionary(WnnEngine.DICTIONARY_TYPE_LEARN);
            return true;
        case OpenWnnEvent.INITIALIZE_USER_DICTIONARY:
            return mConverterJAJP.initializeDictionary( WnnEngine.DICTIONARY_TYPE_USER );
        case OpenWnnEvent.LIST_WORDS_IN_USER_DICTIONARY:
            mUserDictionaryWords = mConverterJAJP.getUserDictionaryWords( );
            return true;
        case OpenWnnEvent.GET_WORD:
            if (mUserDictionaryWords != null) {
                ev.word = mUserDictionaryWords[0];
                for (int i = 0 ; i < mUserDictionaryWords.length - 1 ; i++) {
                    mUserDictionaryWords[i] = mUserDictionaryWords[i + 1];
                }
                mUserDictionaryWords[mUserDictionaryWords.length - 1] = null;
                if (mUserDictionaryWords[0] == null) {
                    mUserDictionaryWords = null;
                }
                return true;
            }
            break;
        case OpenWnnEvent.ADD_WORD:
            mConverterJAJP.addWord(ev.word);
            return true;
        case OpenWnnEvent.DELETE_WORD:
            mConverterJAJP.deleteWord(ev.word);
            return true;
        case OpenWnnEvent.CHANGE_MODE:
            changeEngineMode(ev.mode);
            if (!(ev.mode == ENGINE_MODE_SYMBOL || ev.mode == ENGINE_MODE_EISU_KANA)) {
                initializeScreen();
            }
            return true;
        case OpenWnnEvent.UPDATE_CANDIDATE:
            if (mEngineState.isRenbun()) {
                mComposingText.setCursor(ComposingText.LAYER1,
                                         mComposingText.toString(ComposingText.LAYER1).length());
                mExactMatchMode = false;
                updateViewStatusForPrediction(true, true);
            } else {
                updateViewStatus(mTargetLayer, true, true);
            }
            return true;
        case OpenWnnEvent.CHANGE_INPUT_VIEW:
            setInputView(onCreateInputView());
            return true;
        case OpenWnnEvent.CANDIDATE_VIEW_TOUCH:
            boolean ret;
            ret = ((TextCandidatesViewManager)mCandidatesViewManager).onTouchSync();
            return ret;
        case OpenWnnEvent.TOUCH_OTHER_KEY:
            mStatus |= STATUS_INPUT_EDIT;
            return true;
        default:
            break;
        }
        KeyEvent keyEvent = ev.keyEvent;
        int keyCode = 0;
        if (keyEvent != null) {
            keyCode = keyEvent.getKeyCode();
        }
        if (mDirectInputMode) {
            if (mInputConnection != null) {
                switch (ev.code) {
                case OpenWnnEvent.INPUT_SOFT_KEY:
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        sendKeyChar('\n');
                    } else {
                        mInputConnection.sendKeyEvent(keyEvent);
                        mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                                                                   keyEvent.getKeyCode()));
                    }
                    break;
                case OpenWnnEvent.INPUT_CHAR:
                    sendKeyChar(ev.chars[0]);
                    break;
                default:
                    break;
                }
            }
            return false;
        }
        if (!((ev.code == OpenWnnEvent.COMMIT_COMPOSING_TEXT)
              || ((keyEvent != null)
                  && ((keyCode == KeyEvent.KEYCODE_SHIFT_LEFT)
                      || (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT)
                      || (keyCode == KeyEvent.KEYCODE_ALT_LEFT)
                      || (keyCode == KeyEvent.KEYCODE_ALT_RIGHT)
                      || (keyEvent.isAltPressed() && (keyCode == KeyEvent.KEYCODE_SPACE)))))) {
            clearCommitInfo();
        }       
        if (!((ev.code == OpenWnnEvent.SELECT_CANDIDATE)
              || (ev.code == OpenWnnEvent.LIST_CANDIDATES_NORMAL)
              || (ev.code == OpenWnnEvent.LIST_CANDIDATES_FULL)
              || ((keyEvent != null)
                  && ((keyCode == KeyEvent.KEYCODE_SHIFT_LEFT)
                      ||(keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT)
                      ||(keyCode == KeyEvent.KEYCODE_ALT_LEFT)
                      ||(keyCode == KeyEvent.KEYCODE_ALT_RIGHT)
                      ||(keyCode == KeyEvent.KEYCODE_BACK && mCandidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL)
                      ||(keyEvent.isAltPressed() && (keyCode == KeyEvent.KEYCODE_SPACE)))))) {
            state = new EngineState();
            state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
            updateEngineState(state);
        }
        if (ev.code == OpenWnnEvent.LIST_CANDIDATES_FULL) {
            mStatus |= STATUS_CANDIDATE_FULL;
            mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_FULL);
            return true;
        } else if (ev.code == OpenWnnEvent.LIST_CANDIDATES_NORMAL) {
            mStatus &= ~STATUS_CANDIDATE_FULL;
            mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
            return true;
        }
        boolean ret = false;
        switch (ev.code) {
        case OpenWnnEvent.INPUT_CHAR:
            if ((mPreConverter == null) && !isEnableL2Converter()) {
                commitText(false);
                commitText(new String(ev.chars));
                mCandidatesViewManager.clearCandidates();
            } else if (!isEnableL2Converter()) {
                processSoftKeyboardCodeWithoutConversion(ev.chars);
            } else {
                processSoftKeyboardCode(ev.chars);
            }
            ret = true;
            break;
        case OpenWnnEvent.TOGGLE_CHAR:
            processSoftKeyboardToggleChar(ev.toggleTable);
            ret = true;
            break;
        case OpenWnnEvent.TOGGLE_REVERSE_CHAR:
            if (((mStatus & ~STATUS_CANDIDATE_FULL) == STATUS_INPUT)
                && !(mEngineState.isConvertState()) && (ev.toggleTable != null)) {
                int cursor = mComposingText.getCursor(ComposingText.LAYER1);
                if (cursor > 0) {
                    String prevChar = mComposingText.getStrSegment(ComposingText.LAYER1, cursor - 1).string;
                    String c = searchToggleCharacter(prevChar, ev.toggleTable, true);
                    if (c != null) {
                        mComposingText.delete(ComposingText.LAYER1, false);
                        appendStrSegment(new StrSegment(c));
                        updateViewStatusForPrediction(true, true);
                        ret = true;
                        break;
                    }
                }
            }
            break;
        case OpenWnnEvent.REPLACE_CHAR:
            int cursor = mComposingText.getCursor(ComposingText.LAYER1);
            if ((cursor > 0)
                && !(mEngineState.isConvertState())) {
                String search = mComposingText.getStrSegment(ComposingText.LAYER1, cursor - 1).string;
                String c = (String)ev.replaceTable.get(search);
                if (c != null) {
                    mComposingText.delete(1, false);
                    appendStrSegment(new StrSegment(c));
                    updateViewStatusForPrediction(true, true);
                    ret = true;
                    mStatus = STATUS_INPUT_EDIT;
                    break;
                }
            }
            break;
        case OpenWnnEvent.INPUT_KEY:
            switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_UP:
                if (mTutorial != null) {
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
                if (keyEvent.getRepeatCount() == 0) {
                    if (++mHardAlt > 2) { mHardAlt = 0; }
                }
                mAltPressing   = true;
                updateMetaKeyStateDisplay();
                return true;
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                if (keyEvent.getRepeatCount() == 0) {
                    if (++mHardShift > 2) { mHardShift = 0; }
                }
                mShiftPressing = true;
                updateMetaKeyStateDisplay();
                return true;
            }
            ret = processKeyEvent(keyEvent);
            break;
        case OpenWnnEvent.INPUT_SOFT_KEY:
            ret = processKeyEvent(keyEvent);
            if (!ret) {
                int code = keyEvent.getKeyCode();
                if (code == KeyEvent.KEYCODE_ENTER) {
                    sendKeyChar('\n');
                } else {
                    mInputConnection.sendKeyEvent(keyEvent);
                    mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, code));
                }
                ret = true;
            }
            break;
        case OpenWnnEvent.SELECT_CANDIDATE:
            initCommitInfoForWatchCursor();
            if (isEnglishPrediction()) {
                mComposingText.clear();
            }
            mStatus = commitText(ev.word);
            if (isEnglishPrediction() && !mEngineState.isSymbolList() && mEnableAutoInsertSpace) {
                commitSpaceJustOne();
            }
            checkCommitInfo();
            if (mEngineState.isSymbolList()) {
                mEnableAutoDeleteSpace = false;
            }
            break;
        case OpenWnnEvent.CONVERT:           
            startConvert(EngineState.CONVERT_TYPE_RENBUN);
            break;
        case OpenWnnEvent.COMMIT_COMPOSING_TEXT:
            commitAllText();
            break;
        }
        return ret;
    }
    @Override public boolean onEvaluateFullscreenMode() {
        return false;
    }
    @Override public boolean onEvaluateInputViewShown() {
        return true;
    }
    public static OpenWnnJAJP getInstance() {
        return mSelf;
    }
    private StrSegment createStrSegment(int charCode) {
        if (charCode == 0) {
            return null;
        }
        return new StrSegment(Character.toChars(charCode));
    }
    private boolean processKeyEvent(KeyEvent ev) {
        int key = ev.getKeyCode();
        if (ev.isPrintingKey()) {
            if ((mHardShift > 0 && mHardAlt > 0) ||
                (ev.isAltPressed() && ev.isShiftPressed())) {
                int charCode = ev.getUnicodeChar(MetaKeyKeyListener.META_SHIFT_ON | MetaKeyKeyListener.META_ALT_ON);
                if (charCode == 0 || (charCode & KeyCharacterMap.COMBINING_ACCENT) != 0 || charCode == PRIVATE_AREA_CODE) {
                    if(mHardShift == 1){
                        mShiftPressing = false;
                    }
                    if(mHardAlt == 1){
                        mAltPressing   = false;
                    }
                    if(!ev.isAltPressed()){
                        if (mHardAlt == 1) {
                            mHardAlt = 0;
                        }
                    }
                    if(!ev.isShiftPressed()){
                        if (mHardShift == 1) {
                            mHardShift = 0;
                        }
                    }
                    if(!ev.isShiftPressed() && !ev.isAltPressed()){
                        updateMetaKeyStateDisplay();
                    }
                    return true;
                }
            }
            commitConvertingText();
            EditorInfo edit = getCurrentInputEditorInfo();
            StrSegment str;
            if (mHardShift== 0 && mHardAlt == 0) {
                int shift = (mAutoCaps)? getShiftKeyState(edit) : 0;
                if (shift != mHardShift && (key >= KeyEvent.KEYCODE_A && key <= KeyEvent.KEYCODE_Z)) {
                    str = createStrSegment(ev.getUnicodeChar(MetaKeyKeyListener.META_SHIFT_ON));
                } else {
                    str = createStrSegment(ev.getUnicodeChar());
                }
            } else {
                str = createStrSegment(ev.getUnicodeChar(mShiftKeyToggle[mHardShift]
                                                         | mAltKeyToggle[mHardAlt]));
                if(mHardShift == 1){
                    mShiftPressing = false;
                }
                if(mHardAlt == 1){
                    mAltPressing   = false;
                }
                if (!ev.isAltPressed()) {
                    if (mHardAlt == 1) {
                        mHardAlt = 0;
                    }
                }
                if (!ev.isShiftPressed()) {
                    if (mHardShift == 1) {
                        mHardShift = 0;
                    }
                }
                if (!ev.isShiftPressed() && !ev.isShiftPressed()) {
                    updateMetaKeyStateDisplay();
                }
            }
            if (str == null) {
                return true;
            }
            if (str.string.charAt(0) != '\u0009') {
                processHardwareKeyboardInputChar(str);
                return true;
            } else {
                commitText(true);
                commitText(str.string);
                initializeScreen();
                return true;
            }
        } else if (key == KeyEvent.KEYCODE_SPACE) {
            processHardwareKeyboardSpaceKey(ev);
            return true;
        } else if (key == KeyEvent.KEYCODE_SYM) {
            initCommitInfoForWatchCursor();
            mStatus = commitText(true);
            checkCommitInfo();
            changeEngineMode(ENGINE_MODE_SYMBOL);
            mHardAlt = 0;
            updateMetaKeyStateDisplay();
            return true;
        }
        if (mComposingText.size(ComposingText.LAYER1) > 0) {
            switch (key) {
            case KeyEvent.KEYCODE_DEL:
                mStatus = STATUS_INPUT_EDIT;
                if (mEngineState.isConvertState()) {
                    mComposingText.setCursor(ComposingText.LAYER1,
                                             mComposingText.toString(ComposingText.LAYER1).length());
                    mExactMatchMode = false;
                } else {
                    if ((mComposingText.size(ComposingText.LAYER1) == 1)
                        && mComposingText.getCursor(ComposingText.LAYER1) != 0) {
                        initializeScreen();
                        return true;
                    } else {
                        mComposingText.delete(ComposingText.LAYER1, false);
                    }
                }
                updateViewStatusForPrediction(true, true);
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (mCandidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL) {
                    mStatus &= ~STATUS_CANDIDATE_FULL;
                    mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
                } else {
                    if (!mEngineState.isConvertState()) {
                        initializeScreen();
                        if (mConverter != null) {
                            mConverter.init();
                        }
                    } else {
                        mCandidatesViewManager.clearCandidates();
                        mStatus = STATUS_INPUT_EDIT;
                        mExactMatchMode = false;
                        mComposingText.setCursor(ComposingText.LAYER1,
                                                 mComposingText.toString(ComposingText.LAYER1).length());
                        updateViewStatusForPrediction(true, true);
                    }
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (!isEnableL2Converter()) {
                    commitText(false);
                    return false;
                } else {
                    processLeftKeyEvent();
                    return true;
                }
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (!isEnableL2Converter()) {
                    if (mEngineState.keyboard == EngineState.KEYBOARD_12KEY) {
                        commitText(false);
                    }
                } else {
                    processRightKeyEvent();
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (!isEnglishPrediction()) {
                    int cursor = mComposingText.getCursor(ComposingText.LAYER1);
                    if (cursor < 1) {
                        return true;
                    }
                }
                initCommitInfoForWatchCursor();
                mStatus = commitText(true);
                checkCommitInfo();
                if (isEnglishPrediction()) {
                    initializeScreen();
                }
                if (mEnableAutoHideKeyboard) {
                    mInputViewManager.closing();
                    requestHideSelf(0);
                }
                return true;
            case KeyEvent.KEYCODE_CALL:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                return false;
            default:
                return true;
            }
        } else {
            if (mCandidatesViewManager.getCurrentView().isShown()) {
                switch (key) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (isEnableL2Converter()) {
                        mConverter.init();
                    }
                    mStatus = STATUS_INPUT_EDIT;
                    updateViewStatusForPrediction(true, true);
                    return false;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (isEnableL2Converter()) {
                        mConverter.init();
                    }
                    mStatus = STATUS_INPUT_EDIT;
                    updateViewStatusForPrediction(true, true);
                    return false;
                default:
                    return processKeyEventNoInputCandidateShown(ev);
                }
            } else {
                switch (key) {
                case KeyEvent.KEYCODE_BACK:
                    if (isInputViewShown()) {
                        mInputViewManager.closing();
                        requestHideSelf(0);
                        return true;
                    }
                    break;
                default:
                    break;
                }
            }
        }
        return false;
    }
    private void processHardwareKeyboardSpaceKey(KeyEvent ev) {
        if (ev.isShiftPressed()) {
            mHardAlt = 0;
            mHardShift = 0;
            updateMetaKeyStateDisplay();
            if (mEngineState.isEnglish()) {
                ((DefaultSoftKeyboardJAJP) mInputViewManager).changeKeyMode(DefaultSoftKeyboard.KEYMODE_JA_FULL_HIRAGANA);
                mConverter = mConverterJAJP;
            } else {
                ((DefaultSoftKeyboardJAJP) mInputViewManager).changeKeyMode(DefaultSoftKeyboard.KEYMODE_JA_HALF_ALPHABET);
                mConverter = mConverterEN;
            }
            mCandidatesViewManager.clearCandidates();
        } else if(ev.isAltPressed()){
            if (!mEngineState.isSymbolList()) {
                commitAllText();
            }
            changeEngineMode(ENGINE_MODE_SYMBOL);
            mHardAlt = 0;
            updateMetaKeyStateDisplay();
        } else if (isEnglishPrediction()) {
            if (mComposingText.size(0) == 0) {
                commitText(" ");
                mCandidatesViewManager.clearCandidates();
                breakSequence();
            } else {
                initCommitInfoForWatchCursor();
                commitText(true);
                commitSpaceJustOne();
                checkCommitInfo();
            }
            mEnableAutoDeleteSpace = false;
        } else {
            if (mComposingText.size(0) == 0) {
                commitText(" ");
                mCandidatesViewManager.clearCandidates();
                breakSequence();
            } else {
                startConvert(EngineState.CONVERT_TYPE_RENBUN);
            }
        }
    }
    private void processHardwareKeyboardInputChar(StrSegment str) {
        if (isEnableL2Converter()) {
            boolean commit = false;
            if (mPreConverter == null) {
                Matcher m = mEnglishAutoCommitDelimiter.matcher(str.string);
                if (m.matches()) {
                    commitText(true);
                    commit = true;
                }
                appendStrSegment(str);
            } else {
                appendStrSegment(str);
                mPreConverter.convert(mComposingText);
            }
            if (commit) {
                commitText(true);
            } else {
                mStatus = STATUS_INPUT;
                updateViewStatusForPrediction(true, true);
            }
        } else {
            appendStrSegment(str);
            boolean completed = true;
            if (mPreConverter != null) {
                completed = mPreConverter.convert(mComposingText);
            }
            if (completed) {
                if (!mEngineState.isEnglish()) {
                    commitTextWithoutLastAlphabet();
                } else {
                    commitText(false);
                }
            } else {
                updateViewStatus(ComposingText.LAYER1, false, true);
            }
        }
    }
    private void updatePrediction() {
        int candidates = 0;
        int cursor = mComposingText.getCursor(ComposingText.LAYER1);
        if (isEnableL2Converter() || mEngineState.isSymbolList()) {
            if (mExactMatchMode) {
                candidates = mConverter.predict(mComposingText, 0, cursor);
            } else {
                candidates = mConverter.predict(mComposingText, 0, -1);
            }
        }
        if (candidates > 0) {
            mHasContinuedPrediction = ((mComposingText.size(ComposingText.LAYER1) == 0)
                                       && !mEngineState.isSymbolList());
            mCandidatesViewManager.displayCandidates(mConverter);
        } else {
            mCandidatesViewManager.clearCandidates();
        }
    }
    private void processLeftKeyEvent() {
        if (mEngineState.isConvertState()) {
            if (mEngineState.isEisuKana()) {
                mExactMatchMode = true;
            }
            if (1 < mComposingText.getCursor(ComposingText.LAYER1)) {
                mComposingText.moveCursor(ComposingText.LAYER1, -1);
            }
        } else if (mExactMatchMode) {
            mComposingText.moveCursor(ComposingText.LAYER1, -1);
        } else {
            if (isEnglishPrediction()) {
                mComposingText.moveCursor(ComposingText.LAYER1, -1);
            } else {
                mExactMatchMode = true;
            }
        }
        mCommitCount = 0; 
        mStatus = STATUS_INPUT_EDIT;
        updateViewStatus(mTargetLayer, true, true);
    }
    private void processRightKeyEvent() {
        int layer = mTargetLayer;
        ComposingText composingText = mComposingText;
        if (mExactMatchMode || (mEngineState.isConvertState())) {
            int textSize = composingText.size(ComposingText.LAYER1);
            if (composingText.getCursor(ComposingText.LAYER1) == textSize) {
                mExactMatchMode = false;
                layer = ComposingText.LAYER1; 
                EngineState state = new EngineState();
                state.convertType = EngineState.CONVERT_TYPE_NONE;
                updateEngineState(state);
            } else {
                if (mEngineState.isEisuKana()) {
                    mExactMatchMode = true;
                }
                composingText.moveCursor(ComposingText.LAYER1, 1);
            }
        } else {
            if (composingText.getCursor(ComposingText.LAYER1)
                    < composingText.size(ComposingText.LAYER1)) {
                composingText.moveCursor(ComposingText.LAYER1, 1);
            }
        }
        mCommitCount = 0; 
        mStatus = STATUS_INPUT_EDIT;
        updateViewStatus(layer, true, true);
    }
    boolean processKeyEventNoInputCandidateShown(KeyEvent ev) {
        boolean ret = true;
        switch (ev.getKeyCode()) {
        case KeyEvent.KEYCODE_DEL:
            ret = true;
            break;
        case KeyEvent.KEYCODE_ENTER:
        case KeyEvent.KEYCODE_DPAD_UP:
        case KeyEvent.KEYCODE_DPAD_DOWN:
        case KeyEvent.KEYCODE_MENU:
            ret = false;
            break;
        case KeyEvent.KEYCODE_CALL:
        case KeyEvent.KEYCODE_VOLUME_DOWN:
        case KeyEvent.KEYCODE_VOLUME_UP:
            return false;
        case KeyEvent.KEYCODE_DPAD_CENTER:
            ret = true;
            break;
        case KeyEvent.KEYCODE_BACK:
            if (mCandidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL) {
                mStatus &= ~STATUS_CANDIDATE_FULL;
                mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
                return true;
            } else {
                ret = true;
            }
            break;
        default:
            return true;
        }
        if (mConverter != null) {
            mConverter.init();
        }
        updateViewStatusForPrediction(true, true);
        return ret;
    }
    private void updateViewStatusForPrediction(boolean updateCandidates, boolean updateEmptyText) {
        EngineState state = new EngineState();
        state.convertType = EngineState.CONVERT_TYPE_NONE;
        updateEngineState(state);
        updateViewStatus(ComposingText.LAYER1, updateCandidates, updateEmptyText);
    }
    private void updateViewStatus(int layer, boolean updateCandidates, boolean updateEmptyText) {
        mTargetLayer = layer;
        if (updateCandidates) {
            updateCandidateView();
        }
        mInputViewManager.onUpdateState(this);
        mDisplayText.clear();
        mDisplayText.insert(0, mComposingText.toString(layer));
        int cursor = mComposingText.getCursor(layer);
        if ((mInputConnection != null) && (mDisplayText.length() != 0 || updateEmptyText)) {
            if (cursor != 0) {
                int highlightEnd = 0;
                if ((mExactMatchMode && (!mEngineState.isEisuKana()))
                    || (FIX_CURSOR_TEXT_END && isEnglishPrediction()
                        && (cursor < mComposingText.size(ComposingText.LAYER1)))){
                    mDisplayText.setSpan(SPAN_EXACT_BGCOLOR_HL, 0, cursor,
                                         Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    highlightEnd = cursor;
                } else if (FIX_CURSOR_TEXT_END && mEngineState.isEisuKana()) {
                    mDisplayText.setSpan(SPAN_EISUKANA_BGCOLOR_HL, 0, cursor,
                                         Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    highlightEnd = cursor;
                } else if (layer == ComposingText.LAYER2) {
                    highlightEnd = mComposingText.toString(layer, 0, 0).length();
                    mDisplayText.setSpan(SPAN_CONVERT_BGCOLOR_HL, 0,
                                         highlightEnd,
                                         Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (FIX_CURSOR_TEXT_END && (highlightEnd != 0)) {
                    mDisplayText.setSpan(SPAN_REMAIN_BGCOLOR_HL, highlightEnd,
                                         mComposingText.toString(layer).length(),
                                         Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mDisplayText.setSpan(SPAN_TEXTCOLOR, 0,
                                         mComposingText.toString(layer).length(),
                                         Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); 
                }
            }
            mDisplayText.setSpan(SPAN_UNDERLINE, 0, mDisplayText.length(),
                                 Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            int displayCursor = mComposingText.toString(layer, 0, cursor - 1).length();
            if (FIX_CURSOR_TEXT_END) {
                displayCursor = (cursor == 0) ?  0 : 1;
            } 
            if ((mDisplayText.length() != 0) || !mHasStartedTextSelection) {
                mInputConnection.setComposingText(mDisplayText, displayCursor);
            }
        }
    }
    private void updateCandidateView() {
        switch (mTargetLayer) {
        case ComposingText.LAYER0:
        case ComposingText.LAYER1: 
            if (mEnablePrediction || mEngineState.isSymbolList() || mEngineState.isEisuKana()) {
                if ((mComposingText.size(ComposingText.LAYER1) != 0)
                    && !mEngineState.isConvertState()) {
                    mHandler.removeMessages(MSG_PREDICTION);
                    if (mCandidatesViewManager.getCurrentView().isShown()) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PREDICTION),
                                                    PREDICTION_DELAY_MS_SHOWING_CANDIDATE);
                    } else {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PREDICTION),
                                                    PREDICTION_DELAY_MS_1ST);
                    }
                } else {
                    mHandler.removeMessages(MSG_PREDICTION);
                    updatePrediction();
                }
            } else {
                mHandler.removeMessages(MSG_PREDICTION);
                mCandidatesViewManager.clearCandidates();
            }
            break;
        case ComposingText.LAYER2: 
            if (mCommitCount == 0) {
                mHandler.removeMessages(MSG_PREDICTION);
                mConverter.convert(mComposingText);
            }
            int candidates = mConverter.makeCandidateListOf(mCommitCount);
            if (candidates != 0) {
                mComposingText.setCursor(ComposingText.LAYER2, 1);
                mCandidatesViewManager.displayCandidates(mConverter);
            } else {
                mComposingText.setCursor(ComposingText.LAYER1,
                                         mComposingText.toString(ComposingText.LAYER1).length());
                mCandidatesViewManager.clearCandidates();
            }
            break;
        default:
            break;
        }
    }
    private int commitText(boolean learn) {
        if (isEnglishPrediction()) {
            mComposingText.setCursor(ComposingText.LAYER1,
                                     mComposingText.size(ComposingText.LAYER1));
        }
        int layer = mTargetLayer;
        int cursor = mComposingText.getCursor(layer);
        if (cursor == 0) {
            return mStatus;
        }
        String tmp = mComposingText.toString(layer, 0, cursor - 1);
        if (mConverter != null) {
            if (learn) {
                if (mEngineState.isRenbun()) {
                    learnWord(0); 
                } else {
                    if (mComposingText.size(ComposingText.LAYER1) != 0) {
                        String stroke = mComposingText.toString(ComposingText.LAYER1, 0, mComposingText.getCursor(layer) - 1);
                        WnnWord word = new WnnWord(tmp, stroke);
                        learnWord(word);
                    }
                }
            } else {
                breakSequence();
            }
        }
        return commitTextThroughInputConnection(tmp);
    }
    private void commitTextWithoutLastAlphabet() {
        int layer = mTargetLayer;
        String tmp = mComposingText.getStrSegment(layer, -1).string;
        if (isAlphabetLast(tmp)) {
            mComposingText.moveCursor(ComposingText.LAYER1, -1);
            commitText(false);
            mComposingText.moveCursor(ComposingText.LAYER1, 1);
        } else {
            commitText(false);
        }
    }
    private void commitAllText() {
        initCommitInfoForWatchCursor();
        if (mEngineState.isConvertState()) {
            commitConvertingText();
        } else {
            mComposingText.setCursor(ComposingText.LAYER1,
                                     mComposingText.size(ComposingText.LAYER1));
            mStatus = commitText(true);
        }
        checkCommitInfo();
    }
    private int commitText(WnnWord word) {
        if (mConverter != null) {
            learnWord(word);
        }
        return commitTextThroughInputConnection(word.candidate);
    }
    private void commitText(String str) {
        mInputConnection.commitText(str, (FIX_CURSOR_TEXT_END ? 1 : str.length()));
        mPrevCommitText.append(str);
        mPrevCommitCount++;
        mEnableAutoDeleteSpace = true;
        updateViewStatusForPrediction(false, false);
    }
    private int commitTextThroughInputConnection(String string) {
        int layer = mTargetLayer;
        mInputConnection.commitText(string, (FIX_CURSOR_TEXT_END ? 1 : string.length()));
        mPrevCommitText.append(string);
        mPrevCommitCount++;
        int cursor = mComposingText.getCursor(layer);
        if (cursor > 0) {
            mComposingText.deleteStrSegment(layer, 0, mComposingText.getCursor(layer) - 1);
            mComposingText.setCursor(layer, mComposingText.size(layer));
        }
        mExactMatchMode = false;
        mCommitCount++;
        if ((layer == ComposingText.LAYER2) && (mComposingText.size(layer) == 0)) {
            layer = 1; 
        }
        boolean committed = autoCommitEnglish();
        mEnableAutoDeleteSpace = true;
        if (layer == ComposingText.LAYER2) {
            EngineState state = new EngineState();
            state.convertType = EngineState.CONVERT_TYPE_RENBUN;
            updateEngineState(state);
            updateViewStatus(layer, !committed, false);
        } else {
            updateViewStatusForPrediction(!committed, false);
        }
        if (mComposingText.size(ComposingText.LAYER0) == 0) {
            return STATUS_INIT;
        } else {
            return STATUS_INPUT_EDIT;
        }
    }
    private boolean isEnglishPrediction() {
        return (mEngineState.isEnglish() && isEnableL2Converter());
    }
    private void changeEngineMode(int mode) {
        EngineState state = new EngineState();
        switch (mode) {
        case ENGINE_MODE_OPT_TYPE_QWERTY:
            state.keyboard = EngineState.KEYBOARD_QWERTY;
            updateEngineState(state);
            clearCommitInfo();
            return;
        case ENGINE_MODE_OPT_TYPE_12KEY:
            state.keyboard = EngineState.KEYBOARD_12KEY;
            updateEngineState(state);
            clearCommitInfo();
            return;
        case ENGINE_MODE_EISU_KANA:
            if (mEngineState.isEisuKana()) {
                state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
                updateEngineState(state);
                updateViewStatusForPrediction(true, true); 
            } else {
                startConvert(EngineState.CONVERT_TYPE_EISU_KANA);
            }
            return;
        case ENGINE_MODE_SYMBOL:
            if (mEnableSymbolList && !mDirectInputMode) {
                state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_SYMBOL;
                updateEngineState(state);
                updateViewStatusForPrediction(true, true);
            }
            return;
        default:
            break;
        }
        state = new EngineState();
        state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
        updateEngineState(state);
        state = new EngineState();
        switch (mode) {
        case OpenWnnEvent.Mode.DIRECT:
            mConverter = null;
            mPreConverter = null;
            break;
        case OpenWnnEvent.Mode.NO_LV1_CONV:
            state.dictionarySet = EngineState.DICTIONARYSET_EN;
            updateEngineState(state);
            mConverter = mConverterEN;
            mPreConverter = null;
            break;
        case OpenWnnEvent.Mode.NO_LV2_CONV:
            mConverter = null;
            mPreConverter = mPreConverterHiragana;
            break;
        case ENGINE_MODE_FULL_KATAKANA:
            mConverter = null;
            mPreConverter = mPreConverterFullKatakana;
            break;
        case ENGINE_MODE_HALF_KATAKANA:
            mConverter = null;
            mPreConverter = mPreConverterHalfKatakana;
            break;
        default:
            state.dictionarySet = EngineState.DICTIONARYSET_JP;
            updateEngineState(state);
            mConverter = mConverterJAJP;
            mPreConverter = mPreConverterHiragana;
            break;
        }
        mPreConverterBack = mPreConverter;
        mConverterBack = mConverter;
    }
    private void updateEngineState(EngineState state) {
        EngineState myState = mEngineState;
        if ((state.dictionarySet != EngineState.INVALID) 
            && (myState.dictionarySet != state.dictionarySet)) {
            switch (state.dictionarySet) {
            case EngineState.DICTIONARYSET_EN:
                setDictionary(OpenWnnEngineJAJP.DIC_LANG_EN);
                break;
            case EngineState.DICTIONARYSET_JP:
            default:
                setDictionary(OpenWnnEngineJAJP.DIC_LANG_JP);
                break;
            }
            myState.dictionarySet = state.dictionarySet;
            breakSequence();
            if (state.keyboard == EngineState.INVALID) {
                state.keyboard = myState.keyboard;
            }
        }
        if ((state.convertType != EngineState.INVALID)
            && (myState.convertType != state.convertType)) {
            switch (state.convertType) {
            case EngineState.CONVERT_TYPE_NONE:
                setDictionary(mPrevDictionarySet);
                break;
            case EngineState.CONVERT_TYPE_EISU_KANA:
                setDictionary(OpenWnnEngineJAJP.DIC_LANG_JP_EISUKANA);
                break;
            case EngineState.CONVERT_TYPE_RENBUN:
            default:
                setDictionary(OpenWnnEngineJAJP.DIC_LANG_JP);
                break;
            }
            myState.convertType = state.convertType;
        }
        if (state.temporaryMode != EngineState.INVALID) {
            switch (state.temporaryMode) {
            case EngineState.TEMPORARY_DICTIONARY_MODE_NONE:
                if (myState.temporaryMode != EngineState.TEMPORARY_DICTIONARY_MODE_NONE) {
                    setDictionary(mPrevDictionarySet);
                    mCurrentSymbol = 0;
                    mPreConverter = mPreConverterBack;
                    mConverter = mConverterBack;
                    mDisableAutoCommitEnglishMask &= ~AUTO_COMMIT_ENGLISH_SYMBOL;
                }
                break;
            case EngineState.TEMPORARY_DICTIONARY_MODE_SYMBOL:
                if (++mCurrentSymbol >= SYMBOL_LISTS.length) {
                    mCurrentSymbol = 0;
                }
                if (mEnableSymbolListNonHalf) {
                    mConverterSymbolEngineBack.setDictionary(SYMBOL_LISTS[mCurrentSymbol]);
                } else {
                    mConverterSymbolEngineBack.setDictionary(SymbolList.SYMBOL_ENGLISH);
                }
                mConverter = mConverterSymbolEngineBack;
                mDisableAutoCommitEnglishMask |= AUTO_COMMIT_ENGLISH_SYMBOL;
                breakSequence();
                break;
            default:
                break;
            }
            myState.temporaryMode = state.temporaryMode;
        }
        if ((state.preferenceDictionary != EngineState.INVALID) 
            && (myState.preferenceDictionary != state.preferenceDictionary)) {
            myState.preferenceDictionary = state.preferenceDictionary;
            setDictionary(mPrevDictionarySet);
        }
        if (state.keyboard != EngineState.INVALID) {
            switch (state.keyboard) {
            case EngineState.KEYBOARD_12KEY:
                mConverterJAJP.setKeyboardType(OpenWnnEngineJAJP.KEYBOARD_KEYPAD12);
                mConverterEN.setDictionary(OpenWnnEngineEN.DICT_DEFAULT);
                break;
            case EngineState.KEYBOARD_QWERTY:
            default:
                mConverterJAJP.setKeyboardType(OpenWnnEngineJAJP.KEYBOARD_QWERTY);
                if (mEnableSpellCorrection) {
                    mConverterEN.setDictionary(OpenWnnEngineEN.DICT_FOR_CORRECT_MISTYPE);
                } else {
                    mConverterEN.setDictionary(OpenWnnEngineEN.DICT_DEFAULT);
                }
                break;
            }
            myState.keyboard = state.keyboard;
        }
    }
    private void setDictionary(int mode) {
        int target = mode;
        switch (target) {
        case OpenWnnEngineJAJP.DIC_LANG_JP:
            switch (mEngineState.preferenceDictionary) {
            case EngineState.PREFERENCE_DICTIONARY_PERSON_NAME:
                target = OpenWnnEngineJAJP.DIC_LANG_JP_PERSON_NAME;
                break;
            case EngineState.PREFERENCE_DICTIONARY_POSTAL_ADDRESS:
                target = OpenWnnEngineJAJP.DIC_LANG_JP_POSTAL_ADDRESS;
                break;
            default:
                break;
            }
            break;
        case OpenWnnEngineJAJP.DIC_LANG_EN:
            switch (mEngineState.preferenceDictionary) {
            case EngineState.PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI:
                target = OpenWnnEngineJAJP.DIC_LANG_EN_EMAIL_ADDRESS;
                break;
            default:
                break;
            }
            break;
        default:
            break;
        }
        switch (mode) {
        case OpenWnnEngineJAJP.DIC_LANG_JP:
        case OpenWnnEngineJAJP.DIC_LANG_EN:
            mPrevDictionarySet = mode;
            break;
        default:
            break;
        }
        mConverterJAJP.setDictionary(target);
    }
    private void processSoftKeyboardToggleChar(String[] table) {
        if (table == null) {
            return;
        }
        commitConvertingText();
        boolean toggled = false;
        if ((mStatus & ~STATUS_CANDIDATE_FULL) == STATUS_INPUT) {
            int cursor = mComposingText.getCursor(ComposingText.LAYER1);
            if (cursor > 0) {
                String prevChar = mComposingText.getStrSegment(ComposingText.LAYER1,
                                                               cursor - 1).string;
                String c = searchToggleCharacter(prevChar, table, false);
                if (c != null) {
                    mComposingText.delete(ComposingText.LAYER1, false);
                    appendStrSegment(new StrSegment(c));
                    toggled = true;
                }
            }
        }
        if (!toggled) {
            if (!isEnableL2Converter()) {
                commitText(false);
            }
            String str = table[0];
            if (mAutoCaps && (getShiftKeyState(getCurrentInputEditorInfo()) == 1)) {
                char top = table[0].charAt(0);
                if (Character.isLowerCase(top)) {
                    str = Character.toString(Character.toUpperCase(top));
                }
            } 
            appendStrSegment(new StrSegment(str));
        }
        mStatus = STATUS_INPUT;
        updateViewStatusForPrediction(true, true);
    }
    private void processSoftKeyboardCodeWithoutConversion(char[] chars) {
        if (chars == null) {
            return;
        }
        ComposingText text = mComposingText;
        appendStrSegment(new StrSegment(chars));
        if (!isAlphabetLast(text.toString(ComposingText.LAYER1))) {
            commitText(false);
        } else {
            boolean completed = mPreConverter.convert(text);
            if (completed) {
                commitTextWithoutLastAlphabet();
            } else {
                mStatus = STATUS_INPUT;
                updateViewStatusForPrediction(true, true);
            }
        }
    }
    private void processSoftKeyboardCode(char[] chars) {
        if (chars == null) {
            return;
        }
        if ((chars[0] == ' ') || (chars[0] == '\u3000' )) {
            if (mComposingText.size(0) == 0) {
                mCandidatesViewManager.clearCandidates();
                commitText(new String(chars));
                breakSequence();
            } else {
                if (isEnglishPrediction()) {
                    initCommitInfoForWatchCursor();
                    commitText(true);
                    commitSpaceJustOne();
                    checkCommitInfo();
                } else {
                    startConvert(EngineState.CONVERT_TYPE_RENBUN);
                }
            }
            mEnableAutoDeleteSpace = false;
        } else {
            commitConvertingText();
            boolean commit = false;
            if (isEnglishPrediction()
                && (mEngineState.keyboard == EngineState.KEYBOARD_QWERTY)) {
                Matcher m = mEnglishAutoCommitDelimiter.matcher(new String(chars));
                if (m.matches()) {
                    commit = true;
                }
            }
            if (commit) {
                commitText(true);
                appendStrSegment(new StrSegment(chars));
                commitText(true);
            } else {
                appendStrSegment(new StrSegment(chars));
                if (mPreConverter != null) {
                    mPreConverter.convert(mComposingText);
                    mStatus = STATUS_INPUT;
                }
                updateViewStatusForPrediction(true, true);
            }
        }
    }
    private void startConvert(int convertType) {
        if (!isEnableL2Converter()) {
            return;
        }
        if (mEngineState.convertType != convertType) {
            if (!mExactMatchMode) {
                if (convertType == EngineState.CONVERT_TYPE_RENBUN) {
                    mComposingText.setCursor(ComposingText.LAYER1, 0);
                } else {
                    if (mEngineState.isRenbun()) {
                        mExactMatchMode = true;
                    } else {
                        mComposingText.setCursor(ComposingText.LAYER1,
                                                 mComposingText.size(ComposingText.LAYER1));
                    }
                }
            } 
            if (convertType == EngineState.CONVERT_TYPE_RENBUN) {
                mExactMatchMode = false;
            }
            mCommitCount = 0;
            int layer;
            if (convertType == EngineState.CONVERT_TYPE_EISU_KANA) {
                layer = ComposingText.LAYER1;
            } else {
                layer = ComposingText.LAYER2;
            }
            EngineState state = new EngineState();
            state.convertType = convertType;
            updateEngineState(state);
            updateViewStatus(layer, true, true);
        }
    }
    private boolean autoCommitEnglish() {
        if (isEnglishPrediction() && (mDisableAutoCommitEnglishMask == AUTO_COMMIT_ENGLISH_ON)) {
            CharSequence seq = mInputConnection.getTextBeforeCursor(2, 0);
            Matcher m = mEnglishAutoCommitDelimiter.matcher(seq);
            if (m.matches()) {
                if ((seq.charAt(0) == ' ') && mEnableAutoDeleteSpace) {
                    mInputConnection.deleteSurroundingText(2, 0);
                    CharSequence str = seq.subSequence(1, 2);
                    mInputConnection.commitText(str, 1);
                    mPrevCommitText.append(str);
                    mPrevCommitCount++;
                }
                mHandler.removeMessages(MSG_PREDICTION);
                mCandidatesViewManager.clearCandidates();
                return true;
            }
        }
        return false;
    }
    private void commitSpaceJustOne() {
        CharSequence seq = mInputConnection.getTextBeforeCursor(1, 0);
        if (seq.charAt(0) != ' ') {
            commitText(" ");
        }
    }
    protected int getShiftKeyState(EditorInfo editor) {
        return (getCurrentInputConnection().getCursorCapsMode(editor.inputType) == 0) ? 0 : 1;
    }
    private void updateMetaKeyStateDisplay() {
        int mode = 0;
        if(mHardShift == 0 && mHardAlt == 0){
            mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_OFF_ALT_OFF;
        }else if(mHardShift == 1 && mHardAlt == 0){
            mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_ON_ALT_OFF;
        }else if(mHardShift == 2  && mHardAlt == 0){
            mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_LOCK_ALT_OFF;
        }else if(mHardShift == 0 && mHardAlt == 1){
            mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_OFF_ALT_ON;
        }else if(mHardShift == 0 && mHardAlt == 2){
            mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_OFF_ALT_LOCK;
        }else if(mHardShift == 1 && mHardAlt == 1){
            mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_ON_ALT_ON;
        }else if(mHardShift == 1 && mHardAlt == 2){
            mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_ON_ALT_LOCK;
        }else if(mHardShift == 2 && mHardAlt == 1){
            mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_LOCK_ALT_ON;
        }else if(mHardShift == 2 && mHardAlt == 2){
            mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_LOCK_ALT_LOCK;
        }else{
            mode = DefaultSoftKeyboard.HARD_KEYMODE_SHIFT_OFF_ALT_OFF;
        }
        ((DefaultSoftKeyboard) mInputViewManager).updateIndicator(mode);
    }
    private void learnWord(WnnWord word) {
        if (mEnableLearning && word != null) {
            mConverter.learn(word);
        }
    }
    private void learnWord(int index) {
        ComposingText composingText = mComposingText;
        if (mEnableLearning && composingText.size(ComposingText.LAYER2) > index) {
            StrSegment seg = composingText.getStrSegment(ComposingText.LAYER2, index);
            if (seg instanceof StrSegmentClause) {
                mConverter.learn(((StrSegmentClause)seg).clause);
            } else {
                String stroke = composingText.toString(ComposingText.LAYER1, seg.from, seg.to);
                mConverter.learn(new WnnWord(seg.string, stroke));
            }
        }
    }
    private void fitInputType(SharedPreferences preference, EditorInfo info) {
        if (info.inputType == EditorInfo.TYPE_NULL) {
            mDirectInputMode = true;
            return;
        }
        mEnableLearning   = preference.getBoolean("opt_enable_learning", true);
        mEnablePrediction = preference.getBoolean("opt_prediction", true);
        mEnableSpellCorrection = preference.getBoolean("opt_spell_correction", true);
        mDisableAutoCommitEnglishMask &= ~AUTO_COMMIT_ENGLISH_OFF;
        int preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_NONE;
        mEnableConverter = true;
        mEnableSymbolList = true;
        mEnableSymbolListNonHalf = true;
        mAutoCaps = preference.getBoolean("auto_caps", true);
        mFilter.filter = 0;
        mEnableAutoInsertSpace = true;
        mEnableAutoHideKeyboard = false;
        switch (info.inputType & EditorInfo.TYPE_MASK_CLASS) {
        case EditorInfo.TYPE_CLASS_NUMBER:
        case EditorInfo.TYPE_CLASS_DATETIME:
            mEnableConverter = false;
            break;
        case EditorInfo.TYPE_CLASS_PHONE:
            mEnableSymbolList = false;
            mEnableConverter = false;
            break;
        case EditorInfo.TYPE_CLASS_TEXT:
            switch (info.inputType & EditorInfo.TYPE_MASK_VARIATION) {
            case EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME:
                preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_PERSON_NAME;
                break;
            case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
            case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                mEnableLearning = false;
                mEnableConverter = false;
                mEnableSymbolListNonHalf = false;
                mFilter.filter = CandidateFilter.FILTER_NON_ASCII; 
                mDisableAutoCommitEnglishMask |= AUTO_COMMIT_ENGLISH_OFF;
                break;
            case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                mEnableAutoInsertSpace = false;
                mDisableAutoCommitEnglishMask |= AUTO_COMMIT_ENGLISH_OFF;
                preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI;
                break;
            case EditorInfo.TYPE_TEXT_VARIATION_URI:
                mEnableAutoInsertSpace = false;
                mDisableAutoCommitEnglishMask |= AUTO_COMMIT_ENGLISH_OFF;
                preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_EMAIL_ADDRESS_URI;
                break;
            case EditorInfo.TYPE_TEXT_VARIATION_POSTAL_ADDRESS:
                preferenceDictionary = EngineState.PREFERENCE_DICTIONARY_POSTAL_ADDRESS;
                break;
            case EditorInfo.TYPE_TEXT_VARIATION_PHONETIC:
                mEnableLearning = false;
                mEnableConverter = false;
                mEnableSymbolList = false;
                break;
            default:
                break;
            }
            break;
        default:
            break;
        }
        if (mFilter.filter == 0) {
            mConverterEN.setFilter(null);
            mConverterJAJP.setFilter(null);
        } else {
            mConverterEN.setFilter(mFilter);
            mConverterJAJP.setFilter(mFilter);
        }
        EngineState state = new EngineState();
        state.preferenceDictionary = preferenceDictionary;
        state.convertType = EngineState.CONVERT_TYPE_NONE;
        state.keyboard = mEngineState.keyboard;
        updateEngineState(state);
        updateMetaKeyStateDisplay();
        checkTutorial(info.privateImeOptions);
    }
    private void appendStrSegment(StrSegment str) {
        ComposingText composingText = mComposingText;
        if (composingText.size(ComposingText.LAYER1) >= LIMIT_INPUT_NUMBER) {
            return; 
        }
        composingText.insertStrSegment(ComposingText.LAYER0, ComposingText.LAYER1, str);
        return;
    }
    private void commitConvertingText() {
        if (mEngineState.isConvertState()) {
            int size = mComposingText.size(ComposingText.LAYER2);
            for (int i = 0; i < size; i++) {
                learnWord(i);
            }
            String text = mComposingText.toString(ComposingText.LAYER2);
            mInputConnection.commitText(text, (FIX_CURSOR_TEXT_END ? 1 : text.length()));
            mPrevCommitText.append(text);
            mPrevCommitCount++;
            initializeScreen();
        }
    }
    private void initializeScreen() {
        if (mComposingText.size(ComposingText.LAYER0) != 0) {
            mInputConnection.setComposingText("", 0);
        }
        mComposingText.clear();
        mExactMatchMode = false;       
        mStatus = STATUS_INIT;
        mHandler.removeMessages(MSG_PREDICTION);
        View candidateView = mCandidatesViewManager.getCurrentView();
        if ((candidateView != null) && candidateView.isShown()) {
            mCandidatesViewManager.clearCandidates();
        }
        mInputViewManager.onUpdateState(this);
        EngineState state = new EngineState();
        state.temporaryMode = EngineState.TEMPORARY_DICTIONARY_MODE_NONE;
        updateEngineState(state);
    }
    private boolean isAlphabetLast(String str) {
        Matcher m = ENGLISH_CHARACTER_LAST.matcher(str);
        return m.matches();
    }
    @Override public void onFinishInput() {
        if (mInputConnection != null) {
            initializeScreen();
        }
        super.onFinishInput();
    }
    private boolean isEnableL2Converter() {
        if (mConverter == null || !mEnableConverter) {
            return false;
        }
        if (mEngineState.isEnglish() && !mEnablePrediction) {
            return false;
        }
        return true;
    }
    private void onKeyUpEvent(KeyEvent ev) {
        int key = ev.getKeyCode();
        if(!mShiftPressing){
            if(key == KeyEvent.KEYCODE_SHIFT_LEFT || key == KeyEvent.KEYCODE_SHIFT_RIGHT){
                mHardShift = 0;
                mShiftPressing = true;
                updateMetaKeyStateDisplay();
            }
        }
        if(!mAltPressing ){
            if(key == KeyEvent.KEYCODE_ALT_LEFT || key == KeyEvent.KEYCODE_ALT_RIGHT){
                mHardAlt = 0;
                mAltPressing   = true;
                updateMetaKeyStateDisplay();
            }
        }
    }
    private void initCommitInfoForWatchCursor() {
        if (!isEnableL2Converter()) {
            return;
        }
        mCommitStartCursor = mComposingStartCursor;
        mPrevCommitText.delete(0, mPrevCommitText.length());
    }
    private boolean clearCommitInfo() {
        if (mCommitStartCursor < 0) {
            return false;
        }
        mCommitStartCursor = -1;
        return true;
    }
    private void checkCommitInfo() {
        if (mCommitStartCursor < 0) {
            return;
        }
        int composingLength = mComposingText.toString(mTargetLayer).length();
        CharSequence seq = mInputConnection.getTextBeforeCursor(mPrevCommitText.length() + composingLength, 0);
        seq = seq.subSequence(0, seq.length() - composingLength);
        if (!seq.equals(mPrevCommitText.toString())) {
            mPrevCommitCount = 0;
            clearCommitInfo();
        }
    }
    private void checkTutorial(String privateImeOptions) {
        if (privateImeOptions == null) return;
        if (privateImeOptions.equals("com.android.setupwizard:ShowTutorial")) {
            if ((mTutorial == null) && mEnableTutorial) startTutorial();
        } else if (privateImeOptions.equals("com.android.setupwizard:HideTutorial")) {
            if (mTutorial != null) {
                if (mTutorial.close()) {
                    mTutorial = null;
                }
            }
        }
    }
    private void startTutorial() {
        DefaultSoftKeyboardJAJP manager = (DefaultSoftKeyboardJAJP) mInputViewManager;
        manager.setDefaultKeyboard();
        if (mEngineState.keyboard == EngineState.KEYBOARD_QWERTY) {
            manager.changeKeyboardType(DefaultSoftKeyboard.KEYBOARD_12KEY);
        }
        DefaultSoftKeyboardJAJP inputManager = ((DefaultSoftKeyboardJAJP) mInputViewManager);
        View v = inputManager.getKeyboardView();
        v.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }});
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_START_TUTORIAL), 500);
    }
    public void tutorialDone() {
        mTutorial = null;
    }
    @Override protected void close() {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_CLOSE), 0);
    }
    private void breakSequence() {
        mEnableAutoDeleteSpace = false;
        mConverterJAJP.breakSequence();
        mConverterEN.breakSequence();
    }
}
