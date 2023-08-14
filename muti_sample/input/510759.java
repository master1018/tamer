public class SlowSuggestionLauncher extends Activity {
    private static final String TAG = SlowSuggestionLauncher.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Log.i(TAG, "Launched a slow suggestion: " + intent);
        finish();
    }
}
