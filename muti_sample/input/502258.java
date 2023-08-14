public class PermissionStubActivity extends Activity {
    private ListView mListView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListView = new ListView(this);
        mListView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        setContentView(mListView);
    }
    public Dialog getDialog() {
        return new AlertDialog.Builder(PermissionStubActivity.this).create();
    }
}
