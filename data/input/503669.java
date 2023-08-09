public class AgendaActivity extends Activity implements Navigator {
    private static final String TAG = "AgendaActivity";
    private static boolean DEBUG = false;
    protected static final String BUNDLE_KEY_RESTORE_TIME = "key_restore_time";
    private static final long INITIAL_HEAP_SIZE = 4*1024*1024;
    private ContentResolver mContentResolver;
    private AgendaListView mAgendaListView;
    private Time mTime;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_CHANGED)
                    || action.equals(Intent.ACTION_DATE_CHANGED)
                    || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                mAgendaListView.refresh(true);
            }
        }
    };
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
        @Override
        public void onChange(boolean selfChange) {
            mAgendaListView.refresh(true);
        }
    };
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        VMRuntime.getRuntime().setMinimumHeapSize(INITIAL_HEAP_SIZE);
        mAgendaListView = new AgendaListView(this);
        setContentView(mAgendaListView);
        mContentResolver = getContentResolver();
        setTitle(R.string.agenda_view);
        long millis = 0;
        mTime = new Time();
        if (icicle != null) {
            millis = icicle.getLong(BUNDLE_KEY_RESTORE_TIME);
            if (DEBUG) {
                Log.v(TAG, "Restore value from icicle: " + millis);
            }
        }
        if (millis == 0) {
            millis = getIntent().getLongExtra(EVENT_BEGIN_TIME, 0);
            if (DEBUG) {
                Time time = new Time();
                time.set(millis);
                Log.v(TAG, "Restore value from intent: " + time.toString());
            }
        }
        if (millis == 0) {
            if (DEBUG) {
                Log.v(TAG, "Restored from current time");
            }
            millis = System.currentTimeMillis();
        }
        mTime.set(millis);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        long time = Utils.timeFromIntentInMillis(intent);
        if (time > 0) {
            mTime.set(time);
            goTo(mTime, false);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) {
            Log.v(TAG, "OnResume to " + mTime.toString());
        }
        SharedPreferences prefs = CalendarPreferenceActivity.getSharedPreferences(
                getApplicationContext());
        boolean hideDeclined = prefs.getBoolean(
                CalendarPreferenceActivity.KEY_HIDE_DECLINED, false);
        mAgendaListView.setHideDeclinedEvents(hideDeclined);
        mAgendaListView.goTo(mTime, true);
        mAgendaListView.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(mIntentReceiver, filter);
        mContentResolver.registerContentObserver(Events.CONTENT_URI, true, mObserver);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        long firstVisibleTime = mAgendaListView.getFirstVisibleTime();
        if (firstVisibleTime > 0) {
            mTime.set(firstVisibleTime);
            outState.putLong(BUNDLE_KEY_RESTORE_TIME, firstVisibleTime);
            if (DEBUG) {
                Log.v(TAG, "onSaveInstanceState " + mTime.toString());
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        mAgendaListView.onPause();
        mContentResolver.unregisterContentObserver(mObserver);
        unregisterReceiver(mIntentReceiver);
        Utils.setDefaultView(this, CalendarApplication.AGENDA_VIEW_ID);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuHelper.onPrepareOptionsMenu(this, menu);
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuHelper.onCreateOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuHelper.onOptionsItemSelected(this, item, this);
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                mAgendaListView.deleteSelectedEvent();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void goToToday() {
        Time now = new Time();
        now.setToNow();
        mAgendaListView.goTo(now, true); 
    }
    public void goTo(Time time, boolean animate) {
        mAgendaListView.goTo(time, false);
    }
    public long getSelectedTime() {
        return mAgendaListView.getSelectedTime();
    }
    public boolean getAllDay() {
        return false;
    }
}
