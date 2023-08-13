public class WebConcurrency extends SourceLatency {
    private static final String QUERY = "hillary clinton";
    private static final long DELAY_MS = 150;
    @Override
    protected void onResume() {
        super.onResume();
        testEnhancedGoogleSearchConcurrent();
        finish();
    }
    private ComponentName getWebSearchComponent() {
        Intent webSearchIntent = new Intent(Intent.ACTION_WEB_SEARCH);
        PackageManager pm = getPackageManager();
        return webSearchIntent.resolveActivity(pm);
    }
    private void testEnhancedGoogleSearchConcurrent() {
        ComponentName webComponent = getWebSearchComponent();
        checkSourceConcurrent("WEB", webComponent, QUERY, DELAY_MS);
    }
}
