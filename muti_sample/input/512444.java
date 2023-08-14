public class GoneParentFocusedChild extends Activity {
    private LinearLayout mGoneGroup;
    private Button mButton;
    private boolean mUnhandledKeyEvent = false;
    private LinearLayout mLayout;
    public boolean isUnhandledKeyEvent() {
        return mUnhandledKeyEvent;
    }
    public LinearLayout getLayout() {
        return mLayout;
    }
    public LinearLayout getGoneGroup() {
        return mGoneGroup;
    }
    public Button getButton() {
        return mButton;
    }
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mLayout = new LinearLayout(this);
        mLayout.setOrientation(LinearLayout.HORIZONTAL);
        mLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mGoneGroup = new LinearLayout(this);
        mGoneGroup.setOrientation(LinearLayout.HORIZONTAL);
        mGoneGroup.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mButton = new Button(this);
        mButton.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        mGoneGroup.addView(mButton);
        setContentView(mLayout);
        mGoneGroup.setVisibility(View.GONE);
        mButton.requestFocus();
    }
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        mUnhandledKeyEvent = true;
        return true;
    }
}
