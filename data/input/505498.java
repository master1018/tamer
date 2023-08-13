public class NumberPicker extends LinearLayout {
    public interface OnChangedListener {
        void onChanged(NumberPicker picker, int oldVal, int newVal);
    }
    public interface Formatter {
        String toString(int value);
    }
    public static final NumberPicker.Formatter TWO_DIGIT_FORMATTER =
            new NumberPicker.Formatter() {
                final StringBuilder mBuilder = new StringBuilder();
                final java.util.Formatter mFmt = new java.util.Formatter(mBuilder);
                final Object[] mArgs = new Object[1];
                public String toString(int value) {
                    mArgs[0] = value;
                    mBuilder.delete(0, mBuilder.length());
                    mFmt.format("%02d", mArgs);
                    return mFmt.toString();
                }
        };
    private final Handler mHandler;
    private final Runnable mRunnable = new Runnable() {
        public void run() {
            if (mIncrement) {
                changeCurrent(mCurrent + 1);
                mHandler.postDelayed(this, mSpeed);
            } else if (mDecrement) {
                changeCurrent(mCurrent - 1);
                mHandler.postDelayed(this, mSpeed);
            }
        }
    };
    private final EditText mText;
    private final InputFilter mNumberInputFilter;
    private String[] mDisplayedValues;
    private int mStart;
    private int mEnd;
    private int mCurrent;
    private int mPrevious;
    private OnChangedListener mListener;
    private Formatter mFormatter;
    private long mSpeed = 300;
    private boolean mIncrement;
    private boolean mDecrement;
    public NumberPicker(Context context) {
        this(context, null);
    }
    public NumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        LayoutInflater inflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.number_picker, this, true);
        mHandler = new Handler();
        OnClickListener clickListener = new OnClickListener() {
            public void onClick(View v) {
                validateInput(mText);
                if (!mText.hasFocus()) mText.requestFocus();
                if (R.id.increment == v.getId()) {
                    changeCurrent(mCurrent + 1);
                } else if (R.id.decrement == v.getId()) {
                    changeCurrent(mCurrent - 1);
                }
            }
        };
        OnFocusChangeListener focusListener = new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    validateInput(v);
                }
            }
        };
        OnLongClickListener longClickListener = new OnLongClickListener() {
            public boolean onLongClick(View v) {
                mText.clearFocus();
                if (R.id.increment == v.getId()) {
                    mIncrement = true;
                    mHandler.post(mRunnable);
                } else if (R.id.decrement == v.getId()) {
                    mDecrement = true;
                    mHandler.post(mRunnable);
                }
                return true;
            }
        };
        InputFilter inputFilter = new NumberPickerInputFilter();
        mNumberInputFilter = new NumberRangeKeyListener();
        mIncrementButton = (NumberPickerButton) findViewById(R.id.increment);
        mIncrementButton.setOnClickListener(clickListener);
        mIncrementButton.setOnLongClickListener(longClickListener);
        mIncrementButton.setNumberPicker(this);
        mDecrementButton = (NumberPickerButton) findViewById(R.id.decrement);
        mDecrementButton.setOnClickListener(clickListener);
        mDecrementButton.setOnLongClickListener(longClickListener);
        mDecrementButton.setNumberPicker(this);
        mText = (EditText) findViewById(R.id.timepicker_input);
        mText.setOnFocusChangeListener(focusListener);
        mText.setFilters(new InputFilter[] {inputFilter});
        mText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        if (!isEnabled()) {
            setEnabled(false);
        }
    }
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mIncrementButton.setEnabled(enabled);
        mDecrementButton.setEnabled(enabled);
        mText.setEnabled(enabled);
    }
    public void setOnChangeListener(OnChangedListener listener) {
        mListener = listener;
    }
    public void setFormatter(Formatter formatter) {
        mFormatter = formatter;
    }
    public void setRange(int start, int end) {
        setRange(start, end, null);
    }
    public void setRange(int start, int end, String[] displayedValues) {
        mDisplayedValues = displayedValues;
        mStart = start;
        mEnd = end;
        mCurrent = start;
        updateView();
    }
    public void setCurrent(int current) {
        if (current < mStart || current > mEnd) {
            throw new IllegalArgumentException(
                    "current should be >= start and <= end");
        }
        mCurrent = current;
        updateView();
    }
    public void setSpeed(long speed) {
        mSpeed = speed;
    }
    private String formatNumber(int value) {
        return (mFormatter != null)
                ? mFormatter.toString(value)
                : String.valueOf(value);
    }
    protected void changeCurrent(int current) {
        if (current > mEnd) {
            current = mStart;
        } else if (current < mStart) {
            current = mEnd;
        }
        mPrevious = mCurrent;
        mCurrent = current;
        notifyChange();
        updateView();
    }
    private void notifyChange() {
        if (mListener != null) {
            mListener.onChanged(this, mPrevious, mCurrent);
        }
    }
    private void updateView() {
        if (mDisplayedValues == null) {
            mText.setText(formatNumber(mCurrent));
        } else {
            mText.setText(mDisplayedValues[mCurrent - mStart]);
        }
        mText.setSelection(mText.getText().length());
    }
    private void validateCurrentView(CharSequence str) {
        int val = getSelectedPos(str.toString());
        if ((val >= mStart) && (val <= mEnd)) {
            if (mCurrent != val) {
                mPrevious = mCurrent;
                mCurrent = val;
                notifyChange();
            }
        }
        updateView();
    }
    private void validateInput(View v) {
        String str = String.valueOf(((TextView) v).getText());
        if ("".equals(str)) {
            updateView();
        } else {
            validateCurrentView(str);
        }
    }
    public void cancelIncrement() {
        mIncrement = false;
    }
    public void cancelDecrement() {
        mDecrement = false;
    }
    private static final char[] DIGIT_CHARACTERS = new char[] {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };
    private NumberPickerButton mIncrementButton;
    private NumberPickerButton mDecrementButton;
    private class NumberPickerInputFilter implements InputFilter {
        public CharSequence filter(CharSequence source, int start, int end,
                Spanned dest, int dstart, int dend) {
            if (mDisplayedValues == null) {
                return mNumberInputFilter.filter(source, start, end, dest, dstart, dend);
            }
            CharSequence filtered = String.valueOf(source.subSequence(start, end));
            String result = String.valueOf(dest.subSequence(0, dstart))
                    + filtered
                    + dest.subSequence(dend, dest.length());
            String str = String.valueOf(result).toLowerCase();
            for (String val : mDisplayedValues) {
                val = val.toLowerCase();
                if (val.startsWith(str)) {
                    return filtered;
                }
            }
            return "";
        }
    }
    private class NumberRangeKeyListener extends NumberKeyListener {
        public int getInputType() {
            return InputType.TYPE_CLASS_NUMBER;
        }
        @Override
        protected char[] getAcceptedChars() {
            return DIGIT_CHARACTERS;
        }
        @Override
        public CharSequence filter(CharSequence source, int start, int end,
                Spanned dest, int dstart, int dend) {
            CharSequence filtered = super.filter(source, start, end, dest, dstart, dend);
            if (filtered == null) {
                filtered = source.subSequence(start, end);
            }
            String result = String.valueOf(dest.subSequence(0, dstart))
                    + filtered
                    + dest.subSequence(dend, dest.length());
            if ("".equals(result)) {
                return result;
            }
            int val = getSelectedPos(result);
            if (val > mEnd) {
                return "";
            } else {
                return filtered;
            }
        }
    }
    private int getSelectedPos(String str) {
        if (mDisplayedValues == null) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
            }
        } else {
            for (int i = 0; i < mDisplayedValues.length; i++) {
                str = str.toLowerCase();
                if (mDisplayedValues[i].toLowerCase().startsWith(str)) {
                    return mStart + i;
                }
            }
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
            }
        }
        return mStart;
    }
    public int getCurrent() {
        return mCurrent;
    }
    protected int getEndRange() {
        return mEnd;
    }
    protected int getBeginRange() {
        return mStart;
    }
}
