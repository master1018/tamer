public class ApplicationsLatency extends SourceLatency {
    private static final String[] queries = { "", "a", "s", "e", "r", "pub", "sanxjkashasrxae" };
    private static ComponentName APPS_COMPONENT =
            new ComponentName("com.android.providers.applications",
                "com.android.providers.applications.ApplicationLauncher");
    @Override
    protected void onResume() {
        super.onResume();
        testApps();
    }
    private void testApps() {
        for (String query : queries) {
            checkSource("APPS", APPS_COMPONENT, query);
        }
    }
}
