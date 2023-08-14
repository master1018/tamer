public class BottomEditTextActivityPanScan extends Activity 
{
    private View mRootView;
    private View mDefaultFocusedView;
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        mRootView = new LinearLayout(this);
        ((LinearLayout) mRootView).setOrientation(LinearLayout.VERTICAL);
        View view = getLayoutInflater().inflate(R.layout.one_edit_text_activity, ((LinearLayout) mRootView), false);
        mDefaultFocusedView = view.findViewById(R.id.dialog_edit_text);
        ((LinearLayout) mRootView).addView(view);
        setContentView(mRootView);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }  
    public View getRootView() {
        return mRootView;
    }
    public View getDefaultFocusedView() {
        return mDefaultFocusedView;
    }
}
