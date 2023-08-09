public class CalendarProvider2 extends SQLiteContentProvider implements OnAccountsUpdateListener {
    private static final String TAG = "CalendarProvider2";
    private static final boolean PROFILE = false;
    private static final boolean MULTIPLE_ATTENDEES_PER_EVENT = true;
    private static final String INVALID_CALENDARALERTS_SELECTOR =
            "_id IN (SELECT ca._id FROM CalendarAlerts AS ca"
                    + " LEFT OUTER JOIN Instances USING (event_id, begin, end)"
                    + " LEFT OUTER JOIN Reminders AS r ON"
                    + " (ca.event_id=r.event_id AND ca.minutes=r.minutes)"
                    + " WHERE Instances.begin ISNULL OR ca.alarmTime<?"
                    + "   OR (r.minutes ISNULL AND ca.minutes<>0))";
    private static final String[] ID_ONLY_PROJECTION =
            new String[] {Events._ID};
    private static final String[] EVENTS_PROJECTION = new String[] {
            Events._SYNC_ID,
            Events.RRULE,
            Events.RDATE,
            Events.ORIGINAL_EVENT,
    };
    private static final int EVENTS_SYNC_ID_INDEX = 0;
    private static final int EVENTS_RRULE_INDEX = 1;
    private static final int EVENTS_RDATE_INDEX = 2;
    private static final int EVENTS_ORIGINAL_EVENT_INDEX = 3;
    private static final String[] ID_PROJECTION = new String[] {
            Attendees._ID,
            Attendees.EVENT_ID, 
    };
    private static final int ID_INDEX = 0;
    private static final int EVENT_ID_INDEX = 1;
    private static final String[] ALLDAY_TIME_PROJECTION = new String[] {
        Events._ID,
        Events.DTSTART,
        Events.DTEND,
        Events.DURATION
    };
    private static final int ALLDAY_ID_INDEX = 0;
    private static final int ALLDAY_DTSTART_INDEX = 1;
    private static final int ALLDAY_DTEND_INDEX = 2;
    private static final int ALLDAY_DURATION_INDEX = 3;
    private static final int DAY_IN_SECONDS = 24 * 60 * 60;
    MetaData mMetaData;
    CalendarCache mCalendarCache;
    private CalendarDatabaseHelper mDbHelper;
    private static final Uri SYNCSTATE_CONTENT_URI =
            Uri.parse("content:
     static final String SCHEDULE_ALARM_PATH = "schedule_alarms";
     static final String SCHEDULE_ALARM_REMOVE_PATH = "schedule_alarms_remove";
     static final Uri SCHEDULE_ALARM_URI =
            Uri.withAppendedPath(Calendar.CONTENT_URI, SCHEDULE_ALARM_PATH);
     static final Uri SCHEDULE_ALARM_REMOVE_URI =
            Uri.withAppendedPath(Calendar.CONTENT_URI, SCHEDULE_ALARM_REMOVE_PATH);
    private static final int MAX_ASSUMED_DURATION = 7*24*60*60*1000;
    public static final class TimeRange {
        public long begin;
        public long end;
        public boolean allDay;
    }
    public static final class InstancesRange {
        public long begin;
        public long end;
        public InstancesRange(long begin, long end) {
            this.begin = begin;
            this.end = end;
        }
    }
    public static final class InstancesList
            extends ArrayList<ContentValues> {
    }
    public static final class EventInstancesMap
            extends HashMap<String, InstancesList> {
        public void add(String syncIdKey, ContentValues values) {
            InstancesList instances = get(syncIdKey);
            if (instances == null) {
                instances = new InstancesList();
                put(syncIdKey, instances);
            }
            instances.add(values);
        }
    }
    private class AlarmScheduler extends Thread {
        boolean mRemoveAlarms;
        public AlarmScheduler(boolean removeAlarms) {
            mRemoveAlarms = removeAlarms;
        }
        @Override
        public void run() {
            try {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                runScheduleNextAlarm(mRemoveAlarms);
            } catch (SQLException e) {
                Log.e(TAG, "runScheduleNextAlarm() failed", e);
            }
        }
    }
    private static final long SCHEDULE_ALARM_SLACK = 2 * DateUtils.HOUR_IN_MILLIS;
    private static final long CLEAR_OLD_ALARM_THRESHOLD =
            7 * DateUtils.DAY_IN_MILLIS + SCHEDULE_ALARM_SLACK;
    private Object mAlarmLock = new Object();
    private static final long MINIMUM_EXPANSION_SPAN =
            2L * 31 * 24 * 60 * 60 * 1000;
    private static final String[] sCalendarsIdProjection = new String[] { Calendars._ID };
    private static final int CALENDARS_INDEX_ID = 0;
    private static final String CALENDAR_ID_SELECTION = "calendar_id=?";
    private static final String[] sInstancesProjection =
            new String[] { Instances.START_DAY, Instances.END_DAY,
                    Instances.START_MINUTE, Instances.END_MINUTE, Instances.ALL_DAY };
    private static final int INSTANCES_INDEX_START_DAY = 0;
    private static final int INSTANCES_INDEX_END_DAY = 1;
    private static final int INSTANCES_INDEX_START_MINUTE = 2;
    private static final int INSTANCES_INDEX_END_MINUTE = 3;
    private static final int INSTANCES_INDEX_ALL_DAY = 4;
    private AlarmManager mAlarmManager;
    private CalendarAppWidgetProvider mAppWidgetProvider = CalendarAppWidgetProvider.getInstance();
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onReceive() " + action);
            }
            if (Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                updateTimezoneDependentFields();
                scheduleNextAlarm(false );
            } else if (Intent.ACTION_DEVICE_STORAGE_OK.equals(action)) {
                updateTimezoneDependentFields();
                scheduleNextAlarm(false );
            } else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
                scheduleNextAlarm(false );
            }
        }
    };
    public interface EventsRawTimesColumns
    {
        public static final String EVENT_ID = "event_id";
        public static final String DTSTART_2445 = "dtstart2445";
        public static final String DTEND_2445 = "dtend2445";
        public static final String ORIGINAL_INSTANCE_TIME_2445 = "originalInstanceTime2445";
        public static final String LAST_DATE_2445 = "lastDate2445";
    }
    protected void verifyAccounts() {
        AccountManager.get(getContext()).addOnAccountsUpdatedListener(this, null, false);
        onAccountsUpdated(AccountManager.get(getContext()).getAccounts());
    }
    @Override
    protected CalendarDatabaseHelper getDatabaseHelper(final Context context) {
        return CalendarDatabaseHelper.getInstance(context);
    }
    @Override
    public boolean onCreate() {
        super.onCreate();
        mDbHelper = (CalendarDatabaseHelper)getDatabaseHelper();
        verifyAccounts();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        final Context c = getContext();
        c.registerReceiver(mIntentReceiver, filter);
        mMetaData = new MetaData(mDbHelper);
        mCalendarCache = new CalendarCache(mDbHelper);
        updateTimezoneDependentFields();
        return true;
    }
    protected void updateTimezoneDependentFields() {
        Thread thread = new TimezoneCheckerThread();
        thread.start();
    }
    private class TimezoneCheckerThread extends Thread {
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            try {
                doUpdateTimezoneDependentFields();
            } catch (SQLException e) {
                Log.e(TAG, "doUpdateTimezoneDependentFields() failed", e);
                try {
                    mMetaData.clearInstanceRange();
                } catch (SQLException e2) {
                    Log.e(TAG, "clearInstanceRange() also failed: " + e2);
                }
            }
        }
    }
    private void doUpdateTimezoneDependentFields() {
        if (! isSameTimezoneDatabaseVersion()) {
            doProcessEventRawTimes(null  ,
            TimeUtils.getTimeZoneDatabaseVersion());
        }
        if (isSameTimezone()) {
            rescheduleMissedAlarms();
            return;
        }
        regenerateInstancesTable();
    }
    protected void doProcessEventRawTimes(String timezone, String timeZoneDatabaseVersion) {
        mDb = mDbHelper.getWritableDatabase();
        if (mDb == null) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Cannot update Events table from EventsRawTimes table");
            }
            return;
        }
        mDb.beginTransaction();
        try {
            updateEventsStartEndFromEventRawTimesLocked(timezone);
            updateTimezoneDatabaseVersion(timeZoneDatabaseVersion);
            cleanInstancesTable();
            regenerateInstancesTable();
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }
    }
    private void updateEventsStartEndFromEventRawTimesLocked(String timezone) {
        Cursor cursor = mDb.query("EventsRawTimes",
                            new String[] { EventsRawTimesColumns.EVENT_ID,
                                    EventsRawTimesColumns.DTSTART_2445,
                                    EventsRawTimesColumns.DTEND_2445} ,
                            null ,
                            null ,
                            null ,
                            null ,
                            null 
                );
        try {
            while (cursor.moveToNext()) {
                long eventId = cursor.getLong(0);
                String dtStart2445 = cursor.getString(1);
                String dtEnd2445 = cursor.getString(2);
                updateEventsStartEndLocked(eventId,
                        timezone,
                        dtStart2445,
                        dtEnd2445);
            }
        } finally {
            cursor.close();
            cursor = null;
        }
    }
    private long get2445ToMillis(String timezone, String dt2445) {
        if (null == dt2445) {
            Log.v( TAG, "Cannot parse null RFC2445 date");
            return 0;
        }
        Time time = (timezone != null) ? new Time(timezone) : new Time();
        try {
            time.parse(dt2445);
        } catch (TimeFormatException e) {
            Log.v( TAG, "Cannot parse RFC2445 date " + dt2445);
            return 0;
        }
        return time.toMillis(true );
    }
    private void updateEventsStartEndLocked(long eventId,
            String timezone, String dtStart2445, String dtEnd2445) {
        ContentValues values = new ContentValues();
        values.put("dtstart", get2445ToMillis(timezone, dtStart2445));
        values.put("dtend", get2445ToMillis(timezone, dtEnd2445));
        int result = mDb.update("Events", values, "_id=?",
                new String[] {String.valueOf(eventId)});
        if (0 == result) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Could not update Events table with values " + values);
            }
        }
    }
    private void cleanInstancesTable() {
        mDb.delete("Instances", null , null );
    }
    private void updateTimezoneDatabaseVersion(String timeZoneDatabaseVersion) {
        try {
            mCalendarCache.writeTimezoneDatabaseVersion(timeZoneDatabaseVersion);
        } catch (CalendarCache.CacheException e) {
            Log.e(TAG, "Could not write timezone database version in the cache");
        }
    }
    private boolean isSameTimezone() {
        MetaData.Fields fields = mMetaData.getFields();
        String localTimezone = TimeZone.getDefault().getID();
        return TextUtils.equals(fields.timezone, localTimezone);
    }
    protected boolean isSameTimezoneDatabaseVersion() {
        String timezoneDatabaseVersion = null;
        try {
            timezoneDatabaseVersion = mCalendarCache.readTimezoneDatabaseVersion();
        } catch (CalendarCache.CacheException e) {
            Log.e(TAG, "Could not read timezone database version from the cache");
            return false;
        }
        return TextUtils.equals(timezoneDatabaseVersion, TimeUtils.getTimeZoneDatabaseVersion());
    }
    @VisibleForTesting
    protected String getTimezoneDatabaseVersion() {
        String timezoneDatabaseVersion = null;
        try {
            timezoneDatabaseVersion = mCalendarCache.readTimezoneDatabaseVersion();
        } catch (CalendarCache.CacheException e) {
            Log.e(TAG, "Could not read timezone database version from the cache");
            return "";
        }
        Log.i(TAG, "timezoneDatabaseVersion = " + timezoneDatabaseVersion);
        return timezoneDatabaseVersion;
    }
    private void regenerateInstancesTable() {
        long now = System.currentTimeMillis();
        Time time = new Time();
        time.set(now);
        time.monthDay = 1;
        time.hour = 0;
        time.minute = 0;
        time.second = 0;
        long begin = time.normalize(true);
        long end = begin + MINIMUM_EXPANSION_SPAN;
        Cursor cursor = null;
        try {
            cursor = handleInstanceQuery(new SQLiteQueryBuilder(),
                    begin, end,
                    new String[] { Instances._ID },
                    null , null , false );
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        rescheduleMissedAlarms();
    }
    private void rescheduleMissedAlarms() {
        AlarmManager manager = getAlarmManager();
        if (manager != null) {
            Context context = getContext();
            ContentResolver cr = context.getContentResolver();
            CalendarAlerts.rescheduleMissedAlarms(cr, context, manager);
        }
    }
    private void appendIds(StringBuilder sb, HashSet<Long> ids) {
        for (long id : ids) {
            sb.append(id).append(',');
        }
        sb.setLength(sb.length() - 1); 
    }
    @Override
    protected void notifyChange() {
        getContext().getContentResolver().notifyChange(Calendar.CONTENT_URI, null,
                true );
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "query uri - " + uri);
        }
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String groupBy = null;
        String limit = null; 
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SYNCSTATE:
                return mDbHelper.getSyncState().query(db, projection, selection,  selectionArgs,
                        sortOrder);
            case EVENTS:
                qb.setTables(CalendarDatabaseHelper.Views.EVENTS);
                qb.setProjectionMap(sEventsProjectionMap);
                appendAccountFromParameter(qb, uri);
                break;
            case EVENTS_ID:
                qb.setTables(CalendarDatabaseHelper.Views.EVENTS);
                qb.setProjectionMap(sEventsProjectionMap);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getPathSegments().get(1));
                qb.appendWhere("_id=?");
                break;
            case EVENT_ENTITIES:
                qb.setTables(CalendarDatabaseHelper.Views.EVENTS);
                qb.setProjectionMap(sEventEntitiesProjectionMap);
                appendAccountFromParameter(qb, uri);
                break;
            case EVENT_ENTITIES_ID:
                qb.setTables(CalendarDatabaseHelper.Views.EVENTS);
                qb.setProjectionMap(sEventEntitiesProjectionMap);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getPathSegments().get(1));
                qb.appendWhere("_id=?");
                break;
            case CALENDARS:
                qb.setTables("Calendars");
                appendAccountFromParameter(qb, uri);
                break;
            case CALENDARS_ID:
                qb.setTables("Calendars");
                selectionArgs = insertSelectionArg(selectionArgs, uri.getPathSegments().get(1));
                qb.appendWhere("_id=?");
                break;
            case INSTANCES:
            case INSTANCES_BY_DAY:
                long begin;
                long end;
                try {
                    begin = Long.valueOf(uri.getPathSegments().get(2));
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("Cannot parse begin "
                            + uri.getPathSegments().get(2));
                }
                try {
                    end = Long.valueOf(uri.getPathSegments().get(3));
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("Cannot parse end "
                            + uri.getPathSegments().get(3));
                }
                return handleInstanceQuery(qb, begin, end, projection,
                        selection, sortOrder, match == INSTANCES_BY_DAY);
            case EVENT_DAYS:
                int startDay;
                int endDay;
                try {
                    startDay = Integer.valueOf(uri.getPathSegments().get(2));
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("Cannot parse start day "
                            + uri.getPathSegments().get(2));
                }
                try {
                    endDay = Integer.valueOf(uri.getPathSegments().get(3));
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("Cannot parse end day "
                            + uri.getPathSegments().get(3));
                }
                return handleEventDayQuery(qb, startDay, endDay, projection, selection);
            case ATTENDEES:
                qb.setTables("Attendees, Events");
                qb.setProjectionMap(sAttendeesProjectionMap);
                qb.appendWhere("Events._id=Attendees.event_id");
                break;
            case ATTENDEES_ID:
                qb.setTables("Attendees, Events");
                qb.setProjectionMap(sAttendeesProjectionMap);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getPathSegments().get(1));
                qb.appendWhere("Attendees._id=?  AND Events._id=Attendees.event_id");
                break;
            case REMINDERS:
                qb.setTables("Reminders");
                break;
            case REMINDERS_ID:
                qb.setTables("Reminders, Events");
                qb.setProjectionMap(sRemindersProjectionMap);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                qb.appendWhere("Reminders._id=? AND Events._id=Reminders.event_id");
                break;
            case CALENDAR_ALERTS:
                qb.setTables("CalendarAlerts, " + CalendarDatabaseHelper.Views.EVENTS);
                qb.setProjectionMap(sCalendarAlertsProjectionMap);
                qb.appendWhere(CalendarDatabaseHelper.Views.EVENTS +
                        "._id=CalendarAlerts.event_id");
                break;
            case CALENDAR_ALERTS_BY_INSTANCE:
                qb.setTables("CalendarAlerts, " + CalendarDatabaseHelper.Views.EVENTS);
                qb.setProjectionMap(sCalendarAlertsProjectionMap);
                qb.appendWhere(CalendarDatabaseHelper.Views.EVENTS +
                        "._id=CalendarAlerts.event_id");
                groupBy = CalendarAlerts.EVENT_ID + "," + CalendarAlerts.BEGIN;
                break;
            case CALENDAR_ALERTS_ID:
                qb.setTables("CalendarAlerts, " + CalendarDatabaseHelper.Views.EVENTS);
                qb.setProjectionMap(sCalendarAlertsProjectionMap);
                selectionArgs = insertSelectionArg(selectionArgs, uri.getLastPathSegment());
                qb.appendWhere(CalendarDatabaseHelper.Views.EVENTS +
                        "._id=CalendarAlerts.event_id AND CalendarAlerts._id=?");
                break;
            case EXTENDED_PROPERTIES:
                qb.setTables("ExtendedProperties");
                break;
            case EXTENDED_PROPERTIES_ID:
                qb.setTables("ExtendedProperties");
                selectionArgs = insertSelectionArg(selectionArgs, uri.getPathSegments().get(1));
                qb.appendWhere("ExtendedProperties._id=?");
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
        return query(db, qb, projection, selection, selectionArgs, sortOrder, groupBy, limit);
    }
    private Cursor query(final SQLiteDatabase db, SQLiteQueryBuilder qb, String[] projection,
            String selection, String[] selectionArgs, String sortOrder, String groupBy,
            String limit) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "query sql - projection: " + Arrays.toString(projection) +
                    " selection: " + selection +
                    " selectionArgs: " + Arrays.toString(selectionArgs) +
                    " sortOrder: " + sortOrder +
                    " groupBy: " + groupBy +
                    " limit: " + limit);
        }
        final Cursor c = qb.query(db, projection, selection, selectionArgs, groupBy, null,
                sortOrder, limit);
        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), Calendar.Events.CONTENT_URI);
        }
        return c;
    }
    private Cursor handleInstanceQuery(SQLiteQueryBuilder qb, long rangeBegin,
            long rangeEnd, String[] projection,
            String selection, String sort, boolean searchByDay) {
        qb.setTables("Instances INNER JOIN Events ON (Instances.event_id=Events._id) " +
                "INNER JOIN Calendars ON (Events.calendar_id = Calendars._id)");
        qb.setProjectionMap(sInstancesProjectionMap);
        if (searchByDay) {
            Time time = new Time();
            long beginMs = time.setJulianDay((int) rangeBegin);
            long endMs = time.setJulianDay((int) rangeEnd + 1);
            acquireInstanceRange(beginMs, endMs, true );
            qb.appendWhere("startDay<=? AND endDay>=?");
        } else {
            acquireInstanceRange(rangeBegin, rangeEnd, true );
            qb.appendWhere("begin<=? AND end>=?");
        }
        String selectionArgs[] = new String[] {String.valueOf(rangeEnd),
                String.valueOf(rangeBegin)};
        return qb.query(mDb, projection, selection, selectionArgs, null ,
                null , sort);
    }
    private Cursor handleEventDayQuery(SQLiteQueryBuilder qb, int begin, int end,
            String[] projection, String selection) {
        qb.setTables("Instances INNER JOIN Events ON (Instances.event_id=Events._id) " +
                "INNER JOIN Calendars ON (Events.calendar_id = Calendars._id)");
        qb.setProjectionMap(sInstancesProjectionMap);
        Time time = new Time();
        long beginMs = time.setJulianDay(begin);
        long endMs = time.setJulianDay(end + 1);
        acquireInstanceRange(beginMs, endMs, true);
        qb.appendWhere("startDay<=? AND endDay>=?");
        String selectionArgs[] = new String[] {String.valueOf(end), String.valueOf(begin)};
        return qb.query(mDb, projection, selection, selectionArgs,
                Instances.START_DAY , null , null);
    }
    private void acquireInstanceRange(final long begin,
            final long end,
            final boolean useMinimumExpansionWindow) {
        mDb.beginTransaction();
        try {
            acquireInstanceRangeLocked(begin, end, useMinimumExpansionWindow);
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }
    }
    private void acquireInstanceRangeLocked(long begin, long end,
            boolean useMinimumExpansionWindow) {
        long expandBegin = begin;
        long expandEnd = end;
        if (useMinimumExpansionWindow) {
            long span = end - begin;
            if (span < MINIMUM_EXPANSION_SPAN) {
                long additionalRange = (MINIMUM_EXPANSION_SPAN - span) / 2;
                expandBegin -= additionalRange;
                expandEnd += additionalRange;
            }
        }
        MetaData.Fields fields = mMetaData.getFieldsLocked();
        String dbTimezone = fields.timezone;
        long maxInstance = fields.maxInstance;
        long minInstance = fields.minInstance;
        String localTimezone = TimeZone.getDefault().getID();
        boolean timezoneChanged = (dbTimezone == null) || !dbTimezone.equals(localTimezone);
        if (maxInstance == 0 || timezoneChanged) {
            mDb.execSQL("DELETE FROM Instances;");
            if (Config.LOGV) {
                Log.v(TAG, "acquireInstanceRangeLocked() deleted Instances,"
                        + " timezone changed: " + timezoneChanged);
            }
            expandInstanceRangeLocked(expandBegin, expandEnd, localTimezone);
            mMetaData.writeLocked(localTimezone, expandBegin, expandEnd);
            return;
        }
        if ((begin >= minInstance) && (end <= maxInstance)) {
            if (Config.LOGV) {
                Log.v(TAG, "Canceled instance query (" + expandBegin + ", " + expandEnd
                        + ") falls within previously expanded range.");
            }
            return;
        }
        if (begin < minInstance) {
            expandInstanceRangeLocked(expandBegin, minInstance, localTimezone);
            minInstance = expandBegin;
        }
        if (end > maxInstance) {
            expandInstanceRangeLocked(maxInstance, expandEnd, localTimezone);
            maxInstance = expandEnd;
        }
        mMetaData.writeLocked(localTimezone, minInstance, maxInstance);
    }
    private static final String[] EXPAND_COLUMNS = new String[] {
            Events._ID,
            Events._SYNC_ID,
            Events.STATUS,
            Events.DTSTART,
            Events.DTEND,
            Events.EVENT_TIMEZONE,
            Events.RRULE,
            Events.RDATE,
            Events.EXRULE,
            Events.EXDATE,
            Events.DURATION,
            Events.ALL_DAY,
            Events.ORIGINAL_EVENT,
            Events.ORIGINAL_INSTANCE_TIME,
            Events.CALENDAR_ID,
            Events.DELETED
    };
    private void expandInstanceRangeLocked(long begin, long end, String localTimezone) {
        if (PROFILE) {
            Debug.startMethodTracing("expandInstanceRangeLocked");
        }
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Expanding events between " + begin + " and " + end);
        }
        Cursor entries = getEntries(begin, end);
        try {
            performInstanceExpansion(begin, end, localTimezone, entries);
        } finally {
            if (entries != null) {
                entries.close();
            }
        }
        if (PROFILE) {
            Debug.stopMethodTracing();
        }
    }
    private Cursor getEntries(long begin, long end) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(CalendarDatabaseHelper.Views.EVENTS);
        qb.setProjectionMap(sEventsProjectionMap);
        String beginString = String.valueOf(begin);
        String endString = String.valueOf(end);
        qb.appendWhere("((dtstart <= ? AND (lastDate IS NULL OR lastDate >= ?)) OR " +
                "(originalInstanceTime IS NOT NULL AND originalInstanceTime <= ? AND " +
                "originalInstanceTime >= ?)) AND (sync_events != 0)");
        String selectionArgs[] = new String[] {endString, beginString, endString,
                String.valueOf(begin - MAX_ASSUMED_DURATION)};
        Cursor c = qb.query(mDb, EXPAND_COLUMNS, null ,
                selectionArgs, null ,
                null , null );
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Instance expansion:  got " + c.getCount() + " entries");
        }
        return c;
    }
    private String getSyncIdKey(String syncId, long calendarId) {
        return calendarId + ":" + syncId;
    }
    private void performInstanceExpansion(long begin, long end, String localTimezone,
                                          Cursor entries) {
        RecurrenceProcessor rp = new RecurrenceProcessor();
        final String ORIGINAL_EVENT_AND_CALENDAR = "ORIGINAL_EVENT_AND_CALENDAR";
        int statusColumn = entries.getColumnIndex(Events.STATUS);
        int dtstartColumn = entries.getColumnIndex(Events.DTSTART);
        int dtendColumn = entries.getColumnIndex(Events.DTEND);
        int eventTimezoneColumn = entries.getColumnIndex(Events.EVENT_TIMEZONE);
        int durationColumn = entries.getColumnIndex(Events.DURATION);
        int rruleColumn = entries.getColumnIndex(Events.RRULE);
        int rdateColumn = entries.getColumnIndex(Events.RDATE);
        int exruleColumn = entries.getColumnIndex(Events.EXRULE);
        int exdateColumn = entries.getColumnIndex(Events.EXDATE);
        int allDayColumn = entries.getColumnIndex(Events.ALL_DAY);
        int idColumn = entries.getColumnIndex(Events._ID);
        int syncIdColumn = entries.getColumnIndex(Events._SYNC_ID);
        int originalEventColumn = entries.getColumnIndex(Events.ORIGINAL_EVENT);
        int originalInstanceTimeColumn = entries.getColumnIndex(Events.ORIGINAL_INSTANCE_TIME);
        int calendarIdColumn = entries.getColumnIndex(Events.CALENDAR_ID);
        int deletedColumn = entries.getColumnIndex(Events.DELETED);
        ContentValues initialValues;
        EventInstancesMap instancesMap = new EventInstancesMap();
        Duration duration = new Duration();
        Time eventTime = new Time();
        while (entries.moveToNext()) {
            try {
                initialValues = null;
                boolean allDay = entries.getInt(allDayColumn) != 0;
                String eventTimezone = entries.getString(eventTimezoneColumn);
                if (allDay || TextUtils.isEmpty(eventTimezone)) {
                    eventTimezone = Time.TIMEZONE_UTC;
                }
                long dtstartMillis = entries.getLong(dtstartColumn);
                Long eventId = Long.valueOf(entries.getLong(idColumn));
                String durationStr = entries.getString(durationColumn);
                if (durationStr != null) {
                    try {
                        duration.parse(durationStr);
                    }
                    catch (DateException e) {
                        Log.w(TAG, "error parsing duration for event "
                                + eventId + "'" + durationStr + "'", e);
                        duration.sign = 1;
                        duration.weeks = 0;
                        duration.days = 0;
                        duration.hours = 0;
                        duration.minutes = 0;
                        duration.seconds = 0;
                        durationStr = "+P0S";
                    }
                }
                String syncId = entries.getString(syncIdColumn);
                String originalEvent = entries.getString(originalEventColumn);
                long originalInstanceTimeMillis = -1;
                if (!entries.isNull(originalInstanceTimeColumn)) {
                    originalInstanceTimeMillis= entries.getLong(originalInstanceTimeColumn);
                }
                int status = entries.getInt(statusColumn);
                boolean deleted = (entries.getInt(deletedColumn) != 0);
                String rruleStr = entries.getString(rruleColumn);
                String rdateStr = entries.getString(rdateColumn);
                String exruleStr = entries.getString(exruleColumn);
                String exdateStr = entries.getString(exdateColumn);
                long calendarId = entries.getLong(calendarIdColumn);
                String syncIdKey = getSyncIdKey(syncId, calendarId); 
                RecurrenceSet recur = null;
                try {
                    recur = new RecurrenceSet(rruleStr, rdateStr, exruleStr, exdateStr);
                } catch (EventRecurrence.InvalidFormatException e) {
                    Log.w(TAG, "Could not parse RRULE recurrence string: " + rruleStr, e);
                    continue;
                }
                if (null != recur && recur.hasRecurrence()) {
                    if (status == Events.STATUS_CANCELED) {
                        Log.e(TAG, "Found canceled recurring event in "
                                + "Events table.  Ignoring.");
                        continue;
                    }
                    eventTime.timezone = eventTimezone;
                    eventTime.set(dtstartMillis);
                    eventTime.allDay = allDay;
                    if (durationStr == null) {
                        Log.e(TAG, "Repeating event has no duration -- "
                                + "should not happen.");
                        if (allDay) {
                            duration.sign = 1;
                            duration.weeks = 0;
                            duration.days = 1;
                            duration.hours = 0;
                            duration.minutes = 0;
                            duration.seconds = 0;
                            durationStr = "+P1D";
                        } else {
                            duration.sign = 1;
                            duration.weeks = 0;
                            duration.days = 0;
                            duration.hours = 0;
                            duration.minutes = 0;
                            if (!entries.isNull(dtendColumn)) {
                                long dtendMillis = entries.getLong(dtendColumn);
                                duration.seconds = (int) ((dtendMillis - dtstartMillis) / 1000);
                                durationStr = "+P" + duration.seconds + "S";
                            } else {
                                duration.seconds = 0;
                                durationStr = "+P0S";
                            }
                        }
                    }
                    long[] dates;
                    dates = rp.expand(eventTime, recur, begin, end);
                    if (allDay) {
                        eventTime.timezone = Time.TIMEZONE_UTC;
                    } else {
                        eventTime.timezone = localTimezone;
                    }
                    long durationMillis = duration.getMillis();
                    for (long date : dates) {
                        initialValues = new ContentValues();
                        initialValues.put(Instances.EVENT_ID, eventId);
                        initialValues.put(Instances.BEGIN, date);
                        long dtendMillis = date + durationMillis;
                        initialValues.put(Instances.END, dtendMillis);
                        computeTimezoneDependentFields(date, dtendMillis,
                                eventTime, initialValues);
                        instancesMap.add(syncIdKey, initialValues);
                    }
                } else {
                    initialValues = new ContentValues();
                    if (originalEvent != null && originalInstanceTimeMillis != -1) {
                        initialValues.put(ORIGINAL_EVENT_AND_CALENDAR,
                                getSyncIdKey(originalEvent, calendarId));
                        initialValues.put(Events.ORIGINAL_INSTANCE_TIME,
                                originalInstanceTimeMillis);
                        initialValues.put(Events.STATUS, status);
                    }
                    long dtendMillis = dtstartMillis;
                    if (durationStr == null) {
                        if (!entries.isNull(dtendColumn)) {
                            dtendMillis = entries.getLong(dtendColumn);
                        }
                    } else {
                        dtendMillis = duration.addTo(dtstartMillis);
                    }
                    if ((dtendMillis < begin) || (dtstartMillis > end)) {
                        if (originalEvent != null && originalInstanceTimeMillis != -1) {
                            initialValues.put(Events.STATUS, Events.STATUS_CANCELED);
                        } else {
                            Log.w(TAG, "Unexpected event outside window: " + syncId);
                            continue;
                        }
                    }
                    initialValues.put(Instances.EVENT_ID, eventId);
                    initialValues.put(Instances.BEGIN, dtstartMillis);
                    initialValues.put(Instances.END, dtendMillis);
                    initialValues.put(Events.DELETED, deleted);
                    if (allDay) {
                        eventTime.timezone = Time.TIMEZONE_UTC;
                    } else {
                        eventTime.timezone = localTimezone;
                    }
                    computeTimezoneDependentFields(dtstartMillis, dtendMillis,
                            eventTime, initialValues);
                    instancesMap.add(syncIdKey, initialValues);
                }
            } catch (DateException e) {
                Log.w(TAG, "RecurrenceProcessor error ", e);
            } catch (TimeFormatException e) {
                Log.w(TAG, "RecurrenceProcessor error ", e);
            }
        }
        Set<String> keys = instancesMap.keySet();
        for (String syncIdKey : keys) {
            InstancesList list = instancesMap.get(syncIdKey);
            for (ContentValues values : list) {
                if (!values.containsKey(ORIGINAL_EVENT_AND_CALENDAR)) {
                    continue;
                }
                String originalEventPlusCalendar = values.getAsString(ORIGINAL_EVENT_AND_CALENDAR);
                long originalTime = values.getAsLong(Events.ORIGINAL_INSTANCE_TIME);
                InstancesList originalList = instancesMap.get(originalEventPlusCalendar);
                if (originalList == null) {
                    continue;
                }
                for (int num = originalList.size() - 1; num >= 0; num--) {
                    ContentValues originalValues = originalList.get(num);
                    long beginTime = originalValues.getAsLong(Instances.BEGIN);
                    if (beginTime == originalTime) {
                        originalList.remove(num);
                    }
                }
            }
        }
        for (String syncIdKey : keys) {
            InstancesList list = instancesMap.get(syncIdKey);
            for (ContentValues values : list) {
                Integer status = values.getAsInteger(Events.STATUS);
                boolean deleted = values.containsKey(Events.DELETED) ?
                        values.getAsBoolean(Events.DELETED) : false;
                if ((status != null && status == Events.STATUS_CANCELED) || deleted) {
                    continue;
                }
                values.remove(Events.DELETED);
                values.remove(ORIGINAL_EVENT_AND_CALENDAR);
                values.remove(Events.ORIGINAL_INSTANCE_TIME);
                values.remove(Events.STATUS);
                mDbHelper.instancesReplace(values);
            }
        }
    }
    private void computeTimezoneDependentFields(long begin, long end,
            Time local, ContentValues values) {
        local.set(begin);
        int startDay = Time.getJulianDay(begin, local.gmtoff);
        int startMinute = local.hour * 60 + local.minute;
        local.set(end);
        int endDay = Time.getJulianDay(end, local.gmtoff);
        int endMinute = local.hour * 60 + local.minute;
        if (endMinute == 0 && endDay > startDay) {
            endMinute = 24 * 60;
            endDay -= 1;
        }
        values.put(Instances.START_DAY, startDay);
        values.put(Instances.END_DAY, endDay);
        values.put(Instances.START_MINUTE, startMinute);
        values.put(Instances.END_MINUTE, endMinute);
    }
    @Override
    public String getType(Uri url) {
        int match = sUriMatcher.match(url);
        switch (match) {
            case EVENTS:
                return "vnd.android.cursor.dir/event";
            case EVENTS_ID:
                return "vnd.android.cursor.item/event";
            case REMINDERS:
                return "vnd.android.cursor.dir/reminder";
            case REMINDERS_ID:
                return "vnd.android.cursor.item/reminder";
            case CALENDAR_ALERTS:
                return "vnd.android.cursor.dir/calendar-alert";
            case CALENDAR_ALERTS_BY_INSTANCE:
                return "vnd.android.cursor.dir/calendar-alert-by-instance";
            case CALENDAR_ALERTS_ID:
                return "vnd.android.cursor.item/calendar-alert";
            case INSTANCES:
            case INSTANCES_BY_DAY:
            case EVENT_DAYS:
                return "vnd.android.cursor.dir/event-instance";
            case TIME:
                return "time/epoch";
            default:
                throw new IllegalArgumentException("Unknown URL " + url);
        }
    }
    public static boolean isRecurrenceEvent(ContentValues values) {
        return (!TextUtils.isEmpty(values.getAsString(Events.RRULE))||
                !TextUtils.isEmpty(values.getAsString(Events.RDATE))||
                !TextUtils.isEmpty(values.getAsString(Events.ORIGINAL_EVENT)));
    }
    private boolean fixAllDayTime(Uri uri, ContentValues updatedValues) {
        boolean neededCorrection = false;
        if (updatedValues.containsKey(Events.ALL_DAY)
                && updatedValues.getAsInteger(Events.ALL_DAY).intValue() == 1) {
            Long dtstart = updatedValues.getAsLong(Events.DTSTART);
            Long dtend = updatedValues.getAsLong(Events.DTEND);
            String duration = updatedValues.getAsString(Events.DURATION);
            Time time = new Time();
            Cursor currentTimesCursor = null;
            String tempValue;
            if(dtstart == null || (dtend == null && duration == null)) {
                if (uri.getPathSegments().size() == 2) {
                    currentTimesCursor = query(uri,
                            ALLDAY_TIME_PROJECTION,
                            null ,
                            null ,
                            null );
                    if (currentTimesCursor != null) {
                        if (!currentTimesCursor.moveToFirst() ||
                                currentTimesCursor.getCount() != 1) {
                            currentTimesCursor.close();
                            currentTimesCursor = null;
                        }
                    }
                }
            }
            if (dtstart == null) {
                if (currentTimesCursor != null) {
                    tempValue = currentTimesCursor.getString(ALLDAY_DTSTART_INDEX);
                    try {
                        dtstart = Long.valueOf(tempValue);
                    } catch (NumberFormatException e) {
                        currentTimesCursor.close();
                        throw new IllegalArgumentException("Event has no DTSTART field, the db " +
                            "may be damaged. Set DTSTART for this event to fix.");
                    }
                } else {
                    throw new IllegalArgumentException("DTSTART cannot be empty for new events.");
                }
            }
            time.clear(Time.TIMEZONE_UTC);
            time.set(dtstart.longValue());
            if (time.hour != 0 || time.minute != 0 || time.second != 0) {
                time.hour = 0;
                time.minute = 0;
                time.second = 0;
                updatedValues.put(Events.DTSTART, time.toMillis(true));
                neededCorrection = true;
            }
            if (dtend == null && currentTimesCursor != null) {
                tempValue = currentTimesCursor.getString(ALLDAY_DTEND_INDEX);
                try {
                    dtend = Long.valueOf(tempValue);
                } catch (NumberFormatException e) {
                    dtend = null;
                }
            }
            if (dtend != null) {
                time.clear(Time.TIMEZONE_UTC);
                time.set(dtend.longValue());
                if (time.hour != 0 || time.minute != 0 || time.second != 0) {
                    time.hour = 0;
                    time.minute = 0;
                    time.second = 0;
                    dtend = time.toMillis(true);
                    updatedValues.put(Events.DTEND, dtend);
                    neededCorrection = true;
                }
            }
            if (currentTimesCursor != null) {
                if (duration == null) {
                    duration = currentTimesCursor.getString(ALLDAY_DURATION_INDEX);
                }
                currentTimesCursor.close();
            }
            if (duration != null) {
                int len = duration.length();
                if (len == 0) {
                    duration = null;
                } else if (duration.charAt(0) == 'P' &&
                        duration.charAt(len - 1) == 'S') {
                    int seconds = Integer.parseInt(duration.substring(1, len - 1));
                    int days = (seconds + DAY_IN_SECONDS - 1) / DAY_IN_SECONDS;
                    duration = "P" + days + "D";
                    updatedValues.put(Events.DURATION, duration);
                    neededCorrection = true;
                } else if (duration.charAt(0) != 'P' ||
                        duration.charAt(len - 1) != 'D') {
                    throw new IllegalArgumentException("duration is not formatted correctly. " +
                            "Should be 'P<seconds>S' or 'P<days>D'.");
                }
            }
            if (duration == null && dtend == null) {
                throw new IllegalArgumentException("DTEND and DURATION cannot both be null for " +
                        "an event.");
            }
        }
        return neededCorrection;
    }
    @Override
    protected Uri insertInTransaction(Uri uri, ContentValues values) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "insertInTransaction: " + uri);
        }
        final boolean callerIsSyncAdapter =
                readBooleanQueryParameter(uri, Calendar.CALLER_IS_SYNCADAPTER, false);
        final int match = sUriMatcher.match(uri);
        long id = 0;
        switch (match) {
              case SYNCSTATE:
                id = mDbHelper.getSyncState().insert(mDb, values);
                break;
            case EVENTS:
                if (!callerIsSyncAdapter) {
                    values.put(Events._SYNC_DIRTY, 1);
                }
                if (!values.containsKey(Events.DTSTART)) {
                    throw new RuntimeException("DTSTART field missing from event");
                }
                ContentValues updatedValues = new ContentValues(values);
                validateEventData(updatedValues);
                updatedValues = updateLastDate(updatedValues);
                if (updatedValues == null) {
                    throw new RuntimeException("Could not insert event.");
                }
                String owner = null;
                if (updatedValues.containsKey(Events.CALENDAR_ID) &&
                        !updatedValues.containsKey(Events.ORGANIZER)) {
                    owner = getOwner(updatedValues.getAsLong(Events.CALENDAR_ID));
                    if (owner != null) {
                        updatedValues.put(Events.ORGANIZER, owner);
                    }
                }
                if (fixAllDayTime(uri, updatedValues)) {
                    Log.w(TAG, "insertInTransaction: " +
                            "allDay is true but sec, min, hour were not 0.");
                }
                id = mDbHelper.eventsInsert(updatedValues);
                if (id != -1) {
                    updateEventRawTimesLocked(id, updatedValues);
                    updateInstancesLocked(updatedValues, id, true , mDb);
                    if (values.containsKey(Events.SELF_ATTENDEE_STATUS)) {
                        int status = values.getAsInteger(Events.SELF_ATTENDEE_STATUS);
                        if (owner == null) {
                            owner = getOwner(updatedValues.getAsLong(Events.CALENDAR_ID));
                        }
                        createAttendeeEntry(id, status, owner);
                    }
                    triggerAppWidgetUpdate(id);
                }
                break;
            case CALENDARS:
                Integer syncEvents = values.getAsInteger(Calendars.SYNC_EVENTS);
                if (syncEvents != null && syncEvents == 1) {
                    String accountName = values.getAsString(Calendars._SYNC_ACCOUNT);
                    String accountType = values.getAsString(
                            Calendars._SYNC_ACCOUNT_TYPE);
                    final Account account = new Account(accountName, accountType);
                    String calendarUrl = values.getAsString(Calendars.URL);
                    mDbHelper.scheduleSync(account, false , calendarUrl);
                }
                id = mDbHelper.calendarsInsert(values);
                break;
            case ATTENDEES:
                if (!values.containsKey(Attendees.EVENT_ID)) {
                    throw new IllegalArgumentException("Attendees values must "
                            + "contain an event_id");
                }
                id = mDbHelper.attendeesInsert(values);
                if (!callerIsSyncAdapter) {
                    setEventDirty(values.getAsInteger(Attendees.EVENT_ID));
                }
                updateEventAttendeeStatus(mDb, values);
                break;
            case REMINDERS:
                if (!values.containsKey(Reminders.EVENT_ID)) {
                    throw new IllegalArgumentException("Reminders values must "
                            + "contain an event_id");
                }
                id = mDbHelper.remindersInsert(values);
                if (!callerIsSyncAdapter) {
                    setEventDirty(values.getAsInteger(Reminders.EVENT_ID));
                }
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "insertInternal() changing reminder");
                }
                scheduleNextAlarm(false );
                break;
            case CALENDAR_ALERTS:
                if (!values.containsKey(CalendarAlerts.EVENT_ID)) {
                    throw new IllegalArgumentException("CalendarAlerts values must "
                            + "contain an event_id");
                }
                id = mDbHelper.calendarAlertsInsert(values);
                break;
            case EXTENDED_PROPERTIES:
                if (!values.containsKey(Calendar.ExtendedProperties.EVENT_ID)) {
                    throw new IllegalArgumentException("ExtendedProperties values must "
                            + "contain an event_id");
                }
                id = mDbHelper.extendedPropertiesInsert(values);
                if (!callerIsSyncAdapter) {
                    setEventDirty(values.getAsInteger(Calendar.ExtendedProperties.EVENT_ID));
                }
                break;
            case DELETED_EVENTS:
            case EVENTS_ID:
            case REMINDERS_ID:
            case CALENDAR_ALERTS_ID:
            case EXTENDED_PROPERTIES_ID:
            case INSTANCES:
            case INSTANCES_BY_DAY:
            case EVENT_DAYS:
                throw new UnsupportedOperationException("Cannot insert into that URL: " + uri);
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
        if (id < 0) {
            return null;
        }
        return ContentUris.withAppendedId(uri, id);
    }
    private void validateEventData(ContentValues values) {
        boolean hasDtend = values.getAsLong(Events.DTEND) != null;
        boolean hasDuration = !TextUtils.isEmpty(values.getAsString(Events.DURATION));
        boolean hasRrule = !TextUtils.isEmpty(values.getAsString(Events.RRULE));
        boolean hasRdate = !TextUtils.isEmpty(values.getAsString(Events.RDATE));
        boolean hasOriginalEvent = !TextUtils.isEmpty(values.getAsString(Events.ORIGINAL_EVENT));
        boolean hasOriginalInstanceTime = values.getAsLong(Events.ORIGINAL_INSTANCE_TIME) != null;
        if (hasRrule || hasRdate) {
            if (hasDtend || !hasDuration || hasOriginalEvent || hasOriginalInstanceTime) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.e(TAG, "Invalid values for recurrence: " + values);
                }
                values.remove(Events.DTEND);
                values.remove(Events.ORIGINAL_EVENT);
                values.remove(Events.ORIGINAL_INSTANCE_TIME);
            }
        } else if (hasOriginalEvent || hasOriginalInstanceTime) {
            if (!hasDtend || hasDuration || !hasOriginalEvent || !hasOriginalInstanceTime) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.e(TAG, "Invalid values for recurrence exception: " + values);
                }
                values.remove(Events.DURATION);
            }
        } else {
            if (!hasDtend || hasDuration) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.e(TAG, "Invalid values for event: " + values);
                }
                values.remove(Events.DURATION);
            }
        }
    }
    private void setEventDirty(int eventId) {
        mDb.execSQL("UPDATE Events SET _sync_dirty=1 where _id=?", new Integer[] {eventId});
    }
    private String getOwner(long calId) {
        if (calId < 0) {
            Log.e(TAG, "Calendar Id is not valid: " + calId);
            return null;
        }
        String emailAddress = null;
        Cursor cursor = null;
        try {
            cursor = query(ContentUris.withAppendedId(Calendars.CONTENT_URI, calId),
                    new String[] { Calendars.OWNER_ACCOUNT },
                    null ,
                    null ,
                    null );
            if (cursor == null || !cursor.moveToFirst()) {
                Log.d(TAG, "Couldn't find " + calId + " in Calendars table");
                return null;
            }
            emailAddress = cursor.getString(0);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return emailAddress;
    }
    private void createAttendeeEntry(long eventId, int status, String emailAddress) {
        ContentValues values = new ContentValues();
        values.put(Attendees.EVENT_ID, eventId);
        values.put(Attendees.ATTENDEE_STATUS, status);
        values.put(Attendees.ATTENDEE_TYPE, Attendees.TYPE_NONE);
        values.put(Attendees.ATTENDEE_RELATIONSHIP,
                Attendees.RELATIONSHIP_ATTENDEE);
        values.put(Attendees.ATTENDEE_EMAIL, emailAddress);
        mDbHelper.attendeesInsert(values);
    }
    private void updateEventAttendeeStatus(SQLiteDatabase db, ContentValues attendeeValues) {
        long eventId = attendeeValues.getAsLong(Attendees.EVENT_ID);
        if (MULTIPLE_ATTENDEES_PER_EVENT) {
            Cursor cursor = null;
            long calId;
            try {
                cursor = query(ContentUris.withAppendedId(Events.CONTENT_URI, eventId),
                        new String[] { Events.CALENDAR_ID },
                        null ,
                        null ,
                        null );
                if (cursor == null || !cursor.moveToFirst()) {
                    Log.d(TAG, "Couldn't find " + eventId + " in Events table");
                    return;
                }
                calId = cursor.getLong(0);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            String calendarEmail = null;
            cursor = null;
            try {
                cursor = query(ContentUris.withAppendedId(Calendars.CONTENT_URI, calId),
                        new String[] { Calendars.OWNER_ACCOUNT },
                        null ,
                        null ,
                        null );
                if (cursor == null || !cursor.moveToFirst()) {
                    Log.d(TAG, "Couldn't find " + calId + " in Calendars table");
                    return;
                }
                calendarEmail = cursor.getString(0);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            if (calendarEmail == null) {
                return;
            }
            String attendeeEmail = null;
            if (attendeeValues.containsKey(Attendees.ATTENDEE_EMAIL)) {
                attendeeEmail = attendeeValues.getAsString(Attendees.ATTENDEE_EMAIL);
            }
            if (!calendarEmail.equals(attendeeEmail)) {
                return;
            }
        }
        int status = Attendees.ATTENDEE_STATUS_NONE;
        if (attendeeValues.containsKey(Attendees.ATTENDEE_RELATIONSHIP)) {
            int rel = attendeeValues.getAsInteger(Attendees.ATTENDEE_RELATIONSHIP);
            if (rel == Attendees.RELATIONSHIP_ORGANIZER) {
                status = Attendees.ATTENDEE_STATUS_ACCEPTED;
            }
        }
        if (attendeeValues.containsKey(Attendees.ATTENDEE_STATUS)) {
            status = attendeeValues.getAsInteger(Attendees.ATTENDEE_STATUS);
        }
        ContentValues values = new ContentValues();
        values.put(Events.SELF_ATTENDEE_STATUS, status);
        db.update("Events", values, "_id=?", new String[] {String.valueOf(eventId)});
    }
    private void updateInstancesLocked(ContentValues values,
            long rowId,
            boolean newEvent,
            SQLiteDatabase db) {
        MetaData.Fields fields = mMetaData.getFieldsLocked();
        if (fields.maxInstance == 0) {
            return;
        }
        Long dtstartMillis = values.getAsLong(Events.DTSTART);
        if (dtstartMillis == null) {
            if (newEvent) {
                throw new RuntimeException("DTSTART missing.");
            }
            if (Config.LOGV) Log.v(TAG, "Missing DTSTART.  "
                    + "No need to update instance.");
            return;
        }
        Long lastDateMillis = values.getAsLong(Events.LAST_DATE);
        Long originalInstanceTime = values.getAsLong(Events.ORIGINAL_INSTANCE_TIME);
        if (!newEvent) {
            db.delete("Instances", "event_id=?", new String[] {String.valueOf(rowId)});
        }
        if (isRecurrenceEvent(values))  {
            boolean insideWindow = dtstartMillis <= fields.maxInstance &&
                    (lastDateMillis == null || lastDateMillis >= fields.minInstance);
            boolean affectsWindow = originalInstanceTime != null &&
                    originalInstanceTime <= fields.maxInstance &&
                    originalInstanceTime >= fields.minInstance - MAX_ASSUMED_DURATION;
            if (insideWindow || affectsWindow) {
                updateRecurrenceInstancesLocked(values, rowId, db);
            }
            return;
        }
        Long dtendMillis = values.getAsLong(Events.DTEND);
        if (dtendMillis == null) {
            dtendMillis = dtstartMillis;
        }
        if (dtstartMillis <= fields.maxInstance && dtendMillis >= fields.minInstance) {
            ContentValues instanceValues = new ContentValues();
            instanceValues.put(Instances.EVENT_ID, rowId);
            instanceValues.put(Instances.BEGIN, dtstartMillis);
            instanceValues.put(Instances.END, dtendMillis);
            boolean allDay = false;
            Integer allDayInteger = values.getAsInteger(Events.ALL_DAY);
            if (allDayInteger != null) {
                allDay = allDayInteger != 0;
            }
            Time local = new Time();
            if (allDay) {
                local.timezone = Time.TIMEZONE_UTC;
            } else {
                local.timezone = fields.timezone;
            }
            computeTimezoneDependentFields(dtstartMillis, dtendMillis, local, instanceValues);
            mDbHelper.instancesInsert(instanceValues);
        }
    }
    private Cursor getRelevantRecurrenceEntries(String recurrenceSyncId, long rowId) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(CalendarDatabaseHelper.Views.EVENTS);
        qb.setProjectionMap(sEventsProjectionMap);
        String selectionArgs[];
        if (recurrenceSyncId == null) {
            String where = "_id =?";
            qb.appendWhere(where);
            selectionArgs = new String[] {String.valueOf(rowId)};
        } else {
            String where = "_sync_id = ? OR originalEvent = ?";
            qb.appendWhere(where);
            selectionArgs = new String[] {recurrenceSyncId, recurrenceSyncId};
        }
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Retrieving events to expand: " + qb.toString());
        }
        return qb.query(mDb, EXPAND_COLUMNS, null , selectionArgs,
                null , null , null );
    }
    private void updateRecurrenceInstancesLocked(ContentValues values,
            long rowId,
            SQLiteDatabase db) {
        MetaData.Fields fields = mMetaData.getFieldsLocked();
        String originalEvent = values.getAsString(Events.ORIGINAL_EVENT);
        String recurrenceSyncId = null;
        if (originalEvent != null) {
            recurrenceSyncId = originalEvent;
        } else {
            recurrenceSyncId = DatabaseUtils.stringForQuery(db, "SELECT _sync_id FROM Events"
                    + " WHERE _id=?", new String[] {String.valueOf(rowId)});
        }
        if (recurrenceSyncId == null) {
            String where = "_id IN (SELECT Instances._id as _id"
                    + " FROM Instances INNER JOIN Events"
                    + " ON (Events._id = Instances.event_id)"
                    + " WHERE Events._id =?)";
            db.delete("Instances", where, new String[]{"" + rowId});
        } else {
            String where = "_id IN (SELECT Instances._id as _id"
                    + " FROM Instances INNER JOIN Events"
                    + " ON (Events._id = Instances.event_id)"
                    + " WHERE Events._sync_id =?"
                    + " OR Events.originalEvent =?)";
            db.delete("Instances", where, new String[]{recurrenceSyncId, recurrenceSyncId});
        }
        Cursor entries = getRelevantRecurrenceEntries(recurrenceSyncId, rowId);
        try {
            performInstanceExpansion(fields.minInstance, fields.maxInstance, fields.timezone,
                                     entries);
        } finally {
            if (entries != null) {
                entries.close();
            }
        }
        mMetaData.writeLocked(fields.timezone, fields.minInstance, fields.maxInstance);
    }
    long calculateLastDate(ContentValues values)
            throws DateException {
        if (!values.containsKey(Events.DTSTART)) {
            if (values.containsKey(Events.DTEND) || values.containsKey(Events.RRULE)
                    || values.containsKey(Events.DURATION)
                    || values.containsKey(Events.EVENT_TIMEZONE)
                    || values.containsKey(Events.RDATE)
                    || values.containsKey(Events.EXRULE)
                    || values.containsKey(Events.EXDATE)) {
                throw new RuntimeException("DTSTART field missing from event");
            }
            return -1;
        }
        long dtstartMillis = values.getAsLong(Events.DTSTART);
        long lastMillis = -1;
        Long dtEnd = values.getAsLong(Events.DTEND);
        if (dtEnd != null) {
            lastMillis = dtEnd;
        } else {
            Duration duration = new Duration();
            String durationStr = values.getAsString(Events.DURATION);
            if (durationStr != null) {
                duration.parse(durationStr);
            }
            RecurrenceSet recur = null;
            try {
                recur = new RecurrenceSet(values);
            } catch (EventRecurrence.InvalidFormatException e) {
                Log.w(TAG, "Could not parse RRULE recurrence string: " +
                        values.get(Calendar.Events.RRULE), e);
                return lastMillis; 
            }
            if (null != recur && recur.hasRecurrence()) {
                String tz = values.getAsString(Events.EVENT_TIMEZONE);
                if (TextUtils.isEmpty(tz)) {
                    tz = Time.TIMEZONE_UTC;
                }
                Time dtstartLocal = new Time(tz);
                dtstartLocal.set(dtstartMillis);
                RecurrenceProcessor rp = new RecurrenceProcessor();
                lastMillis = rp.getLastOccurence(dtstartLocal, recur);
                if (lastMillis == -1) {
                    return lastMillis;  
                }
            } else {
                lastMillis = dtstartMillis;
            }
            lastMillis = duration.addTo(lastMillis);
        }
        return lastMillis;
    }
    private ContentValues updateLastDate(ContentValues values) {
        try {
            long last = calculateLastDate(values);
            if (last != -1) {
                values.put(Events.LAST_DATE, last);
            }
            return values;
        } catch (DateException e) {
            Log.w(TAG, "Could not calculate last date.", e);
            return null;
        }
    }
    private void updateEventRawTimesLocked(long eventId, ContentValues values) {
        ContentValues rawValues = new ContentValues();
        rawValues.put("event_id", eventId);
        String timezone = values.getAsString(Events.EVENT_TIMEZONE);
        boolean allDay = false;
        Integer allDayInteger = values.getAsInteger(Events.ALL_DAY);
        if (allDayInteger != null) {
            allDay = allDayInteger != 0;
        }
        if (allDay || TextUtils.isEmpty(timezone)) {
            timezone = Time.TIMEZONE_UTC;
        }
        Time time = new Time(timezone);
        time.allDay = allDay;
        Long dtstartMillis = values.getAsLong(Events.DTSTART);
        if (dtstartMillis != null) {
            time.set(dtstartMillis);
            rawValues.put("dtstart2445", time.format2445());
        }
        Long dtendMillis = values.getAsLong(Events.DTEND);
        if (dtendMillis != null) {
            time.set(dtendMillis);
            rawValues.put("dtend2445", time.format2445());
        }
        Long originalInstanceMillis = values.getAsLong(Events.ORIGINAL_INSTANCE_TIME);
        if (originalInstanceMillis != null) {
            allDayInteger = values.getAsInteger(Events.ORIGINAL_ALL_DAY);
            if (allDayInteger != null) {
                time.allDay = allDayInteger != 0;
            }
            time.set(originalInstanceMillis);
            rawValues.put("originalInstanceTime2445", time.format2445());
        }
        Long lastDateMillis = values.getAsLong(Events.LAST_DATE);
        if (lastDateMillis != null) {
            time.allDay = allDay;
            time.set(lastDateMillis);
            rawValues.put("lastDate2445", time.format2445());
        }
        mDbHelper.eventsRawTimesReplace(rawValues);
    }
    @Override
    protected int deleteInTransaction(Uri uri, String selection, String[] selectionArgs) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "deleteInTransaction: " + uri);
        }
        final boolean callerIsSyncAdapter =
                readBooleanQueryParameter(uri, Calendar.CALLER_IS_SYNCADAPTER, false);
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SYNCSTATE:
                return mDbHelper.getSyncState().delete(mDb, selection, selectionArgs);
            case SYNCSTATE_ID:
                String selectionWithId = (BaseColumns._ID + "=?")
                        + (selection == null ? "" : " AND (" + selection + ")");
                selectionArgs = insertSelectionArg(selectionArgs,
                        String.valueOf(ContentUris.parseId(uri)));
                return mDbHelper.getSyncState().delete(mDb, selectionWithId,
                        selectionArgs);
            case EVENTS:
            {
                int result = 0;
                selection = appendAccountToSelection(uri, selection);
                Cursor cursor = mDb.query("Events", ID_ONLY_PROJECTION,
                        selection, selectionArgs, null ,
                        null , null );
                try {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(0);
                        result += deleteEventInternal(id, callerIsSyncAdapter, true );
                    }
                    scheduleNextAlarm(false );
                    triggerAppWidgetUpdate(-1 );
                } finally {
                    cursor.close();
                    cursor = null;
                }
                return result;
            }
            case EVENTS_ID:
            {
                long id = ContentUris.parseId(uri);
                if (selection != null) {
                    throw new UnsupportedOperationException("CalendarProvider2 "
                            + "doesn't support selection based deletion for type "
                            + match);
                }
                return deleteEventInternal(id, callerIsSyncAdapter, false );
            }
            case ATTENDEES:
            {
                if (callerIsSyncAdapter) {
                    return mDb.delete("Attendees", selection, selectionArgs);
                } else {
                    return deleteFromTable("Attendees", uri, selection, selectionArgs);
                }
            }
            case ATTENDEES_ID:
            {
                if (selection != null) {
                    throw new UnsupportedOperationException("Selection not permitted for " + uri);
                }
                if (callerIsSyncAdapter) {
                    long id = ContentUris.parseId(uri);
                    return mDb.delete("Attendees", "_id=?", new String[] {String.valueOf(id)});
                } else {
                    return deleteFromTable("Attendees", uri, null ,
                                           null );
                }
            }
            case REMINDERS:
            {
                if (callerIsSyncAdapter) {
                    return mDb.delete("Reminders", selection, selectionArgs);
                } else {
                    return deleteFromTable("Reminders", uri, selection, selectionArgs);
                }
            }
            case REMINDERS_ID:
            {
                if (selection != null) {
                    throw new UnsupportedOperationException("Selection not permitted for " + uri);
                }
                if (callerIsSyncAdapter) {
                    long id = ContentUris.parseId(uri);
                    return mDb.delete("Reminders", "_id=?", new String[] {String.valueOf(id)});
                } else {
                    return deleteFromTable("Reminders", uri, null ,
                                           null );
                }
            }
            case EXTENDED_PROPERTIES:
            {
                if (callerIsSyncAdapter) {
                    return mDb.delete("ExtendedProperties", selection, selectionArgs);
                } else {
                    return deleteFromTable("ExtendedProperties", uri, selection, selectionArgs);
                }
            }
            case EXTENDED_PROPERTIES_ID:
            {
                if (selection != null) {
                    throw new UnsupportedOperationException("Selection not permitted for " + uri);
                }
                if (callerIsSyncAdapter) {
                    long id = ContentUris.parseId(uri);
                    return mDb.delete("ExtendedProperties", "_id=?",
                            new String[] {String.valueOf(id)});
                } else {
                    return deleteFromTable("ExtendedProperties", uri, null ,
                                           null );
                }
            }
            case CALENDAR_ALERTS:
            {
                if (callerIsSyncAdapter) {
                    return mDb.delete("CalendarAlerts", selection, selectionArgs);
                } else {
                    return deleteFromTable("CalendarAlerts", uri, selection, selectionArgs);
                }
            }
            case CALENDAR_ALERTS_ID:
            {
                if (selection != null) {
                    throw new UnsupportedOperationException("Selection not permitted for " + uri);
                }
                long id = ContentUris.parseId(uri);
                return mDb.delete("CalendarAlerts", "_id=?", new String[] {String.valueOf(id)});
            }
            case DELETED_EVENTS:
                throw new UnsupportedOperationException("Cannot delete that URL: " + uri);
            case CALENDARS_ID:
                StringBuilder selectionSb = new StringBuilder("_id=");
                selectionSb.append(uri.getPathSegments().get(1));
                if (!TextUtils.isEmpty(selection)) {
                    selectionSb.append(" AND (");
                    selectionSb.append(selection);
                    selectionSb.append(')');
                }
                selection = selectionSb.toString();
            case CALENDARS:
                selection = appendAccountToSelection(uri, selection);
                return deleteMatchingCalendars(selection); 
            case INSTANCES:
            case INSTANCES_BY_DAY:
            case EVENT_DAYS:
                throw new UnsupportedOperationException("Cannot delete that URL");
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }
    private int deleteEventInternal(long id, boolean callerIsSyncAdapter, boolean isBatch) {
        int result = 0;
        String selectionArgs[] = new String[] {String.valueOf(id)};
        Cursor cursor = mDb.query("Events", EVENTS_PROJECTION,
                "_id=?", selectionArgs,
                null ,
                null , null );
        try {
            if (cursor.moveToNext()) {
                result = 1;
                String syncId = cursor.getString(EVENTS_SYNC_ID_INDEX);
                boolean emptySyncId = TextUtils.isEmpty(syncId);
                if (!emptySyncId) {
                }
                String rrule = cursor.getString(EVENTS_RRULE_INDEX);
                String rdate = cursor.getString(EVENTS_RDATE_INDEX);
                String origEvent = cursor.getString(EVENTS_ORIGINAL_EVENT_INDEX);
                if (!TextUtils.isEmpty(rrule) || !TextUtils.isEmpty(rdate)
                        || !TextUtils.isEmpty(origEvent)) {
                    mMetaData.clearInstanceRange();
                }
                if (callerIsSyncAdapter || emptySyncId) {
                    mDb.delete("Events", "_id=?", selectionArgs);
                    mDb.delete("Attendees", "event_id=?", selectionArgs);
                } else {
                    ContentValues values = new ContentValues();
                    values.put(Events.DELETED, 1);
                    values.put(Events._SYNC_DIRTY, 1);
                    mDb.update("Events", values, "_id=?", selectionArgs);
                }
            }
        } finally {
            cursor.close();
            cursor = null;
        }
        if (!isBatch) {
            scheduleNextAlarm(false );
            triggerAppWidgetUpdate(-1 );
        }
        mDb.delete("Instances", "event_id=?", selectionArgs);
        mDb.delete("EventsRawTimes", "event_id=?", selectionArgs);
        mDb.delete("Reminders", "event_id=?", selectionArgs);
        mDb.delete("CalendarAlerts", "event_id=?", selectionArgs);
        mDb.delete("ExtendedProperties", "event_id=?", selectionArgs);
        return result;
    }
    private int deleteFromTable(String table, Uri uri, String selection, String[] selectionArgs) {
        Cursor c = query(uri, ID_PROJECTION, selection, selectionArgs, null);
        ContentValues values = new ContentValues();
        values.put(Events._SYNC_DIRTY, "1");
        int count = 0;
        try {
            while(c.moveToNext()) {
                long id = c.getLong(ID_INDEX);
                long event_id = c.getLong(EVENT_ID_INDEX);
                mDb.delete(table, "_id=?", new String[] {String.valueOf(id)});
                mDb.update("Events", values, "_id=?", new String[] {String.valueOf(event_id)});
                count++;
            }
        } finally {
            c.close();
        }
        return count;
    }
    private int updateInTable(String table, ContentValues values, Uri uri, String selection,
            String[] selectionArgs) {
        Cursor c = query(uri, ID_PROJECTION, selection, selectionArgs, null);
        ContentValues dirtyValues = new ContentValues();
        dirtyValues.put(Events._SYNC_DIRTY, "1");
        int count = 0;
        try {
            while(c.moveToNext()) {
                long id = c.getLong(ID_INDEX);
                long event_id = c.getLong(EVENT_ID_INDEX);
                mDb.update(table, values, "_id=?", new String[] {String.valueOf(id)});
                mDb.update("Events", dirtyValues, "_id=?", new String[] {String.valueOf(event_id)});
                count++;
            }
        } finally {
            c.close();
        }
        return count;
    }
    private int deleteMatchingCalendars(String where) {
        Cursor c = mDb.query("Calendars", sCalendarsIdProjection, where,
                null , null ,
                null , null );
        if (c == null) {
            return 0;
        }
        try {
            while (c.moveToNext()) {
                long id = c.getLong(CALENDARS_INDEX_ID);
                modifyCalendarSubscription(id, false );
            }
        } finally {
            c.close();
        }
        return mDb.delete("Calendars", where, null );
    }
    @Override
    protected int updateInTransaction(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "updateInTransaction: " + uri);
        }
        int count = 0;
        final int match = sUriMatcher.match(uri);
        final boolean callerIsSyncAdapter =
                readBooleanQueryParameter(uri, Calendar.CALLER_IS_SYNCADAPTER, false);
        if (!TextUtils.isEmpty(selection) && match != CALENDAR_ALERTS && match != EVENTS) {
            throw new IllegalArgumentException(
                    "WHERE based updates not supported");
        }
        switch (match) {
            case SYNCSTATE:
                return mDbHelper.getSyncState().update(mDb, values,
                        appendAccountToSelection(uri, selection), selectionArgs);
            case SYNCSTATE_ID: {
                selection = appendAccountToSelection(uri, selection);
                String selectionWithId = (BaseColumns._ID + "=?")
                        + (selection == null ? "" : " AND (" + selection + ")");
                selectionArgs = insertSelectionArg(selectionArgs,
                        String.valueOf(ContentUris.parseId(uri)));
                return mDbHelper.getSyncState().update(mDb, values, selectionWithId, selectionArgs);
            }
            case CALENDARS_ID:
            {
                if (selection != null) {
                    throw new UnsupportedOperationException("Selection not permitted for " + uri);
                }
                long id = ContentUris.parseId(uri);
                Integer syncEvents = values.getAsInteger(Calendars.SYNC_EVENTS);
                if (syncEvents != null) {
                    modifyCalendarSubscription(id, syncEvents == 1);
                }
                int result = mDb.update("Calendars", values, "_id=?",
                        new String[] {String.valueOf(id)});
                return result;
            }
            case EVENTS:
            case EVENTS_ID:
            {
                long id = 0;
                if (match == EVENTS_ID) {
                    id = ContentUris.parseId(uri);
                } else if (callerIsSyncAdapter) {
                    if (selection != null && selection.startsWith("_id=")) {
                        id = Long.parseLong(selection.substring(4));
                    } else {
                        if (fixAllDayTime(uri, values)) {
                            Log.w(TAG, "updateInTransaction: Caller is sync adapter. " +
                                    "allDay is true but sec, min, hour were not 0.");
                        }
                        return mDb.update("Events", values, selection, selectionArgs);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown URL " + uri);
                }
                if (!callerIsSyncAdapter) {
                    values.put(Events._SYNC_DIRTY, 1);
                }
                if (values.containsKey(Events.SELF_ATTENDEE_STATUS)) {
                    throw new IllegalArgumentException("Updating "
                            + Events.SELF_ATTENDEE_STATUS
                            + " in Events table is not allowed.");
                }
                if (values.containsKey(Events.HTML_URI) && !callerIsSyncAdapter) {
                    throw new IllegalArgumentException("Updating "
                            + Events.HTML_URI
                            + " in Events table is not allowed.");
                }
                ContentValues updatedValues = new ContentValues(values);
                updatedValues = updateLastDate(updatedValues);
                if (updatedValues == null) {
                    Log.w(TAG, "Could not update event.");
                    return 0;
                }
                Uri allDayUri;
                if (uri.getPathSegments().size() == 1) {
                    allDayUri = ContentUris.withAppendedId(uri, id);
                } else {
                    allDayUri = uri;
                }
                if (fixAllDayTime(allDayUri, updatedValues)) {
                    Log.w(TAG, "updateInTransaction: " +
                            "allDay is true but sec, min, hour were not 0.");
                }
                int result = mDb.update("Events", updatedValues, "_id=?",
                        new String[] {String.valueOf(id)});
                if (result > 0) {
                    updateEventRawTimesLocked(id, updatedValues);
                    updateInstancesLocked(updatedValues, id, false , mDb);
                    if (values.containsKey(Events.DTSTART)) {
                        if (Log.isLoggable(TAG, Log.DEBUG)) {
                            Log.d(TAG, "updateInternal() changing event");
                        }
                        scheduleNextAlarm(false );
                        triggerAppWidgetUpdate(id);
                    }
                }
                return result;
            }
            case ATTENDEES_ID: {
                if (selection != null) {
                    throw new UnsupportedOperationException("Selection not permitted for " + uri);
                }
                updateEventAttendeeStatus(mDb, values);
                if (callerIsSyncAdapter) {
                    long id = ContentUris.parseId(uri);
                    return mDb.update("Attendees", values, "_id=?",
                            new String[] {String.valueOf(id)});
                } else {
                    return updateInTable("Attendees", values, uri, null ,
                            null );
                }
            }
            case CALENDAR_ALERTS_ID: {
                if (selection != null) {
                    throw new UnsupportedOperationException("Selection not permitted for " + uri);
                }
                long id = ContentUris.parseId(uri);
                return mDb.update("CalendarAlerts", values, "_id=?",
                        new String[] {String.valueOf(id)});
            }
            case CALENDAR_ALERTS: {
                return mDb.update("CalendarAlerts", values, selection, selectionArgs);
            }
            case REMINDERS_ID: {
                if (selection != null) {
                    throw new UnsupportedOperationException("Selection not permitted for " + uri);
                }
                if (callerIsSyncAdapter) {
                    long id = ContentUris.parseId(uri);
                    count = mDb.update("Reminders", values, "_id=?",
                            new String[] {String.valueOf(id)});
                } else {
                    count = updateInTable("Reminders", values, uri, null ,
                            null );
                }
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "updateInternal() changing reminder");
                }
                scheduleNextAlarm(false );
                return count;
            }
            case EXTENDED_PROPERTIES_ID: {
                if (selection != null) {
                    throw new UnsupportedOperationException("Selection not permitted for " + uri);
                }
                if (callerIsSyncAdapter) {
                    long id = ContentUris.parseId(uri);
                    return mDb.update("ExtendedProperties", values, "_id=?",
                            new String[] {String.valueOf(id)});
                } else {
                    return updateInTable("ExtendedProperties", values, uri, null ,
                            null );
                }
            }
            case SCHEDULE_ALARM: {
                scheduleNextAlarm(false);
                return 0;
            }
            case SCHEDULE_ALARM_REMOVE: {
                scheduleNextAlarm(true);
                return 0;
            }
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }
    private void appendAccountFromParameter(SQLiteQueryBuilder qb, Uri uri) {
        final String accountName = getQueryParameter(uri, Calendar.EventsEntity.ACCOUNT_NAME);
        final String accountType = getQueryParameter(uri, Calendar.EventsEntity.ACCOUNT_TYPE);
        if (!TextUtils.isEmpty(accountName)) {
            qb.appendWhere(Calendar.Calendars._SYNC_ACCOUNT + "="
                    + DatabaseUtils.sqlEscapeString(accountName) + " AND "
                    + Calendar.Calendars._SYNC_ACCOUNT_TYPE + "="
                    + DatabaseUtils.sqlEscapeString(accountType));
        } else {
            qb.appendWhere("1"); 
        }
    }
    private String appendAccountToSelection(Uri uri, String selection) {
        final String accountName = getQueryParameter(uri, Calendar.EventsEntity.ACCOUNT_NAME);
        final String accountType = getQueryParameter(uri, Calendar.EventsEntity.ACCOUNT_TYPE);
        if (!TextUtils.isEmpty(accountName)) {
            StringBuilder selectionSb = new StringBuilder(Calendar.Calendars._SYNC_ACCOUNT + "="
                    + DatabaseUtils.sqlEscapeString(accountName) + " AND "
                    + Calendar.Calendars._SYNC_ACCOUNT_TYPE + "="
                    + DatabaseUtils.sqlEscapeString(accountType));
            if (!TextUtils.isEmpty(selection)) {
                selectionSb.append(" AND (");
                selectionSb.append(selection);
                selectionSb.append(')');
            }
            return selectionSb.toString();
        } else {
            return selection;
        }
    }
    private void modifyCalendarSubscription(long id, boolean syncEvents) {
        Cursor cursor = query(ContentUris.withAppendedId(Calendars.CONTENT_URI, id),
                new String[] {Calendars._SYNC_ACCOUNT, Calendars._SYNC_ACCOUNT_TYPE,
                        Calendars.URL, Calendars.SYNC_EVENTS},
                null ,
                null ,
                null );
        Account account = null;
        String calendarUrl = null;
        boolean oldSyncEvents = false;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    final String accountName = cursor.getString(0);
                    final String accountType = cursor.getString(1);
                    account = new Account(accountName, accountType);
                    calendarUrl = cursor.getString(2);
                    oldSyncEvents = (cursor.getInt(3) != 0);
                }
            } finally {
                cursor.close();
            }
        }
        if (account == null) {
            Log.w(TAG, "Cannot update subscription because account "
                    + "is empty -- should not happen.");
            return;
        }
        if (TextUtils.isEmpty(calendarUrl)) {
            calendarUrl = null;
        }
        if (oldSyncEvents == syncEvents) {
            return;
        }
        mDbHelper.scheduleSync(account, !syncEvents, calendarUrl);
    }
    private synchronized void triggerAppWidgetUpdate(long changedEventId) {
        Context context = getContext();
        if (context != null) {
            mAppWidgetProvider.providerUpdated(context, changedEventId);
        }
    }
    private AlarmManager getAlarmManager() {
        synchronized(mAlarmLock) {
            if (mAlarmManager == null) {
                Context context = getContext();
                if (context == null) {
                    Log.e(TAG, "getAlarmManager() cannot get Context");
                    return null;
                }
                Object service = context.getSystemService(Context.ALARM_SERVICE);
                mAlarmManager = (AlarmManager) service;
            }
            return mAlarmManager;
        }
    }
    void scheduleNextAlarmCheck(long triggerTime) {
        AlarmManager manager = getAlarmManager();
        if (manager == null) {
            Log.e(TAG, "scheduleNextAlarmCheck() cannot get AlarmManager");
            return;
        }
        Context context = getContext();
        Intent intent = new Intent(CalendarReceiver.SCHEDULE);
        intent.setClass(context, CalendarReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_NO_CREATE);
        if (pending != null) {
            manager.cancel(pending);
        }
        pending = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Time time = new Time();
            time.set(triggerTime);
            String timeStr = time.format(" %a, %b %d, %Y %I:%M%P");
            Log.d(TAG, "scheduleNextAlarmCheck at: " + triggerTime + timeStr);
        }
        manager.set(AlarmManager.RTC_WAKEUP, triggerTime, pending);
    }
    void scheduleNextAlarm(boolean removeAlarms) {
        Thread thread = new AlarmScheduler(removeAlarms);
        thread.start();
    }
    private void runScheduleNextAlarm(boolean removeAlarms) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            if (removeAlarms) {
                removeScheduledAlarmsLocked(db);
            }
            scheduleNextAlarmLocked(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }
    private void scheduleNextAlarmLocked(SQLiteDatabase db) {
        AlarmManager alarmManager = getAlarmManager();
        if (alarmManager == null) {
            Log.e(TAG, "Failed to find the AlarmManager. Could not schedule the next alarm!");
            return;
        }
        final long currentMillis = System.currentTimeMillis();
        final long start = currentMillis - SCHEDULE_ALARM_SLACK;
        final long end = start + (24 * 60 * 60 * 1000);
        ContentResolver cr = getContext().getContentResolver();
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Time time = new Time();
            time.set(start);
            String startTimeStr = time.format(" %a, %b %d, %Y %I:%M%P");
            Log.d(TAG, "runScheduleNextAlarm() start search: " + startTimeStr);
        }
        String selectArg[] = new String[] {
            Long.toString(currentMillis - CLEAR_OLD_ALARM_THRESHOLD)
        };
        int rowsDeleted =
            db.delete(CalendarAlerts.TABLE_NAME, INVALID_CALENDARALERTS_SELECTOR, selectArg);
        long nextAlarmTime = end;
        final long tmpAlarmTime = CalendarAlerts.findNextAlarmTime(cr, currentMillis);
        if (tmpAlarmTime != -1 && tmpAlarmTime < nextAlarmTime) {
            nextAlarmTime = tmpAlarmTime;
        }
        String query = "SELECT begin-(minutes*60000) AS myAlarmTime,"
                + " Instances.event_id AS eventId, begin, end,"
                + " title, allDay, method, minutes"
                + " FROM Instances INNER JOIN Events"
                + " ON (Events._id = Instances.event_id)"
                + " INNER JOIN Reminders"
                + " ON (Instances.event_id = Reminders.event_id)"
                + " WHERE method=" + Reminders.METHOD_ALERT
                + " AND myAlarmTime>=CAST(? AS INT)"
                + " AND myAlarmTime<=CAST(? AS INT)"
                + " AND end>=?"
                + " AND 0=(SELECT count(*) from CalendarAlerts CA"
                + " where CA.event_id=Instances.event_id AND CA.begin=Instances.begin"
                + " AND CA.alarmTime=myAlarmTime)"
                + " ORDER BY myAlarmTime,begin,title";
        String queryParams[] = new String[] {String.valueOf(start), String.valueOf(nextAlarmTime),
                String.valueOf(currentMillis)};
        acquireInstanceRangeLocked(start, end, false );
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, queryParams);
            final int beginIndex = cursor.getColumnIndex(Instances.BEGIN);
            final int endIndex = cursor.getColumnIndex(Instances.END);
            final int eventIdIndex = cursor.getColumnIndex("eventId");
            final int alarmTimeIndex = cursor.getColumnIndex("myAlarmTime");
            final int minutesIndex = cursor.getColumnIndex(Reminders.MINUTES);
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Time time = new Time();
                time.set(nextAlarmTime);
                String alarmTimeStr = time.format(" %a, %b %d, %Y %I:%M%P");
                Log.d(TAG, "cursor results: " + cursor.getCount() + " nextAlarmTime: "
                        + alarmTimeStr);
            }
            while (cursor.moveToNext()) {
                final long alarmTime = cursor.getLong(alarmTimeIndex);
                final long eventId = cursor.getLong(eventIdIndex);
                final int minutes = cursor.getInt(minutesIndex);
                final long startTime = cursor.getLong(beginIndex);
                final long endTime = cursor.getLong(endIndex);
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Time time = new Time();
                    time.set(alarmTime);
                    String schedTime = time.format(" %a, %b %d, %Y %I:%M%P");
                    time.set(startTime);
                    String startTimeStr = time.format(" %a, %b %d, %Y %I:%M%P");
                    Log.d(TAG, "  looking at id: " + eventId + " " + startTime + startTimeStr
                            + " alarm: " + alarmTime + schedTime);
                }
                if (alarmTime < nextAlarmTime) {
                    nextAlarmTime = alarmTime;
                } else if (alarmTime >
                           nextAlarmTime + DateUtils.MINUTE_IN_MILLIS) {
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        Log.d(TAG, "This event alarm (and all later ones) will be scheduled later");
                    }
                    break;
                }
                if (CalendarAlerts.alarmExists(cr, eventId, startTime, alarmTime)) {
                    if (Log.isLoggable(TAG, Log.DEBUG)) {
                        int titleIndex = cursor.getColumnIndex(Events.TITLE);
                        String title = cursor.getString(titleIndex);
                        Log.d(TAG, "  alarm exists for id: " + eventId + " " + title);
                    }
                    continue;
                }
                Uri uri = CalendarAlerts.insert(cr, eventId, startTime,
                        endTime, alarmTime, minutes);
                if (uri == null) {
                    Log.e(TAG, "runScheduleNextAlarm() insert into CalendarAlerts table failed");
                    continue;
                }
                CalendarAlerts.scheduleAlarm(getContext(), alarmManager, alarmTime);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (rowsDeleted > 0) {
            CalendarAlerts.scheduleAlarm(getContext(), alarmManager, currentMillis);
        }
        if (nextAlarmTime != Long.MAX_VALUE) {
            scheduleNextAlarmCheck(nextAlarmTime + DateUtils.MINUTE_IN_MILLIS);
        } else {
            scheduleNextAlarmCheck(currentMillis + DateUtils.DAY_IN_MILLIS);
        }
    }
    private void removeScheduledAlarmsLocked(SQLiteDatabase db) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "removing scheduled alarms");
        }
        db.delete(CalendarAlerts.TABLE_NAME,
                CalendarAlerts.STATE + "=" + CalendarAlerts.SCHEDULED, null );
    }
    private static String sEventsTable = "Events";
    private static String sAttendeesTable = "Attendees";
    private static String sRemindersTable = "Reminders";
    private static String sCalendarAlertsTable = "CalendarAlerts";
    private static String sExtendedPropertiesTable = "ExtendedProperties";
    private static final int EVENTS = 1;
    private static final int EVENTS_ID = 2;
    private static final int INSTANCES = 3;
    private static final int DELETED_EVENTS = 4;
    private static final int CALENDARS = 5;
    private static final int CALENDARS_ID = 6;
    private static final int ATTENDEES = 7;
    private static final int ATTENDEES_ID = 8;
    private static final int REMINDERS = 9;
    private static final int REMINDERS_ID = 10;
    private static final int EXTENDED_PROPERTIES = 11;
    private static final int EXTENDED_PROPERTIES_ID = 12;
    private static final int CALENDAR_ALERTS = 13;
    private static final int CALENDAR_ALERTS_ID = 14;
    private static final int CALENDAR_ALERTS_BY_INSTANCE = 15;
    private static final int INSTANCES_BY_DAY = 16;
    private static final int SYNCSTATE = 17;
    private static final int SYNCSTATE_ID = 18;
    private static final int EVENT_ENTITIES = 19;
    private static final int EVENT_ENTITIES_ID = 20;
    private static final int EVENT_DAYS = 21;
    private static final int SCHEDULE_ALARM = 22;
    private static final int SCHEDULE_ALARM_REMOVE = 23;
    private static final int TIME = 24;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final HashMap<String, String> sInstancesProjectionMap;
    private static final HashMap<String, String> sEventsProjectionMap;
    private static final HashMap<String, String> sEventEntitiesProjectionMap;
    private static final HashMap<String, String> sAttendeesProjectionMap;
    private static final HashMap<String, String> sRemindersProjectionMap;
    private static final HashMap<String, String> sCalendarAlertsProjectionMap;
    static {
        sUriMatcher.addURI(Calendar.AUTHORITY, "instances/when*", INSTANCES_BY_DAY);
        sUriMatcher.addURI(Calendar.AUTHORITY, "instances/groupbyday
    public void onAccountsUpdated(Account[] accounts) {
        mDb = mDbHelper.getWritableDatabase();
        if (mDb == null) return;
        HashMap<Account, Boolean> accountHasCalendar = new HashMap<Account, Boolean>();
        HashSet<Account> validAccounts = new HashSet<Account>();
        for (Account account : accounts) {
            validAccounts.add(new Account(account.name, account.type));
            accountHasCalendar.put(account, false);
        }
        ArrayList<Account> accountsToDelete = new ArrayList<Account>();
        mDb.beginTransaction();
        try {
            for (String table : new String[]{"Calendars"}) {
                Cursor c = mDb.rawQuery("SELECT DISTINCT " + CalendarDatabaseHelper.ACCOUNT_NAME
                                        + ","
                                        + CalendarDatabaseHelper.ACCOUNT_TYPE + " from "
                        + table, null);
                while (c.moveToNext()) {
                    if (c.getString(0) != null && c.getString(1) != null) {
                        Account currAccount = new Account(c.getString(0), c.getString(1));
                        if (!validAccounts.contains(currAccount)) {
                            accountsToDelete.add(currAccount);
                        }
                    }
                }
                c.close();
            }
            for (Account account : accountsToDelete) {
                Log.d(TAG, "removing data for removed account " + account);
                String[] params = new String[]{account.name, account.type};
                mDb.execSQL("DELETE FROM Calendars"
                        + " WHERE " + CalendarDatabaseHelper.ACCOUNT_NAME + "= ? AND "
                        + CalendarDatabaseHelper.ACCOUNT_TYPE
                        + "= ?", params);
            }
            mDbHelper.getSyncState().onAccountsChanged(mDb, accounts);
            mDb.setTransactionSuccessful();
        } finally {
            mDb.endTransaction();
        }
    }
     static boolean readBooleanQueryParameter(Uri uri, String name,
            boolean defaultValue) {
        final String flag = getQueryParameter(uri, name);
        return flag == null
                ? defaultValue
                : (!"false".equals(flag.toLowerCase()) && !"0".equals(flag.toLowerCase()));
    }
     static String getQueryParameter(Uri uri, String parameter) {
        String query = uri.getEncodedQuery();
        if (query == null) {
            return null;
        }
        int queryLength = query.length();
        int parameterLength = parameter.length();
        String value;
        int index = 0;
        while (true) {
            index = query.indexOf(parameter, index);
            if (index == -1) {
                return null;
            }
            index += parameterLength;
            if (queryLength == index) {
                return null;
            }
            if (query.charAt(index) == '=') {
                index++;
                break;
            }
        }
        int ampIndex = query.indexOf('&', index);
        if (ampIndex == -1) {
            value = query.substring(index);
        } else {
            value = query.substring(index, ampIndex);
        }
        return Uri.decode(value);
    }
    private String[] insertSelectionArg(String[] selectionArgs, String arg) {
        if (selectionArgs == null) {
            return new String[] {arg};
        } else {
            int newLength = selectionArgs.length + 1;
            String[] newSelectionArgs = new String[newLength];
            newSelectionArgs[0] = arg;
            System.arraycopy(selectionArgs, 0, newSelectionArgs, 1, selectionArgs.length);
            return newSelectionArgs;
        }
    }
}
