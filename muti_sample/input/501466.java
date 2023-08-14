public class MockSRSProvider extends SearchRecentSuggestionsProvider {
    final static String AUTHORITY = "android.content.cts.MockSRSProvider";
    final static int MODE = DATABASE_MODE_QUERIES + DATABASE_MODE_2LINES;
    public static boolean setupSuggestCalled;
    private boolean mOnCreateCalled;
    public MockSRSProvider() {
        super();
        setupSuggestions(AUTHORITY, MODE);
    }
    public MockSRSProvider(String tag) {
        super();
    }
    @Override
    public void setupSuggestions(String authority, int mode) {
        setupSuggestCalled = true;
        super.setupSuggestions(authority, mode);
    }
    @Override
    public boolean onCreate() {
        mOnCreateCalled = true;
        return super.onCreate();
    }
    public boolean isOnCreateCalled() {
        return mOnCreateCalled;
    }
}
