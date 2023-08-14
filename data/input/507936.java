public class RecentCallsListActivityTests
        extends ActivityInstrumentationTestCase2<RecentCallsListActivity> {
    static private final String TAG = "RecentCallsListActivityTests";
    static private final String[] CALL_LOG_PROJECTION = new String[] {
            Calls._ID,
            Calls.NUMBER,
            Calls.DATE,
            Calls.DURATION,
            Calls.TYPE,
            Calls.CACHED_NAME,
            Calls.CACHED_NUMBER_TYPE,
            Calls.CACHED_NUMBER_LABEL
    };
    static private final int RAND_DURATION = -1;
    static private final long NOW = -1L;
    private RecentCallsListActivity mActivity;
    private FrameLayout mParentView;
    private RecentCallsListActivity.RecentCallsAdapter mAdapter;
    private String mVoicemail;
    private MatrixCursor mCursor;
    private int mIndex;  
    private Random mRnd;
    private HashMap<Integer, Bitmap> mCallTypeIcons;
    private RecentCallsListActivity.RecentCallsListItemViews mItem;
    private View[] mList;
    public RecentCallsListActivityTests() {
        super("com.android.contacts", RecentCallsListActivity.class);
        mIndex = 1;
        mRnd = new Random();
    }
    @Override
    public void setUp() {
        mActivity = getActivity();
        mVoicemail = mActivity.mVoiceMailNumber;
        mAdapter = mActivity.mAdapter;
        mParentView = new FrameLayout(mActivity);
        mCursor = new MatrixCursor(CALL_LOG_PROJECTION);
        buildIconMap();
    }
    @MediumTest
    public void testCallViewIsNotVisibleForPrivateAndUnknownNumbers() {
        final int SIZE = 100;
        mList = new View[SIZE];
        mCursor.moveToFirst();
        insertRandomEntries(SIZE / 2);
        int startOfSecondBatch = mCursor.getPosition();
        buildViewListFromDb();
        checkCallStatus();
        mCursor.move(startOfSecondBatch);
        insertRandomEntries(SIZE / 2);
        buildViewListFromDb();
        checkCallStatus();
    }
    private void checkDate(long date) {
        if (NOW == date) {
            assertEquals("0 mins ago", mItem.dateView.getText());
        }
        throw new UnsupportedOperationException();
    }
    private void checkCallType(int type) {
        Bitmap icon = ((BitmapDrawable) mItem.iconView.getDrawable()).getBitmap();
        assertEquals(mCallTypeIcons.get(type), icon);
    }
    private void checkCallStatus() {
        for (int i = 0; i < mList.length; i++) {
            if (null == mList[i]) {
                break;
            }
            mItem = (RecentCallsListActivity.RecentCallsListItemViews) mList[i].getTag();
            String number = (String) mItem.callView.getTag();
            if (CallerInfo.PRIVATE_NUMBER.equals(number) ||
                CallerInfo.UNKNOWN_NUMBER.equals(number)) {
                assertFalse(View.VISIBLE == mItem.callView.getVisibility());
            } else {
                assertEquals(View.VISIBLE, mItem.callView.getVisibility());
            }
        }
    }
    private Bitmap getBitmap(String resName) {
        Resources r = mActivity.getResources();
        int resid = r.getIdentifier(resName, "drawable", "com.android.contacts");
        BitmapDrawable d = (BitmapDrawable) r.getDrawable(resid);
        assertNotNull(d);
        return d.getBitmap();
    }
    private void buildIconMap() {
        mCallTypeIcons = new HashMap<Integer, Bitmap>(3);
        mCallTypeIcons.put(Calls.INCOMING_TYPE, getBitmap("ic_call_log_list_incoming_call"));
        mCallTypeIcons.put(Calls.MISSED_TYPE, getBitmap("ic_call_log_list_missed_call"));
        mCallTypeIcons.put(Calls.OUTGOING_TYPE, getBitmap("ic_call_log_list_outgoing_call"));
    }
    private void buildViewListFromDb() {
        int i = 0;
        mCursor.moveToLast();
        while(!mCursor.isBeforeFirst()) {
            if (null == mList[i]) {
                mList[i] = mAdapter.newStandAloneView(mActivity, mParentView);
            }
            mAdapter.bindStandAloneView(mList[i], mActivity, mCursor);
            mCursor.moveToPrevious();
            i++;
        }
    }
    private void insertRandomEntries(int num) {
        if (num < 10) {
            throw new IllegalArgumentException("num should be >= 10");
        }
        boolean privateOrUnknownOrVm[];
        privateOrUnknownOrVm = insertRandomRange(0, num - 2);
        if (privateOrUnknownOrVm[0] && privateOrUnknownOrVm[1]) {
            insertRandomRange(num - 2, num);
        } else {
            insertPrivate(NOW, RAND_DURATION);
            insertUnknown(NOW, RAND_DURATION);
        }
    }
    private void insert(String number, long date, int duration, int type) {
        MatrixCursor.RowBuilder row = mCursor.newRow();
        row.add(mIndex);
        mIndex ++;
        row.add(number);
        if (NOW == date) {
            row.add(new Date().getTime());
        }
        if (duration < 0) {
            duration = mRnd.nextInt(10 * 60);  
        }
        row.add(duration);  
        if (mVoicemail != null && mVoicemail.equals(number)) {
            assertEquals(Calls.OUTGOING_TYPE, type);
        }
        row.add(type);  
        row.add("");    
        row.add(0);     
        row.add("");    
    }
    private void insertPrivate(long date, int duration) {
        insert(CallerInfo.PRIVATE_NUMBER, date, duration, Calls.INCOMING_TYPE);
    }
    private void insertUnknown(long date, int duration) {
        insert(CallerInfo.UNKNOWN_NUMBER, date, duration, Calls.INCOMING_TYPE);
    }
    private void insertVoicemail(long date, int duration) {
        if (mVoicemail != null) {
            insert(mVoicemail, date, duration, Calls.OUTGOING_TYPE);
        }
    }
    private boolean[] insertRandomRange(int start, int end) {
        boolean[] privateOrUnknownOrVm = new boolean[] {false, false, false};
        for (int i = start; i < end; i++ ) {
            int type = mRnd.nextInt(10);
            if (0 == type) {
                insertPrivate(NOW, RAND_DURATION);
                privateOrUnknownOrVm[0] = true;
            } else if (1 == type) {
                insertUnknown(NOW, RAND_DURATION);
                privateOrUnknownOrVm[1] = true;
            } else if (2 == type) {
                insertVoicemail(NOW, RAND_DURATION);
                privateOrUnknownOrVm[2] = true;
            } else {
                int inout = mRnd.nextBoolean() ? Calls.OUTGOING_TYPE :  Calls.INCOMING_TYPE;
                String number = new Formatter().format("1800123%04d", i).toString();
                insert(number, NOW, RAND_DURATION, inout);
            }
        }
        return privateOrUnknownOrVm;
    }
}
