public class DialtactsActivity extends TabActivity implements TabHost.OnTabChangeListener {
    private static final String TAG = "Dailtacts";
    private static final String FAVORITES_ENTRY_COMPONENT =
            "com.android.contacts.DialtactsFavoritesEntryActivity";
    private static final String CONTACTS_LAUNCH_ACTIVITY =
            "com.android.contacts.ContactsLaunchActivity";
    private static final int TAB_INDEX_DIALER = 0;
    private static final int TAB_INDEX_CALL_LOG = 1;
    private static final int TAB_INDEX_CONTACTS = 2;
    private static final int TAB_INDEX_FAVORITES = 3;
    static final String EXTRA_IGNORE_STATE = "ignore-state";
    static final String PREFS_DIALTACTS = "dialtacts";
    static final String PREF_FAVORITES_AS_CONTACTS = "favorites_as_contacts";
    static final boolean PREF_FAVORITES_AS_CONTACTS_DEFAULT = false;
    private static final String PREF_LAST_MANUALLY_SELECTED_TAB = "last_manually_selected_tab";
    private static final int PREF_LAST_MANUALLY_SELECTED_TAB_DEFAULT = TAB_INDEX_DIALER;
    private TabHost mTabHost;
    private String mFilterText;
    private Uri mDialUri;
    private int mLastManuallySelectedTab;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final Intent intent = getIntent();
        fixIntent(intent);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialer_activity);
        mTabHost = getTabHost();
        mTabHost.setOnTabChangedListener(this);
        setupDialerTab();
        setupCallLogTab();
        setupContactsTab();
        setupFavoritesTab();
        final SharedPreferences prefs = getSharedPreferences(PREFS_DIALTACTS, MODE_PRIVATE);
        mLastManuallySelectedTab = prefs.getInt(PREF_LAST_MANUALLY_SELECTED_TAB,
                PREF_LAST_MANUALLY_SELECTED_TAB_DEFAULT);
        setCurrentTab(intent);
        if (intent.getAction().equals(UI.FILTER_CONTACTS_ACTION)
                && icicle == null) {
            setupFilterText(intent);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        final int currentTabIndex = mTabHost.getCurrentTab();
        final SharedPreferences.Editor editor =
                getSharedPreferences(PREFS_DIALTACTS, MODE_PRIVATE).edit();
        if (currentTabIndex == TAB_INDEX_CONTACTS || currentTabIndex == TAB_INDEX_FAVORITES) {
            editor.putBoolean(PREF_FAVORITES_AS_CONTACTS, currentTabIndex == TAB_INDEX_FAVORITES);
        }
        editor.putInt(PREF_LAST_MANUALLY_SELECTED_TAB, mLastManuallySelectedTab);
        editor.commit();
    }
    private void fixIntent(Intent intent) {
        if (Intent.ACTION_CALL_BUTTON.equals(intent.getAction())) {
            intent.setDataAndType(Calls.CONTENT_URI, Calls.CONTENT_TYPE);
            intent.putExtra("call_key", true);
            setIntent(intent);
        }
    }
    private void setupCallLogTab() {
        Intent intent = new Intent("com.android.phone.action.RECENT_CALLS");
        intent.setClass(this, RecentCallsListActivity.class);
        mTabHost.addTab(mTabHost.newTabSpec("call_log")
                .setIndicator(getString(R.string.recentCallsIconLabel),
                        getResources().getDrawable(R.drawable.ic_tab_recent))
                .setContent(intent));
    }
    private void setupDialerTab() {
        Intent intent = new Intent("com.android.phone.action.TOUCH_DIALER");
        intent.setClass(this, TwelveKeyDialer.class);
        mTabHost.addTab(mTabHost.newTabSpec("dialer")
                .setIndicator(getString(R.string.dialerIconLabel),
                        getResources().getDrawable(R.drawable.ic_tab_dialer))
                .setContent(intent));
    }
    private void setupContactsTab() {
        Intent intent = new Intent(UI.LIST_DEFAULT);
        intent.setClass(this, ContactsListActivity.class);
        mTabHost.addTab(mTabHost.newTabSpec("contacts")
                .setIndicator(getText(R.string.contactsIconLabel),
                        getResources().getDrawable(R.drawable.ic_tab_contacts))
                .setContent(intent));
    }
    private void setupFavoritesTab() {
        Intent intent = new Intent(UI.LIST_STREQUENT_ACTION);
        intent.setClass(this, ContactsListActivity.class);
        mTabHost.addTab(mTabHost.newTabSpec("favorites")
                .setIndicator(getString(R.string.contactsFavoritesLabel),
                        getResources().getDrawable(R.drawable.ic_tab_starred))
                .setContent(intent));
    }
    private boolean isSendKeyWhileInCall(final Intent intent, final boolean recentCallsRequest) {
        if (recentCallsRequest) {
            final boolean callKey = intent.getBooleanExtra("call_key", false);
            try {
                ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                if (callKey && phone != null && phone.showCallScreen()) {
                    return true;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to handle send while in call", e);
            }
        }
        return false;
    }
    private void setCurrentTab(Intent intent) {
        final boolean recentCallsRequest = Calls.CONTENT_TYPE.equals(intent.getType());
        if (isSendKeyWhileInCall(intent, recentCallsRequest)) {
            finish();
            return;
        }
        Activity activity = getLocalActivityManager().
                getActivity(mTabHost.getCurrentTabTag());
        if (activity != null) {
            activity.closeOptionsMenu();
        }
        intent.putExtra(EXTRA_IGNORE_STATE, true);
        final int savedTabIndex = mLastManuallySelectedTab;
        String componentName = intent.getComponent().getClassName();
        if (getClass().getName().equals(componentName)) {
            if (recentCallsRequest) {
                mTabHost.setCurrentTab(TAB_INDEX_CALL_LOG);
            } else {
                mTabHost.setCurrentTab(TAB_INDEX_DIALER);
            }
        } else if (FAVORITES_ENTRY_COMPONENT.equals(componentName)) {
            mTabHost.setCurrentTab(TAB_INDEX_FAVORITES);
        } else if (CONTACTS_LAUNCH_ACTIVITY.equals(componentName)) {
            mTabHost.setCurrentTab(mLastManuallySelectedTab);
        } else {
            SharedPreferences prefs = getSharedPreferences(PREFS_DIALTACTS, MODE_PRIVATE);
            boolean favoritesAsContacts = prefs.getBoolean(PREF_FAVORITES_AS_CONTACTS,
                    PREF_FAVORITES_AS_CONTACTS_DEFAULT);
            if (favoritesAsContacts) {
                mTabHost.setCurrentTab(TAB_INDEX_FAVORITES);
            } else {
                mTabHost.setCurrentTab(TAB_INDEX_CONTACTS);
            }
        }
        mLastManuallySelectedTab = savedTabIndex;
        intent.putExtra(EXTRA_IGNORE_STATE, false);
    }
    @Override
    public void onNewIntent(Intent newIntent) {
        setIntent(newIntent);
        fixIntent(newIntent);
        setCurrentTab(newIntent);
        final String action = newIntent.getAction();
        if (action.equals(UI.FILTER_CONTACTS_ACTION)) {
            setupFilterText(newIntent);
        } else if (isDialIntent(newIntent)) {
            setupDialUri(newIntent);
        }
    }
    private boolean isDialIntent(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_DIAL.equals(action)) {
            return true;
        }
        if (Intent.ACTION_VIEW.equals(action)) {
            final Uri data = intent.getData();
            if (data != null && "tel".equals(data.getScheme())) {
                return true;
            }
        }
        return false;
    }
    public String getAndClearFilterText() {
        String filterText = mFilterText;
        mFilterText = null;
        return filterText;
    }
    private void setupFilterText(Intent intent) {
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            return;
        }
        String filter = intent.getStringExtra(UI.FILTER_TEXT_EXTRA_KEY);
        if (filter != null && filter.length() > 0) {
            mFilterText = filter;
        }
    }
    public Uri getAndClearDialUri() {
        Uri dialUri = mDialUri;
        mDialUri = null;
        return dialUri;
    }
    private void setupDialUri(Intent intent) {
        if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            return;
        }
        mDialUri = intent.getData();
    }
    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            moveTaskToBack(false);
        } else {
            super.onBackPressed();
        }
    }
    public void onTabChanged(String tabId) {
        Activity activity = getLocalActivityManager().getActivity(tabId);
        if (activity != null) {
            activity.onWindowFocusChanged(true);
        }
        mLastManuallySelectedTab = mTabHost.getCurrentTab();
    }
}
