public class ClearUserDictionaryDialogPreferenceJAJP extends DialogPreference {
    protected Context mContext = null;
    public ClearUserDictionaryDialogPreferenceJAJP(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }
    public ClearUserDictionaryDialogPreferenceJAJP(Context context) {
        this(context, null);
    }
    @Override protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            OpenWnnEvent ev = new OpenWnnEvent(OpenWnnEvent.INITIALIZE_USER_DICTIONARY, new WnnWord());
            OpenWnnJAJP.getInstance().onEvent(ev);
            Toast.makeText(mContext.getApplicationContext(), R.string.dialog_clear_user_dictionary_done,
                           Toast.LENGTH_LONG).show();
        }
    }
}
