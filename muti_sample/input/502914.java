public class BigEditTextActivityScrollablePanScan extends Activity {
    private View mRootView;
    private View mDefaultFocusedView;
    private LinearLayout mLayout;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mRootView = new ScrollView(this);
        ((ScrollView) mRootView).setFillViewport(true);
        mRootView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mLayout = new LinearLayout(this);
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        View view = getLayoutInflater().inflate(
                R.layout.full_screen_edit_text, ((ScrollView) mRootView), false);
        mLayout.addView(view);
        ((ScrollView) mRootView).addView(mLayout);
        mDefaultFocusedView = view.findViewById(R.id.data);
        setContentView(mRootView);
    }
    public View getRootView() {
        return mRootView;
    }
    public View getDefaultFocusedView() {
        return mDefaultFocusedView;
    }
}
