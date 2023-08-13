public class LaunchActivity extends Activity {
    private static final String TAG = "LaunchActivity";
    static final String KEY_DETAIL_VIEW = "DETAIL_VIEW";
    static final String KEY_VIEW_TYPE = "VIEW";
    static final String VIEW_TYPE_DAY = "DAY";
    private Bundle mExtras;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mExtras = getIntent().getExtras();
        if (icicle == null) {
            Account[] accounts = AccountManager.get(this).getAccounts();
            if(accounts.length > 0) {
                launchCalendarView();
            } else {
                final Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {
                    Calendar.AUTHORITY
                });
                startActivityForResult(intent, 0);
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Account[] accounts = AccountManager.get(this).getAccounts();
        if(accounts.length > 0) {
            launchCalendarView();
        } else {
            finish();
        }
    }
    private void launchCalendarView() {
        Intent myIntent = getIntent();
        Uri myData = myIntent.getData();
        Intent intent = new Intent();
        if (myData != null) {
            intent.setData(myData);
        }
        String defaultViewKey = CalendarPreferenceActivity.KEY_START_VIEW;
        if (mExtras != null) {
            intent.putExtras(mExtras);
            if (mExtras.getBoolean(KEY_DETAIL_VIEW, false)) {
                defaultViewKey = CalendarPreferenceActivity.KEY_DETAILED_VIEW;
            } else if (VIEW_TYPE_DAY.equals(mExtras.getString(KEY_VIEW_TYPE))) {
                defaultViewKey = VIEW_TYPE_DAY;
            }
        }
        intent.putExtras(myIntent);
        SharedPreferences prefs = CalendarPreferenceActivity.getSharedPreferences(this);
        String startActivity;
        if (defaultViewKey.equals(VIEW_TYPE_DAY)) {
            startActivity = CalendarApplication.ACTIVITY_NAMES[CalendarApplication.DAY_VIEW_ID];
        } else if (defaultViewKey.equals(CalendarPreferenceActivity.KEY_DETAILED_VIEW)) {
            startActivity = prefs.getString(defaultViewKey,
                    CalendarPreferenceActivity.DEFAULT_DETAILED_VIEW);
        } else {
            startActivity = prefs.getString(defaultViewKey,
                    CalendarPreferenceActivity.DEFAULT_START_VIEW);
        }
        intent.setClassName(this, startActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
