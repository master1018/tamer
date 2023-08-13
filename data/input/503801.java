public class OneEditTextActivitySelected extends Activity 
{
    private View mRootView;
    private View mDefaultFocusedView;
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        mRootView = new ScrollView(this);
        EditText editText = new EditText(this);
        editText.requestFocus();
        mDefaultFocusedView = editText;
        layout.addView(editText);
        ((ScrollView) mRootView).addView(layout);
        setContentView(mRootView);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
    public View getRootView() {
        return mRootView;
    }
    public View getDefaultFocusedView() {
        return mDefaultFocusedView;
    }
}
