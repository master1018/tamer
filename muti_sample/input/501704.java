public class SpammySuggestionLauncher extends Activity {
    private static final String TAG = SpammySuggestionLauncher.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Log.i(TAG, "Launched a spammy suggestion: " + intent);
        finish();
    }
}
