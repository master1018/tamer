public class CalendarProvider2Test extends ProviderTestCase2<CalendarProvider2ForTesting> {
    static final String TAG = "calendar";
    private SQLiteDatabase mDb;
    private MetaData mMetaData;
    private Context mContext;
    private MockContentResolver mResolver;
    private Uri mEventsUri = Events.CONTENT_URI;
    private int mCalendarId;
    protected boolean mWipe = false;
    protected boolean mForceDtend = false;
    private int mGlobalSyncId = 1000;
    private static final String CALENDAR_URL =
            "http:
    private static final String TIME_ZONE_AMERICA_ANCHORAGE = "America/Anchorage";
    private static final String TIME_ZONE_AMERICA_LOS_ANGELES = "America/Los_Angeles";
    private static final String DEFAULT_TIMEZONE = TIME_ZONE_AMERICA_LOS_ANGELES;
    private static final String MOCK_TIME_ZONE_DATABASE_VERSION = "2010a";
    private class KeyValue {
        String key;
        String value;
        public KeyValue(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
    private interface Command {
        public void execute();
    }
    private class Insert implements Command {
        EventInfo eventInfo;
        public Insert(String eventName) {
            eventInfo = findEvent(eventName);
        }
        public void execute() {
            Log.i(TAG, "insert " + eventInfo.mTitle);
            insertEvent(mCalendarId, eventInfo);
        }
    }
    private class Delete implements Command {
        String eventName;
        int expected;
        public Delete(String eventName, int expected) {
            this.eventName = eventName;
            this.expected = expected;
        }
        public void execute() {
            Log.i(TAG, "delete " + eventName);
            int rows = deleteMatchingEvents(eventName);
            assertEquals(expected, rows);
        }
    }
    private class Update implements Command {
        String eventName;
        KeyValue[] pairs;
        public Update(String eventName, KeyValue[] pairs) {
            this.eventName = eventName;
            this.pairs = pairs;
        }
        public void execute() {
            Log.i(TAG, "update " + eventName);
            if (mWipe) {
                mMetaData.clearInstanceRange();
            }
            ContentValues map = new ContentValues();
            for (KeyValue pair : pairs) {
                String value = pair.value;
                if (Calendar.EventsColumns.STATUS.equals(pair.key)) {
                    map.put(pair.key, Integer.parseInt(value));
                } else {
                    map.put(pair.key, value);
                }
            }
            updateMatchingEvents(eventName, map);
        }
    }
    private class QueryNumEvents implements Command {
        int expected;
        public QueryNumEvents(int expected) {
            this.expected = expected;
        }
        public void execute() {
            Cursor cursor = mResolver.query(mEventsUri, null, null, null, null);
            assertEquals(expected, cursor.getCount());
            cursor.close();
        }
    }
    private class DumpEvents implements Command {
        public DumpEvents() {
        }
        public void execute() {
            Cursor cursor = mResolver.query(mEventsUri, null, null, null, null);
            dumpCursor(cursor);
            cursor.close();
        }
    }
    private class DumpInstances implements Command {
        long begin;
        long end;
        public DumpInstances(String startDate, String endDate) {
            Time time = new Time(DEFAULT_TIMEZONE);
            time.parse3339(startDate);
            begin = time.toMillis(false );
            time.parse3339(endDate);
            end = time.toMillis(false );
        }
        public void execute() {
            Cursor cursor = queryInstances(begin, end);
            dumpCursor(cursor);
            cursor.close();
        }
    }
    private class QueryNumInstances implements Command {
        int expected;
        long begin;
        long end;
        public QueryNumInstances(String startDate, String endDate, int expected) {
            Time time = new Time(DEFAULT_TIMEZONE);
            time.parse3339(startDate);
            begin = time.toMillis(false );
            time.parse3339(endDate);
            end = time.toMillis(false );
            this.expected = expected;
        }
        public void execute() {
            Cursor cursor = queryInstances(begin, end);
            assertEquals(expected, cursor.getCount());
            cursor.close();
        }
    }
    private class VerifyAllInstances implements Command {
        long[] instances;
        long begin;
        long end;
        public VerifyAllInstances(String startDate, String endDate, String[] dates) {
            Time time = new Time(DEFAULT_TIMEZONE);
            time.parse3339(startDate);
            begin = time.toMillis(false );
            time.parse3339(endDate);
            end = time.toMillis(false );
            if (dates == null) {
                return;
            }
            int len = dates.length;
            this.instances = new long[len];
            int index = 0;
            for (String instance : dates) {
                time.parse3339(instance);
                this.instances[index++] = time.toMillis(false );
            }
        }
        public void execute() {
            Cursor cursor = queryInstances(begin, end);
            int len = 0;
            if (instances != null) {
                len = instances.length;
            }
            if (len != cursor.getCount()) {
                dumpCursor(cursor);
            }
            assertEquals("number of instances don't match", len, cursor.getCount());
            if (instances == null) {
                return;
            }
            int beginColumn = cursor.getColumnIndex(Instances.BEGIN);
            while (cursor.moveToNext()) {
                long begin = cursor.getLong(beginColumn);
                boolean found = false;
                for (long instance : instances) {
                    if (instance == begin) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    int titleColumn = cursor.getColumnIndex(Events.TITLE);
                    int allDayColumn = cursor.getColumnIndex(Events.ALL_DAY);
                    String title = cursor.getString(titleColumn);
                    boolean allDay = cursor.getInt(allDayColumn) != 0;
                    int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE |
                            DateUtils.FORMAT_24HOUR;
                    if (allDay) {
                        flags |= DateUtils.FORMAT_UTC;
                    } else {
                        flags |= DateUtils.FORMAT_SHOW_TIME;
                    }
                    String date = DateUtils.formatDateRange(mContext, begin, begin, flags);
                    String mesg = String.format("Test failed!"
                            + " unexpected instance (\"%s\") at %s",
                            title, date);
                    Log.e(TAG, mesg);
                }
                if (!found) {
                    dumpCursor(cursor);
                }
                assertTrue(found);
            }
            cursor.close();
        }
    }
    private class VerifyInstance implements Command {
        long instance;
        boolean allDay;
        long begin;
        long end;
        public VerifyInstance(String startDate, String endDate, String date) {
            Time time = new Time(DEFAULT_TIMEZONE);
            time.parse3339(startDate);
            begin = time.toMillis(false );
            time.parse3339(endDate);
            end = time.toMillis(false );
            time.parse3339(date);
            allDay = time.allDay;
            instance = time.toMillis(false );
        }
        public void execute() {
            Cursor cursor = queryInstances(begin, end);
            int beginColumn = cursor.getColumnIndex(Instances.BEGIN);
            boolean found = false;
            while (cursor.moveToNext()) {
                long begin = cursor.getLong(beginColumn);
                if (instance == begin) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE;
                if (allDay) {
                    flags |= DateUtils.FORMAT_UTC;
                } else {
                    flags |= DateUtils.FORMAT_SHOW_TIME;
                }
                String date = DateUtils.formatDateRange(mContext, instance, instance, flags);
                String mesg = String.format("Test failed!"
                        + " cannot find instance at %s",
                        date);
                Log.e(TAG, mesg);
            }
            assertTrue(found);
            cursor.close();
        }
    }
    private class EventInfo {
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
    private class InstanceInfo {
        EventInfo mEvent;
        long mBegin;
        long mEnd;
        int mExpectedOccurrences;
        public InstanceInfo(String eventName, String startDate, String endDate, int expected) {
            mEvent = findEvent(eventName);
            Time time = new Time(mEvent.mTimezone);
            time.parse3339(startDate);
            mBegin = time.toMillis(false );
            time.parse3339(endDate);
            mEnd = time.toMillis(false );
            mExpectedOccurrences = expected;
        }
    }
    private EventInfo[] mEvents = {
            new EventInfo("normal0", "2008-05-01T00:00:00", "2008-05-02T00:00:00", false),
            new EventInfo("normal1", "2008-05-26T08:30:00", "2008-05-26T09:30:00", false),
            new EventInfo("normal2", "2008-05-26T14:30:00", "2008-05-26T15:30:00", false),
            new EventInfo("allday0", "2008-05-02T00:00:00", "2008-05-03T00:00:00", true),
            new EventInfo("allday1", "2008-05-02T00:00:00", "2008-05-31T00:00:00", true),
            new EventInfo("daily0", "daily from 5/1/2008 12am to 1am",
                    "2008-05-01T00:00:00", "2008-05-01T01:00:00",
                    "FREQ=DAILY;WKST=SU", false),
            new EventInfo("daily1", "daily from 5/1/2008 8:30am to 9:30am until 5/3/2008 8am",
                    "2008-05-01T08:30:00", "2008-05-01T09:30:00",
                    "FREQ=DAILY;UNTIL=20080503T150000Z;WKST=SU", false),
            new EventInfo("daily2", "daily from 5/1/2008 8:45am to 9:15am until 5/3/2008 10am",
                    "2008-05-01T08:45:00", "2008-05-01T09:15:00",
                    "FREQ=DAILY;UNTIL=20080503T170000Z;WKST=SU", false),
            new EventInfo("allday daily0", "all-day daily from 5/1/2008",
                    "2008-05-01", null,
                    "FREQ=DAILY;WKST=SU", true),
            new EventInfo("allday daily1", "all-day daily from 5/1/2008 until 5/3/2008",
                    "2008-05-01", null,
                    "FREQ=DAILY;UNTIL=20080503T000000Z;WKST=SU", true),
            new EventInfo("allday weekly0", "all-day weekly from 5/1/2008",
                    "2008-05-01", null,
                    "FREQ=WEEKLY;WKST=SU", true),
            new EventInfo("allday weekly1", "all-day for 2 days weekly from 5/1/2008",
                    "2008-05-01", "2008-05-03",
                    "FREQ=WEEKLY;WKST=SU", true),
            new EventInfo("allday yearly0", "all-day yearly on 5/1/2008",
                    "2008-05-01T", null,
                    "FREQ=YEARLY;WKST=SU", true),
            new EventInfo("weekly0", "weekly from 5/6/2008 on Tue 1pm to 2pm",
                    "2008-05-06T13:00:00", "2008-05-06T14:00:00",
                    "FREQ=WEEKLY;BYDAY=TU;WKST=MO", false),
            new EventInfo("weekly1", "every 2 weeks from 5/6/2008 on Tue from 2:30pm to 3:30pm",
                    "2008-05-06T14:30:00", "2008-05-06T15:30:00",
                    "FREQ=WEEKLY;INTERVAL=2;BYDAY=TU;WKST=MO", false),
            new EventInfo("monthly0", "monthly from 5/20/2008 on the 3rd Tues from 3pm to 4pm",
                    "2008-05-20T15:00:00", "2008-05-20T16:00:00",
                    "FREQ=MONTHLY;BYDAY=3TU;WKST=SU", false),
            new EventInfo("monthly1", "monthly from 5/1/2008 on the 1st from 12:00am to 12:10am",
                    "2008-05-01T00:00:00", "2008-05-01T00:10:00",
                    "FREQ=MONTHLY;WKST=SU;BYMONTHDAY=1", false),
            new EventInfo("monthly2", "monthly from 5/31/2008 on the 31st 11pm to midnight",
                    "2008-05-31T23:00:00", "2008-06-01T00:00:00",
                    "FREQ=MONTHLY;WKST=SU;BYMONTHDAY=31", false),
            new EventInfo("daily0", "2008-05-01T00:00:00",
                    "except0", "daily0 exception for 5/1/2008 12am, change to 5/1/2008 2am to 3am",
                    "2008-05-01T02:00:00", "2008-05-01T01:03:00", false),
            new EventInfo("daily0", "2008-05-03T00:00:00",
                    "except1", "daily0 exception for 5/3/2008 12am, change to 5/3/2008 2am to 3am",
                    "2008-05-03T02:00:00", "2008-05-03T01:03:00", false),
            new EventInfo("daily0", "2008-05-02T00:00:00",
                    "except2", "daily0 exception for 5/2/2008 12am, change to 1/2/2008",
                    "2008-01-02T00:00:00", "2008-01-02T01:00:00", false),
            new EventInfo("weekly0", "2008-05-13T13:00:00",
                    "except3", "daily0 exception for 5/11/2008 1pm, change to 12/11/2008 1pm",
                    "2008-12-11T13:00:00", "2008-12-11T14:00:00", false),
            new EventInfo("weekly0", "2008-05-13T13:00:00",
                    "cancel0", "weekly0 exception for 5/13/2008 1pm",
                    "2008-05-13T13:00:00", "2008-05-13T14:00:00", false),
            new EventInfo("yearly0", "yearly on 5/1/2008 from 1pm to 2pm",
                    "2008-05-01T13:00:00", "2008-05-01T14:00:00",
                    "FREQ=YEARLY;WKST=SU", false),
    };
    private InstanceInfo[] mInstanceRanges = {
            new InstanceInfo("daily0", "2008-05-01T00:00:00", "2008-05-01T00:01:00", 1),
            new InstanceInfo("daily0", "2008-05-01T00:00:00", "2008-05-01T01:00:00", 1),
            new InstanceInfo("daily0", "2008-05-01T00:00:00", "2008-05-02T00:00:00", 2),
            new InstanceInfo("daily0", "2008-05-01T00:00:00", "2008-05-02T23:59:00", 2),
            new InstanceInfo("daily0", "2008-05-02T00:00:00", "2008-05-02T00:01:00", 1),
            new InstanceInfo("daily0", "2008-05-02T00:00:00", "2008-05-02T01:00:00", 1),
            new InstanceInfo("daily0", "2008-05-02T00:00:00", "2008-05-03T00:00:00", 2),
            new InstanceInfo("daily0", "2008-05-01T00:00:00", "2008-05-31T23:59:00", 31),
            new InstanceInfo("daily0", "2008-05-01T00:00:00", "2008-06-01T23:59:00", 32),
            new InstanceInfo("daily1", "2008-05-01T00:00:00", "2008-05-02T00:00:00", 1),
            new InstanceInfo("daily1", "2008-05-01T00:00:00", "2008-05-31T23:59:00", 2),
            new InstanceInfo("daily2", "2008-05-01T00:00:00", "2008-05-02T00:00:00", 1),
            new InstanceInfo("daily2", "2008-05-01T00:00:00", "2008-05-31T23:59:00", 3),
            new InstanceInfo("allday daily0", "2008-05-01", "2008-05-07", 7),
            new InstanceInfo("allday daily1", "2008-05-01", "2008-05-07", 3),
            new InstanceInfo("allday weekly0", "2008-05-01", "2008-05-07", 1),
            new InstanceInfo("allday weekly0", "2008-05-01", "2008-05-08", 2),
            new InstanceInfo("allday weekly0", "2008-05-01", "2008-05-31", 5),
            new InstanceInfo("allday weekly1", "2008-05-01", "2008-05-31", 5),
            new InstanceInfo("allday yearly0", "2008-05-01", "2009-04-30", 1),
            new InstanceInfo("allday yearly0", "2008-05-01", "2009-05-02", 2),
            new InstanceInfo("weekly0", "2008-05-01T00:00:00", "2008-05-02T00:00:00", 0),
            new InstanceInfo("weekly0", "2008-05-06T00:00:00", "2008-05-07T00:00:00", 1),
            new InstanceInfo("weekly0", "2008-05-01T00:00:00", "2008-05-31T00:00:00", 4),
            new InstanceInfo("weekly0", "2008-05-01T00:00:00", "2008-06-30T00:00:00", 8),
            new InstanceInfo("weekly1", "2008-05-01T00:00:00", "2008-05-02T00:00:00", 0),
            new InstanceInfo("weekly1", "2008-05-06T00:00:00", "2008-05-07T00:00:00", 1),
            new InstanceInfo("weekly1", "2008-05-01T00:00:00", "2008-05-31T00:00:00", 2),
            new InstanceInfo("weekly1", "2008-05-01T00:00:00", "2008-06-30T00:00:00", 4),
            new InstanceInfo("monthly0", "2008-05-01T00:00:00", "2008-05-20T13:00:00", 0),
            new InstanceInfo("monthly0", "2008-05-01T00:00:00", "2008-05-20T15:00:00", 1),
            new InstanceInfo("monthly0", "2008-05-20T16:01:00", "2008-05-31T00:00:00", 0),
            new InstanceInfo("monthly0", "2008-05-20T16:01:00", "2008-06-17T14:59:00", 0),
            new InstanceInfo("monthly0", "2008-05-20T16:01:00", "2008-06-17T15:00:00", 1),
            new InstanceInfo("monthly0", "2008-05-01T00:00:00", "2008-05-31T00:00:00", 1),
            new InstanceInfo("monthly0", "2008-05-01T00:00:00", "2008-06-30T00:00:00", 2),
            new InstanceInfo("monthly1", "2008-05-01T00:00:00", "2008-05-01T01:00:00", 1),
            new InstanceInfo("monthly1", "2008-05-01T00:00:00", "2008-05-31T00:00:00", 1),
            new InstanceInfo("monthly1", "2008-05-01T00:10:00", "2008-05-31T23:59:00", 1),
            new InstanceInfo("monthly1", "2008-05-01T00:11:00", "2008-05-31T23:59:00", 0),
            new InstanceInfo("monthly1", "2008-05-01T00:00:00", "2008-06-01T00:00:00", 2),
            new InstanceInfo("monthly2", "2008-05-01T00:00:00", "2008-05-31T00:00:00", 0),
            new InstanceInfo("monthly2", "2008-05-01T00:10:00", "2008-05-31T23:00:00", 1),
            new InstanceInfo("monthly2", "2008-05-01T00:00:00", "2008-07-01T00:00:00", 1),
            new InstanceInfo("monthly2", "2008-05-01T00:00:00", "2008-08-01T00:00:00", 2),
            new InstanceInfo("yearly0", "2008-05-01", "2009-04-30", 1),
            new InstanceInfo("yearly0", "2008-05-01", "2009-05-02", 2),
    };
    private Command[] mNormalInsertDelete = {
            new Insert("normal0"),
            new Insert("normal1"),
            new Insert("normal2"),
            new QueryNumInstances("2008-05-01T00:00:00", "2008-05-31T00:01:00", 3),
            new Delete("normal1", 1),
            new QueryNumEvents(2),
            new QueryNumInstances("2008-05-01T00:00:00", "2008-05-31T00:01:00", 2),
            new Delete("normal1", 0),
            new Delete("normal2", 1),
            new QueryNumEvents(1),
            new Delete("normal0", 1),
            new QueryNumEvents(0),
    };
    private Command[] mAlldayInsertDelete = {
            new Insert("allday0"),
            new Insert("allday1"),
            new QueryNumEvents(2),
            new QueryNumInstances("2008-05-01T00:00:00", "2008-05-01T00:01:00", 0),
            new QueryNumInstances("2008-05-02T00:00:00", "2008-05-02T00:01:00", 2),
            new QueryNumInstances("2008-05-03T00:00:00", "2008-05-03T00:01:00", 1),
            new Delete("allday0", 1),
            new QueryNumEvents(1),
            new QueryNumInstances("2008-05-02T00:00:00", "2008-05-02T00:01:00", 1),
            new QueryNumInstances("2008-05-03T00:00:00", "2008-05-03T00:01:00", 1),
            new Delete("allday1", 1),
            new QueryNumEvents(0),
    };
    private Command[] mRecurringInsertDelete = {
            new Insert("daily0"),
            new Insert("daily1"),
            new QueryNumEvents(2),
            new QueryNumInstances("2008-05-01T00:00:00", "2008-05-02T00:01:00", 3),
            new QueryNumInstances("2008-05-01T01:01:00", "2008-05-02T00:01:00", 2),
            new QueryNumInstances("2008-05-01T00:00:00", "2008-05-04T00:01:00", 6),
            new Delete("daily1", 1),
            new QueryNumEvents(1),
            new QueryNumInstances("2008-05-01T00:00:00", "2008-05-02T00:01:00", 2),
            new QueryNumInstances("2008-05-01T00:00:00", "2008-05-04T00:01:00", 4),
            new Delete("daily0", 1),
            new QueryNumEvents(0),
    };
    private Command[] mExceptionWithMovedRecurrence = {
            new Insert("daily0"),
            new VerifyAllInstances("2008-05-01T00:00:00", "2008-05-03T00:01:00",
                    new String[] {"2008-05-01T00:00:00", "2008-05-02T00:00:00",
                            "2008-05-03T00:00:00", }),
            new Insert("except2"),
            new VerifyAllInstances("2008-05-01T00:00:00", "2008-05-03T00:01:00",
                    new String[] {"2008-05-01T00:00:00", "2008-05-03T00:00:00"}),
    };
    private Command[] mCancelInstance = {
            new Insert("weekly0"),
            new VerifyAllInstances("2008-05-01T00:00:00", "2008-05-22T00:01:00",
                    new String[] {"2008-05-06T13:00:00", "2008-05-13T13:00:00",
                            "2008-05-20T13:00:00", }),
            new Insert("cancel0"),
            new Update("cancel0", new KeyValue[] {
                    new KeyValue(Calendar.EventsColumns.STATUS,
                            "" + Calendar.EventsColumns.STATUS_CANCELED),
            }),
            new VerifyAllInstances("2008-05-01T00:00:00", "2008-05-22T00:01:00",
                    new String[] {"2008-05-06T13:00:00",
                            "2008-05-20T13:00:00", }),
    };
    private Command[] mExceptionWithMovedRecurrence2 = {
            new Insert("weekly0"),
            new VerifyAllInstances("2008-12-01T00:00:00", "2008-12-22T00:01:00",
                    new String[] {"2008-12-02T13:00:00", "2008-12-09T13:00:00",
                            "2008-12-16T13:00:00", }),
            new Insert("except3"),
            new VerifyAllInstances("2008-12-01T00:00:00", "2008-12-22T00:01:00",
                    new String[] {"2008-12-02T13:00:00", "2008-12-09T13:00:00",
                            "2008-12-11T13:00:00", "2008-12-16T13:00:00", }),
    };
    private Command[]
            mExceptionWithTruncatedRecurrence = {
            new Insert("daily0"),
            new VerifyAllInstances("2008-05-01T00:00:00", "2008-05-04T00:01:00",
                    new String[] {"2008-05-01T00:00:00", "2008-05-02T00:00:00",
                            "2008-05-03T00:00:00", "2008-05-04T00:00:00"}),
            new Insert("except1"),
            new QueryNumEvents(2),
            new VerifyAllInstances("2008-05-01T00:00:00", "2008-05-04T00:01:00",
                    new String[] {"2008-05-01T00:00:00", "2008-05-02T00:00:00",
                            "2008-05-03T02:00:00", "2008-05-04T00:00:00"}),
            new Update("daily0", new KeyValue[] {
                    new KeyValue(Events.RRULE, "FREQ=DAILY;UNTIL=20080505T150000Z;WKST=SU"),
            }),
            new VerifyAllInstances("2008-05-01T00:00:00", "2008-05-04T00:01:00",
                    new String[] {"2008-05-01T00:00:00", "2008-05-02T00:00:00",
                            "2008-05-03T02:00:00", "2008-05-04T00:00:00"}),
            new Update("daily0", new KeyValue[] {
                    new KeyValue(Events.RRULE, "FREQ=DAILY;UNTIL=20080502T150000Z;WKST=SU"),
            }),
            new Update("except1", new KeyValue[] {
                    new KeyValue(Calendar.EventsColumns.STATUS, "" + Calendar.EventsColumns.STATUS_CANCELED),
            }),
            new VerifyAllInstances("2008-05-01T00:00:00", "2008-05-04T00:01:00",
                    new String[] {"2008-05-01T00:00:00", "2008-05-02T00:00:00"}),
    };
    private Command[] mExceptionWithNoRecurrence = {
            new Insert("except0"),
            new QueryNumEvents(1),
            new VerifyAllInstances("2008-05-01T00:00:00", "2008-05-03T00:01:00",
                    new String[] {"2008-05-01T02:00:00"}),
    };
    private EventInfo findEvent(String name) {
        int len = mEvents.length;
        for (int ii = 0; ii < len; ii++) {
            EventInfo event = mEvents[ii];
            if (name.equals(event.mTitle)) {
                return event;
            }
        }
        return null;
    }
    public CalendarProvider2Test() {
        super(CalendarProvider2ForTesting.class, Calendar.AUTHORITY);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getMockContext();
        mResolver = getMockContentResolver();
        mResolver.addProvider("subscribedfeeds", new MockProvider("subscribedfeeds"));
        mResolver.addProvider("sync", new MockProvider("sync"));
        CalendarDatabaseHelper helper = (CalendarDatabaseHelper) getProvider().getDatabaseHelper();
        mDb = helper.getWritableDatabase();
        wipeData(mDb);
        mMetaData = getProvider().mMetaData;
        mForceDtend = false;
    }
    public void wipeData(SQLiteDatabase db) {
        db.execSQL("DELETE FROM Calendars;");
        db.execSQL("DELETE FROM Events;");
        db.execSQL("DELETE FROM EventsRawTimes;");
        db.execSQL("DELETE FROM Instances;");
        db.execSQL("DELETE FROM CalendarMetaData;");
        db.execSQL("DELETE FROM CalendarCache;");
        db.execSQL("DELETE FROM Attendees;");
        db.execSQL("DELETE FROM Reminders;");
        db.execSQL("DELETE FROM CalendarAlerts;");
        db.execSQL("DELETE FROM ExtendedProperties;");
    }
    @Override
    protected void tearDown() throws Exception {
        mDb.close();
        mDb = null;
        getProvider().getDatabaseHelper().close();
        super.tearDown();
    }
    private void dumpCursor(Cursor cursor) {
        cursor.moveToPosition(-1);
        String[] cols = cursor.getColumnNames();
        Log.i(TAG, "dumpCursor() count: " + cursor.getCount());
        int index = 0;
        while (cursor.moveToNext()) {
            Log.i(TAG, index + " {");
            for (int i = 0; i < cols.length; i++) {
                Log.i(TAG, "    " + cols[i] + '=' + cursor.getString(i));
            }
            Log.i(TAG, "}");
            index += 1;
        }
        cursor.moveToPosition(-1);
    }
    private int insertCal(String name, String timezone) {
        return insertCal(name, timezone, "joe@joe.com");
    }
    private int insertCal(String name, String timezone, String account) {
        ContentValues m = new ContentValues();
        m.put(Calendars.NAME, name);
        m.put(Calendars.DISPLAY_NAME, name);
        m.put(Calendars.COLOR, "0xff123456");
        m.put(Calendars.TIMEZONE, timezone);
        m.put(Calendars.SELECTED, 1);
        m.put(Calendars.URL, CALENDAR_URL);
        m.put(Calendars.OWNER_ACCOUNT, account);
        m.put(Calendars._SYNC_ACCOUNT,  account);
        m.put(Calendars._SYNC_ACCOUNT_TYPE,  "com.google");
        m.put(Calendars.SYNC_EVENTS,  1);
        Uri url = mResolver.insert(Calendar.Calendars.CONTENT_URI, m);
        String id = url.getLastPathSegment();
        return Integer.parseInt(id);
    }
    private Uri insertEvent(int calId, EventInfo event) {
        if (mWipe) {
            mMetaData.clearInstanceRange();
        }
        ContentValues m = new ContentValues();
        m.put(Events.CALENDAR_ID, calId);
        m.put(Events.TITLE, event.mTitle);
        m.put(Events.DTSTART, event.mDtstart);
        m.put(Events.ALL_DAY, event.mAllDay ? 1 : 0);
        if (event.mRrule == null || mForceDtend) {
            m.put(Events.DTEND, event.mDtend);
        }
        if (event.mRrule != null) {
            m.put(Events.RRULE, event.mRrule);
            m.put(Events.DURATION, event.mDuration);
        }
        if (event.mDescription != null) {
            m.put(Events.DESCRIPTION, event.mDescription);
        }
        if (event.mTimezone != null) {
            m.put(Events.EVENT_TIMEZONE, event.mTimezone);
        }
        if (event.mOriginalTitle != null) {
            EventInfo recur = findEvent(event.mOriginalTitle);
            assertNotNull(recur);
            String syncId = String.format("%d", recur.mSyncId);
            m.put(Events.ORIGINAL_EVENT, syncId);
            m.put(Events.ORIGINAL_ALL_DAY, recur.mAllDay ? 1 : 0);
            m.put(Events.ORIGINAL_INSTANCE_TIME, event.mOriginalInstance);
        }
        Uri url = mResolver.insert(mEventsUri, m);
        long id = ContentUris.parseId(url);
        mDb.execSQL("UPDATE Events SET _sync_id=" + mGlobalSyncId + " WHERE _id=" + id);
        event.mSyncId = mGlobalSyncId;
        mGlobalSyncId += 1;
        return url;
    }
    private int deleteMatchingEvents(String title) {
        Cursor cursor = mResolver.query(mEventsUri, new String[] { Events._ID },
                "title=?", new String[] { title }, null);
        int numRows = 0;
        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            Uri uri = updatedUri(ContentUris.withAppendedId(Events.CONTENT_URI, id), true);
            numRows += mResolver.delete(uri, null, null);
        }
        cursor.close();
        return numRows;
    }
    private int updateMatchingEvents(String title, ContentValues values) {
        String[] projection = new String[] {
                Events._ID,
                Events.DTSTART,
                Events.DTEND,
                Events.DURATION,
                Events.ALL_DAY,
                Events.RRULE,
                Events.EVENT_TIMEZONE,
                Events.ORIGINAL_EVENT,
        };
        Cursor cursor = mResolver.query(mEventsUri, projection,
                "title=?", new String[] { title }, null);
        int numRows = 0;
        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            if (values.containsKey(Events.DTSTART) || values.containsKey(Events.DTEND)
                    || values.containsKey(Events.DURATION) || values.containsKey(Events.ALL_DAY)
                    || values.containsKey(Events.RRULE)
                    || values.containsKey(Events.EVENT_TIMEZONE)
                    || values.containsKey(Calendar.EventsColumns.STATUS)) {
                long dtstart = cursor.getLong(1);
                long dtend = cursor.getLong(2);
                String duration = cursor.getString(3);
                boolean allDay = cursor.getInt(4) != 0;
                String rrule = cursor.getString(5);
                String timezone = cursor.getString(6);
                String originalEvent = cursor.getString(7);
                if (!values.containsKey(Events.DTSTART)) {
                    values.put(Events.DTSTART, dtstart);
                }
                if (!values.containsKey(Events.DTEND) && rrule == null) {
                    values.put(Events.DTEND, dtend);
                }
                if (!values.containsKey(Events.DURATION) && duration != null) {
                    values.put(Events.DURATION, duration);
                }
                if (!values.containsKey(Events.ALL_DAY)) {
                    values.put(Events.ALL_DAY, allDay ? 1 : 0);
                }
                if (!values.containsKey(Events.RRULE) && rrule != null) {
                    values.put(Events.RRULE, rrule);
                }
                if (!values.containsKey(Events.EVENT_TIMEZONE) && timezone != null) {
                    values.put(Events.EVENT_TIMEZONE, timezone);
                }
                if (!values.containsKey(Events.ORIGINAL_EVENT) && originalEvent != null) {
                    values.put(Events.ORIGINAL_EVENT, originalEvent);
                }
            }
            Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, id);
            numRows += mResolver.update(uri, values, null, null);
        }
        cursor.close();
        return numRows;
    }
    private void deleteAllEvents() {
        mDb.execSQL("DELETE FROM Events;");
        mMetaData.clearInstanceRange();
    }
    public void testInsertNormalEvents() throws Exception {
        Cursor cursor;
        Uri url = null;
        int calId = insertCal("Calendar0", DEFAULT_TIMEZONE);
        cursor = mResolver.query(mEventsUri, null, null, null, null);
        assertEquals(0, cursor.getCount());
        cursor.close();
        int numEvents = 0;
        long begin = 0, end = 0;
        int len = mEvents.length;
        for (int ii = 0; ii < len; ii++) {
            EventInfo event = mEvents[ii];
            if (event.mRrule != null || event.mOriginalTitle != null) {
                continue;
            }
            if (numEvents == 0) {
                begin = event.mDtstart;
                end = event.mDtend;
            } else {
                if (begin > event.mDtstart) {
                    begin = event.mDtstart;
                }
                if (end < event.mDtend) {
                    end = event.mDtend;
                }
            }
            url = insertEvent(calId, event);
            numEvents += 1;
        }
        cursor = mResolver.query(url, null, null, null, null);
        assertEquals(1, cursor.getCount());
        cursor.close();
        cursor = mResolver.query(mEventsUri, null, null, null, null);
        assertEquals(numEvents, cursor.getCount());
        cursor.close();
        cursor = queryInstances(begin, end);
        assertEquals(numEvents, cursor.getCount());
        cursor.close();
    }
    public void testInsertRepeatingEvents() throws Exception {
        Cursor cursor;
        Uri url = null;
        int calId = insertCal("Calendar0", "America/Los_Angeles");
        cursor = mResolver.query(mEventsUri, null, null, null, null);
        assertEquals(0, cursor.getCount());
        cursor.close();
        int numEvents = 0;
        int len = mEvents.length;
        for (int ii = 0; ii < len; ii++) {
            EventInfo event = mEvents[ii];
            if (event.mRrule == null) {
                continue;
            }
            url = insertEvent(calId, event);
            numEvents += 1;
        }
        cursor = mResolver.query(url, null, null, null, null);
        assertEquals(1, cursor.getCount());
        cursor.close();
        cursor = mResolver.query(mEventsUri, null, null, null, null);
        assertEquals(numEvents, cursor.getCount());
        cursor.close();
    }
    public void testInstanceRangeDtend() throws Exception {
        mForceDtend = true;
        testInstanceRange();
    }
    public void testInstanceRange() throws Exception {
        Cursor cursor;
        Uri url = null;
        int calId = insertCal("Calendar0", "America/Los_Angeles");
        cursor = mResolver.query(mEventsUri, null, null, null, null);
        assertEquals(0, cursor.getCount());
        cursor.close();
        int len = mInstanceRanges.length;
        for (int ii = 0; ii < len; ii++) {
            InstanceInfo instance = mInstanceRanges[ii];
            EventInfo event = instance.mEvent;
            url = insertEvent(calId, event);
            cursor = queryInstances(instance.mBegin, instance.mEnd);
            if (instance.mExpectedOccurrences != cursor.getCount()) {
                Log.e(TAG, "Test failed! Instance index: " + ii);
                Log.e(TAG, "title: " + event.mTitle + " desc: " + event.mDescription
                        + " [begin,end]: [" + instance.mBegin + " " + instance.mEnd + "]"
                        + " expected: " + instance.mExpectedOccurrences);
                dumpCursor(cursor);
            }
            assertEquals(instance.mExpectedOccurrences, cursor.getCount());
            cursor.close();
            int rows = mResolver.delete(updatedUri(url, true),
                    null , null );
            assertEquals(1, rows);
        }
    }
    public void testEntityQuery() throws Exception {
        testInsertNormalEvents(); 
        ContentValues reminder = new ContentValues();
        reminder.put(Calendar.Reminders.EVENT_ID, 1);
        reminder.put(Calendar.Reminders.MINUTES, 10);
        reminder.put(Calendar.Reminders.METHOD, Calendar.Reminders.METHOD_SMS);
        mResolver.insert(Calendar.Reminders.CONTENT_URI, reminder);
        reminder.put(Calendar.Reminders.MINUTES, 20);
        mResolver.insert(Calendar.Reminders.CONTENT_URI, reminder);
        ContentValues extended = new ContentValues();
        extended.put(Calendar.ExtendedProperties.NAME, "foo");
        extended.put(Calendar.ExtendedProperties.VALUE, "bar");
        extended.put(Calendar.ExtendedProperties.EVENT_ID, 2);
        mResolver.insert(Calendar.ExtendedProperties.CONTENT_URI, extended);
        extended.put(Calendar.ExtendedProperties.EVENT_ID, 1);
        mResolver.insert(Calendar.ExtendedProperties.CONTENT_URI, extended);
        extended.put(Calendar.ExtendedProperties.NAME, "foo2");
        extended.put(Calendar.ExtendedProperties.VALUE, "bar2");
        mResolver.insert(Calendar.ExtendedProperties.CONTENT_URI, extended);
        ContentValues attendee = new ContentValues();
        attendee.put(Calendar.Attendees.ATTENDEE_NAME, "Joe");
        attendee.put(Calendar.Attendees.ATTENDEE_EMAIL, "joe@joe.com");
        attendee.put(Calendar.Attendees.ATTENDEE_STATUS,
                Calendar.Attendees.ATTENDEE_STATUS_DECLINED);
        attendee.put(Calendar.Attendees.ATTENDEE_TYPE, Calendar.Attendees.TYPE_REQUIRED);
        attendee.put(Calendar.Attendees.ATTENDEE_RELATIONSHIP,
                Calendar.Attendees.RELATIONSHIP_PERFORMER);
        attendee.put(Calendar.Attendees.EVENT_ID, 3);
        mResolver.insert(Calendar.Attendees.CONTENT_URI, attendee);
        EntityIterator ei = EventsEntity.newEntityIterator(
                mResolver.query(EventsEntity.CONTENT_URI, null, null, null, null), mResolver);
        int count = 0;
        try {
            while (ei.hasNext()) {
                Entity entity = ei.next();
                ContentValues values = entity.getEntityValues();
                assertEquals(CALENDAR_URL, values.getAsString(Calendars.URL));
                ArrayList<Entity.NamedContentValues> subvalues = entity.getSubValues();
                switch (values.getAsInteger("_id")) {
                    case 1:
                        assertEquals(4, subvalues.size()); 
                        break;
                    case 2:
                        assertEquals(1, subvalues.size()); 
                        ContentValues subContentValues = subvalues.get(0).values;
                        String name = subContentValues.getAsString(
                                Calendar.ExtendedProperties.NAME);
                        String value = subContentValues.getAsString(
                                Calendar.ExtendedProperties.VALUE);
                        assertEquals("foo", name);
                        assertEquals("bar", value);
                        break;
                    case 3:
                        assertEquals(1, subvalues.size()); 
                        break;
                    default:
                        assertEquals(0, subvalues.size());
                        break;
                }
                count += 1;
            }
            assertEquals(5, count);
        } finally {
            ei.close();
        }
        ei = EventsEntity.newEntityIterator(
                    mResolver.query(EventsEntity.CONTENT_URI, null, "_id = 3", null, null),
                mResolver);
        try {
            count = 0;
            while (ei.hasNext()) {
                Entity entity = ei.next();
                count += 1;
            }
            assertEquals(1, count);
        } finally {
            ei.close();
        }
    }
    public void testDeleteCalendar() throws Exception {
        int calendarId0 = insertCal("Calendar0", DEFAULT_TIMEZONE);
        int calendarId1 = insertCal("Calendar1", DEFAULT_TIMEZONE, "user2@google.com");
        insertEvent(calendarId0, mEvents[0]);
        insertEvent(calendarId1, mEvents[1]);
        testQueryCount(Calendar.Calendars.CONTENT_URI, null , 2);
        testQueryCount(Calendar.Events.CONTENT_URI, null , 2);
        int deletes = mResolver.delete(Calendar.Calendars.CONTENT_URI,
                "ownerAccount='user2@google.com'", null );
        assertEquals(1, deletes);
        testQueryCount(Calendar.Calendars.CONTENT_URI, null , 1);
        testQueryCount(Calendar.Events.CONTENT_URI, null , 1);
        deletes = mResolver.delete(Uri.withAppendedPath(Calendar.Calendars.CONTENT_URI,
                String.valueOf(calendarId0)),
                null  , null );
        assertEquals(1, deletes);
        testQueryCount(Calendar.Calendars.CONTENT_URI, null , 0);
        testQueryCount(Calendar.Events.CONTENT_URI, null , 0);
        deletes = mResolver.delete(Calendar.Calendars.CONTENT_URI,
                "ownerAccount=?", new String[] {"user2@google.com"} );
        assertEquals(0, deletes);
    }
    public void testCalendarAlerts() throws Exception {
        String[] projection = new String[] {
                Calendar.CalendarAlerts._ID,              
                Calendar.CalendarAlerts.TITLE,            
                Calendar.CalendarAlerts.EVENT_LOCATION,   
                Calendar.CalendarAlerts.ALL_DAY,          
                Calendar.CalendarAlerts.BEGIN,            
                Calendar.CalendarAlerts.END,              
                Calendar.CalendarAlerts.EVENT_ID,         
                Calendar.CalendarAlerts.COLOR,            
                Calendar.CalendarAlerts.RRULE,            
                Calendar.CalendarAlerts.HAS_ALARM,        
                Calendar.CalendarAlerts.STATE,            
                Calendar.CalendarAlerts.ALARM_TIME,       
        };
        testInsertNormalEvents(); 
        Uri alertUri = Calendar.CalendarAlerts.insert(mResolver, 1 ,
                2 , 3 , 4 , 5 );
        Calendar.CalendarAlerts.insert(mResolver, 1 ,
                2 , 7 , 8 , 9 );
        Cursor cursor = mResolver.query(Calendar.CalendarAlerts.CONTENT_URI, projection,
                null , null , null );
        assertEquals(2, cursor.getCount());
        cursor.close();
        cursor = mResolver.query(alertUri, projection,
                null , null , null );
        assertEquals(1, cursor.getCount());
        cursor.close();
        cursor = mResolver.query(Calendar.CalendarAlerts.CONTENT_URI_BY_INSTANCE, projection,
                null , null , null );
        assertEquals(1, cursor.getCount());
        cursor.close();
    }
    public void testAttendees() throws Exception {
        mCalendarId = insertCal("Calendar0", DEFAULT_TIMEZONE);
        Uri eventUri = insertEvent(mCalendarId, findEvent("daily0"));
        long eventId = ContentUris.parseId(eventUri);
        ContentValues attendee = new ContentValues();
        attendee.put(Calendar.Attendees.ATTENDEE_NAME, "Joe");
        attendee.put(Calendar.Attendees.ATTENDEE_EMAIL, "joe@joe.com");
        attendee.put(Calendar.Attendees.ATTENDEE_TYPE, Calendar.Attendees.TYPE_REQUIRED);
        attendee.put(Calendar.Attendees.ATTENDEE_RELATIONSHIP,
                Calendar.Attendees.RELATIONSHIP_ORGANIZER);
        attendee.put(Calendar.Attendees.EVENT_ID, eventId);
        Uri attendeesUri = mResolver.insert(Calendar.Attendees.CONTENT_URI, attendee);
        Cursor cursor = mResolver.query(Calendar.Attendees.CONTENT_URI, null,
                "event_id=" + eventId, null, null);
        assertEquals("Created event is missing", 1, cursor.getCount());
        cursor.close();
        cursor = mResolver.query(eventUri, null, null, null, null);
        assertEquals("Created event is missing", 1, cursor.getCount());
        int selfColumn = cursor.getColumnIndex(Calendar.Events.SELF_ATTENDEE_STATUS);
        cursor.moveToNext();
        long selfAttendeeStatus = cursor.getInt(selfColumn);
        assertEquals(Calendar.Attendees.ATTENDEE_STATUS_ACCEPTED, selfAttendeeStatus);
        cursor.close();
        attendee.put(Calendar.Attendees.ATTENDEE_STATUS,
                Calendar.Attendees.ATTENDEE_STATUS_DECLINED);
        mResolver.update(attendeesUri, attendee, null, null);
        cursor = mResolver.query(eventUri, null, null, null, null);
        cursor.moveToNext();
        selfAttendeeStatus = cursor.getInt(selfColumn);
        assertEquals(Calendar.Attendees.ATTENDEE_STATUS_DECLINED, selfAttendeeStatus);
        cursor.close();
        attendee.put(Calendar.Attendees.ATTENDEE_NAME, "Dude");
        attendee.put(Calendar.Attendees.ATTENDEE_EMAIL, "dude@dude.com");
        attendee.put(Calendar.Attendees.ATTENDEE_STATUS,
                Calendar.Attendees.ATTENDEE_STATUS_ACCEPTED);
        mResolver.insert(Calendar.Attendees.CONTENT_URI, attendee);
        cursor = mResolver.query(Calendar.Attendees.CONTENT_URI, null,
                "event_id=" + mCalendarId, null, null);
        assertEquals(2, cursor.getCount());
        cursor.close();
        cursor = mResolver.query(eventUri, null, null, null, null);
        cursor.moveToNext();
        selfAttendeeStatus = cursor.getInt(selfColumn);
        assertEquals(Calendar.Attendees.ATTENDEE_STATUS_DECLINED, selfAttendeeStatus);
        cursor.close();
    }
    private void testAndClearDirty(long eventId, int wanted) {
        Cursor cursor = mResolver.query(
                ContentUris.withAppendedId(Calendar.Events.CONTENT_URI, eventId),
                null, null, null, null);
        try {
            assertEquals("Event count", 1, cursor.getCount());
            cursor.moveToNext();
            int dirty = cursor.getInt(cursor.getColumnIndex(Calendar.Events._SYNC_DIRTY));
            assertEquals("dirty flag", wanted, dirty);
            if (dirty == 1) {
                mDb.execSQL("UPDATE Events SET _sync_dirty=0 WHERE _id=" + eventId);
            }
        } finally {
            cursor.close();
        }
    }
    private void testQueryCount(Uri uri, String where, int wanted) {
        Cursor cursor = mResolver.query(uri, null, where, null ,
                null );
        try {
            assertEquals("query results", wanted, cursor.getCount());
        } finally {
            cursor.close();
        }
    }
    public void testDirty() throws Exception {
        internalTestDirty(false);
    }
    public void testDirtyWithSyncAdapter() throws Exception {
        internalTestDirty(true);
    }
    private Uri updatedUri(Uri uri, boolean syncAdapter) {
        if (syncAdapter) {
            return uri.buildUpon().appendQueryParameter(Calendar.CALLER_IS_SYNCADAPTER, "true")
                    .build();
        } else {
            return uri;
        }
    }
    private void internalTestDirty(boolean syncAdapter) throws Exception {
        mCalendarId = insertCal("Calendar0", DEFAULT_TIMEZONE);
        Uri eventUri = insertEvent(mCalendarId, findEvent("daily0"));
        long eventId = ContentUris.parseId(eventUri);
        testAndClearDirty(eventId, 1);
        ContentValues attendee = new ContentValues();
        attendee.put(Calendar.Attendees.ATTENDEE_NAME, "Joe");
        attendee.put(Calendar.Attendees.ATTENDEE_EMAIL, "joe@joe.com");
        attendee.put(Calendar.Attendees.ATTENDEE_TYPE, Calendar.Attendees.TYPE_REQUIRED);
        attendee.put(Calendar.Attendees.ATTENDEE_RELATIONSHIP,
                Calendar.Attendees.RELATIONSHIP_ORGANIZER);
        attendee.put(Calendar.Attendees.EVENT_ID, eventId);
        Uri attendeeUri = mResolver.insert(
                updatedUri(Calendar.Attendees.CONTENT_URI, syncAdapter),
                attendee);
        testAndClearDirty(eventId, syncAdapter ? 0 : 1);
        testQueryCount(Calendar.Attendees.CONTENT_URI, "event_id=" + eventId, 1);
        ContentValues reminder = new ContentValues();
        reminder.put(Calendar.Reminders.MINUTES, 10);
        reminder.put(Calendar.Reminders.METHOD, Calendar.Reminders.METHOD_EMAIL);
        reminder.put(Calendar.Attendees.EVENT_ID, eventId);
        Uri reminderUri = mResolver.insert(
                updatedUri(Calendar.Reminders.CONTENT_URI, syncAdapter), reminder);
        testAndClearDirty(eventId, syncAdapter ? 0 : 1);
        testQueryCount(Calendar.Reminders.CONTENT_URI, "event_id=" + eventId, 1);
        ContentValues alert = new ContentValues();
        alert.put(Calendar.CalendarAlerts.BEGIN, 10);
        alert.put(Calendar.CalendarAlerts.END, 20);
        alert.put(Calendar.CalendarAlerts.ALARM_TIME, 30);
        alert.put(Calendar.CalendarAlerts.CREATION_TIME, 40);
        alert.put(Calendar.CalendarAlerts.RECEIVED_TIME, 50);
        alert.put(Calendar.CalendarAlerts.NOTIFY_TIME, 60);
        alert.put(Calendar.CalendarAlerts.STATE, Calendar.CalendarAlerts.SCHEDULED);
        alert.put(Calendar.CalendarAlerts.MINUTES, 30);
        alert.put(Calendar.CalendarAlerts.EVENT_ID, eventId);
        Uri alertUri = mResolver.insert(
                updatedUri(Calendar.CalendarAlerts.CONTENT_URI, syncAdapter), alert);
        testAndClearDirty(eventId, 0);
        testQueryCount(Calendar.CalendarAlerts.CONTENT_URI, "event_id=" + eventId, 1);
        ContentValues extended = new ContentValues();
        extended.put(Calendar.ExtendedProperties.NAME, "foo");
        extended.put(Calendar.ExtendedProperties.VALUE, "bar");
        extended.put(Calendar.ExtendedProperties.EVENT_ID, eventId);
        Uri extendedUri = mResolver.insert(
                updatedUri(Calendar.ExtendedProperties.CONTENT_URI, syncAdapter), extended);
        testAndClearDirty(eventId, syncAdapter ? 0 : 1);
        testQueryCount(Calendar.ExtendedProperties.CONTENT_URI, "event_id=" + eventId, 1);
        attendee = new ContentValues();
        attendee.put(Calendar.Attendees.ATTENDEE_NAME, "Sam");
        attendee.put(Calendar.Attendees.EVENT_ID, eventId);
        assertEquals("update", 1, mResolver.update(updatedUri(attendeeUri, syncAdapter), attendee,
                null , null ));
        testAndClearDirty(eventId, syncAdapter ? 0 : 1);
        testQueryCount(Calendar.Attendees.CONTENT_URI, "event_id=" + eventId, 1);
        reminder = new ContentValues();
        reminder.put(Calendar.Reminders.MINUTES, 20);
        assertEquals("update", 1, mResolver.update(updatedUri(reminderUri, syncAdapter), reminder,
                null , null ));
        testAndClearDirty(eventId, syncAdapter ? 0 : 1);
        testQueryCount(Calendar.Reminders.CONTENT_URI, "event_id=" + eventId, 1);
        alert = new ContentValues();
        alert.put(Calendar.CalendarAlerts.STATE, Calendar.CalendarAlerts.DISMISSED);
        assertEquals("update", 1, mResolver.update(updatedUri(alertUri, syncAdapter), alert,
                null , null ));
        testAndClearDirty(eventId, 0);
        testQueryCount(Calendar.CalendarAlerts.CONTENT_URI, "event_id=" + eventId, 1);
        extended = new ContentValues();
        extended.put(Calendar.ExtendedProperties.VALUE, "baz");
        assertEquals("update", 1, mResolver.update(updatedUri(extendedUri, syncAdapter), extended,
                null , null ));
        testAndClearDirty(eventId, syncAdapter ? 0 : 1);
        testQueryCount(Calendar.ExtendedProperties.CONTENT_URI, "event_id=" + eventId, 1);
        assertEquals("delete", 1, mResolver.delete(
                updatedUri(attendeeUri, syncAdapter),
                null, null ));
        testAndClearDirty(eventId, syncAdapter ? 0 : 1);
        testQueryCount(Calendar.Attendees.CONTENT_URI, "event_id=" + eventId, 0);
        assertEquals("delete", 1, mResolver.delete(updatedUri(reminderUri, syncAdapter),
                null , null ));
        testAndClearDirty(eventId, syncAdapter ? 0 : 1);
        testQueryCount(Calendar.Reminders.CONTENT_URI, "event_id=" + eventId, 0);
        assertEquals("delete", 1, mResolver.delete(updatedUri(alertUri, syncAdapter),
                null , null ));
        testAndClearDirty(eventId, 0);
        testQueryCount(Calendar.CalendarAlerts.CONTENT_URI, "event_id=" + eventId, 0);
        assertEquals("delete", 1, mResolver.delete(updatedUri(extendedUri, syncAdapter),
                null , null ));
        testAndClearDirty(eventId, syncAdapter ? 0 : 1);
        testQueryCount(Calendar.ExtendedProperties.CONTENT_URI, "event_id=" + eventId, 0);
    }
    public void testCalendarDeletion() throws Exception {
        mCalendarId = insertCal("Calendar0", DEFAULT_TIMEZONE);
        Uri eventUri = insertEvent(mCalendarId, findEvent("daily0"));
        long eventId = ContentUris.parseId(eventUri);
        testAndClearDirty(eventId, 1);
        Uri eventUri1 = insertEvent(mCalendarId, findEvent("daily1"));
        long eventId1 = ContentUris.parseId(eventUri);
        assertEquals("delete", 1, mResolver.delete(eventUri1, null, null));
        testQueryCount(Calendar.Events.CONTENT_URI, null, 2);
        assertEquals("delete", 1, mResolver.delete(Calendar.Calendars.CONTENT_URI,
                "_id=" + mCalendarId, null));
        testQueryCount(Calendar.Calendars.CONTENT_URI, null, 0);
        testQueryCount(Calendar.Events.CONTENT_URI, null, 0);
    }
    public void testMultipleAccounts() throws Exception {
        mCalendarId = insertCal("Calendar0", DEFAULT_TIMEZONE);
        int calendarId1 = insertCal("Calendar1", DEFAULT_TIMEZONE, "user2@google.com");
        Uri eventUri0 = insertEvent(mCalendarId, findEvent("daily0"));
        Uri eventUri1 = insertEvent(calendarId1, findEvent("daily1"));
        testQueryCount(Calendar.Events.CONTENT_URI, null, 2);
        Uri eventsWithAccount = Calendar.Events.CONTENT_URI.buildUpon()
                .appendQueryParameter(Calendar.EventsEntity.ACCOUNT_NAME, "joe@joe.com")
                .appendQueryParameter(Calendar.EventsEntity.ACCOUNT_TYPE, "com.google")
                .build();
        testQueryCount(eventsWithAccount, null, 1);
        long eventId = ContentUris.parseId(eventUri1);
        assertEquals("delete", 0, mResolver.delete(
                updatedUri(eventsWithAccount, true ),
                "_id=" + eventId, null ));
        testQueryCount(Calendar.Events.CONTENT_URI, null, 2);
        assertEquals("delete", 1, mResolver.delete(
                updatedUri(Calendar.Events.CONTENT_URI, true ),
                "_id=" + eventId, null ));
        testQueryCount(Calendar.Events.CONTENT_URI, null, 1);
    }
    public void testCommandSequences1() throws Exception {
        commandSequences(true);
    }
    public void testCommandSequences2() throws Exception {
        commandSequences(false);
    }
    private void commandSequences(boolean wipe) throws Exception {
        Cursor cursor;
        Uri url = null;
        mWipe = wipe; 
        mCalendarId = insertCal("Calendar0", DEFAULT_TIMEZONE);
        cursor = mResolver.query(mEventsUri, null, null, null, null);
        assertEquals(0, cursor.getCount());
        cursor.close();
        Command[] commands;
        Log.i(TAG, "Normal insert/delete");
        commands = mNormalInsertDelete;
        for (Command command : commands) {
            command.execute();
        }
        deleteAllEvents();
        Log.i(TAG, "All-day insert/delete");
        commands = mAlldayInsertDelete;
        for (Command command : commands) {
            command.execute();
        }
        deleteAllEvents();
        Log.i(TAG, "Recurring insert/delete");
        commands = mRecurringInsertDelete;
        for (Command command : commands) {
            command.execute();
        }
        deleteAllEvents();
        Log.i(TAG, "Exception with truncated recurrence");
        commands = mExceptionWithTruncatedRecurrence;
        for (Command command : commands) {
            command.execute();
        }
        deleteAllEvents();
        Log.i(TAG, "Exception with moved recurrence");
        commands = mExceptionWithMovedRecurrence;
        for (Command command : commands) {
            command.execute();
        }
        deleteAllEvents();
        Log.i(TAG, "Exception with cancel");
        commands = mCancelInstance;
        for (Command command : commands) {
            command.execute();
        }
        deleteAllEvents();
        Log.i(TAG, "Exception with moved recurrence2");
        commands = mExceptionWithMovedRecurrence2;
        for (Command command : commands) {
            command.execute();
        }
        deleteAllEvents();
        Log.i(TAG, "Exception with no recurrence");
        commands = mExceptionWithNoRecurrence;
        for (Command command : commands) {
            command.execute();
        }
    }
    @Suppress
    public void testTimeToString() throws Exception {
        Time time = new Time(Time.TIMEZONE_UTC);
        String str = "2039-01-01T23:00:00.000Z";
        String result = "20390101T230000UTC(0,0,0,-1,0)";
        time.parse3339(str);
        assertEquals(result, time.toString());
    }
    public void testInstanceQuery() throws Exception {
        final String[] PROJECTION = new String[] {
                Instances.TITLE,                 
                Instances.EVENT_LOCATION,        
                Instances.ALL_DAY,               
                Instances.COLOR,                 
                Instances.EVENT_TIMEZONE,        
                Instances.EVENT_ID,              
                Instances.BEGIN,                 
                Instances.END,                   
                Instances._ID,                   
                Instances.START_DAY,             
                Instances.END_DAY,               
                Instances.START_MINUTE,          
                Instances.END_MINUTE,            
                Instances.HAS_ALARM,             
                Instances.RRULE,                 
                Instances.RDATE,                 
                Instances.SELF_ATTENDEE_STATUS,  
                Events.ORGANIZER,                
                Events.GUESTS_CAN_MODIFY,        
        };
        String orderBy = Instances.SORT_CALENDAR_VIEW;
        String where = Instances.SELF_ATTENDEE_STATUS + "!=" + Calendar.Attendees.ATTENDEE_STATUS_DECLINED;
        int calId = insertCal("Calendar0", DEFAULT_TIMEZONE);
        final String START = "2008-05-01T00:00:00";
        final String END = "2008-05-01T20:00:00";
        EventInfo[] events = { new EventInfo("normal0",
                START,
                END,
                false ,
                DEFAULT_TIMEZONE) };
        insertEvent(calId, events[0]);
        Time time = new Time(DEFAULT_TIMEZONE);
        time.parse3339(START);
        long startMs = time.toMillis(true );
        Cursor cursor = Instances.query(mResolver, PROJECTION,
                startMs - DateUtils.YEAR_IN_MILLIS, startMs + DateUtils.HOUR_IN_MILLIS,
                where, orderBy);
        try {
            assertEquals(1, cursor.getCount());
        } finally {
            cursor.close();
        }
        cursor = Instances.query(mResolver, PROJECTION,
                startMs - DateUtils.YEAR_IN_MILLIS, startMs + 2 * DateUtils.HOUR_IN_MILLIS,
                where, orderBy);
        try {
            assertEquals(1, cursor.getCount());
        } finally {
            cursor.close();
        }
    }
    private Cursor queryInstances(long begin, long end) {
        Uri url = Uri.withAppendedPath(Calendar.Instances.CONTENT_URI, begin + "/" + end);
        return mResolver.query(url, null, null, null, null);
    }
    protected static class MockProvider extends ContentProvider {
        private String mAuthority;
        private int mNumItems = 0;
        public MockProvider(String authority) {
            mAuthority = authority;
        }
        @Override
        public boolean onCreate() {
            return true;
        }
        @Override
        public Cursor query(Uri uri, String[] projection, String selection,
                String[] selectionArgs, String sortOrder) {
            return new ArrayListCursor(new String[]{}, new ArrayList<ArrayList>());
        }
        @Override
        public String getType(Uri uri) {
            throw new UnsupportedOperationException();
        }
        @Override
        public Uri insert(Uri uri, ContentValues values) {
            mNumItems++;
            return Uri.parse("content:
        }
        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            return 0;
        }
        @Override
        public int update(Uri uri, ContentValues values, String selection,
                String[] selectionArgs) {
            return 0;
        }
    }
    private void cleanCalendarDataTable(SQLiteOpenHelper helper) {
        if (null == helper) {
            return;
        }
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM CalendarCache;");
    }
    public void testGetAndSetTimezoneDatabaseVersion() throws CalendarCache.CacheException {
        CalendarDatabaseHelper helper = (CalendarDatabaseHelper) getProvider().getDatabaseHelper();
        cleanCalendarDataTable(helper);
        CalendarCache cache = new CalendarCache(helper);
        boolean hasException = false;
        try {
            String value = cache.readData(null);
        } catch (CalendarCache.CacheException e) {
            hasException = true;
        }
        assertTrue(hasException);
        assertNull(cache.readTimezoneDatabaseVersion());
        cache.writeTimezoneDatabaseVersion("1234");
        assertEquals("1234", cache.readTimezoneDatabaseVersion());
        cache.writeTimezoneDatabaseVersion("5678");
        assertEquals("5678", cache.readTimezoneDatabaseVersion());
    }
    private void checkEvent(int eventId, String title, long dtStart, long dtEnd, boolean allDay) {
        Uri uri = Uri.parse("content:
        Log.i(TAG, "Looking for EventId = " + eventId);
        Cursor cursor = mResolver.query(uri, null, null, null, null);
        assertEquals(1, cursor.getCount());
        int colIndexTitle = cursor.getColumnIndex(Calendar.Events.TITLE);
        int colIndexDtStart = cursor.getColumnIndex(Calendar.Events.DTSTART);
        int colIndexDtEnd = cursor.getColumnIndex(Calendar.Events.DTEND);
        int colIndexAllDay = cursor.getColumnIndex(Calendar.Events.ALL_DAY);
        if (!cursor.moveToNext()) {
            Log.e(TAG,"Could not find inserted event");
            assertTrue(false);
        }
        assertEquals(title, cursor.getString(colIndexTitle));
        assertEquals(dtStart, cursor.getLong(colIndexDtStart));
        assertEquals(dtEnd, cursor.getLong(colIndexDtEnd));
        assertEquals(allDay, (cursor.getInt(colIndexAllDay) != 0));
        cursor.close();
    }
    public void testChangeTimezoneDB() {
        int calId = insertCal("Calendar0", DEFAULT_TIMEZONE);
        Cursor cursor = mResolver.query(Calendar.Events.CONTENT_URI, null, null, null, null);
        assertEquals(0, cursor.getCount());
        cursor.close();
        EventInfo[] events = { new EventInfo("normal0",
                                        "2008-05-01T00:00:00",
                                        "2008-05-02T00:00:00",
                                        false,
                                        DEFAULT_TIMEZONE) };
        Uri uri = insertEvent(calId, events[0]);
        assertNotNull(uri);
        checkEvent(1, events[0].mTitle, events[0].mDtstart, events[0].mDtend, events[0].mAllDay);
        getProvider().doProcessEventRawTimes(TIME_ZONE_AMERICA_ANCHORAGE,
                MOCK_TIME_ZONE_DATABASE_VERSION);
        assertEquals(MOCK_TIME_ZONE_DATABASE_VERSION, getProvider().getTimezoneDatabaseVersion());
        long deltaMillisForTimezones = 3600000L;
        checkEvent(1, events[0].mTitle,
                events[0].mDtstart + deltaMillisForTimezones,
                events[0].mDtend + deltaMillisForTimezones,
                events[0].mAllDay);
    }
}
