public class QueryTextView extends EditText {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.QueryTextView";
    private SuggestionClickListener mSuggestionClickListener;
    public QueryTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public QueryTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public QueryTextView(Context context) {
        super(context);
    }
    public void setTextSelection(boolean selectAll) {
        if (selectAll) {
            selectAll();
        } else {
            setSelection(length());
        }
    }
    protected void replaceText(CharSequence text) {
        clearComposingText();
        setText(text);
        setTextSelection(false);
    }
    public void setSuggestionClickListener(SuggestionClickListener listener) {
        mSuggestionClickListener = listener;
    }
    private InputMethodManager getInputMethodManager() {
        return (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }
    public void showInputMethod() {
        InputMethodManager imm = getInputMethodManager();
        if (imm != null) {
            imm.showSoftInput(this, 0);
        }
    }
    public void hideInputMethod() {
        InputMethodManager imm = getInputMethodManager();
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }
    @Override
    public void onCommitCompletion(CompletionInfo completion) {
        if (DBG) Log.d(TAG, "onCommitCompletion(" + completion + ")");
        hideInputMethod();
        replaceText(completion.getText());
        if (mSuggestionClickListener != null) {
            mSuggestionClickListener.onSuggestionClicked(completion.getPosition());
        }
    }
}
