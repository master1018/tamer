public class GlobalFocusChange extends Activity implements ViewTreeObserver.OnGlobalFocusChangeListener {
    public View mOldFocus;
    public View mNewFocus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focus_listener);
        findViewById(R.id.left).getViewTreeObserver().addOnGlobalFocusChangeListener(this);
    }
    public void reset() {
        mOldFocus = mNewFocus = null;
    }
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        mOldFocus = oldFocus;
        mNewFocus = newFocus;
    }
}
