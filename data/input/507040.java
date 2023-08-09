public class SaveRestoreState extends Activity
{
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_restore_state);
        ((TextView)findViewById(R.id.msg)).setText(R.string.save_restore_msg);
    }
    CharSequence getSavedText() {
        return ((EditText)findViewById(R.id.saved)).getText();
    }
    void setSavedText(CharSequence text) {
        ((EditText)findViewById(R.id.saved)).setText(text);
    }
}
