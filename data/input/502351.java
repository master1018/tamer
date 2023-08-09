public class InputModeSwitcher {
    private static final int USERDEF_KEYCODE_SHIFT_1 = -1;
    private static final int USERDEF_KEYCODE_LANG_2 = -2;
    private static final int USERDEF_KEYCODE_SYM_3 = -3;
    public static final int USERDEF_KEYCODE_PHONE_SYM_4 = -4;
    private static final int USERDEF_KEYCODE_MORE_SYM_5 = -5;
    private static final int USERDEF_KEYCODE_SMILEY_6 = -6;
    private static final int MASK_SKB_LAYOUT = 0xf0000000;
    private static final int MASK_SKB_LAYOUT_QWERTY = 0x10000000;
    private static final int MASK_SKB_LAYOUT_SYMBOL1 = 0x20000000;
    private static final int MASK_SKB_LAYOUT_SYMBOL2 = 0x30000000;
    private static final int MASK_SKB_LAYOUT_SMILEY = 0x40000000;
    private static final int MASK_SKB_LAYOUT_PHONE = 0x50000000;
    private static final int MASK_LANGUAGE = 0x0f000000;
    private static final int MASK_LANGUAGE_CN = 0x01000000;
    private static final int MASK_LANGUAGE_EN = 0x02000000;
    private static final int MASK_CASE = 0x00f00000;
    private static final int MASK_CASE_LOWER = 0x00100000;
    private static final int MASK_CASE_UPPER = 0x00200000;
    public static final int MODE_SKB_CHINESE = (MASK_SKB_LAYOUT_QWERTY | MASK_LANGUAGE_CN);
    public static final int MODE_SKB_SYMBOL1_CN = (MASK_SKB_LAYOUT_SYMBOL1 | MASK_LANGUAGE_CN);
    public static final int MODE_SKB_SYMBOL2_CN = (MASK_SKB_LAYOUT_SYMBOL2 | MASK_LANGUAGE_CN);
    public static final int MODE_SKB_ENGLISH_LOWER = (MASK_SKB_LAYOUT_QWERTY
            | MASK_LANGUAGE_EN | MASK_CASE_LOWER);
    public static final int MODE_SKB_ENGLISH_UPPER = (MASK_SKB_LAYOUT_QWERTY
            | MASK_LANGUAGE_EN | MASK_CASE_UPPER);
    public static final int MODE_SKB_SYMBOL1_EN = (MASK_SKB_LAYOUT_SYMBOL1 | MASK_LANGUAGE_EN);
    public static final int MODE_SKB_SYMBOL2_EN = (MASK_SKB_LAYOUT_SYMBOL2 | MASK_LANGUAGE_EN);
    public static final int MODE_SKB_SMILEY = (MASK_SKB_LAYOUT_SMILEY | MASK_LANGUAGE_CN);
    public static final int MODE_SKB_PHONE_NUM = (MASK_SKB_LAYOUT_PHONE);
    public static final int MODE_SKB_PHONE_SYM = (MASK_SKB_LAYOUT_PHONE | MASK_CASE_UPPER);
    public static final int MODE_HKB_CHINESE = (MASK_LANGUAGE_CN);
    public static final int MODE_HKB_ENGLISH = (MASK_LANGUAGE_EN);
    public static final int MODE_UNSET = 0;
    public static final int MAX_TOGGLE_STATES = 4;
    private int mInputMode = MODE_UNSET;
    private int mPreviousInputMode = MODE_SKB_CHINESE;
    private int mRecentLauageInputMode = MODE_SKB_CHINESE;
    private EditorInfo mEditorInfo;
    private ToggleStates mToggleStates = new ToggleStates();
    private boolean mShortMessageField;
    private boolean mEnterKeyNormal = true;
    int mInputIcon = R.drawable.ime_pinyin;
    private PinyinIME mImeService;
    private int mToggleStateCn;
    private int mToggleStateCnCand;
    private int mToggleStateEnLower;
    private int mToggleStateEnUpper;
    private int mToggleStateEnSym1;
    private int mToggleStateEnSym2;
    private int mToggleStateSmiley;
    private int mToggleStatePhoneSym;
    private int mToggleStateGo;
    private int mToggleStateSearch;
    private int mToggleStateSend;
    private int mToggleStateNext;
    private int mToggleStateDone;
    private int mToggleRowCn;
    private int mToggleRowEn;
    private int mToggleRowUri;
    private int mToggleRowEmailAddress;
    class ToggleStates {
        boolean mQwerty;
        boolean mQwertyUpperCase;
        public int mRowIdToEnable;
        public int mKeyStates[] = new int[MAX_TOGGLE_STATES];
        public int mKeyStatesNum;
    }
    public InputModeSwitcher(PinyinIME imeService) {
        mImeService = imeService;
        Resources r = mImeService.getResources();
        mToggleStateCn = Integer.parseInt(r.getString(R.string.toggle_cn));
        mToggleStateCnCand = Integer.parseInt(r
                .getString(R.string.toggle_cn_cand));
        mToggleStateEnLower = Integer.parseInt(r
                .getString(R.string.toggle_en_lower));
        mToggleStateEnUpper = Integer.parseInt(r
                .getString(R.string.toggle_en_upper));
        mToggleStateEnSym1 = Integer.parseInt(r
                .getString(R.string.toggle_en_sym1));
        mToggleStateEnSym2 = Integer.parseInt(r
                .getString(R.string.toggle_en_sym2));
        mToggleStateSmiley = Integer.parseInt(r
                .getString(R.string.toggle_smiley));
        mToggleStatePhoneSym = Integer.parseInt(r
                .getString(R.string.toggle_phone_sym));
        mToggleStateGo = Integer
                .parseInt(r.getString(R.string.toggle_enter_go));
        mToggleStateSearch = Integer.parseInt(r
                .getString(R.string.toggle_enter_search));
        mToggleStateSend = Integer.parseInt(r
                .getString(R.string.toggle_enter_send));
        mToggleStateNext = Integer.parseInt(r
                .getString(R.string.toggle_enter_next));
        mToggleStateDone = Integer.parseInt(r
                .getString(R.string.toggle_enter_done));
        mToggleRowCn = Integer.parseInt(r.getString(R.string.toggle_row_cn));
        mToggleRowEn = Integer.parseInt(r.getString(R.string.toggle_row_en));
        mToggleRowUri = Integer.parseInt(r.getString(R.string.toggle_row_uri));
        mToggleRowEmailAddress = Integer.parseInt(r
                .getString(R.string.toggle_row_emailaddress));
    }
    public int getInputMode() {
        return mInputMode;
    }
    public ToggleStates getToggleStates() {
        return mToggleStates;
    }
    public int getSkbLayout() {
        int layout = (mInputMode & MASK_SKB_LAYOUT);
        switch (layout) {
        case MASK_SKB_LAYOUT_QWERTY:
            return R.xml.skb_qwerty;
        case MASK_SKB_LAYOUT_SYMBOL1:
            return R.xml.skb_sym1;
        case MASK_SKB_LAYOUT_SYMBOL2:
            return R.xml.skb_sym2;
        case MASK_SKB_LAYOUT_SMILEY:
            return R.xml.skb_smiley;
        case MASK_SKB_LAYOUT_PHONE:
            return R.xml.skb_phone;
        }
        return 0;
    }
    public int switchLanguageWithHkb() {
        int newInputMode = MODE_HKB_CHINESE;
        mInputIcon = R.drawable.ime_pinyin;
        if (MODE_HKB_CHINESE == mInputMode) {
            newInputMode = MODE_HKB_ENGLISH;
            mInputIcon = R.drawable.ime_en;
        }
        saveInputMode(newInputMode);
        return mInputIcon;
    }
    public int switchModeForUserKey(int userKey) {
        int newInputMode = MODE_UNSET;
        if (USERDEF_KEYCODE_LANG_2 == userKey) {
            if (MODE_SKB_CHINESE == mInputMode) {
                newInputMode = MODE_SKB_ENGLISH_LOWER;
            } else if (MODE_SKB_ENGLISH_LOWER == mInputMode
                    || MODE_SKB_ENGLISH_UPPER == mInputMode) {
                newInputMode = MODE_SKB_CHINESE;
            } else if (MODE_SKB_SYMBOL1_CN == mInputMode) {
                newInputMode = MODE_SKB_SYMBOL1_EN;
            } else if (MODE_SKB_SYMBOL1_EN == mInputMode) {
                newInputMode = MODE_SKB_SYMBOL1_CN;
            } else if (MODE_SKB_SYMBOL2_CN == mInputMode) {
                newInputMode = MODE_SKB_SYMBOL2_EN;
            } else if (MODE_SKB_SYMBOL2_EN == mInputMode) {
                newInputMode = MODE_SKB_SYMBOL2_CN;
            } else if (MODE_SKB_SMILEY == mInputMode) {
                newInputMode = MODE_SKB_CHINESE;
            }
        } else if (USERDEF_KEYCODE_SYM_3 == userKey) {
            if (MODE_SKB_CHINESE == mInputMode) {
                newInputMode = MODE_SKB_SYMBOL1_CN;
            } else if (MODE_SKB_ENGLISH_UPPER == mInputMode
                    || MODE_SKB_ENGLISH_LOWER == mInputMode) {
                newInputMode = MODE_SKB_SYMBOL1_EN;
            } else if (MODE_SKB_SYMBOL1_EN == mInputMode
                    || MODE_SKB_SYMBOL2_EN == mInputMode) {
                newInputMode = MODE_SKB_ENGLISH_LOWER;
            } else if (MODE_SKB_SYMBOL1_CN == mInputMode
                    || MODE_SKB_SYMBOL2_CN == mInputMode) {
                newInputMode = MODE_SKB_CHINESE;
            } else if (MODE_SKB_SMILEY == mInputMode) {
                newInputMode = MODE_SKB_SYMBOL1_CN;
            }
        } else if (USERDEF_KEYCODE_SHIFT_1 == userKey) {
            if (MODE_SKB_ENGLISH_LOWER == mInputMode) {
                newInputMode = MODE_SKB_ENGLISH_UPPER;
            } else if (MODE_SKB_ENGLISH_UPPER == mInputMode) {
                newInputMode = MODE_SKB_ENGLISH_LOWER;
            }
        } else if (USERDEF_KEYCODE_MORE_SYM_5 == userKey) {
            int sym = (MASK_SKB_LAYOUT & mInputMode);
            if (MASK_SKB_LAYOUT_SYMBOL1 == sym) {
                sym = MASK_SKB_LAYOUT_SYMBOL2;
            } else {
                sym = MASK_SKB_LAYOUT_SYMBOL1;
            }
            newInputMode = ((mInputMode & (~MASK_SKB_LAYOUT)) | sym);
        } else if (USERDEF_KEYCODE_SMILEY_6 == userKey) {
            if (MODE_SKB_CHINESE == mInputMode) {
                newInputMode = MODE_SKB_SMILEY;
            } else {
                newInputMode = MODE_SKB_CHINESE;
            }
        } else if (USERDEF_KEYCODE_PHONE_SYM_4 == userKey) {
            if (MODE_SKB_PHONE_NUM == mInputMode) {
                newInputMode = MODE_SKB_PHONE_SYM;
            } else {
                newInputMode = MODE_SKB_PHONE_NUM;
            }
        }
        if (newInputMode == mInputMode || MODE_UNSET == newInputMode) {
            return mInputIcon;
        }
        saveInputMode(newInputMode);
        prepareToggleStates(true);
        return mInputIcon;
    }
    public int requestInputWithHkb(EditorInfo editorInfo) {
        mShortMessageField = false;
        boolean english = false;
        int newInputMode = MODE_HKB_CHINESE;
        switch (editorInfo.inputType & EditorInfo.TYPE_MASK_CLASS) {
        case EditorInfo.TYPE_CLASS_NUMBER:
        case EditorInfo.TYPE_CLASS_PHONE:
        case EditorInfo.TYPE_CLASS_DATETIME:
            english = true;
            break;
        case EditorInfo.TYPE_CLASS_TEXT:
            int v = editorInfo.inputType & EditorInfo.TYPE_MASK_VARIATION;
            if (v == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    || v == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
                    || v == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    || v == EditorInfo.TYPE_TEXT_VARIATION_URI) {
                english = true;
            } else if (v == EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE) {
                mShortMessageField = true;
            }
            break;
        default:
        }
        if (english) {
            newInputMode = MODE_HKB_ENGLISH;
        } else {
            if ((mRecentLauageInputMode & MASK_LANGUAGE) == MASK_LANGUAGE_CN) {
                newInputMode = MODE_HKB_CHINESE;
            } else {
                newInputMode = MODE_HKB_ENGLISH;
            }
        }
        mEditorInfo = editorInfo;
        saveInputMode(newInputMode);
        prepareToggleStates(false);
        return mInputIcon;
    }
    public int requestInputWithSkb(EditorInfo editorInfo) {
        mShortMessageField = false;
        int newInputMode = MODE_SKB_CHINESE;
        switch (editorInfo.inputType & EditorInfo.TYPE_MASK_CLASS) {
        case EditorInfo.TYPE_CLASS_NUMBER:
        case EditorInfo.TYPE_CLASS_DATETIME:
            newInputMode = MODE_SKB_SYMBOL1_EN;
            break;
        case EditorInfo.TYPE_CLASS_PHONE:
            newInputMode = MODE_SKB_PHONE_NUM;
            break;
        case EditorInfo.TYPE_CLASS_TEXT:
            int v = editorInfo.inputType & EditorInfo.TYPE_MASK_VARIATION;
            if (v == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    || v == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
                    || v == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    || v == EditorInfo.TYPE_TEXT_VARIATION_URI) {
                newInputMode = MODE_SKB_ENGLISH_LOWER;
            } else {
                if (v == EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE) {
                    mShortMessageField = true;
                }
                int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
                newInputMode = mInputMode;
                if (0 == skbLayout) {
                    if ((mInputMode & MASK_LANGUAGE) == MASK_LANGUAGE_CN) {
                        newInputMode = MODE_SKB_CHINESE;
                    } else {
                        newInputMode = MODE_SKB_ENGLISH_LOWER;
                    }
                }
            }
            break;
        default:
            int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
            newInputMode = mInputMode;
            if (0 == skbLayout) {
                if ((mInputMode & MASK_LANGUAGE) == MASK_LANGUAGE_CN) {
                    newInputMode = MODE_SKB_CHINESE;
                } else {
                    newInputMode = MODE_SKB_ENGLISH_LOWER;
                }
            }
            break;
        }
        mEditorInfo = editorInfo;
        saveInputMode(newInputMode);
        prepareToggleStates(true);
        return mInputIcon;
    }
    public int requestBackToPreviousSkb() {
        int layout = (mInputMode & MASK_SKB_LAYOUT);
        int lastLayout = (mPreviousInputMode & MASK_SKB_LAYOUT);
        if (0 != layout && 0 != lastLayout) {
            mInputMode = mPreviousInputMode;
            saveInputMode(mInputMode);
            prepareToggleStates(true);
            return mInputIcon;
        }
        return 0;
    }
    public int getTooggleStateForCnCand() {
        return mToggleStateCnCand;
    }
    public boolean isEnglishWithHkb() {
        return MODE_HKB_ENGLISH == mInputMode;
    }
    public boolean isEnglishWithSkb() {
        return MODE_SKB_ENGLISH_LOWER == mInputMode
                || MODE_SKB_ENGLISH_UPPER == mInputMode;
    }
    public boolean isEnglishUpperCaseWithSkb() {
        return MODE_SKB_ENGLISH_UPPER == mInputMode;
    }
    public boolean isChineseText() {
        int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
        if (MASK_SKB_LAYOUT_QWERTY == skbLayout || 0 == skbLayout) {
            int language = (mInputMode & MASK_LANGUAGE);
            if (MASK_LANGUAGE_CN == language) return true;
        }
        return false;
    }
    public boolean isChineseTextWithHkb() {
        int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
        if (0 == skbLayout) {
            int language = (mInputMode & MASK_LANGUAGE);
            if (MASK_LANGUAGE_CN == language) return true;
        }
        return false;
    }
    public boolean isChineseTextWithSkb() {
        int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
        if (MASK_SKB_LAYOUT_QWERTY == skbLayout) {
            int language = (mInputMode & MASK_LANGUAGE);
            if (MASK_LANGUAGE_CN == language) return true;
        }
        return false;
    }
    public boolean isSymbolWithSkb() {
        int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
        if (MASK_SKB_LAYOUT_SYMBOL1 == skbLayout
                || MASK_SKB_LAYOUT_SYMBOL2 == skbLayout) {
            return true;
        }
        return false;
    }
    public boolean isEnterNoramlState() {
        return mEnterKeyNormal;
    }
    public boolean tryHandleLongPressSwitch(int keyCode) {
        if (USERDEF_KEYCODE_LANG_2 == keyCode
                || USERDEF_KEYCODE_PHONE_SYM_4 == keyCode) {
            mImeService.showOptionsMenu();
            return true;
        }
        return false;
    }
    private void saveInputMode(int newInputMode) {
        mPreviousInputMode = mInputMode;
        mInputMode = newInputMode;
        int skbLayout = (mInputMode & MASK_SKB_LAYOUT);
        if (MASK_SKB_LAYOUT_QWERTY == skbLayout || 0 == skbLayout) {
            mRecentLauageInputMode = mInputMode;
        }
        mInputIcon = R.drawable.ime_pinyin;
        if (isEnglishWithHkb()) {
            mInputIcon = R.drawable.ime_en;
        } else if (isChineseTextWithHkb()) {
            mInputIcon = R.drawable.ime_pinyin;
        }
        if (!Environment.getInstance().hasHardKeyboard()) {
            mInputIcon = 0;
        }
    }
    private void prepareToggleStates(boolean needSkb) {
        mEnterKeyNormal = true;
        if (!needSkb) return;
        mToggleStates.mQwerty = false;
        mToggleStates.mKeyStatesNum = 0;
        int states[] = mToggleStates.mKeyStates;
        int statesNum = 0;
        int language = (mInputMode & MASK_LANGUAGE);
        int layout = (mInputMode & MASK_SKB_LAYOUT);
        int charcase = (mInputMode & MASK_CASE);
        int variation = mEditorInfo.inputType & EditorInfo.TYPE_MASK_VARIATION;
        if (MASK_SKB_LAYOUT_PHONE != layout) {
            if (MASK_LANGUAGE_CN == language) {
                if (MASK_SKB_LAYOUT_QWERTY == layout) {
                    mToggleStates.mQwerty = true;
                    mToggleStates.mQwertyUpperCase = true;
                    if (mShortMessageField) {
                        states[statesNum] = mToggleStateSmiley;
                        statesNum++;
                    }
                }
            } else if (MASK_LANGUAGE_EN == language) {
                if (MASK_SKB_LAYOUT_QWERTY == layout) {
                    mToggleStates.mQwerty = true;
                    mToggleStates.mQwertyUpperCase = false;
                    states[statesNum] = mToggleStateEnLower;
                    if (MASK_CASE_UPPER == charcase) {
                        mToggleStates.mQwertyUpperCase = true;
                        states[statesNum] = mToggleStateEnUpper;
                    }
                    statesNum++;
                } else if (MASK_SKB_LAYOUT_SYMBOL1 == layout) {
                    states[statesNum] = mToggleStateEnSym1;
                    statesNum++;
                } else if (MASK_SKB_LAYOUT_SYMBOL2 == layout) {
                    states[statesNum] = mToggleStateEnSym2;
                    statesNum++;
                }
            }
            mToggleStates.mRowIdToEnable = KeyRow.DEFAULT_ROW_ID;
            if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
                mToggleStates.mRowIdToEnable = mToggleRowEmailAddress;
            } else if (variation == EditorInfo.TYPE_TEXT_VARIATION_URI) {
                mToggleStates.mRowIdToEnable = mToggleRowUri;
            } else if (MASK_LANGUAGE_CN == language) {
                mToggleStates.mRowIdToEnable = mToggleRowCn;
            } else if (MASK_LANGUAGE_EN == language) {
                mToggleStates.mRowIdToEnable = mToggleRowEn;
            }
        } else {
            if (MASK_CASE_UPPER == charcase) {
                states[statesNum] = mToggleStatePhoneSym;
                statesNum++;
            }
        }
        int action = mEditorInfo.imeOptions
                & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        if (action == EditorInfo.IME_ACTION_GO) {
            states[statesNum] = mToggleStateGo;
            statesNum++;
            mEnterKeyNormal = false;
        } else if (action == EditorInfo.IME_ACTION_SEARCH) {
            states[statesNum] = mToggleStateSearch;
            statesNum++;
            mEnterKeyNormal = false;
        } else if (action == EditorInfo.IME_ACTION_SEND) {
            states[statesNum] = mToggleStateSend;
            statesNum++;
            mEnterKeyNormal = false;
        } else if (action == EditorInfo.IME_ACTION_NEXT) {
            int f = mEditorInfo.inputType & EditorInfo.TYPE_MASK_FLAGS;
            if (f != EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) {
                states[statesNum] = mToggleStateNext;
                statesNum++;
                mEnterKeyNormal = false;
            }
        } else if (action == EditorInfo.IME_ACTION_DONE) {
            states[statesNum] = mToggleStateDone;
            statesNum++;
            mEnterKeyNormal = false;
        }
        mToggleStates.mKeyStatesNum = statesNum;
    }
}
