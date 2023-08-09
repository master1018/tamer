public class OpenWnnEN extends OpenWnn {
    private static final char[] SPACE = {' '};
    private static final CharacterStyle SPAN_UNDERLINE   = new UnderlineSpan();
    private static final CharacterStyle SPAN_EXACT_BGCOLOR_HL     = new BackgroundColorSpan(0xFF66CDAA);
    private static final CharacterStyle SPAN_REMAIN_BGCOLOR_HL    = new BackgroundColorSpan(0xFFF0FFFF);
    private static final CharacterStyle SPAN_TEXTCOLOR  = new ForegroundColorSpan(0xFF000000);
    private static final int PRIVATE_AREA_CODE = 61184;
    private static final boolean FIX_CURSOR_TEXT_END = true;
    protected SpannableStringBuilder mDisplayText;
    private String mWordSeparators;
    private int mPreviousEventCode;
    private WnnWord[] mUserDictionaryWords = null;
    private OpenWnnEngineEN mConverterEN;
    private SymbolList mSymbolList;
    private boolean mSymbolMode;
    private boolean mOptPrediction;
    private boolean mOptSpellCorrection;
    private boolean mOptLearning;
    private int mHardShift;
    private boolean mShiftPressing;
    private int mHardAlt;
    private boolean mAltPressing;
    private static OpenWnnEN mSelf = null;
    private static final int[] mShiftKeyToggle = {0, MetaKeyKeyListener.META_SHIFT_ON, MetaKeyKeyListener.META_CAP_LOCKED};
    private static final int[] mAltKeyToggle = {0, MetaKeyKeyListener.META_ALT_ON, MetaKeyKeyListener.META_ALT_LOCKED};
    private boolean mAutoCaps = false;
    private boolean mEnableAutoHideKeyboard = true;
    private TutorialEN mTutorial;
    private boolean mEnableTutorial;
    private static final int MSG_PREDICTION = 0;
    private static final int MSG_START_TUTORIAL = 1;
    private static final int MSG_CLOSE = 2;
    private static final int PREDICTION_DELAY_MS_1ST = 200;
    private static final int PREDICTION_DELAY_MS_SHOWING_CANDIDATE = 200;
    Handler mHandler = new Handler() {
            @Override public void handleMessage(Message msg) {
                switch (msg.what) {
                case MSG_PREDICTION:
                    updatePrediction();
                    break;
                case MSG_START_TUTORIAL:
                    if (mTutorial == null) {
                        if (isInputViewShown()) {
                            DefaultSoftKeyboardEN inputManager = ((DefaultSoftKeyboardEN) mInputViewManager);
                            View v = inputManager.getKeyboardView();
                            mTutorial = new TutorialEN(OpenWnnEN.this, v, inputManager);
                            mTutorial.start();
                        } else {
                            sendMessageDelayed(obtainMessage(MSG_START_TUTORIAL), 100);
                        }
                    }
                    break;
                case MSG_CLOSE:
                    if (mConverterEN != null) mConverterEN.close();
                    if (mSymbolList != null) mSymbolList.close();
                    break;
                }
            }
        };
    public OpenWnnEN() {
        super();
        mSelf = this;
        mComposingText = new ComposingText();
        mCandidatesViewManager = new TextCandidatesViewManager(-1);
        mInputViewManager = new DefaultSoftKeyboardEN();
        mConverterEN = new OpenWnnEngineEN("/data/data/jp.co.omronsoft.openwnn/writableEN.dic");
        mConverter = mConverterEN;
        mSymbolList = null;
        mDisplayText = new SpannableStringBuilder();
        mAutoHideMode = false;
        mSymbolMode = false;
        mOptPrediction = true;
        mOptSpellCorrection = true;
        mOptLearning = true;
    }
    public OpenWnnEN(Context context) {
        this();
        attachBaseContext(context);
    }
    public static OpenWnnEN getInstance() {
        return mSelf;
    }
    private void insertCharToComposingText(char[] chars) {
        StrSegment seg = new StrSegment(chars);
        if (chars[0] == SPACE[0] || chars[0] == '\u0009') {
            commitText(1);
            commitText(seg.string);
            mComposingText.clear();
        } else if (mWordSeparators.contains(seg.string)) {
            if (mPreviousEventCode == OpenWnnEvent.SELECT_CANDIDATE) {
                mInputConnection.deleteSurroundingText(1, 0);
            }
            commitText(1);
            commitText(seg.string);
            mComposingText.clear();
        } else {
            mComposingText.insertStrSegment(0, 1, seg);
            updateComposingText(1);
        }
    }
    private boolean insertCharToComposingText(int charCode) {
        if (charCode == 0) {
            return false;
        }
        insertCharToComposingText(Character.toChars(charCode));
        return true;
    }
    protected int getShiftKeyState(EditorInfo editor) {
        return (getCurrentInputConnection().getCursorCapsMode(editor.inputType) == 0) ? 0 : 1;
    }
    private void setSymbolMode(String mode) {
        if (mode != null) {
            mHandler.removeMessages(MSG_PREDICTION);
            mSymbolMode = true;
            mSymbolList.setDictionary(mode);
            mConverter = mSymbolList;
        } else {
            if (!mSymbolMode) {
                return;
            }
            mHandler.removeMessages(MSG_PREDICTION);
            mSymbolMode = false;
            mConverter = mConverterEN;
        }
    }
    @Override public void onCreate() {
        super.onCreate();
        mWordSeparators = getResources().getString(R.string.en_word_separators);
        if (mSymbolList == null) {
            mSymbolList = new SymbolList(this, SymbolList.LANG_EN);
        }
    }
    @Override public View onCreateInputView() {
        int hiddenState = getResources().getConfiguration().hardKeyboardHidden;
        boolean hidden = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
        ((DefaultSoftKeyboardEN) mInputViewManager).setHardKeyboardHidden(hidden);
        mEnableTutorial = hidden;
        return super.onCreateInputView();
    }
    @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        mCandidatesViewManager.clearCandidates();
        mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_CLOSE);
        mHardShift = 0;
        mHardAlt   = 0;
        updateMetaKeyStateDisplay();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mAutoCaps = pref.getBoolean("auto_caps", true);
        ((TextCandidatesViewManager)mCandidatesViewManager).setAutoHide(true);
        showStatusIcon(R.drawable.immodeic_half_alphabet);
        if (mComposingText != null) {
            mComposingText.clear();
        }
        fitInputType(pref, attribute);
        ((DefaultSoftKeyboard) mInputViewManager).resetCurrentKeyboard();
    }
    @Override public void hideWindow() {
        mComposingText.clear();
        mInputViewManager.onUpdateState(this);
        mHandler.removeMessages(MSG_START_TUTORIAL);
        mInputViewManager.closing();
        if (mTutorial != null) {
            mTutorial.close();
            mTutorial = null;
        }
        super.hideWindow();
    }
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
            int newSelStart, int newSelEnd, int candidatesStart,
            int candidatesEnd) {
        boolean isNotComposing = ((candidatesStart < 0) && (candidatesEnd < 0));
        if (isNotComposing) {
            mComposingText.clear();
            updateComposingText(1);
        } else {
            if (mComposingText.size(1) != 0) {
                updateComposingText(1);
            }
        }
    }
    @Override public void onConfigurationChanged(Configuration newConfig) {
        try {
            super.onConfigurationChanged(newConfig);
            if (mInputConnection != null) {
                updateComposingText(1);
            }
            int hiddenState = newConfig.hardKeyboardHidden;
            boolean hidden = (hiddenState == Configuration.HARDKEYBOARDHIDDEN_YES);
            mEnableTutorial = hidden;
        } catch (Exception ex) {
        }
    }
    @Override public boolean onEvaluateFullscreenMode() {
        return false;
    }
    @Override public boolean onEvaluateInputViewShown() {
        return true;
    }
    @Override synchronized public boolean onEvent(OpenWnnEvent ev) {
        switch (ev.code) {
        case OpenWnnEvent.KEYUP:
            onKeyUpEvent(ev.keyEvent);
            return true;
        case OpenWnnEvent.INITIALIZE_LEARNING_DICTIONARY:
            return mConverterEN.initializeDictionary( WnnEngine.DICTIONARY_TYPE_LEARN );
        case OpenWnnEvent.INITIALIZE_USER_DICTIONARY:
            return mConverterEN.initializeDictionary( WnnEngine.DICTIONARY_TYPE_USER );
        case OpenWnnEvent.LIST_WORDS_IN_USER_DICTIONARY:
            mUserDictionaryWords = mConverterEN.getUserDictionaryWords( );
            return true;
        case OpenWnnEvent.GET_WORD:
            if( mUserDictionaryWords != null ) {
                ev.word = mUserDictionaryWords[ 0 ];
                for( int i = 0 ; i < mUserDictionaryWords.length-1 ; i++ ) {
                    mUserDictionaryWords[ i ] = mUserDictionaryWords[ i + 1 ];
                }
                mUserDictionaryWords[ mUserDictionaryWords.length-1 ] = null;
                if( mUserDictionaryWords[ 0 ] == null ) {
                    mUserDictionaryWords = null;
                }
                return true;
            }
            break;
        case OpenWnnEvent.ADD_WORD:
            mConverterEN.addWord(ev.word);
            return true;
        case OpenWnnEvent.DELETE_WORD:
            mConverterEN.deleteWord(ev.word);
            return true;
        case OpenWnnEvent.CHANGE_MODE:
            return false;
        case OpenWnnEvent.UPDATE_CANDIDATE:
            updateComposingText(ComposingText.LAYER1);
            return true;
        case OpenWnnEvent.CHANGE_INPUT_VIEW:
            setInputView(onCreateInputView());
            return true;
        case OpenWnnEvent.CANDIDATE_VIEW_TOUCH:
            boolean ret;
                ret = ((TextCandidatesViewManager)mCandidatesViewManager).onTouchSync();
            return ret;
        default:
            break;
        }
        dismissPopupKeyboard();
        KeyEvent keyEvent = ev.keyEvent;
        int keyCode = 0;
        if (keyEvent != null) {
            keyCode = keyEvent.getKeyCode();
        }
        if (mDirectInputMode) {
            if (ev.code == OpenWnnEvent.INPUT_SOFT_KEY && mInputConnection != null) {
                mInputConnection.sendKeyEvent(keyEvent);
                mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                                                           keyEvent.getKeyCode()));
            }
            return false;
        }
        if (ev.code == OpenWnnEvent.LIST_CANDIDATES_FULL) {
            mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_FULL);
            return true;
        } else if (ev.code == OpenWnnEvent.LIST_CANDIDATES_NORMAL) {
            mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
            return true;
        }
        boolean ret = false;
        switch (ev.code) {
        case OpenWnnEvent.INPUT_CHAR:
             ((TextCandidatesViewManager)mCandidatesViewManager).setAutoHide(false);
            EditorInfo edit = getCurrentInputEditorInfo();
            if( edit.inputType == EditorInfo.TYPE_CLASS_PHONE){
                commitText(new String(ev.chars));           
            }else{
                setSymbolMode(null);
                insertCharToComposingText(ev.chars);
                ret = true;
                mPreviousEventCode = ev.code;
            }
            break;
        case OpenWnnEvent.INPUT_KEY:
            keyCode = ev.keyEvent.getKeyCode();
            switch (keyCode) {
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
                if (ev.keyEvent.getRepeatCount() == 0) {
                    if (++mHardAlt > 2) { mHardAlt = 0; }
                }
                mAltPressing   = true;
                updateMetaKeyStateDisplay();
                return true;
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                if (ev.keyEvent.getRepeatCount() == 0) {
                    if (++mHardShift > 2) { mHardShift = 0; }
                }
                mShiftPressing = true;
                updateMetaKeyStateDisplay();
                return true;
            }
            setSymbolMode(null);
            updateComposingText(1);
            ret = processKeyEvent(ev.keyEvent);
            mPreviousEventCode = ev.code;
            break;
        case OpenWnnEvent.INPUT_SOFT_KEY:
            setSymbolMode(null);
            updateComposingText(1);
            ret = processKeyEvent(ev.keyEvent);
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
            mPreviousEventCode = ev.code;
            break;
        case OpenWnnEvent.SELECT_CANDIDATE:
            if (mSymbolMode) {
                commitText(ev.word, false);
            } else {
                if (mWordSeparators.contains(ev.word.candidate) && 
                    mPreviousEventCode == OpenWnnEvent.SELECT_CANDIDATE) {
                    mInputConnection.deleteSurroundingText(1, 0);
                }
                commitText(ev.word, true);
            }
            mComposingText.clear();
            mPreviousEventCode = ev.code;
            updateComposingText(1);
            break;
        case OpenWnnEvent.LIST_SYMBOLS:
            commitText(1);
            mComposingText.clear();
            setSymbolMode(SymbolList.SYMBOL_ENGLISH);
            updateComposingText(1);
            break;
        default:
            break;
        }
        if (mCandidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL) {
        	mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
        }
        return ret;
    }
    private boolean processKeyEvent(KeyEvent ev) {
        int key = ev.getKeyCode();
        EditorInfo edit = getCurrentInputEditorInfo();
        if (ev.isPrintingKey()) {
            if ((mHardShift > 0 && mHardAlt > 0) || (ev.isAltPressed() && ev.isShiftPressed())) {
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
            ((TextCandidatesViewManager)mCandidatesViewManager).setAutoHide(false);
            if (mHardShift== 0  && mHardAlt == 0) {
                int shift = (mAutoCaps) ? getShiftKeyState(edit) : 0;
                if (shift != mHardShift && (key >= KeyEvent.KEYCODE_A && key <= KeyEvent.KEYCODE_Z)) {
                    insertCharToComposingText(ev.getUnicodeChar(MetaKeyKeyListener.META_SHIFT_ON));
                } else {
                    insertCharToComposingText(ev.getUnicodeChar());
                }
            } else {
                insertCharToComposingText(ev.getUnicodeChar(mShiftKeyToggle[mHardShift]
                                                            | mAltKeyToggle[mHardAlt]));
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
            }
            if (edit.inputType == EditorInfo.TYPE_CLASS_PHONE) {
                commitText(1);
                mComposingText.clear();
                return true;
            }
            return true;
        } else if (key == KeyEvent.KEYCODE_SPACE) {
            if (ev.isAltPressed()) {
                commitText(1);
                mComposingText.clear();
                setSymbolMode(SymbolList.SYMBOL_ENGLISH);
                updateComposingText(1);     
                mHardAlt = 0;
                updateMetaKeyStateDisplay();
            } else {
                insertCharToComposingText(SPACE);   
            }
            return true;
        } else if (key == KeyEvent.KEYCODE_SYM) {
            commitText(1);
            mComposingText.clear();
            setSymbolMode(SymbolList.SYMBOL_ENGLISH);
            updateComposingText(1);     
            mHardAlt = 0;
            updateMetaKeyStateDisplay();
        } 
        if (mComposingText.size(1) > 0) {
            switch (key) {
            case KeyEvent.KEYCODE_DEL:
                mComposingText.delete(1, false);
                updateComposingText(1);
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (mCandidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL) {
                    mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
                } else {
                    mComposingText.clear();
                    updateComposingText(1);
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mComposingText.moveCursor(1, -1);
                updateComposingText(1);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mComposingText.moveCursor(1, 1);
                updateComposingText(1);
                return true;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                commitText(1);
                mComposingText.clear();
                if (mEnableAutoHideKeyboard) {
                    mInputViewManager.closing();
                    requestHideSelf(0);
                }
                return true;
            default:
                break;
            }
        } else {
            if (mCandidatesViewManager.getCurrentView().isShown()) {
            	if (key == KeyEvent.KEYCODE_BACK) {
            		if (mCandidatesViewManager.getViewType() == CandidatesViewManager.VIEW_TYPE_FULL) {
            			mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_NORMAL);
            		} else {
            			mCandidatesViewManager.setViewType(CandidatesViewManager.VIEW_TYPE_CLOSE);
            		}
            		return true;
            	}
            } else {
                switch (key) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                case KeyEvent.KEYCODE_ENTER:
                    if (mEnableAutoHideKeyboard) {
                        mInputViewManager.closing();
                        requestHideSelf(0);
                        return true;
                    }
                    break;
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
    private void updatePrediction() {
        int candidates = 0;
        if (mConverter != null) {
            candidates = mConverter.predict(mComposingText, 0, -1);
        }
        if (candidates > 0) {
            mCandidatesViewManager.displayCandidates(mConverter);
        } else {
            mCandidatesViewManager.clearCandidates();
        }
    }
    private void updateComposingText(int layer) {
        if (!mOptPrediction) {
            commitText(1);
            mComposingText.clear();
            if (mSymbolMode) {
                mHandler.removeMessages(MSG_PREDICTION);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PREDICTION), 0);
            }
        } else {
            if (mComposingText.size(1) != 0) {
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
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PREDICTION), 0);
            }
            this.mInputViewManager.onUpdateState(this);
            SpannableStringBuilder disp = mDisplayText;
            disp.clear();
            disp.insert(0, mComposingText.toString(layer));
            int cursor = mComposingText.getCursor(layer);
            if (disp.length() != 0) {
                if (cursor > 0 && cursor < disp.length()) {
                    disp.setSpan(SPAN_EXACT_BGCOLOR_HL, 0, cursor,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                if (cursor < disp.length()) {
                    mDisplayText.setSpan(SPAN_REMAIN_BGCOLOR_HL, cursor, disp.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mDisplayText.setSpan(SPAN_TEXTCOLOR, 0, disp.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); 
                }
                disp.setSpan(SPAN_UNDERLINE, 0, disp.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            int displayCursor = cursor;
            if (FIX_CURSOR_TEXT_END) {
                displayCursor = (cursor == 0) ?  0 : 1;
            } 
            mInputConnection.setComposingText(disp, displayCursor);
        }
    }
    private void commitText(int layer) {
        String tmp = mComposingText.toString(layer);
        if (mOptLearning && mConverter != null && tmp.length() > 0) {
            WnnWord word = new WnnWord(tmp, tmp);
            mConverter.learn(word);
        }
        mInputConnection.commitText(tmp, (FIX_CURSOR_TEXT_END ? 1 : tmp.length()));
        mCandidatesViewManager.clearCandidates();
    }
    private void commitText(WnnWord word, boolean withSpace) {
        if (mOptLearning && mConverter != null) {
            mConverter.learn(word);
        }
        mInputConnection.commitText(word.candidate, (FIX_CURSOR_TEXT_END ? 1 : word.candidate.length()));
        if (withSpace) {
            commitText(" ");
        }       
    }
    private void commitText(String str) {
        mInputConnection.commitText(str, (FIX_CURSOR_TEXT_END ? 1 : str.length()));
        mCandidatesViewManager.clearCandidates();
    }
    protected void dismissPopupKeyboard() {
        DefaultSoftKeyboardEN kbd = (DefaultSoftKeyboardEN)mInputViewManager;
        if (kbd != null) {
            kbd.dismissPopupKeyboard();
        }
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
    private void fitInputType(SharedPreferences preference, EditorInfo info) {
        if (info.inputType == EditorInfo.TYPE_NULL) {
            mDirectInputMode = true;
            return;
        }
        mEnableAutoHideKeyboard = false;
        mOptPrediction      = preference.getBoolean("opt_en_prediction", true);
        mOptSpellCorrection = preference.getBoolean("opt_en_spell_correction", true);
        mOptLearning        = preference.getBoolean("opt_en_enable_learning", true);
        switch (info.inputType & EditorInfo.TYPE_MASK_CLASS) {
        case EditorInfo.TYPE_CLASS_NUMBER:
        case EditorInfo.TYPE_CLASS_DATETIME:
        case EditorInfo.TYPE_CLASS_PHONE:
            mOptPrediction = false;
            mOptLearning = false;
            break;
        case EditorInfo.TYPE_CLASS_TEXT:
            switch (info.inputType & EditorInfo.TYPE_MASK_VARIATION) {
            case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
            case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                mOptLearning = false;
                mOptPrediction = false;
                break;
            case EditorInfo.TYPE_TEXT_VARIATION_PHONETIC:
                mOptLearning = false;
                mOptPrediction = false;
                break;
            default:
                break;
            }
        }
        if (!mOptPrediction) {
            mOptLearning = false;
        }
        if (mOptSpellCorrection) {
            mConverterEN.setDictionary(OpenWnnEngineEN.DICT_FOR_CORRECT_MISTYPE);
        } else {
            mConverterEN.setDictionary(OpenWnnEngineEN.DICT_DEFAULT);
        }
        checkTutorial(info.privateImeOptions);
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
        DefaultSoftKeyboardEN inputManager = ((DefaultSoftKeyboardEN) mInputViewManager);
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
}
