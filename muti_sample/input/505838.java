public class DeleteEventHelper {
    private final Activity mParent;
    private final ContentResolver mContentResolver;
    private long mStartMillis;
    private long mEndMillis;
    private Cursor mCursor;
    private boolean mExitWhenDone;
    static final int DELETE_SELECTED = 0;
    static final int DELETE_ALL_FOLLOWING = 1;
    static final int DELETE_ALL = 2;
    private int mWhichDelete;
    private AlertDialog mAlertDialog;
    private static final String[] EVENT_PROJECTION = new String[] {
        Events._ID,
        Events.TITLE,
        Events.ALL_DAY,
        Events.CALENDAR_ID,
        Events.RRULE,
        Events.DTSTART,
        Events._SYNC_ID,
        Events.EVENT_TIMEZONE,
    };
    private int mEventIndexId;
    private int mEventIndexRrule;
    private String mSyncId;
    public DeleteEventHelper(Activity parent, boolean exitWhenDone) {
        mParent = parent;
        mContentResolver = mParent.getContentResolver();
        mExitWhenDone = exitWhenDone;
    }
    public void setExitWhenDone(boolean exitWhenDone) {
        mExitWhenDone = exitWhenDone;
    }
    private DialogInterface.OnClickListener mDeleteNormalDialogListener =
            new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int button) {
            long id = mCursor.getInt(mEventIndexId);
            Uri uri = ContentUris.withAppendedId(Calendar.Events.CONTENT_URI, id);
            mContentResolver.delete(uri, null , null );
            if (mExitWhenDone) {
                mParent.finish();
            }
        }
    };
    private DialogInterface.OnClickListener mDeleteListListener =
            new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int button) {
            mWhichDelete = button;
            Button ok = mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            ok.setEnabled(true);
        }
    };
    private DialogInterface.OnClickListener mDeleteRepeatingDialogListener =
            new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int button) {
            if (mWhichDelete != -1) {
                deleteRepeatingEvent(mWhichDelete);
            }
        }
    };
    public void delete(long begin, long end, long eventId, int which) {
        Uri uri = ContentUris.withAppendedId(Calendar.Events.CONTENT_URI, eventId);
        Cursor cursor = mParent.managedQuery(uri, EVENT_PROJECTION, null, null, null);
        if (cursor == null) {
            return;
        }
        cursor.moveToFirst();
        delete(begin, end, cursor, which);
    }
    public void delete(long begin, long end, Cursor cursor, int which) {
        mWhichDelete = which;
        mStartMillis = begin;
        mEndMillis = end;
        mCursor = cursor;
        mEventIndexId = mCursor.getColumnIndexOrThrow(Events._ID);
        mEventIndexRrule = mCursor.getColumnIndexOrThrow(Events.RRULE);
        int eventIndexSyncId = mCursor.getColumnIndexOrThrow(Events._SYNC_ID);
        mSyncId = mCursor.getString(eventIndexSyncId);
        String rRule = mCursor.getString(mEventIndexRrule);
        if (rRule == null) {
            new AlertDialog.Builder(mParent)
            .setTitle(R.string.delete_title)
            .setMessage(R.string.delete_this_event_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.ok, mDeleteNormalDialogListener)
            .setNegativeButton(android.R.string.cancel, null)
            .show();
        } else {
            int labelsArrayId = R.array.delete_repeating_labels;
            if (mSyncId == null) {
                labelsArrayId = R.array.delete_repeating_labels_no_selected;
            }
            AlertDialog dialog = new AlertDialog.Builder(mParent)
            .setTitle(R.string.delete_title)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setSingleChoiceItems(labelsArrayId, which, mDeleteListListener)
            .setPositiveButton(android.R.string.ok, mDeleteRepeatingDialogListener)
            .setNegativeButton(android.R.string.cancel, null)
            .show();
            mAlertDialog = dialog;
            if (which == -1) {
                Button ok = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                ok.setEnabled(false);
            }
        }
    }
    private void deleteRepeatingEvent(int which) {
        int indexDtstart = mCursor.getColumnIndexOrThrow(Events.DTSTART);
        int indexAllDay = mCursor.getColumnIndexOrThrow(Events.ALL_DAY);
        int indexTitle = mCursor.getColumnIndexOrThrow(Events.TITLE);
        int indexTimezone = mCursor.getColumnIndexOrThrow(Events.EVENT_TIMEZONE);
        int indexCalendarId = mCursor.getColumnIndexOrThrow(Events.CALENDAR_ID);
        String rRule = mCursor.getString(mEventIndexRrule);
        boolean allDay = mCursor.getInt(indexAllDay) != 0;
        long dtstart = mCursor.getLong(indexDtstart);
        long id = mCursor.getInt(mEventIndexId);
        if (mSyncId == null) {
            which += 1;
        }
        switch (which) {
            case DELETE_SELECTED:
            {
                if (dtstart == mStartMillis) {
                }
                ContentValues values = new ContentValues();
                String title = mCursor.getString(indexTitle);
                values.put(Events.TITLE, title);
                String timezone = mCursor.getString(indexTimezone);
                int calendarId = mCursor.getInt(indexCalendarId);
                values.put(Events.EVENT_TIMEZONE, timezone);
                values.put(Events.ALL_DAY, allDay ? 1 : 0);
                values.put(Events.CALENDAR_ID, calendarId);
                values.put(Events.DTSTART, mStartMillis);
                values.put(Events.DTEND, mEndMillis);
                values.put(Events.ORIGINAL_EVENT, mSyncId);
                values.put(Events.ORIGINAL_INSTANCE_TIME, mStartMillis);
                values.put(Events.STATUS, Events.STATUS_CANCELED);
                mContentResolver.insert(Events.CONTENT_URI, values);
                break;
            }
            case DELETE_ALL: {
                Uri uri = ContentUris.withAppendedId(Calendar.Events.CONTENT_URI, id);
                mContentResolver.delete(uri, null , null );
                break;
            }
            case DELETE_ALL_FOLLOWING: {
                if (dtstart == mStartMillis) {
                    Uri uri = ContentUris.withAppendedId(Calendar.Events.CONTENT_URI, id);
                    mContentResolver.delete(uri, null , null );
                    break;
                }
                EventRecurrence eventRecurrence = new EventRecurrence();
                eventRecurrence.parse(rRule);
                Time date = new Time();
                if (allDay) {
                    date.timezone = Time.TIMEZONE_UTC;
                }
                date.set(mStartMillis);
                date.second--;
                date.normalize(false);
                date.switchTimezone(Time.TIMEZONE_UTC);
                eventRecurrence.until = date.format2445();
                ContentValues values = new ContentValues();
                values.put(Events.DTSTART, dtstart);
                values.put(Events.RRULE, eventRecurrence.toString());
                Uri uri = ContentUris.withAppendedId(Calendar.Events.CONTENT_URI, id);
                mContentResolver.update(uri, values, null, null);
                break;
            }
        }
        if (mExitWhenDone) {
            mParent.finish();
        }
    }
}
