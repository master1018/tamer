public class CalendarSyncTestingBase extends SyncBaseInstrumentation {
    protected AccountManager mAccountManager;
    protected Context mTargetContext;
    protected String mAccount;
    protected ContentResolver mResolver;
    protected Uri mEventsUri = Calendar.Events.CONTENT_URI;
    static final String TAG = "calendar";
    static final String DEFAULT_TIMEZONE = "America/Los_Angeles";
    static final Set<String> EVENT_COLUMNS_TO_SKIP = new HashSet<String>();
    static final Set<String> ATTENDEES_COLUMNS_TO_SKIP = new HashSet<String>();
    static final Set<String> CALENDARS_COLUMNS_TO_SKIP = new HashSet<String>();
    static final Set<String> INSTANCES_COLUMNS_TO_SKIP = new HashSet<String>();
    static {
        EVENT_COLUMNS_TO_SKIP.add(Calendar.Events._ID);
        EVENT_COLUMNS_TO_SKIP.add(Calendar.Events._SYNC_TIME);
        EVENT_COLUMNS_TO_SKIP.add(Calendar.Events._SYNC_VERSION);
        EVENT_COLUMNS_TO_SKIP.add(Calendar.Events._SYNC_DATA);
        EVENT_COLUMNS_TO_SKIP.add(Calendar.Events._SYNC_DIRTY);
        EVENT_COLUMNS_TO_SKIP.add(Calendar.Events._SYNC_MARK);
        ATTENDEES_COLUMNS_TO_SKIP.add(Calendar.Attendees._ID);
        CALENDARS_COLUMNS_TO_SKIP.add(Calendar.Calendars._ID);
        CALENDARS_COLUMNS_TO_SKIP.add(Calendar.Calendars._SYNC_TIME);
        CALENDARS_COLUMNS_TO_SKIP.add(Calendar.Calendars._SYNC_VERSION);
        CALENDARS_COLUMNS_TO_SKIP.add(Calendar.Calendars._SYNC_DATA);
        CALENDARS_COLUMNS_TO_SKIP.add(Calendar.Calendars._SYNC_DIRTY);
        CALENDARS_COLUMNS_TO_SKIP.add(Calendar.Calendars._SYNC_MARK);
        INSTANCES_COLUMNS_TO_SKIP.add(Calendar.Instances._ID);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mTargetContext = getInstrumentation().getTargetContext();
        mAccountManager = AccountManager.get(mTargetContext);
        mAccount = getAccount();
        mResolver = mTargetContext.getContentResolver();
    }
    protected void syncCalendar() throws Exception {
        cancelSyncsandDisableAutoSync();
        syncProvider(Calendar.CONTENT_URI, mAccount, Calendar.AUTHORITY);
    }
    protected Uri insertEvent(EventInfo event) throws Exception {
        return insertEvent(getDefaultCalendarId(), event);
    }
    protected Uri insertEvent(int calendarId, EventInfo event) throws Exception{
        ContentValues m = new ContentValues();
        m.put(Calendar.Events.CALENDAR_ID, calendarId);
        m.put(Calendar.Events.TITLE, event.mTitle);
        m.put(Calendar.Events.DTSTART, event.mDtstart);
        m.put(Calendar.Events.ALL_DAY, event.mAllDay ? 1 : 0);
        if (event.mRrule == null) {
            m.put(Calendar.Events.DTEND, event.mDtend);
        } else {
            m.put(Calendar.Events.RRULE, event.mRrule);
            m.put(Calendar.Events.DURATION, event.mDuration);
        }
        if (event.mDescription != null) {
            m.put(Calendar.Events.DESCRIPTION, event.mDescription);
        }
        if (event.mTimezone != null) {
            m.put(Calendar.Events.EVENT_TIMEZONE, event.mTimezone);
        }
        Uri url = mResolver.insert(mEventsUri, m);
        syncCalendar();
        return url;
    }
    protected void editEvent(long eventId, EventInfo event) throws Exception {
        ContentValues values = new ContentValues();
        values.put(Calendar.Events.TITLE, event.mTitle);
        values.put(Calendar.Events.DTSTART, event.mDtstart);
        values.put(Calendar.Events.DTEND, event.mDtend);
        values.put(Calendar.Events.ALL_DAY, event.mAllDay ? 1 : 0);
        if (event.mDescription != null) {
            values.put(Calendar.Events.DESCRIPTION, event.mDescription);
        }
        Uri uri = ContentUris.withAppendedId(Calendar.Events.CONTENT_URI, eventId);
        mResolver.update(uri, values, null, null);
        syncCalendar();
    }
    protected void deleteEvent(Uri uri) throws Exception {
        mResolver.delete(uri, null, null);
        syncCalendar();
    }
    protected void insertCalendar(String name, String timezone, String calendarUrl)
            throws Exception {
        ContentValues values = new ContentValues();
        values.put(Calendar.Calendars._SYNC_ACCOUNT, getAccount());
        values.put(Calendar.Calendars.URL, calendarUrl);
        values.put(Calendar.Calendars.NAME, name);
        values.put(Calendar.Calendars.DISPLAY_NAME, name);
        values.put(Calendar.Calendars.SYNC_EVENTS, 1);
        values.put(Calendar.Calendars.SELECTED, 1);
        values.put(Calendar.Calendars.HIDDEN, 0);
        values.put(Calendar.Calendars.COLOR, -14069085 );
        values.put(Calendar.Calendars.ACCESS_LEVEL, Calendar.Calendars.OWNER_ACCESS);
        values.put(Calendar.Calendars.COLOR, "0xff123456");
        values.put(Calendar.Calendars.TIMEZONE, timezone);
        mResolver.insert(Calendar.Calendars.CONTENT_URI, values);
        syncCalendar();
    }
    protected int getEventsCount() {
        Cursor cursor;
        cursor = mResolver.query(mEventsUri, null, null, null, null);
        return cursor.getCount();
    }
    protected int getDefaultCalendarId() {
        Cursor calendarsCursor;
        calendarsCursor = mResolver.query(Calendar.Calendars.CONTENT_URI, null, null, null, null);
        calendarsCursor.moveToNext();
        return calendarsCursor.getInt(calendarsCursor.getColumnIndex("_id"));
    }
    protected class EventInfo {
        String mTitle;
        String mDescription;
        String mTimezone;
        boolean mAllDay;
        long mDtstart;
        long mDtend;
        String mRrule;
        String mDuration;
        String mOriginalTitle;
        long mOriginalInstance;
        int mSyncId;
        public EventInfo(String title, String startDate, String endDate,
                boolean allDay) {
            init(title, startDate, endDate, allDay, DEFAULT_TIMEZONE);
        }
        public EventInfo(String title, long startDate, long endDate,
                boolean allDay) {
            mTitle = title;
            mTimezone = DEFAULT_TIMEZONE;
            mDtstart = startDate;
            mDtend = endDate;
            mDuration = null;
            mRrule = null;
            mAllDay = allDay;
        }
        public EventInfo(String title, long startDate, long endDate,
                boolean allDay, String description) {
            mTitle = title;
            mTimezone = DEFAULT_TIMEZONE;
            mDtstart = startDate;
            mDtend = endDate;
            mDuration = null;
            mRrule = null;
            mAllDay = allDay;
            mDescription = description;
        }
        public EventInfo(String title, String startDate, String endDate,
                boolean allDay, String timezone) {
            init(title, startDate, endDate, allDay, timezone);
        }
        public void init(String title, String startDate, String endDate,
                boolean allDay, String timezone) {
            mTitle = title;
            Time time = new Time();
            if (allDay) {
                time.timezone = Time.TIMEZONE_UTC;
            } else if (timezone != null) {
                time.timezone = timezone;
            }
            mTimezone = time.timezone;
            time.parse3339(startDate);
            mDtstart = time.toMillis(false );
            time.parse3339(endDate);
            mDtend = time.toMillis(false );
            mDuration = null;
            mRrule = null;
            mAllDay = allDay;
        }
        public EventInfo(String title, String description, String startDate, String endDate,
                String rrule, boolean allDay) {
            init(title, description, startDate, endDate, rrule, allDay, DEFAULT_TIMEZONE);
        }
        public EventInfo(String title, String description, String startDate, String endDate,
                String rrule, boolean allDay, String timezone) {
            init(title, description, startDate, endDate, rrule, allDay, timezone);
        }
        public void init(String title, String description, String startDate, String endDate,
                String rrule, boolean allDay, String timezone) {
            mTitle = title;
            mDescription = description;
            Time time = new Time();
            if (allDay) {
                time.timezone = Time.TIMEZONE_UTC;
            } else if (timezone != null) {
                time.timezone = timezone;
            }
            mTimezone = time.timezone;
            time.parse3339(startDate);
            mDtstart = time.toMillis(false );
            if (endDate != null) {
                time.parse3339(endDate);
                mDtend = time.toMillis(false );
            }
            if (allDay) {
                long days = 1;
                if (endDate != null) {
                    days = (mDtend - mDtstart) / DateUtils.DAY_IN_MILLIS;
                }
                mDuration = "P" + days + "D";
            } else {
                long seconds = (mDtend - mDtstart) / DateUtils.SECOND_IN_MILLIS;
                mDuration = "P" + seconds + "S";
            }
            mRrule = rrule;
            mAllDay = allDay;
        }
        public EventInfo(String originalTitle, String originalInstance, String title,
                String description, String startDate, String endDate, boolean allDay) {
            init(originalTitle, originalInstance,
                    title, description, startDate, endDate, allDay, DEFAULT_TIMEZONE);
        }
        public void init(String originalTitle, String originalInstance,
                String title, String description, String startDate, String endDate,
                boolean allDay, String timezone) {
            mOriginalTitle = originalTitle;
            Time time = new Time(timezone);
            time.parse3339(originalInstance);
            mOriginalInstance = time.toMillis(false );
            init(title, description, startDate, endDate, null , allDay, timezone);
        }
    }
    protected String getAccount() {
        Account[] accounts = mAccountManager.getAccountsByType("com.google");
        assertTrue("Didn't find any Google accounts", accounts.length > 0);
        Account account = accounts[accounts.length - 1];
        Log.v(TAG, "Found " + accounts.length + " accounts; using the last one, " + account.name);
        return account.name;
    }
    protected void compareCursors(Cursor cursor1, Cursor cursor2,
                                  Set<String> columnsToSkip, String tableName) {
        String[] cols = cursor1.getColumnNames();
        int length = cols.length;
        assertEquals(tableName + " count failed to match", cursor1.getCount(),
                cursor2.getCount());
        Map<String, String> row = Maps.newHashMap();
        while (cursor1.moveToNext() && cursor2.moveToNext()) {
            for (int i = 0; i < length; i++) {
                String col = cols[i];
                if (columnsToSkip != null && columnsToSkip.contains(col)) {
                    continue;
                }
                row.put(col, cursor1.getString(i));
                assertEquals("Row: " + row + " Table: " + tableName + ": " + cols[i] +
                        " failed to match", cursor1.getString(i),
                        cursor2.getString(i));
            }
        }
    }
}
