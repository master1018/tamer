public class LaunchpadTabActivity extends TabActivity {
    public LaunchpadTabActivity() {
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent tabIntent = new Intent(getIntent());
        tabIntent.setComponent((ComponentName)tabIntent.getParcelableExtra("tab"));
        TabHost th = getTabHost();
        TabHost.TabSpec ts = th.newTabSpec("1");
        ts.setIndicator("One");
        ts.setContent(tabIntent);
        th.addTab(ts);
    }
}
