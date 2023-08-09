public class OneEditTextActivityNotSelected extends Activity 
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
        Button button = new Button(this);
        button.setText("The focus is here.");
        button.setFocusableInTouchMode(true);
        button.requestFocus();
        mDefaultFocusedView = button;
        layout.addView(button);
        layout.addView(editText);
        ((ScrollView) mRootView).addView(layout);
        setContentView(mRootView);
    }  
    public View getRootView() {
        return mRootView;
    }
    public View getDefaultFocusedView() {
        return mDefaultFocusedView;
    }
}
