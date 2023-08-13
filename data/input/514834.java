public class DialerFilter extends RelativeLayout
{
    public DialerFilter(Context context) {
        super(context);
    }
    public DialerFilter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInputFilters = new InputFilter[] { new InputFilter.AllCaps() };
        mHint = (EditText) findViewById(com.android.internal.R.id.hint);
        if (mHint == null) {
            throw new IllegalStateException("DialerFilter must have a child EditText named hint");
        }
        mHint.setFilters(mInputFilters);
        mLetters = mHint;
        mLetters.setKeyListener(TextKeyListener.getInstance());
        mLetters.setMovementMethod(null);
        mLetters.setFocusable(false);
        mPrimary = (EditText) findViewById(com.android.internal.R.id.primary);
        if (mPrimary == null) {
            throw new IllegalStateException("DialerFilter must have a child EditText named primary");
        }
        mPrimary.setFilters(mInputFilters);
        mDigits = mPrimary;
        mDigits.setKeyListener(DialerKeyListener.getInstance());
        mDigits.setMovementMethod(null);
        mDigits.setFocusable(false);
        mIcon = (ImageView) findViewById(com.android.internal.R.id.icon);
        setFocusable(true);
        KeyCharacterMap kmap
                = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
        mIsQwerty = kmap.getKeyboardType() != KeyCharacterMap.NUMERIC;
        if (mIsQwerty) {
            Log.i("DialerFilter", "This device looks to be QWERTY");
        } else {
            Log.i("DialerFilter", "This device looks to be 12-KEY");
        }
        mIsQwerty = true;
        setMode(DIGITS_AND_LETTERS);
    }
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (mIcon != null) {
            mIcon.setVisibility(focused ? View.VISIBLE : View.GONE);
        }
    }
    public boolean isQwertyKeyboard() {
        return mIsQwerty;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                break;
            case KeyEvent.KEYCODE_DEL:
                switch (mMode) {
                    case DIGITS_AND_LETTERS:
                        handled = mDigits.onKeyDown(keyCode, event);
                        handled &= mLetters.onKeyDown(keyCode, event);
                        break;
                    case DIGITS_AND_LETTERS_NO_DIGITS:
                        handled = mLetters.onKeyDown(keyCode, event);
                        if (mLetters.getText().length() == mDigits.getText().length()) {
                            setMode(DIGITS_AND_LETTERS);
                        }
                        break;
                    case DIGITS_AND_LETTERS_NO_LETTERS:
                        if (mDigits.getText().length() == mLetters.getText().length()) {
                            mLetters.onKeyDown(keyCode, event);
                            setMode(DIGITS_AND_LETTERS);
                        }
                        handled = mDigits.onKeyDown(keyCode, event);
                        break;
                    case DIGITS_ONLY:
                        handled = mDigits.onKeyDown(keyCode, event);
                        break;
                    case LETTERS_ONLY:
                        handled = mLetters.onKeyDown(keyCode, event);
                        break;
                }
                break;
            default:
                switch (mMode) {
                    case DIGITS_AND_LETTERS:
                        handled = mLetters.onKeyDown(keyCode, event);
                        if (KeyEvent.isModifierKey(keyCode)) {
                            mDigits.onKeyDown(keyCode, event);
                            handled = true;
                            break;
                        }
                        boolean isPrint = event.isPrintingKey();
                        if (isPrint || keyCode == KeyEvent.KEYCODE_SPACE
                                || keyCode == KeyEvent.KEYCODE_TAB) {
                            char c = event.getMatch(DialerKeyListener.CHARACTERS);
                            if (c != 0) {
                                handled &= mDigits.onKeyDown(keyCode, event);
                            } else {
                                setMode(DIGITS_AND_LETTERS_NO_DIGITS);
                            }
                        }
                        break;
                    case DIGITS_AND_LETTERS_NO_LETTERS:
                    case DIGITS_ONLY:
                        handled = mDigits.onKeyDown(keyCode, event);
                        break;
                    case DIGITS_AND_LETTERS_NO_DIGITS:
                    case LETTERS_ONLY:
                        handled = mLetters.onKeyDown(keyCode, event);
                        break;
                }
        }
        if (!handled) {
            return super.onKeyDown(keyCode, event);
        } else {
            return true;
        }
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean a = mLetters.onKeyUp(keyCode, event);
        boolean b = mDigits.onKeyUp(keyCode, event);
        return a || b;
    }
    public int getMode() {
        return mMode;
    }
    public void setMode(int newMode) {
        switch (newMode) {
            case DIGITS_AND_LETTERS:
                makeDigitsPrimary();
                mLetters.setVisibility(View.VISIBLE);
                mDigits.setVisibility(View.VISIBLE);
                break;
            case DIGITS_ONLY:
                makeDigitsPrimary();
                mLetters.setVisibility(View.GONE);
                mDigits.setVisibility(View.VISIBLE);
                break;
            case LETTERS_ONLY:
                makeLettersPrimary();
                mLetters.setVisibility(View.VISIBLE);
                mDigits.setVisibility(View.GONE);
                break;
            case DIGITS_AND_LETTERS_NO_LETTERS:
                makeDigitsPrimary();
                mLetters.setVisibility(View.INVISIBLE);
                mDigits.setVisibility(View.VISIBLE);
                break;
            case DIGITS_AND_LETTERS_NO_DIGITS:
                makeLettersPrimary();
                mLetters.setVisibility(View.VISIBLE);
                mDigits.setVisibility(View.INVISIBLE);
                break;
        }
        int oldMode = mMode;
        mMode = newMode;
        onModeChange(oldMode, newMode);
    }
    private void makeLettersPrimary() {
        if (mPrimary == mDigits) {
            swapPrimaryAndHint(true);
        }
    }
    private void makeDigitsPrimary() {
        if (mPrimary == mLetters) {
            swapPrimaryAndHint(false);
        }
    }
    private void swapPrimaryAndHint(boolean makeLettersPrimary) {
        Editable lettersText = mLetters.getText();
        Editable digitsText = mDigits.getText();
        KeyListener lettersInput = mLetters.getKeyListener();
        KeyListener digitsInput = mDigits.getKeyListener();
        if (makeLettersPrimary) {
            mLetters = mPrimary;
            mDigits = mHint;
        } else {
            mLetters = mHint;
            mDigits = mPrimary;
        }
        mLetters.setKeyListener(lettersInput);
        mLetters.setText(lettersText);
        lettersText = mLetters.getText();
        Selection.setSelection(lettersText, lettersText.length());
        mDigits.setKeyListener(digitsInput);
        mDigits.setText(digitsText);
        digitsText = mDigits.getText();
        Selection.setSelection(digitsText, digitsText.length());
        mPrimary.setFilters(mInputFilters);
        mHint.setFilters(mInputFilters);
    }
    public CharSequence getLetters() {
        if (mLetters.getVisibility() == View.VISIBLE) {
            return mLetters.getText();
        } else {
            return "";
        }
    }
    public CharSequence getDigits() {
        if (mDigits.getVisibility() == View.VISIBLE) {
            return mDigits.getText();
        } else {
            return "";
        }
    }
    public CharSequence getFilterText() {
        if (mMode != DIGITS_ONLY) {
            return getLetters();
        } else {
            return getDigits();
        }
    }
    public void append(String text) {
        switch (mMode) {
            case DIGITS_AND_LETTERS:
                mDigits.getText().append(text);
                mLetters.getText().append(text);
                break;
            case DIGITS_AND_LETTERS_NO_LETTERS:
            case DIGITS_ONLY:
                mDigits.getText().append(text);
                break;
            case DIGITS_AND_LETTERS_NO_DIGITS:
            case LETTERS_ONLY:
                mLetters.getText().append(text);
                break;
        }
    }
    public void clearText() {
        Editable text;
        text = mLetters.getText();
        text.clear();
        text = mDigits.getText();
        text.clear();
        if (mIsQwerty) {
            setMode(DIGITS_AND_LETTERS);
        } else {
            setMode(DIGITS_ONLY);
        }
    }
    public void setLettersWatcher(TextWatcher watcher) {
        CharSequence text = mLetters.getText();
        Spannable span = (Spannable)text;
        span.setSpan(watcher, 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }
    public void setDigitsWatcher(TextWatcher watcher) {
        CharSequence text = mDigits.getText();
        Spannable span = (Spannable)text;
        span.setSpan(watcher, 0, text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    }
    public void setFilterWatcher(TextWatcher watcher) {
        if (mMode != DIGITS_ONLY) {
            setLettersWatcher(watcher);
        } else {
            setDigitsWatcher(watcher);
        }
    }
    public void removeFilterWatcher(TextWatcher watcher) {
        Spannable text;
        if (mMode != DIGITS_ONLY) {
            text = mLetters.getText();
        } else {
            text = mDigits.getText();
        }
        text.removeSpan(watcher);
    }
    protected void onModeChange(int oldMode, int newMode) {
    }
    public static final int DIGITS_AND_LETTERS = 1;
    public static final int DIGITS_AND_LETTERS_NO_DIGITS = 2;
    public static final int DIGITS_AND_LETTERS_NO_LETTERS = 3;
    public static final int DIGITS_ONLY = 4;
    public static final int LETTERS_ONLY = 5;
    EditText mLetters;
    EditText mDigits;
    EditText mPrimary;
    EditText mHint;
    InputFilter mInputFilters[];
    ImageView mIcon;
    int mMode;
    private boolean mIsQwerty;
}
