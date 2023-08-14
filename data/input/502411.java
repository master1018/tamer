public class LLEditTextThenButton extends Activity {
    private EditText mEditText;
    private Button mButton;
    private LinearLayout mLayout;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.linear_layout_edittext_then_button);
        mLayout = (LinearLayout) findViewById(R.id.layout);
        mEditText = (EditText) findViewById(R.id.editText);
        mButton = (Button) findViewById(R.id.button);
    }
    public LinearLayout getLayout() {
        return mLayout;
    }
    public EditText getEditText() {
        return mEditText;
    }
    public Button getButton() {
        return mButton;
    }
}
