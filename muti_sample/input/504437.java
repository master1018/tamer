public class SelectCalendarsActivity extends ExpandableListActivity
    implements AdapterView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "Calendar";
    private static final String EXPANDED_KEY = "is_expanded";
    private View mView = null;
    private Cursor mCursor = null;
    private ExpandableListView mList;
    private SelectCalendarsAdapter mAdapter;
    private static final String[] PROJECTION = new String[] {
        Calendars._ID,
        Calendars._SYNC_ACCOUNT_TYPE,
        Calendars._SYNC_ACCOUNT
    };
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.calendars_activity);
        getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS,
                Window.PROGRESS_INDETERMINATE_ON);
        mList = getExpandableListView();
        mView = findViewById(R.id.calendars);
        Context context = mView.getContext();
        mCursor = managedQuery(Calendars.CONTENT_URI, PROJECTION,
                "1) GROUP BY (_sync_account", 
                null ,
                Calendars._SYNC_ACCOUNT );
        MatrixCursor accountsCursor = Utils.matrixCursorFromCursor(mCursor);
        startManagingCursor(accountsCursor);
        mAdapter = new SelectCalendarsAdapter(context, accountsCursor, this);
        mList.setAdapter(mAdapter);
        mList.setOnChildClickListener(this);
        findViewById(R.id.btn_done).setOnClickListener(this);
        findViewById(R.id.btn_discard).setOnClickListener(this);
        startCalendarMetafeedSync();
        int count = mList.getCount();
        for(int i = 0; i < count; i++) {
            mList.expandGroup(i);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.startRefreshStopDelay();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.cancelRefreshStopDelay();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        boolean[] isExpanded;
        mList = getExpandableListView();
        if(mList != null) {
            int count = mList.getCount();
            isExpanded = new boolean[count];
            for(int i = 0; i < count; i++) {
                isExpanded[i] = mList.isGroupExpanded(i);
            }
        } else {
            isExpanded = null;
        }
        outState.putBooleanArray(EXPANDED_KEY, isExpanded);
    }
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mList = getExpandableListView();
        boolean[] isExpanded = state.getBooleanArray(EXPANDED_KEY);
        if(mList != null && isExpanded != null && mList.getCount() >= isExpanded.length) {
            for(int i = 0; i < isExpanded.length; i++) {
                if(isExpanded[i] && !mList.isGroupExpanded(i)) {
                    mList.expandGroup(i);
                } else if(!isExpanded[i] && mList.isGroupExpanded(i)){
                    mList.collapseGroup(i);
                }
            }
        }
    }
    @Override
    public boolean onChildClick(ExpandableListView parent, View view, int groupPosition,
            int childPosition, long id) {
        MultiStateButton button = (MultiStateButton) view.findViewById(R.id.multiStateButton);
        return button.performClick();
    }
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MultiStateButton button = (MultiStateButton) view.findViewById(R.id.multiStateButton);
        button.performClick();
    }
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_done: {
                doSaveAction();
                break;
            }
            case R.id.btn_discard: {
                finish();
                break;
            }
        }
    }
    private void doSaveAction() {
        mAdapter.doSaveAction();
        finish();
    }
    private void startCalendarMetafeedSync() {
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean("metafeedonly", true);
        ContentResolver.requestSync(null ,
                Calendars.CONTENT_URI.getAuthority(), extras);
    }
}
