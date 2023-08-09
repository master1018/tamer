public class TabHostStubActivity extends TabActivity {
    public static final String INITIAL_TAB_TAG = "initial tag";
    public static final String INITIAL_TAB_LABEL = "initial label";
    public static final String INITIAL_VIEW_TEXT = "initial view text";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabhost_layout);
        TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec(INITIAL_TAB_TAG)
                .setIndicator(INITIAL_TAB_LABEL)
                .setContent(new MyTabContentFactory()));
    }
    private class MyTabContentFactory implements TabHost.TabContentFactory {
        public View createTabContent(String tag) {
            final TextView tv = new TextView(getApplicationContext());
            tv.setText(INITIAL_VIEW_TEXT);
            return tv;
        }
    }
}
