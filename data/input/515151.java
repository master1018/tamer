public class SelectCalendarsAdapter extends CursorTreeAdapter implements View.OnClickListener {
    private static final String TAG = "Calendar";
    private static final String COLLATE_NOCASE = " COLLATE NOCASE";
    private static final String IS_PRIMARY = "\"primary\"";
    private static final String CALENDARS_ORDERBY = IS_PRIMARY + " DESC," + Calendars.DISPLAY_NAME +
            COLLATE_NOCASE;
    private static final String ACCOUNT_SELECTION = Calendars._SYNC_ACCOUNT + "=?"
            + " AND " + Calendars._SYNC_ACCOUNT_TYPE + "=?";
    private static final int[] SYNC_VIS_BUTTON_RES = new int[] {
        R.drawable.widget_show,
        R.drawable.widget_sync,
        R.drawable.widget_off
    };
    private final LayoutInflater mInflater;
    private final ContentResolver mResolver;
    private final SelectCalendarsActivity mActivity;
    private final View mView;
    private final static Runnable mStopRefreshing = new Runnable() {
        public void run() {
            mRefresh = false;
        }
    };
    private Map<String, AuthenticatorDescription> mTypeToAuthDescription
        = new HashMap<String, AuthenticatorDescription>();
    protected AuthenticatorDescription[] mAuthDescs;
    private Map<Long, Boolean[]> mCalendarChanges
        = new HashMap<Long, Boolean[]>();
    private Map<Long, Boolean[]> mCalendarInitialStates
        = new HashMap<Long, Boolean[]>();
    private static final int SELECTED_INDEX = 0;
    private static final int SYNCED_INDEX = 1;
    private static final int CHANGES_SIZE = 2;
    private static Map<String, Cursor> mChildrenCursors
        = new HashMap<String, Cursor>();
    private static AsyncCalendarsUpdater mCalendarsUpdater;
    private static final int MIN_UPDATE_TOKEN = 1000;
    private static int mUpdateToken = MIN_UPDATE_TOKEN;
    private static final int REFRESH_DELAY = 5000;
    private static final int REFRESH_DURATION = 60000;
    private static boolean mRefresh = true;
    private int mNumAccounts;
    private static String syncedVisible;
    private static String syncedNotVisible;
    private static String notSyncedNotVisible;
    private static HashMap<String, Boolean> mIsDuplicateName = new HashMap<String, Boolean>();
    private static final String[] PROJECTION = new String[] {
      Calendars._ID,
      Calendars._SYNC_ACCOUNT,
      Calendars.OWNER_ACCOUNT,
      Calendars.DISPLAY_NAME,
      Calendars.COLOR,
      Calendars.SELECTED,
      Calendars.SYNC_EVENTS,
      "(" + Calendars._SYNC_ACCOUNT + "=" + Calendars.OWNER_ACCOUNT + ") AS " + IS_PRIMARY,
    };
    private static final int ID_COLUMN = 0;
    private static final int ACCOUNT_COLUMN = 1;
    private static final int OWNER_COLUMN = 2;
    private static final int NAME_COLUMN = 3;
    private static final int COLOR_COLUMN = 4;
    private static final int SELECTED_COLUMN = 5;
    private static final int SYNCED_COLUMN = 6;
    private static final int PRIMARY_COLUMN = 7;
    private class AsyncCalendarsUpdater extends AsyncQueryHandler {
        public AsyncCalendarsUpdater(ContentResolver cr) {
            super(cr);
        }
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if(cursor == null) {
                return;
            }
            Cursor currentCursor = mChildrenCursors.get(cookie);
            if (currentCursor != null) {
                if (Utils.compareCursors(currentCursor, cursor)) {
                    cursor.close();
                    return;
                }
            }
            MatrixCursor newCursor = Utils.matrixCursorFromCursor(cursor);
            cursor.close();
            Utils.checkForDuplicateNames(mIsDuplicateName, newCursor, NAME_COLUMN);
            mChildrenCursors.put((String)cookie, newCursor);
            try {
                setChildrenCursor(token, newCursor);
                mActivity.startManagingCursor(newCursor);
            } catch (NullPointerException e) {
                Log.w(TAG, "Adapter expired, try again on the next query: " + e);
            }
            if (currentCursor != null) {
                mActivity.stopManagingCursor(currentCursor);
                currentCursor.close();
            }
        }
    }
    public void onClick(View v) {
        View view = (View)v.getTag();
        long id = (Long)view.getTag();
        Uri uri = ContentUris.withAppendedId(Calendars.CONTENT_URI, id);
        String status = syncedNotVisible;
        Boolean[] change;
        Boolean[] initialState = mCalendarInitialStates.get(id);
        if (mCalendarChanges.containsKey(id)) {
            change = mCalendarChanges.get(id);
        } else {
            change = new Boolean[CHANGES_SIZE];
            change[SELECTED_INDEX] = initialState[SELECTED_INDEX];
            change[SYNCED_INDEX] = initialState[SYNCED_INDEX];
            mCalendarChanges.put(id, change);
        }
        if (change[SELECTED_INDEX]) {
            change[SELECTED_INDEX] = false;
            status = syncedNotVisible;
        }
        else if (change[SYNCED_INDEX]) {
            change[SYNCED_INDEX] = false;
            status = notSyncedNotVisible;
        }
        else
        {
            change[SYNCED_INDEX] = true;
            change[SELECTED_INDEX] = true;
            status = syncedVisible;
        }
        setText(view, R.id.status, status);
        if (change[SELECTED_INDEX] == initialState[SELECTED_INDEX] &&
                change[SYNCED_INDEX] == initialState[SYNCED_INDEX]) {
            mCalendarChanges.remove(id);
        }
    }
    public SelectCalendarsAdapter(Context context, Cursor cursor, SelectCalendarsActivity act) {
        super(cursor, context);
        syncedVisible = context.getString(R.string.synced_visible);
        syncedNotVisible = context.getString(R.string.synced_not_visible);
        notSyncedNotVisible = context.getString(R.string.not_synced_not_visible);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResolver = context.getContentResolver();
        mActivity = act;
        if (mCalendarsUpdater == null) {
            mCalendarsUpdater = new AsyncCalendarsUpdater(mResolver);
        }
        mNumAccounts = cursor.getCount();
        if(mNumAccounts == 0) {
            Log.e(TAG, "SelectCalendarsAdapter: No accounts were returned!");
        }
        mAuthDescs = AccountManager.get(context).getAuthenticatorTypes();
        for (int i = 0; i < mAuthDescs.length; i++) {
            mTypeToAuthDescription.put(mAuthDescs[i].type, mAuthDescs[i]);
        }
        mView = mActivity.getExpandableListView();
        mRefresh = true;
    }
    public void startRefreshStopDelay() {
        mRefresh = true;
        mView.postDelayed(mStopRefreshing, REFRESH_DURATION);
    }
    public void cancelRefreshStopDelay() {
        mView.removeCallbacks(mStopRefreshing);
    }
    public void doSaveAction() {
        mCalendarsUpdater.cancelOperation(mUpdateToken);
        mUpdateToken++;
        if(mUpdateToken < MIN_UPDATE_TOKEN) mUpdateToken = MIN_UPDATE_TOKEN;
        Iterator<Long> changeKeys = mCalendarChanges.keySet().iterator();
        while (changeKeys.hasNext()) {
            long id = changeKeys.next();
            Boolean[] change = mCalendarChanges.get(id);
            int newSelected = change[SELECTED_INDEX] ? 1 : 0;
            int newSynced = change[SYNCED_INDEX] ? 1 : 0;
            Uri uri = ContentUris.withAppendedId(Calendars.CONTENT_URI, id);
            ContentValues values = new ContentValues();
            values.put(Calendars.SELECTED, newSelected);
            values.put(Calendars.SYNC_EVENTS, newSynced);
            mCalendarsUpdater.startUpdate(mUpdateToken, id, uri, values, null, null);
        }
    }
    private static void setText(View view, int id, String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        TextView textView = (TextView) view.findViewById(id);
        textView.setText(text);
    }
    protected CharSequence getLabelForType(final String accountType) {
        CharSequence label = null;
        if (mTypeToAuthDescription.containsKey(accountType)) {
             try {
                 AuthenticatorDescription desc = mTypeToAuthDescription.get(accountType);
                 Context authContext = mActivity.createPackageContext(desc.packageName, 0);
                 label = authContext.getResources().getText(desc.labelId);
             } catch (PackageManager.NameNotFoundException e) {
                 Log.w(TAG, "No label for account type " + ", type " + accountType);
             }
        }
        return label;
    }
    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        String account = cursor.getString(ACCOUNT_COLUMN);
        String status = notSyncedNotVisible;
        int state = 2;
        int position = cursor.getPosition();
        long id = cursor.getLong(ID_COLUMN);
        Boolean[] initialState = mCalendarChanges.get(id);
        if (initialState == null) {
            initialState = new Boolean[CHANGES_SIZE];
            initialState[SELECTED_INDEX] = cursor.getInt(SELECTED_COLUMN) == 1;
            initialState[SYNCED_INDEX] = cursor.getInt(SYNCED_COLUMN) == 1;
            mCalendarInitialStates.put(id, initialState);
        }
        if(initialState[SYNCED_INDEX]) {
            if(initialState[SELECTED_INDEX]) {
                status = syncedVisible;
                state = 0;
            } else {
                status = syncedNotVisible;
                state = 1;
            }
        }
        view.findViewById(R.id.color)
            .setBackgroundDrawable(Utils.getColorChip(cursor.getInt(COLOR_COLUMN)));
        String name = cursor.getString(NAME_COLUMN);
        String owner = cursor.getString(OWNER_COLUMN);
        if (mIsDuplicateName.containsKey(name) && mIsDuplicateName.get(name) &&
                !name.equalsIgnoreCase(owner)) {
            name = new StringBuilder(name)
                    .append(Utils.OPEN_EMAIL_MARKER)
                    .append(owner)
                    .append(Utils.CLOSE_EMAIL_MARKER)
                    .toString();
        }
        setText(view, R.id.calendar, name);
        setText(view, R.id.status, status);
        MultiStateButton button = (MultiStateButton) view.findViewById(R.id.multiStateButton);
        button.setTag(view);
        view.setTag(id);
        button.setOnClickListener(this);
        button.setButtonResources(SYNC_VIS_BUTTON_RES);
        button.setState(state);
    }
    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        int accountColumn = cursor.getColumnIndexOrThrow(Calendars._SYNC_ACCOUNT);
        int accountTypeColumn = cursor.getColumnIndexOrThrow(Calendars._SYNC_ACCOUNT_TYPE);
        String account = cursor.getString(accountColumn);
        String accountType = cursor.getString(accountTypeColumn);
        setText(view, R.id.account, account);
        setText(view, R.id.account_type, getLabelForType(accountType).toString());
    }
    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        int accountColumn = groupCursor.getColumnIndexOrThrow(Calendars._SYNC_ACCOUNT);
        int accountTypeColumn = groupCursor.getColumnIndexOrThrow(Calendars._SYNC_ACCOUNT_TYPE);
        String account = groupCursor.getString(accountColumn);
        String accountType = groupCursor.getString(accountTypeColumn);
        Cursor childCursor = mChildrenCursors.get(account);
        new RefreshCalendars(groupCursor.getPosition(), account, accountType).run();
        return childCursor;
    }
    @Override
    protected View newChildView(Context context, Cursor cursor, boolean isLastChild,
            ViewGroup parent) {
        return mInflater.inflate(R.layout.calendar_item, parent, false);
    }
    @Override
    protected View newGroupView(Context context, Cursor cursor, boolean isExpanded,
            ViewGroup parent) {
        return mInflater.inflate(R.layout.account_item, parent, false);
    }
    private class RefreshCalendars implements Runnable {
        int mToken;
        String mAccount;
        String mAccountType;
        public RefreshCalendars(int token, String cookie, String accountType) {
            mToken = token;
            mAccount = cookie;
            mAccountType = accountType;
        }
        public void run() {
            mCalendarsUpdater.cancelOperation(mToken);
            if(mRefresh) {
                mView.postDelayed(new RefreshCalendars(mToken, mAccount, mAccountType),
                        REFRESH_DELAY);
            }
            mCalendarsUpdater.startQuery(mToken,
                    mAccount,
                    Calendars.CONTENT_URI, PROJECTION,
                    ACCOUNT_SELECTION,
                    new String[] { mAccount, mAccountType } ,
                    CALENDARS_ORDERBY);
        }
    }
}
