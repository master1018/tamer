public class CalendarAppWidgetService extends Service implements Runnable {
    static final String TAG = "CalendarAppWidgetService";
    static final boolean LOGD = false;
    static final String EVENT_SORT_ORDER = "startDay ASC, allDay DESC, begin ASC";
    static final String[] EVENT_PROJECTION = new String[] {
        Instances.ALL_DAY,
        Instances.BEGIN,
        Instances.END,
        Instances.COLOR,
        Instances.TITLE,
        Instances.RRULE,
        Instances.HAS_ALARM,
        Instances.EVENT_LOCATION,
        Instances.CALENDAR_ID,
        Instances.EVENT_ID,
    };
    static final int INDEX_ALL_DAY = 0;
    static final int INDEX_BEGIN = 1;
    static final int INDEX_END = 2;
    static final int INDEX_COLOR = 3;
    static final int INDEX_TITLE = 4;
    static final int INDEX_RRULE = 5;
    static final int INDEX_HAS_ALARM = 6;
    static final int INDEX_EVENT_LOCATION = 7;
    static final int INDEX_CALENDAR_ID = 8;
    static final int INDEX_EVENT_ID = 9;
    static final long SEARCH_DURATION = DateUtils.WEEK_IN_MILLIS;
    static final long UPDATE_NO_EVENTS = DateUtils.HOUR_IN_MILLIS * 6;
    static final String ACTION_PACKAGE = "com.google.android.calendar";
    static final String ACTION_CLASS = "com.android.calendar.LaunchActivity";
    static final String KEY_DETAIL_VIEW = "DETAIL_VIEW";
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        synchronized (AppWidgetShared.sLock) {
            if (!AppWidgetShared.sUpdateRunning) {
                if (LOGD) Log.d(TAG, "no thread running, so starting new one");
                AppWidgetShared.sUpdateRunning = true;
                new Thread(this).start();
            }
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    public void run() {
        while (true) {
            long now = -1;
            int[] appWidgetIds;
            Set<Long> changedEventIds;
            synchronized (AppWidgetShared.sLock) {
                if (!AppWidgetShared.sUpdateRequested) {
                    if (LOGD) Log.d(TAG, "no requested update or expired wakelock, bailing");
                    AppWidgetShared.clearLocked();
                    stopSelf();
                    return;
                }
                AppWidgetShared.sUpdateRequested = false;
                now = AppWidgetShared.sLastRequest;
                appWidgetIds = AppWidgetShared.collectAppWidgetIdsLocked();
                changedEventIds = AppWidgetShared.collectChangedEventIdsLocked();
            }
            if (LOGD) Log.d(TAG, "processing requested update now=" + now);
            performUpdate(this, appWidgetIds, changedEventIds, now);
        }
    }
    private void performUpdate(Context context, int[] appWidgetIds,
            Set<Long> changedEventIds, long now) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        RemoteViews views = null;
        long triggerTime = -1;
        try {
            cursor = getUpcomingInstancesCursor(resolver, SEARCH_DURATION, now);
            if (cursor != null) {
                MarkedEvents events = buildMarkedEvents(cursor, changedEventIds, now);
                boolean shouldUpdate = true;
                if (changedEventIds.size() > 0) {
                    shouldUpdate = events.watchFound;
                }
                if (events.primaryCount == 0) {
                    views = getAppWidgetNoEvents(context);
                } else if (shouldUpdate) {
                    views = getAppWidgetUpdate(context, cursor, events);
                    triggerTime = calculateUpdateTime(cursor, events);
                }
            } else {
                views = getAppWidgetNoEvents(context);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (views == null) {
            if (LOGD) Log.d(TAG, "Didn't build update, possibly because changedEventIds=" +
                    changedEventIds.toString());
            return;
        }
        AppWidgetManager gm = AppWidgetManager.getInstance(context);
        if (appWidgetIds != null && appWidgetIds.length > 0) {
            gm.updateAppWidget(appWidgetIds, views);
        } else {
            ComponentName thisWidget = CalendarAppWidgetProvider.getComponentName(context);
            gm.updateAppWidget(thisWidget, views);
        }
        if (triggerTime == -1 || triggerTime < now) {
            if (LOGD) Log.w(TAG, "Encountered bad trigger time " + formatDebugTime(triggerTime, now));
            triggerTime = now + UPDATE_NO_EVENTS;
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingUpdate = CalendarAppWidgetProvider.getUpdateIntent(context);
        am.cancel(pendingUpdate);
        am.set(AlarmManager.RTC, triggerTime, pendingUpdate);
        if (LOGD) Log.d(TAG, "Scheduled next update at " + formatDebugTime(triggerTime, now));
    }
    private String formatDebugTime(long unixTime, long now) {
        Time time = new Time();
        time.set(unixTime);
        long delta = unixTime - now;
        if (delta > DateUtils.MINUTE_IN_MILLIS) {
            delta /= DateUtils.MINUTE_IN_MILLIS;
            return String.format("[%d] %s (%+d mins)", unixTime, time.format("%H:%M:%S"), delta);
        } else {
            delta /= DateUtils.SECOND_IN_MILLIS;
            return String.format("[%d] %s (%+d secs)", unixTime, time.format("%H:%M:%S"), delta);
        }
    }
    private long convertUtcToLocal(Time recycle, long utcTime) {
        if (recycle == null) {
            recycle = new Time();
        }
        recycle.timezone = Time.TIMEZONE_UTC;
        recycle.set(utcTime);
        recycle.timezone = TimeZone.getDefault().getID();
        return recycle.normalize(true);
    }
    private long calculateUpdateTime(Cursor cursor, MarkedEvents events) {
        long result = -1;
        if (events.primaryRow != -1) {
            cursor.moveToPosition(events.primaryRow);
            long start = cursor.getLong(INDEX_BEGIN);
            long end = cursor.getLong(INDEX_END);
            boolean allDay = cursor.getInt(INDEX_ALL_DAY) != 0;
            if (allDay) {
                final Time recycle = new Time();
                start = convertUtcToLocal(recycle, start);
                end = convertUtcToLocal(recycle, end);
            }
            result = getEventFlip(cursor, start, end, allDay);
            long midnight = getNextMidnightTimeMillis();
            result = Math.min(midnight, result);
        }
        return result;
    }
    private long getNextMidnightTimeMillis() {
        Time time = new Time();
        time.setToNow();
        time.monthDay++;
        time.hour = 0;
        time.minute = 0;
        time.second = 0;
        long midnight = time.normalize(true);
        return midnight;
    }
    private long getEventFlip(Cursor cursor, long start, long end, boolean allDay) {
        long duration = end - start;
        if (allDay || duration > DateUtils.DAY_IN_MILLIS) {
            return start;
        } else {
            return (start + end) / 2;
        }
    }
    private void setNoEventsVisible(RemoteViews views, boolean noEvents) {
        views.setViewVisibility(R.id.no_events, noEvents ? View.VISIBLE : View.GONE);
        int otherViews = noEvents ? View.GONE : View.VISIBLE;
        views.setViewVisibility(R.id.day_of_month, otherViews);
        views.setViewVisibility(R.id.day_of_week, otherViews);
        views.setViewVisibility(R.id.divider, otherViews);
        views.setViewVisibility(R.id.when, otherViews);
        views.setViewVisibility(R.id.title, otherViews);
        if (noEvents) {
            views.setViewVisibility(R.id.conflict, otherViews);
            views.setViewVisibility(R.id.where, otherViews);
        }
    }
    private RemoteViews getAppWidgetUpdate(Context context, Cursor cursor, MarkedEvents events) {
        Resources res = context.getResources();
        ContentResolver resolver = context.getContentResolver();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.agenda_appwidget);
        setNoEventsVisible(views, false);
        Time time = new Time();
        time.setToNow();
        int yearDay = time.yearDay;
        int dateNumber = time.monthDay;
        String dayOfWeek = DateUtils.getDayOfWeekString(time.weekDay + 1,
                DateUtils.LENGTH_MEDIUM).toUpperCase();
        views.setTextViewText(R.id.day_of_week, dayOfWeek);
        views.setTextViewText(R.id.day_of_month, Integer.toString(time.monthDay));
        cursor.moveToPosition(events.primaryRow);
        long start = cursor.getLong(INDEX_BEGIN);
        boolean allDay = cursor.getInt(INDEX_ALL_DAY) != 0;
        int flags;
        String whenString;
        if (allDay) {
            flags = DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_UTC
                    | DateUtils.FORMAT_SHOW_DATE;
        } else {
            flags = DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_TIME;
            time.set(start);
            if (yearDay != time.yearDay) {
                flags = flags | DateUtils.FORMAT_SHOW_DATE;
            }
        }
        if (DateFormat.is24HourFormat(context)) {
            flags |= DateUtils.FORMAT_24HOUR;
        }
        whenString = DateUtils.formatDateRange(context, start, start, flags);
        views.setTextViewText(R.id.when, whenString);
        PendingIntent pendingIntent = getLaunchPendingIntent(context, start);
        views.setOnClickPendingIntent(R.id.agenda_appwidget, pendingIntent);
        String titleString = cursor.getString(INDEX_TITLE);
        if (titleString == null || titleString.length() == 0) {
            titleString = context.getString(R.string.no_title_label);
        }
        views.setTextViewText(R.id.title, titleString);
        int titleLines = 4;
        if (events.primaryCount > 1) {
            int count = events.primaryCount - 1;
            String conflictString = String.format(res.getQuantityString(
                    R.plurals.gadget_more_events, count), count);
            views.setTextViewText(R.id.conflict, conflictString);
            views.setViewVisibility(R.id.conflict, View.VISIBLE);
            titleLines -= 1;
        } else {
            views.setViewVisibility(R.id.conflict, View.GONE);
        }
        String whereString = cursor.getString(INDEX_EVENT_LOCATION);
        if (whereString != null && whereString.length() > 0) {
            views.setViewVisibility(R.id.where, View.VISIBLE);
            views.setTextViewText(R.id.where, whereString);
            titleLines -= 1;
        } else {
            views.setViewVisibility(R.id.where, View.GONE);
        }
        views.setInt(R.id.title, "setMaxLines", titleLines);
        return views;
    }
    private RemoteViews getAppWidgetNoEvents(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.agenda_appwidget);
        setNoEventsVisible(views, true);
        PendingIntent pendingIntent = getLaunchPendingIntent(context, 0);
        views.setOnClickPendingIntent(R.id.agenda_appwidget, pendingIntent);
        return views;
    }
    private PendingIntent getLaunchPendingIntent(Context context, long goToTime) {
        Intent launchIntent = new Intent();
        String dataString = "content:
        launchIntent.setAction(Intent.ACTION_VIEW);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (goToTime != 0) {
            launchIntent.putExtra(KEY_DETAIL_VIEW, true);
            dataString += "/" + goToTime;
        }
        Uri data = Uri.parse(dataString);
        launchIntent.setData(data);
        return PendingIntent.getActivity(context, 0 ,
                launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    private class MarkedEvents {
        long primaryTime = -1;
        int primaryRow = -1;
        int primaryConflictRow = -1;
        int primaryCount = 0;
        long secondaryTime = -1;
        int secondaryRow = -1;
        int secondaryCount = 0;
        boolean watchFound = false;
    }
    private MarkedEvents buildMarkedEvents(Cursor cursor, Set<Long> watchEventIds, long now) {
        MarkedEvents events = new MarkedEvents();
        final Time recycle = new Time();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            int row = cursor.getPosition();
            long eventId = cursor.getLong(INDEX_EVENT_ID);
            long start = cursor.getLong(INDEX_BEGIN);
            long end = cursor.getLong(INDEX_END);
            boolean allDay = cursor.getInt(INDEX_ALL_DAY) != 0;
            if (allDay) {
                start = convertUtcToLocal(recycle, start);
                end = convertUtcToLocal(recycle, end);
            }
            long eventFlip = getEventFlip(cursor, start, end, allDay);
            if (LOGD) Log.d(TAG, "Calculated flip time " + formatDebugTime(eventFlip, now));
            if (eventFlip < now) {
                continue;
            }
            if (watchEventIds.contains(eventId)) {
                events.watchFound = true;
            }
            if (events.primaryRow == -1) {
                events.primaryRow = row;
                events.primaryTime = start;
                events.primaryCount = 1;
            } else if (events.primaryTime == start) {
                if (events.primaryConflictRow == -1) {
                    events.primaryConflictRow = row;
                }
                events.primaryCount += 1;
            } else if (events.secondaryRow == -1) {
                events.secondaryRow = row;
                events.secondaryTime = start;
                events.secondaryCount = 1;
            } else if (events.secondaryTime == start) {
                events.secondaryCount += 1;
            } else {
                break;
            }
        }
        return events;
    }
    private Cursor getUpcomingInstancesCursor(ContentResolver resolver,
            long searchDuration, long now) {
        long end = now + searchDuration;
        Uri uri = Uri.withAppendedPath(Instances.CONTENT_URI,
                String.format("%d/%d", now, end));
        String selection = String.format("%s=1 AND %s!=%d",
                Calendars.SELECTED, Instances.SELF_ATTENDEE_STATUS,
                Attendees.ATTENDEE_STATUS_DECLINED);
        return resolver.query(uri, EVENT_PROJECTION, selection, null,
                EVENT_SORT_ORDER);
    }
}
