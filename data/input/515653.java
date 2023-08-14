public class DefaultSoftKeyboardEN extends DefaultSoftKeyboard {
    public static final int KEYCODE_PHONE  = -116;
    private static final boolean[] TOGGLE_KEYBOARD = {true, true, false};
    private boolean mAutoCaps = false;
    public DefaultSoftKeyboardEN() { }
    public void dismissPopupKeyboard() {
    	try {
    		if (mKeyboardView != null) {
    			mKeyboardView.handleBack();
    		}
    	} catch (Exception ex) {
    	}
    }
    @Override protected void createKeyboards(OpenWnn parent) {
        mKeyboard = new Keyboard[3][2][4][2][7][2];
        Keyboard[][] keyList;
        keyList = mKeyboard[LANG_EN][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF];
        keyList[KEYMODE_EN_ALPHABET][0] = new Keyboard(parent, R.xml.default_en_qwerty);
        keyList[KEYMODE_EN_NUMBER][0]   = new Keyboard(parent, R.xml.default_en_symbols);
        keyList[KEYMODE_EN_PHONE][0]    = new Keyboard(parent, R.xml.keyboard_12key_phone);
        keyList = mKeyboard[LANG_EN][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_ON];
        keyList[KEYMODE_EN_ALPHABET][0] =
            mKeyboard[LANG_EN][PORTRAIT][KEYBOARD_QWERTY][KEYBOARD_SHIFT_OFF][KEYMODE_EN_ALPHABET][0];
        keyList[KEYMODE_EN_NUMBER][0]   = new Keyboard(parent, R.xml.default_en_symbols_shift);
        keyList[KEYMODE_EN_PHONE][0]    = new Keyboard(parent, R.xml.keyboard_12key_phone);
    }
    private int getShiftKeyState(EditorInfo editor) {
        InputConnection connection = mWnn.getCurrentInputConnection();
        if (connection != null) {
            int caps = connection.getCursorCapsMode(editor.inputType);
            return (caps == 0) ? 0 : 1;
        } else {
            return 0;
        }
    }
	private void changeKeyMode(int keyMode) {
		Keyboard keyboard = super.getModeChangeKeyboard(keyMode);
    	if (keyboard != null) {
    		mCurrentKeyMode = keyMode;
    		super.changeKeyboard(keyboard);
		}
	}
    @Override public View initView(OpenWnn parent, int width, int height) {
        View view = super.initView(parent, width, height);
    	mCurrentLanguage     = LANG_EN;
    	mCurrentKeyboardType = KEYBOARD_QWERTY;
    	mShiftOn             = KEYBOARD_SHIFT_OFF;
    	mCurrentKeyMode      = KEYMODE_EN_ALPHABET;
    	Keyboard kbd = mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mCurrentKeyMode][0];
    	if (kbd == null) {
    		if(mDisplayMode == LANDSCAPE){
    			return view;
    		}
    		return null;
    	}
    	mCurrentKeyboard = null;
    	changeKeyboard(kbd);
    	return view;
    }
    @Override public void setPreferences(SharedPreferences pref, EditorInfo editor) {
        super.setPreferences(pref, editor);
        mAutoCaps = pref.getBoolean("auto_caps", true);
        switch (editor.inputType & EditorInfo.TYPE_MASK_CLASS) {
        case EditorInfo.TYPE_CLASS_NUMBER:
        case EditorInfo.TYPE_CLASS_DATETIME:
            mCurrentLanguage     = LANG_EN;
            mCurrentKeyboardType = KEYBOARD_QWERTY;
            mShiftOn             = KEYBOARD_SHIFT_OFF;
            mCurrentKeyMode      = KEYMODE_EN_NUMBER;
            Keyboard kbdn =
                mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mCurrentKeyMode][0];
            changeKeyboard(kbdn);           
            break;
        case EditorInfo.TYPE_CLASS_PHONE:
            mCurrentLanguage     = LANG_EN;
            mCurrentKeyboardType = KEYBOARD_QWERTY;
            mShiftOn             = KEYBOARD_SHIFT_OFF;
            mCurrentKeyMode      = KEYMODE_EN_PHONE;
            Keyboard kbdp =
                mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mCurrentKeyMode][0];
            changeKeyboard(kbdp);           
            break;
        default:
            mCurrentLanguage     = LANG_EN;
            mCurrentKeyboardType = KEYBOARD_QWERTY;
            mShiftOn             = KEYBOARD_SHIFT_OFF;
            mCurrentKeyMode      = KEYMODE_EN_ALPHABET;
            Keyboard kbdq =
                mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mCurrentKeyMode][0];
            changeKeyboard(kbdq);           
             break;
        }
        int shift = (mAutoCaps)? getShiftKeyState(mWnn.getCurrentInputEditorInfo()) : 0;
        if (shift != mShiftOn) {
            Keyboard kbd = getShiftChangeKeyboard(shift);
            mShiftOn = shift;
            changeKeyboard(kbd);
        }
    }
    @Override public void onKey(int primaryCode, int[] keyCodes) {
        switch (primaryCode) {
        case KEYCODE_QWERTY_HAN_ALPHA:
            this.changeKeyMode(KEYMODE_EN_ALPHABET);
            break;
        case KEYCODE_QWERTY_HAN_NUM:
            this.changeKeyMode(KEYMODE_EN_NUMBER);
            break;
        case KEYCODE_PHONE:
            this.changeKeyMode(KEYMODE_EN_PHONE);
            break;
        case KEYCODE_QWERTY_EMOJI:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.LIST_SYMBOLS));
            break;
        case KEYCODE_QWERTY_TOGGLE_MODE:
            switch(mCurrentKeyMode){
            case KEYMODE_EN_ALPHABET:
                if (TOGGLE_KEYBOARD[KEYMODE_EN_NUMBER]){
                    mCurrentKeyMode = KEYMODE_EN_NUMBER;
                } else if (TOGGLE_KEYBOARD[KEYMODE_EN_PHONE]) {
                    mCurrentKeyMode = KEYMODE_EN_PHONE;
                }
                break;
            case KEYMODE_EN_NUMBER:
                if (TOGGLE_KEYBOARD[KEYMODE_EN_PHONE]) {
                    mCurrentKeyMode = KEYMODE_EN_PHONE;
                } else if(TOGGLE_KEYBOARD[KEYMODE_EN_ALPHABET]) {
                    mCurrentKeyMode = KEYMODE_EN_ALPHABET;
                }        			
                break;
            case KEYMODE_EN_PHONE:
                if (TOGGLE_KEYBOARD[KEYMODE_EN_ALPHABET]) {
                    mCurrentKeyMode = KEYMODE_EN_ALPHABET;
                } else if (TOGGLE_KEYBOARD[KEYMODE_EN_NUMBER]) {
                    mCurrentKeyMode = KEYMODE_EN_NUMBER;
                }
                break;
            }
            Keyboard kbdp =
                mKeyboard[mCurrentLanguage][mDisplayMode][mCurrentKeyboardType][mShiftOn][mCurrentKeyMode][0];
            super.changeKeyboard(kbdp);	
            break;
        case DefaultSoftKeyboard.KEYCODE_QWERTY_BACKSPACE:
        case DefaultSoftKeyboard.KEYCODE_JP12_BACKSPACE:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)));
            break;
        case DefaultSoftKeyboard.KEYCODE_QWERTY_SHIFT:
            toggleShiftLock();
            break;
        case DefaultSoftKeyboard.KEYCODE_QWERTY_ALT:
            processAltKey();
            break;
        case KEYCODE_QWERTY_ENTER:
        case KEYCODE_JP12_ENTER:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)));
            break;
        case KEYCODE_JP12_LEFT:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KeyEvent.KEYCODE_DPAD_LEFT)));
            break;
        case KEYCODE_JP12_RIGHT:
            mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_SOFT_KEY,
                                          new KeyEvent(KeyEvent.ACTION_DOWN,
                                                       KeyEvent.KEYCODE_DPAD_RIGHT)));
        default:
            if (primaryCode >= 0) {
                if (mKeyboardView.isShifted()) {
                    primaryCode = Character.toUpperCase(primaryCode);
                }
                mWnn.onEvent(new OpenWnnEvent(OpenWnnEvent.INPUT_CHAR, (char)primaryCode));
            }
        }
        if (!mCapsLock && primaryCode != KEYCODE_QWERTY_SHIFT) {
            if(mCurrentKeyMode != KEYMODE_EN_NUMBER){
                int shift = (mAutoCaps)? getShiftKeyState(mWnn.getCurrentInputEditorInfo()) : 0;
                if (shift != mShiftOn) {
                    Keyboard kbd = getShiftChangeKeyboard(shift);
                    mShiftOn = shift;
                    changeKeyboard(kbd);
                }
            }else{
                mShiftOn = KEYBOARD_SHIFT_OFF;
                Keyboard kbd = getShiftChangeKeyboard(mShiftOn);
                changeKeyboard(kbd);
            }
        }
    }
}
