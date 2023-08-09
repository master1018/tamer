public class BookmarkSearch extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (Intent.ACTION_VIEW.equals(action)) {
                intent.setClass(this, BrowserActivity.class);
                startActivity(intent);
            }
        }
        finish();
    }
}
