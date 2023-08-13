public class ClearLearnDictionaryDialogPreferenceJAJP extends DialogPreference {
    protected Context mContext = null;
    public ClearLearnDictionaryDialogPreferenceJAJP(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    public ClearLearnDictionaryDialogPreferenceJAJP(Context context) {
        this(context, null);
    }
    @Override protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            OpenWnnEvent ev = new OpenWnnEvent(OpenWnnEvent.INITIALIZE_LEARNING_DICTIONARY, new WnnWord());
            OpenWnnJAJP.getInstance().onEvent(ev);
            Toast.makeText(mContext.getApplicationContext(), R.string.dialog_clear_learning_dictionary_done,
                           Toast.LENGTH_LONG).show();
        }
    }
}
