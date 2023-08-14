public class EditPinPreference extends EditTextPreference {
    private boolean shouldHideButtons;
    interface OnPinEnteredListener {
        void onPinEntered(EditPinPreference preference, boolean positiveResult);
    }
    private OnPinEnteredListener mPinListener;
    public void setOnPinEnteredListener(OnPinEnteredListener listener) {
        mPinListener = listener;
    }
    public EditPinPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public EditPinPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    protected View onCreateDialogView() {
        setDialogLayoutResource(R.layout.pref_dialog_editpin);
        View dialog = super.onCreateDialogView();
        final EditText textfield = getEditText();
        textfield.setTransformationMethod(PasswordTransformationMethod.getInstance());
        textfield.setKeyListener(DigitsKeyListener.getInstance());
        return dialog;
    }
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        shouldHideButtons = (view.findViewById(android.R.id.edit) == null);
    }
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        if (shouldHideButtons) {
            builder.setPositiveButton(null, this);
            builder.setNegativeButton(null, this);
        }
    }
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (mPinListener != null) {
            mPinListener.onPinEntered(this, positiveResult);
        }
    }
    public void showPinDialog() {
        showDialog(null);
    }
}
