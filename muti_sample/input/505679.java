public class WebLatency extends SourceLatency {
    private static final String[] queries = {
        "", "a", "s", "e", "r", "pub", "taxi", "kilt hire", "pizza",
             "weather london uk", "terminator showtimes", "obama news",
             "12 USD in GBP", "how to pass a drug test", "goog stock",
             "76 Bucking",
             "sanxjkashasrxae"
    };
    @Override
    protected void onResume() {
        super.onResume();
        testWebSuggestions();
        finish();
    }
    private ComponentName getWebSearchComponent() {
        Intent webSearchIntent = new Intent(Intent.ACTION_WEB_SEARCH);
        PackageManager pm = getPackageManager();
        return webSearchIntent.resolveActivity(pm);
    }
    private void testWebSuggestions() {
        ComponentName webComponent = getWebSearchComponent();
        checkSource("WEB", webComponent, queries);
    }
}
