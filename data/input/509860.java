public class NumberPickerDialog extends AlertDialog implements OnClickListener {
    private int mInitialNumber;
    private static final String NUMBER = "number";
    public interface OnNumberSetListener {
        void onNumberSet(int number);
    }
    private final NonWrapNumberPicker mNumberPicker;
    private final OnNumberSetListener mCallback;
    public NumberPickerDialog(Context context,
            OnNumberSetListener callBack,
            int number,
            int rangeMin,
            int rangeMax,
            int title) {
        this(context, com.android.internal.R.style.Theme_Dialog_Alert,
                callBack, number, rangeMin, rangeMax, title);
    }
    public NumberPickerDialog(Context context,
            int theme,
            OnNumberSetListener callBack,
            int number,
            int rangeMin,
            int rangeMax,
            int title) {
        super(context, theme);
        mCallback = callBack;
        mInitialNumber = number;
        setTitle(title);
        setButton(context.getText(R.string.set), this);
        setButton2(context.getText(R.string.no), (OnClickListener) null);
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.number_picker_dialog, null);
        setView(view);
        mNumberPicker = (NonWrapNumberPicker) view.findViewById(R.id.number_picker);
        mNumberPicker.setRange(rangeMin, rangeMax);
        mNumberPicker.setCurrent(number);
        mNumberPicker.setSpeed(150);    
    }
    public void onClick(DialogInterface dialog, int which) {
        if (mCallback != null) {
            mNumberPicker.clearFocus();
            mCallback.onNumberSet(mNumberPicker.getCurrent());
        }
    }
    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(NUMBER, mNumberPicker.getCurrent());
        return state;
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int number = savedInstanceState.getInt(NUMBER);
        mNumberPicker.setCurrent(number);
    }
    public static class NonWrapNumberPicker extends NumberPicker {
        public NonWrapNumberPicker(Context context) {
            this(context, null);
        }
        public NonWrapNumberPicker(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }
        @SuppressWarnings({"UnusedDeclaration"})
        public NonWrapNumberPicker(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs);
        }
        @Override
        protected void changeCurrent(int current) {
            if (current > getEndRange()) {
                current = getEndRange();
            } else if (current < getBeginRange()) {
                current = getBeginRange();
            }
            super.changeCurrent(current);
        }
    }
}
