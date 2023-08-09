public final class Calendar {
    public static final String TAG = "Calendar";
    public static final String EVENT_REMINDER_ACTION = "android.intent.action.EVENT_REMINDER";
    public static final String EVENT_BEGIN_TIME = "beginTime";
    public static final String EVENT_END_TIME = "endTime";
    public static final String AUTHORITY = "com.android.calendar";
    public static final Uri CONTENT_URI =
        Uri.parse("content:
    public static final String CALLER_IS_SYNCADAPTER = "caller_is_syncadapter";
    public interface CalendarsColumns
    {
        public static final String COLOR = "color";
        public static final String ACCESS_LEVEL = "access_level";
        public static final int NO_ACCESS = 0;
        public static final int FREEBUSY_ACCESS = 100;
        public static final int READ_ACCESS = 200;
        public static final int RESPOND_ACCESS = 300;
        public static final int OVERRIDE_ACCESS = 400;
        public static final int CONTRIBUTOR_ACCESS = 500;
        public static final int EDITOR_ACCESS = 600;
        public static final int OWNER_ACCESS = 700;
        public static final int ROOT_ACCESS = 800;
        public static final String SELECTED = "selected";
        public static final String TIMEZONE = "timezone";
        public static final String SYNC_EVENTS = "sync_events";
        public static final String SYNC_STATE = "sync_state";
        public static final String _SYNC_ACCOUNT = "_sync_account";
        public static final String _SYNC_ACCOUNT_TYPE = "_sync_account_type";
        public static final String _SYNC_ID = "_sync_id";
        public static final String _SYNC_TIME = "_sync_time";
        public static final String _SYNC_VERSION = "_sync_version";
        public static final String _SYNC_DATA = "_sync_local_id";
        public static final String _SYNC_MARK = "_sync_mark";
        public static final String _SYNC_DIRTY = "_sync_dirty";
        public static final String ACCOUNT_NAME = "account_name";
        public static final String ACCOUNT_TYPE = "account_type";
    }
    public static class Calendars implements BaseColumns, CalendarsColumns
    {
        private static final String WHERE_DELETE_FOR_ACCOUNT = Calendars._SYNC_ACCOUNT + "=?"
                + " AND " + Calendars._SYNC_ACCOUNT_TYPE + "=?";
        public static final Cursor query(ContentResolver cr, String[] projection,
                                       String where, String orderBy)
        {
            return cr.query(CONTENT_URI, projection, where,
                                         null, orderBy == null ? DEFAULT_SORT_ORDER : orderBy);
        }
        public static int delete(ContentResolver cr, String selection, String[] selectionArgs)
        {
            return cr.delete(CONTENT_URI, selection, selectionArgs);
        }
        public static int deleteCalendarsForAccount(ContentResolver cr, Account account) {
            return Calendar.Calendars.delete(cr,
                    WHERE_DELETE_FOR_ACCOUNT,
                    new String[] { account.name, account.type });
        }
        public static final Uri CONTENT_URI = Uri.parse("content:
        public static final String DEFAULT_SORT_ORDER = "displayName";
        public static final String URL = "url";
        public static final String NAME = "name";
        public static final String DISPLAY_NAME = "displayName";
        public static final String LOCATION = "location";
        public static final String HIDDEN = "hidden";
        public static final String OWNER_ACCOUNT = "ownerAccount";
        public static final String ORGANIZER_CAN_RESPOND = "organizerCanRespond";
    }
    public interface AttendeesColumns {
        public static final String EVENT_ID = "event_id";
        public static final String ATTENDEE_NAME = "attendeeName";
        public static final String ATTENDEE_EMAIL = "attendeeEmail";
        public static final String ATTENDEE_RELATIONSHIP = "attendeeRelationship";
        public static final int RELATIONSHIP_NONE = 0;
        public static final int RELATIONSHIP_ATTENDEE = 1;
        public static final int RELATIONSHIP_ORGANIZER = 2;
        public static final int RELATIONSHIP_PERFORMER = 3;
        public static final int RELATIONSHIP_SPEAKER = 4;
        public static final String ATTENDEE_TYPE = "attendeeType";
        public static final int TYPE_NONE = 0;
        public static final int TYPE_REQUIRED = 1;
        public static final int TYPE_OPTIONAL = 2;
        public static final String ATTENDEE_STATUS = "attendeeStatus";
        public static final int ATTENDEE_STATUS_NONE = 0;
        public static final int ATTENDEE_STATUS_ACCEPTED = 1;
        public static final int ATTENDEE_STATUS_DECLINED = 2;
        public static final int ATTENDEE_STATUS_INVITED = 3;
        public static final int ATTENDEE_STATUS_TENTATIVE = 4;
    }
    public static final class Attendees implements BaseColumns, AttendeesColumns, EventsColumns {
        public static final Uri CONTENT_URI = Uri.parse("content:
    }
    public interface EventsColumns
    {
        public static final String CALENDAR_ID = "calendar_id";
        public static final String HTML_URI = "htmlUri";
        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String EVENT_LOCATION = "eventLocation";
        public static final String STATUS = "eventStatus";
        public static final int STATUS_TENTATIVE = 0;
        public static final int STATUS_CONFIRMED = 1;
        public static final int STATUS_CANCELED = 2;
        public static final String SELF_ATTENDEE_STATUS = "selfAttendeeStatus";
        public static final String SYNC_ADAPTER_DATA = "syncAdapterData";
        public static final String COMMENTS_URI = "commentsUri";
        public static final String DTSTART = "dtstart";
        public static final String DTEND = "dtend";
        public static final String DURATION = "duration";
        public static final String EVENT_TIMEZONE = "eventTimezone";
        public static final String ALL_DAY = "allDay";
        public static final String VISIBILITY = "visibility";
        public static final int VISIBILITY_DEFAULT = 0;
        public static final int VISIBILITY_CONFIDENTIAL = 1;
        public static final int VISIBILITY_PRIVATE = 2;
        public static final int VISIBILITY_PUBLIC = 3;
        public static final String TRANSPARENCY = "transparency";
        public static final int TRANSPARENCY_OPAQUE = 0;
        public static final int TRANSPARENCY_TRANSPARENT = 1;
        public static final String HAS_ALARM = "hasAlarm";
        public static final String HAS_EXTENDED_PROPERTIES = "hasExtendedProperties";
        public static final String RRULE = "rrule";
        public static final String RDATE = "rdate";
        public static final String EXRULE = "exrule";
        public static final String EXDATE = "exdate";
        public static final String ORIGINAL_EVENT = "originalEvent";
        public static final String ORIGINAL_INSTANCE_TIME = "originalInstanceTime";
        public static final String ORIGINAL_ALL_DAY = "originalAllDay";
        public static final String LAST_DATE = "lastDate";
        public static final String HAS_ATTENDEE_DATA = "hasAttendeeData";
        public static final String GUESTS_CAN_MODIFY = "guestsCanModify";
        public static final String GUESTS_CAN_INVITE_OTHERS = "guestsCanInviteOthers";
        public static final String GUESTS_CAN_SEE_GUESTS = "guestsCanSeeGuests";
        public static final String ORGANIZER = "organizer";
        public static final String CAN_INVITE_OTHERS = "canInviteOthers";
        public static final String OWNER_ACCOUNT = "ownerAccount";
        public static final String DELETED = "deleted";
    }
    public static final class EventsEntity implements BaseColumns, EventsColumns, CalendarsColumns {
        public static final Uri CONTENT_URI = Uri.parse("content:
                "/event_entities");
        public static EntityIterator newEntityIterator(Cursor cursor, ContentResolver resolver) {
            return new EntityIteratorImpl(cursor, resolver);
        }
        public static EntityIterator newEntityIterator(Cursor cursor,
                ContentProviderClient provider) {
            return new EntityIteratorImpl(cursor, provider);
        }
        private static class EntityIteratorImpl extends CursorEntityIterator {
            private final ContentResolver mResolver;
            private final ContentProviderClient mProvider;
            private static final String[] REMINDERS_PROJECTION = new String[] {
                    Reminders.MINUTES,
                    Reminders.METHOD,
            };
            private static final int COLUMN_MINUTES = 0;
            private static final int COLUMN_METHOD = 1;
            private static final String[] ATTENDEES_PROJECTION = new String[] {
                    Attendees.ATTENDEE_NAME,
                    Attendees.ATTENDEE_EMAIL,
                    Attendees.ATTENDEE_RELATIONSHIP,
                    Attendees.ATTENDEE_TYPE,
                    Attendees.ATTENDEE_STATUS,
            };
            private static final int COLUMN_ATTENDEE_NAME = 0;
            private static final int COLUMN_ATTENDEE_EMAIL = 1;
            private static final int COLUMN_ATTENDEE_RELATIONSHIP = 2;
            private static final int COLUMN_ATTENDEE_TYPE = 3;
            private static final int COLUMN_ATTENDEE_STATUS = 4;
            private static final String[] EXTENDED_PROJECTION = new String[] {
                    ExtendedProperties._ID,
                    ExtendedProperties.NAME,
                    ExtendedProperties.VALUE
            };
            private static final int COLUMN_ID = 0;
            private static final int COLUMN_NAME = 1;
            private static final int COLUMN_VALUE = 2;
            private static final String WHERE_EVENT_ID = "event_id=?";
            public EntityIteratorImpl(Cursor cursor, ContentResolver resolver) {
                super(cursor);
                mResolver = resolver;
                mProvider = null;
            }
            public EntityIteratorImpl(Cursor cursor, ContentProviderClient provider) {
                super(cursor);
                mResolver = null;
                mProvider = provider;
            }
            @Override
            public Entity getEntityAndIncrementCursor(Cursor cursor) throws RemoteException {
                final long eventId = cursor.getLong(cursor.getColumnIndexOrThrow(Events._ID));
                ContentValues cv = new ContentValues();
                cv.put(Events._ID, eventId);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, CALENDAR_ID);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, HTML_URI);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, TITLE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, DESCRIPTION);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EVENT_LOCATION);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, STATUS);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, SELF_ATTENDEE_STATUS);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, COMMENTS_URI);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, DTSTART);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, DTEND);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, DURATION);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EVENT_TIMEZONE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, ALL_DAY);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, VISIBILITY);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, TRANSPARENCY);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, HAS_ALARM);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv,
                        HAS_EXTENDED_PROPERTIES);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, RRULE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, RDATE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EXRULE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, EXDATE);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, ORIGINAL_EVENT);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv,
                        ORIGINAL_INSTANCE_TIME);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, ORIGINAL_ALL_DAY);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, LAST_DATE);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, HAS_ATTENDEE_DATA);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv,
                        GUESTS_CAN_INVITE_OTHERS);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, GUESTS_CAN_MODIFY);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, GUESTS_CAN_SEE_GUESTS);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, ORGANIZER);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, _SYNC_ID);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, _SYNC_DATA);
                DatabaseUtils.cursorLongToContentValuesIfPresent(cursor, cv, _SYNC_DIRTY);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, _SYNC_VERSION);
                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, cv, DELETED);
                DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, cv, Calendars.URL);
                Entity entity = new Entity(cv);
                Cursor subCursor;
                if (mResolver != null) {
                    subCursor = mResolver.query(Reminders.CONTENT_URI, REMINDERS_PROJECTION,
                            WHERE_EVENT_ID,
                            new String[] { Long.toString(eventId) }  ,
                            null );
                } else {
                    subCursor = mProvider.query(Reminders.CONTENT_URI, REMINDERS_PROJECTION,
                            WHERE_EVENT_ID,
                            new String[] { Long.toString(eventId) }  ,
                            null );
                }
                try {
                    while (subCursor.moveToNext()) {
                        ContentValues reminderValues = new ContentValues();
                        reminderValues.put(Reminders.MINUTES, subCursor.getInt(COLUMN_MINUTES));
                        reminderValues.put(Reminders.METHOD, subCursor.getInt(COLUMN_METHOD));
                        entity.addSubValue(Reminders.CONTENT_URI, reminderValues);
                    }
                } finally {
                    subCursor.close();
                }
                if (mResolver != null) {
                    subCursor = mResolver.query(Attendees.CONTENT_URI, ATTENDEES_PROJECTION,
                            WHERE_EVENT_ID,
                            new String[] { Long.toString(eventId) } ,
                            null );
                } else {
                    subCursor = mProvider.query(Attendees.CONTENT_URI, ATTENDEES_PROJECTION,
                            WHERE_EVENT_ID,
                            new String[] { Long.toString(eventId) } ,
                            null );
                }
                try {
                    while (subCursor.moveToNext()) {
                        ContentValues attendeeValues = new ContentValues();
                        attendeeValues.put(Attendees.ATTENDEE_NAME,
                                subCursor.getString(COLUMN_ATTENDEE_NAME));
                        attendeeValues.put(Attendees.ATTENDEE_EMAIL,
                                subCursor.getString(COLUMN_ATTENDEE_EMAIL));
                        attendeeValues.put(Attendees.ATTENDEE_RELATIONSHIP,
                                subCursor.getInt(COLUMN_ATTENDEE_RELATIONSHIP));
                        attendeeValues.put(Attendees.ATTENDEE_TYPE,
                                subCursor.getInt(COLUMN_ATTENDEE_TYPE));
                        attendeeValues.put(Attendees.ATTENDEE_STATUS,
                                subCursor.getInt(COLUMN_ATTENDEE_STATUS));
                        entity.addSubValue(Attendees.CONTENT_URI, attendeeValues);
                    }
                } finally {
                    subCursor.close();
                }
                if (mResolver != null) {
                    subCursor = mResolver.query(ExtendedProperties.CONTENT_URI, EXTENDED_PROJECTION,
                            WHERE_EVENT_ID,
                            new String[] { Long.toString(eventId) } ,
                            null );
                } else {
                    subCursor = mProvider.query(ExtendedProperties.CONTENT_URI, EXTENDED_PROJECTION,
                            WHERE_EVENT_ID,
                            new String[] { Long.toString(eventId) } ,
                            null );
                }
                try {
                    while (subCursor.moveToNext()) {
                        ContentValues extendedValues = new ContentValues();
                        extendedValues.put(ExtendedProperties._ID,
                                subCursor.getString(COLUMN_ID));
                        extendedValues.put(ExtendedProperties.NAME,
                                subCursor.getString(COLUMN_NAME));
                        extendedValues.put(ExtendedProperties.VALUE,
                                subCursor.getString(COLUMN_VALUE));
                        entity.addSubValue(ExtendedProperties.CONTENT_URI, extendedValues);
                    }
                } finally {
                    subCursor.close();
                }
                cursor.moveToNext();
                return entity;
            }
        }
    }
    public static final class Events implements BaseColumns, EventsColumns, CalendarsColumns {
        private static final String[] FETCH_ENTRY_COLUMNS =
                new String[] { Events._SYNC_ACCOUNT, Events._SYNC_ID };
        private static final String[] ATTENDEES_COLUMNS =
                new String[] { AttendeesColumns.ATTENDEE_NAME,
                               AttendeesColumns.ATTENDEE_EMAIL,
                               AttendeesColumns.ATTENDEE_RELATIONSHIP,
                               AttendeesColumns.ATTENDEE_TYPE,
                               AttendeesColumns.ATTENDEE_STATUS };
        public static final Cursor query(ContentResolver cr, String[] projection) {
            return cr.query(CONTENT_URI, projection, null, null, DEFAULT_SORT_ORDER);
        }
        public static final Cursor query(ContentResolver cr, String[] projection,
                                       String where, String orderBy) {
            return cr.query(CONTENT_URI, projection, where,
                                         null, orderBy == null ? DEFAULT_SORT_ORDER : orderBy);
        }
        private static String extractValue(ICalendar.Component component,
                                           String propertyName) {
            ICalendar.Property property =
                    component.getFirstProperty(propertyName);
            if (property != null) {
                return property.getValue();
            }
            return null;
        }
        public static final Uri CONTENT_URI =
                Uri.parse("content:
        public static final Uri DELETED_CONTENT_URI =
                Uri.parse("content:
        public static final String DEFAULT_SORT_ORDER = "";
    }
    public static final class Instances implements BaseColumns, EventsColumns, CalendarsColumns {
        private static final String WHERE_CALENDARS_SELECTED = Calendars.SELECTED + "=1";
        public static final Cursor query(ContentResolver cr, String[] projection,
                                         long begin, long end) {
            Uri.Builder builder = CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, begin);
            ContentUris.appendId(builder, end);
            return cr.query(builder.build(), projection, WHERE_CALENDARS_SELECTED,
                         null, DEFAULT_SORT_ORDER);
        }
        public static final Cursor query(ContentResolver cr, String[] projection,
                                         long begin, long end, String where, String orderBy) {
            Uri.Builder builder = CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, begin);
            ContentUris.appendId(builder, end);
            if (TextUtils.isEmpty(where)) {
                where = WHERE_CALENDARS_SELECTED;
            } else {
                where = "(" + where + ") AND " + WHERE_CALENDARS_SELECTED;
            }
            return cr.query(builder.build(), projection, where,
                         null, orderBy == null ? DEFAULT_SORT_ORDER : orderBy);
        }
        public static final Uri CONTENT_URI = Uri.parse("content:
                "/instances/when");
        public static final Uri CONTENT_BY_DAY_URI =
            Uri.parse("content:
        public static final String DEFAULT_SORT_ORDER = "begin ASC";
        public static final String SORT_CALENDAR_VIEW = "begin ASC, end DESC, title ASC";
        public static final String BEGIN = "begin";
        public static final String END = "end";
        public static final String EVENT_ID = "event_id";
        public static final String START_DAY = "startDay";
        public static final String END_DAY = "endDay";
        public static final String START_MINUTE = "startMinute";
        public static final String END_MINUTE = "endMinute";
    }
    public interface CalendarMetaDataColumns {
        public static final String LOCAL_TIMEZONE = "localTimezone";
        public static final String MIN_INSTANCE = "minInstance";
        public static final String MAX_INSTANCE = "maxInstance";
        public static final String MIN_EVENTDAYS = "minEventDays";
        public static final String MAX_EVENTDAYS = "maxEventDays";
    }
    public static final class CalendarMetaData implements CalendarMetaDataColumns {
    }
    public interface EventDaysColumns {
        public static final String STARTDAY = "startDay";
        public static final String ENDDAY = "endDay";
    }
    public static final class EventDays implements EventDaysColumns {
        public static final Uri CONTENT_URI = Uri.parse("content:
                "/instances/groupbyday");
        public static final String[] PROJECTION = { STARTDAY, ENDDAY };
        public static final String SELECTION = "selected=1";
        public static final Cursor query(ContentResolver cr, int startDay, int numDays) {
            if (numDays < 1) {
                return null;
            }
            int endDay = startDay + numDays - 1;
            Uri.Builder builder = CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, startDay);
            ContentUris.appendId(builder, endDay);
            return cr.query(builder.build(), PROJECTION, SELECTION,
                    null , STARTDAY);
        }
    }
    public interface RemindersColumns {
        public static final String EVENT_ID = "event_id";
        public static final String MINUTES = "minutes";
        public static final int MINUTES_DEFAULT = -1;
        public static final String METHOD = "method";
        public static final int METHOD_DEFAULT = 0;
        public static final int METHOD_ALERT = 1;
        public static final int METHOD_EMAIL = 2;
        public static final int METHOD_SMS = 3;
    }
    public static final class Reminders implements BaseColumns, RemindersColumns, EventsColumns {
        public static final String TABLE_NAME = "Reminders";
        public static final Uri CONTENT_URI = Uri.parse("content:
    }
    public interface CalendarAlertsColumns {
        public static final String EVENT_ID = "event_id";
        public static final String BEGIN = "begin";
        public static final String END = "end";
        public static final String ALARM_TIME = "alarmTime";
        public static final String CREATION_TIME = "creationTime";
        public static final String RECEIVED_TIME = "receivedTime";
        public static final String NOTIFY_TIME = "notifyTime";
        public static final String STATE = "state";
        public static final int SCHEDULED = 0;
        public static final int FIRED = 1;
        public static final int DISMISSED = 2;
        public static final String MINUTES = "minutes";
        public static final String DEFAULT_SORT_ORDER = "begin ASC,title ASC";
    }
    public static final class CalendarAlerts implements BaseColumns,
            CalendarAlertsColumns, EventsColumns, CalendarsColumns {
        public static final String TABLE_NAME = "CalendarAlerts";
        public static final Uri CONTENT_URI = Uri.parse("content:
                "/calendar_alerts");
        private static final String WHERE_ALARM_EXISTS = EVENT_ID + "=?"
                + " AND " + BEGIN + "=?"
                + " AND " + ALARM_TIME + "=?";
        private static final String WHERE_FINDNEXTALARMTIME = ALARM_TIME + ">=?";
        private static final String SORT_ORDER_ALARMTIME_ASC = ALARM_TIME + " ASC";
        private static final String WHERE_RESCHEDULE_MISSED_ALARMS = STATE + "=" + SCHEDULED
                + " AND " + ALARM_TIME + "<?"
                + " AND " + ALARM_TIME + ">?"
                + " AND " + END + ">=?";
        public static final Uri CONTENT_URI_BY_INSTANCE =
            Uri.parse("content:
        private static final boolean DEBUG = true;
        public static final Uri insert(ContentResolver cr, long eventId,
                long begin, long end, long alarmTime, int minutes) {
            ContentValues values = new ContentValues();
            values.put(CalendarAlerts.EVENT_ID, eventId);
            values.put(CalendarAlerts.BEGIN, begin);
            values.put(CalendarAlerts.END, end);
            values.put(CalendarAlerts.ALARM_TIME, alarmTime);
            long currentTime = System.currentTimeMillis();
            values.put(CalendarAlerts.CREATION_TIME, currentTime);
            values.put(CalendarAlerts.RECEIVED_TIME, 0);
            values.put(CalendarAlerts.NOTIFY_TIME, 0);
            values.put(CalendarAlerts.STATE, SCHEDULED);
            values.put(CalendarAlerts.MINUTES, minutes);
            return cr.insert(CONTENT_URI, values);
        }
        public static final Cursor query(ContentResolver cr, String[] projection,
                String selection, String[] selectionArgs, String sortOrder) {
            return cr.query(CONTENT_URI, projection, selection, selectionArgs,
                    sortOrder);
        }
        public static final long findNextAlarmTime(ContentResolver cr, long millis) {
            String selection = ALARM_TIME + ">=" + millis;
            String[] projection = new String[] { ALARM_TIME };
            Cursor cursor = query(cr, projection,
                    WHERE_FINDNEXTALARMTIME,
                    new String[] {
                        Long.toString(millis)
                    },
                    SORT_ORDER_ALARMTIME_ASC);
            long alarmTime = -1;
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    alarmTime = cursor.getLong(0);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return alarmTime;
        }
        public static final void rescheduleMissedAlarms(ContentResolver cr,
                Context context, AlarmManager manager) {
            long now = System.currentTimeMillis();
            long ancient = now - DateUtils.DAY_IN_MILLIS;
            String[] projection = new String[] {
                    ALARM_TIME,
            };
            Cursor cursor = CalendarAlerts.query(cr,
                    projection,
                    WHERE_RESCHEDULE_MISSED_ALARMS,
                    new String[] {
                        Long.toString(now),
                        Long.toString(ancient),
                        Long.toString(now)
                    },
                    SORT_ORDER_ALARMTIME_ASC);
            if (cursor == null) {
                return;
            }
            if (DEBUG) {
                Log.d(TAG, "missed alarms found: " + cursor.getCount());
            }
            try {
                long alarmTime = -1;
                while (cursor.moveToNext()) {
                    long newAlarmTime = cursor.getLong(0);
                    if (alarmTime != newAlarmTime) {
                        if (DEBUG) {
                            Log.w(TAG, "rescheduling missed alarm. alarmTime: " + newAlarmTime);
                        }
                        scheduleAlarm(context, manager, newAlarmTime);
                        alarmTime = newAlarmTime;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        public static void scheduleAlarm(Context context, AlarmManager manager, long alarmTime) {
            if (DEBUG) {
                Time time = new Time();
                time.set(alarmTime);
                String schedTime = time.format(" %a, %b %d, %Y %I:%M%P");
                Log.d(TAG, "Schedule alarm at " + alarmTime + " " + schedTime);
            }
            if (manager == null) {
                manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            }
            Intent intent = new Intent(EVENT_REMINDER_ACTION);
            intent.setData(ContentUris.withAppendedId(Calendar.CONTENT_URI, alarmTime));
            intent.putExtra(ALARM_TIME, alarmTime);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
            manager.set(AlarmManager.RTC_WAKEUP, alarmTime, pi);
        }
        public static final boolean alarmExists(ContentResolver cr, long eventId,
                long begin, long alarmTime) {
            String[] projection = new String[] { ALARM_TIME };
            Cursor cursor = query(cr,
                    projection,
                    WHERE_ALARM_EXISTS,
                    new String[] {
                        Long.toString(eventId),
                        Long.toString(begin),
                        Long.toString(alarmTime)
                    },
                    null);
            boolean found = false;
            try {
                if (cursor != null && cursor.getCount() > 0) {
                    found = true;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return found;
        }
    }
    public interface ExtendedPropertiesColumns {
        public static final String EVENT_ID = "event_id";
        public static final String NAME = "name";
        public static final String VALUE = "value";
    }
   public static final class ExtendedProperties implements BaseColumns,
            ExtendedPropertiesColumns, EventsColumns {
        public static final Uri CONTENT_URI =
                Uri.parse("content:
   }
    public static final class SyncState implements SyncStateContract.Columns {
        private SyncState() {}
        public static final String CONTENT_DIRECTORY =
                SyncStateContract.Constants.CONTENT_DIRECTORY;
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(Calendar.CONTENT_URI, CONTENT_DIRECTORY);
    }
}
