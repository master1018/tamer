public class AlertService extends Service {
    static final boolean DEBUG = true;
    private static final String TAG = "AlertService";
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private static final String[] ALERT_PROJECTION = new String[] {
        CalendarAlerts._ID,                     
        CalendarAlerts.EVENT_ID,                
        CalendarAlerts.STATE,                   
        CalendarAlerts.TITLE,                   
        CalendarAlerts.EVENT_LOCATION,          
        CalendarAlerts.SELF_ATTENDEE_STATUS,    
        CalendarAlerts.ALL_DAY,                 
        CalendarAlerts.ALARM_TIME,              
        CalendarAlerts.MINUTES,                 
        CalendarAlerts.BEGIN,                   
        CalendarAlerts.END,                     
    };
    private static final int ALERT_INDEX_ID = 0;
    private static final int ALERT_INDEX_EVENT_ID = 1;
    private static final int ALERT_INDEX_STATE = 2;
    private static final int ALERT_INDEX_TITLE = 3;
    private static final int ALERT_INDEX_EVENT_LOCATION = 4;
    private static final int ALERT_INDEX_SELF_ATTENDEE_STATUS = 5;
    private static final int ALERT_INDEX_ALL_DAY = 6;
    private static final int ALERT_INDEX_ALARM_TIME = 7;
    private static final int ALERT_INDEX_MINUTES = 8;
    private static final int ALERT_INDEX_BEGIN = 9;
    private static final int ALERT_INDEX_END = 10;
    private static final String ACTIVE_ALERTS_SELECTION = "(" + CalendarAlerts.STATE + "=? OR "
            + CalendarAlerts.STATE + "=?) AND " + CalendarAlerts.ALARM_TIME + "<=";
    private static final String[] ACTIVE_ALERTS_SELECTION_ARGS = new String[] {
            Integer.toString(CalendarAlerts.FIRED), Integer.toString(CalendarAlerts.SCHEDULED)
    };
    private static final String ACTIVE_ALERTS_SORT = "begin DESC, end DESC";
    @SuppressWarnings("deprecation")
    void processMessage(Message msg) {
        Bundle bundle = (Bundle) msg.obj;
        String action = bundle.getString("action");
        if (DEBUG) {
            Log.d(TAG, "" + bundle.getLong(android.provider.Calendar.CalendarAlerts.ALARM_TIME)
                    + " Action = " + action);
        }
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)
                || action.equals(Intent.ACTION_TIME_CHANGED)) {
            doTimeChanged();
            return;
        }
        if (!action.equals(android.provider.Calendar.EVENT_REMINDER_ACTION)
                && !action.equals(Intent.ACTION_LOCALE_CHANGED)) {
            Log.w(TAG, "Invalid action: " + action);
            return;
        }
        updateAlertNotification(this);
    }
    static boolean updateAlertNotification(Context context) {
        ContentResolver cr = context.getContentResolver();
        final long currentTime = System.currentTimeMillis();
        Cursor alertCursor = CalendarAlerts.query(cr, ALERT_PROJECTION, ACTIVE_ALERTS_SELECTION
                + currentTime, ACTIVE_ALERTS_SELECTION_ARGS, ACTIVE_ALERTS_SORT);
        if (alertCursor == null || alertCursor.getCount() == 0) {
            if (alertCursor != null) {
                alertCursor.close();
            }
            if (DEBUG) Log.d(TAG, "No fired or scheduled alerts");
            NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(0);
            return false;
        }
        if (DEBUG) {
            Log.d(TAG, "alert count:" + alertCursor.getCount());
        }
        String notificationEventName = null;
        String notificationEventLocation = null;
        long notificationEventBegin = 0;
        int notificationEventStatus = 0;
        HashMap<Long, Long> eventIds = new HashMap<Long, Long>();
        int numReminders = 0;
        int numFired = 0;
        try {
            while (alertCursor.moveToNext()) {
                final long alertId = alertCursor.getLong(ALERT_INDEX_ID);
                final long eventId = alertCursor.getLong(ALERT_INDEX_EVENT_ID);
                final int minutes = alertCursor.getInt(ALERT_INDEX_MINUTES);
                final String eventName = alertCursor.getString(ALERT_INDEX_TITLE);
                final String location = alertCursor.getString(ALERT_INDEX_EVENT_LOCATION);
                final boolean allDay = alertCursor.getInt(ALERT_INDEX_ALL_DAY) != 0;
                final int status = alertCursor.getInt(ALERT_INDEX_SELF_ATTENDEE_STATUS);
                final boolean declined = status == Attendees.ATTENDEE_STATUS_DECLINED;
                final long beginTime = alertCursor.getLong(ALERT_INDEX_BEGIN);
                final long endTime = alertCursor.getLong(ALERT_INDEX_END);
                final Uri alertUri = ContentUris
                        .withAppendedId(CalendarAlerts.CONTENT_URI, alertId);
                final long alarmTime = alertCursor.getLong(ALERT_INDEX_ALARM_TIME);
                int state = alertCursor.getInt(ALERT_INDEX_STATE);
                if (DEBUG) {
                    Log.d(TAG, "alarmTime:" + alarmTime + " alertId:" + alertId
                            + " eventId:" + eventId + " state: " + state + " minutes:" + minutes
                            + " declined:" + declined + " beginTime:" + beginTime
                            + " endTime:" + endTime);
                }
                ContentValues values = new ContentValues();
                int newState = -1;
                if (!declined && eventIds.put(eventId, beginTime) == null) {
                    numReminders++;
                    if (state == CalendarAlerts.SCHEDULED) {
                        newState = CalendarAlerts.FIRED;
                        numFired++;
                        values.put(CalendarAlerts.RECEIVED_TIME, currentTime);
                    }
                } else {
                    newState = CalendarAlerts.DISMISSED;
                    if (DEBUG) {
                        if (!declined) Log.d(TAG, "dropping dup alert for event " + eventId);
                    }
                }
                if (newState != -1) {
                    values.put(CalendarAlerts.STATE, newState);
                    state = newState;
                }
                if (state == CalendarAlerts.FIRED) {
                    values.put(CalendarAlerts.NOTIFY_TIME, currentTime);
                }
                if (values.size() > 0) cr.update(alertUri, values, null, null);
                if (state != CalendarAlerts.FIRED) {
                    continue;
                }
                int newStatus;
                switch (status) {
                    case Attendees.ATTENDEE_STATUS_ACCEPTED:
                        newStatus = 2;
                        break;
                    case Attendees.ATTENDEE_STATUS_TENTATIVE:
                        newStatus = 1;
                        break;
                    default:
                        newStatus = 0;
                }
                if (notificationEventName == null
                        || (notificationEventBegin <= beginTime &&
                                notificationEventStatus < newStatus)) {
                    notificationEventName = eventName;
                    notificationEventLocation = location;
                    notificationEventBegin = beginTime;
                    notificationEventStatus = newStatus;
                }
            }
        } finally {
            if (alertCursor != null) {
                alertCursor.close();
            }
        }
        SharedPreferences prefs = CalendarPreferenceActivity.getSharedPreferences(context);
        String reminderType = prefs.getString(CalendarPreferenceActivity.KEY_ALERTS_TYPE,
                CalendarPreferenceActivity.ALERT_TYPE_STATUS_BAR);
        if (reminderType.equals(CalendarPreferenceActivity.ALERT_TYPE_OFF)) {
            if (DEBUG) {
                Log.d(TAG, "alert preference is OFF");
            }
            return true;
        }
        postNotification(context, prefs, notificationEventName, notificationEventLocation,
                numReminders, numFired == 0 );
        if (numFired > 0 && reminderType.equals(CalendarPreferenceActivity.ALERT_TYPE_ALERTS)) {
            Intent alertIntent = new Intent();
            alertIntent.setClass(context, AlertActivity.class);
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(alertIntent);
        }
        return true;
    }
    private static void postNotification(Context context, SharedPreferences prefs,
            String eventName, String location, int numReminders, boolean quietUpdate) {
        if (DEBUG) {
            Log.d(TAG, "###### creating new alarm notification, numReminders: " + numReminders
                    + (quietUpdate ? " QUIET" : " loud"));
        }
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (numReminders == 0) {
            nm.cancel(0);
            return;
        }
        Notification notification = AlertReceiver.makeNewAlertNotification(context, eventName,
                location, numReminders);
        notification.defaults |= Notification.DEFAULT_LIGHTS;
        if (!quietUpdate) {
            notification.tickerText = eventName;
            if (!TextUtils.isEmpty(location)) {
                notification.tickerText = eventName + " - " + location;
            }
            String vibrateWhen; 
            if(prefs.contains(CalendarPreferenceActivity.KEY_ALERTS_VIBRATE_WHEN))
            {
                vibrateWhen =
                    prefs.getString(CalendarPreferenceActivity.KEY_ALERTS_VIBRATE_WHEN, null);
            } else if(prefs.contains(CalendarPreferenceActivity.KEY_ALERTS_VIBRATE)) {
                boolean vibrate =
                    prefs.getBoolean(CalendarPreferenceActivity.KEY_ALERTS_VIBRATE, false);
                vibrateWhen = vibrate ?
                    context.getString(R.string.prefDefault_alerts_vibrate_true) :
                    context.getString(R.string.prefDefault_alerts_vibrate_false);
            } else {
                vibrateWhen = context.getString(R.string.prefDefault_alerts_vibrateWhen);
            }
            boolean vibrateAlways = vibrateWhen.equals("always");
            boolean vibrateSilent = vibrateWhen.equals("silent");
            AudioManager audioManager =
                (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            boolean nowSilent =
                audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE;
            if (vibrateAlways || (vibrateSilent && nowSilent)) {
                notification.defaults |= Notification.DEFAULT_VIBRATE;
            }
            String reminderRingtone = prefs.getString(
                    CalendarPreferenceActivity.KEY_ALERTS_RINGTONE, null);
            notification.sound = TextUtils.isEmpty(reminderRingtone) ? null : Uri
                    .parse(reminderRingtone);
        }
        nm.notify(0, notification);
    }
    private void doTimeChanged() {
        ContentResolver cr = getContentResolver();
        Object service = getSystemService(Context.ALARM_SERVICE);
        AlarmManager manager = (AlarmManager) service;
        CalendarAlerts.rescheduleMissedAlarms(cr, this, manager);
        updateAlertNotification(this);
    }
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            processMessage(msg);
            AlertReceiver.finishStartingService(AlertService.this, msg.arg1);
        }
    }
    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("AlertService",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            msg.obj = intent.getExtras();
            mServiceHandler.sendMessage(msg);
        }
        return START_REDELIVER_INTENT;
    }
    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
