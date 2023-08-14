public class BluetoothPbapCallLogComposer {
    private static final String TAG = "CallLogComposer";
    private static final String FAILURE_REASON_FAILED_TO_GET_DATABASE_INFO =
        "Failed to get database information";
    private static final String FAILURE_REASON_NO_ENTRY =
        "There's no exportable in the database";
    private static final String FAILURE_REASON_NOT_INITIALIZED =
        "The vCard composer object is not correctly initialized";
    private static final String FAILURE_REASON_UNSUPPORTED_URI =
        "The Uri vCard composer received is not supported by the composer.";
    private static final String NO_ERROR = "No error";
    private static final String[] sCallLogProjection = new String[] {
            Calls.NUMBER, Calls.DATE, Calls.TYPE, Calls.CACHED_NAME, Calls.CACHED_NUMBER_TYPE,
            Calls.CACHED_NUMBER_LABEL
    };
    private static final int NUMBER_COLUMN_INDEX = 0;
    private static final int DATE_COLUMN_INDEX = 1;
    private static final int CALL_TYPE_COLUMN_INDEX = 2;
    private static final int CALLER_NAME_COLUMN_INDEX = 3;
    private static final int CALLER_NUMBERTYPE_COLUMN_INDEX = 4;
    private static final int CALLER_NUMBERLABEL_COLUMN_INDEX = 5;
    private static final String VCARD_PROPERTY_X_TIMESTAMP = "X-IRMC-CALL-DATETIME";
    private static final String VCARD_PROPERTY_CALLTYPE_INCOMING = "INCOMING";
    private static final String VCARD_PROPERTY_CALLTYPE_OUTGOING = "OUTGOING";
    private static final String VCARD_PROPERTY_CALLTYPE_MISSED = "MISSED";
    private static final String FLAG_TIMEZONE_UTC = "Z";
    private final Context mContext;
    private ContentResolver mContentResolver;
    private Cursor mCursor;
    private final boolean mCareHandlerErrors;
    private boolean mTerminateIsCalled;
    private final List<OneEntryHandler> mHandlerList;
    private String mErrorReason = NO_ERROR;
    public BluetoothPbapCallLogComposer(final Context context, boolean careHandlerErrors) {
        mContext = context;
        mContentResolver = context.getContentResolver();
        mCareHandlerErrors = careHandlerErrors;
        mHandlerList = new ArrayList<OneEntryHandler>();
    }
    public boolean init(final Uri contentUri, final String selection,
            final String[] selectionArgs, final String sortOrder) {
        final String[] projection;
        if (CallLog.Calls.CONTENT_URI.equals(contentUri)) {
            projection = sCallLogProjection;
        } else {
            mErrorReason = FAILURE_REASON_UNSUPPORTED_URI;
            return false;
        }
        mCursor = mContentResolver.query(
                contentUri, projection, selection, selectionArgs, sortOrder);
        if (mCursor == null) {
            mErrorReason = FAILURE_REASON_FAILED_TO_GET_DATABASE_INFO;
            return false;
        }
        if (mCareHandlerErrors) {
            List<OneEntryHandler> finishedList = new ArrayList<OneEntryHandler>(
                    mHandlerList.size());
            for (OneEntryHandler handler : mHandlerList) {
                if (!handler.onInit(mContext)) {
                    for (OneEntryHandler finished : finishedList) {
                        finished.onTerminate();
                    }
                    return false;
                }
            }
        } else {
            for (OneEntryHandler handler : mHandlerList) {
                handler.onInit(mContext);
            }
        }
        if (mCursor.getCount() == 0 || !mCursor.moveToFirst()) {
            try {
                mCursor.close();
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLiteException on Cursor#close(): " + e.getMessage());
            } finally {
                mErrorReason = FAILURE_REASON_NO_ENTRY;
                mCursor = null;
            }
            return false;
        }
        return true;
    }
    public void addHandler(OneEntryHandler handler) {
        if (handler != null) {
            mHandlerList.add(handler);
        }
    }
    public boolean createOneEntry() {
        if (mCursor == null || mCursor.isAfterLast()) {
            mErrorReason = FAILURE_REASON_NOT_INITIALIZED;
            return false;
        }
        final String vcard;
        try {
            vcard = createOneCallLogEntryInternal();
        } catch (OutOfMemoryError error) {
            Log.e(TAG, "OutOfMemoryError occured. Ignore the entry");
            System.gc();
            return true;
        } finally {
            mCursor.moveToNext();
        }
        if (mCareHandlerErrors) {
            List<OneEntryHandler> finishedList = new ArrayList<OneEntryHandler>(
                    mHandlerList.size());
            for (OneEntryHandler handler : mHandlerList) {
                if (!handler.onEntryCreated(vcard)) {
                    return false;
                }
            }
        } else {
            for (OneEntryHandler handler : mHandlerList) {
                handler.onEntryCreated(vcard);
            }
        }
        return true;
    }
    private String createOneCallLogEntryInternal() {
        final VCardBuilder builder = new VCardBuilder(VCardConfig.VCARD_TYPE_V21_GENERIC_UTF8);
        String name = mCursor.getString(CALLER_NAME_COLUMN_INDEX);
        if (TextUtils.isEmpty(name)) {
            name = mCursor.getString(NUMBER_COLUMN_INDEX);
        }
        final boolean needCharset = !(VCardUtils.containsOnlyPrintableAscii(name));
        builder.appendLine(VCardConstants.PROPERTY_FN, name, needCharset, false);
        builder.appendLine(VCardConstants.PROPERTY_N, name, needCharset, false);
        final String number = mCursor.getString(NUMBER_COLUMN_INDEX);
        final int type = mCursor.getInt(CALLER_NUMBERTYPE_COLUMN_INDEX);
        String label = mCursor.getString(CALLER_NUMBERLABEL_COLUMN_INDEX);
        if (TextUtils.isEmpty(label)) {
            label = Integer.toString(type);
        }
        builder.appendTelLine(type, label, number, false);
        tryAppendCallHistoryTimeStampField(builder);
        return builder.toString();
    }
    public String composeVCardForPhoneOwnNumber(int phonetype, String phoneName,
            String phoneNumber, boolean vcardVer21) {
        final int vcardType = (vcardVer21 ?
                VCardConfig.VCARD_TYPE_V21_GENERIC_UTF8 :
                    VCardConfig.VCARD_TYPE_V30_GENERIC_UTF8);
        final VCardBuilder builder = new VCardBuilder(vcardType);
        boolean needCharset = false;
        if (!(VCardUtils.containsOnlyPrintableAscii(phoneName))) {
            needCharset = true;
        }
        builder.appendLine(VCardConstants.PROPERTY_FN, phoneName, needCharset, false);
        builder.appendLine(VCardConstants.PROPERTY_N, phoneName, needCharset, false);
        if (!TextUtils.isEmpty(phoneNumber)) {
            String label = Integer.toString(phonetype);
            builder.appendTelLine(phonetype, label, phoneNumber, false);
        }
        return builder.toString();
    }
    private final String toRfc2455Format(final long millSecs) {
        Time startDate = new Time();
        startDate.set(millSecs);
        String date = startDate.format2445();
        return date + FLAG_TIMEZONE_UTC;
    }
    private void tryAppendCallHistoryTimeStampField(final VCardBuilder builder) {
        final int callLogType = mCursor.getInt(CALL_TYPE_COLUMN_INDEX);
        final String callLogTypeStr;
        switch (callLogType) {
            case Calls.INCOMING_TYPE: {
                callLogTypeStr = VCARD_PROPERTY_CALLTYPE_INCOMING;
                break;
            }
            case Calls.OUTGOING_TYPE: {
                callLogTypeStr = VCARD_PROPERTY_CALLTYPE_OUTGOING;
                break;
            }
            case Calls.MISSED_TYPE: {
                callLogTypeStr = VCARD_PROPERTY_CALLTYPE_MISSED;
                break;
            }
            default: {
                Log.w(TAG, "Call log type not correct.");
                return;
            }
        }
        final long dateAsLong = mCursor.getLong(DATE_COLUMN_INDEX);
        builder.appendLine(VCARD_PROPERTY_X_TIMESTAMP,
                Arrays.asList(callLogTypeStr), toRfc2455Format(dateAsLong));
    }
    public void terminate() {
        for (OneEntryHandler handler : mHandlerList) {
            handler.onTerminate();
        }
        if (mCursor != null) {
            try {
                mCursor.close();
            } catch (SQLiteException e) {
                Log.e(TAG, "SQLiteException on Cursor#close(): " + e.getMessage());
            }
            mCursor = null;
        }
        mTerminateIsCalled = true;
    }
    @Override
    public void finalize() {
        if (!mTerminateIsCalled) {
            terminate();
        }
    }
    public int getCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }
    public boolean isAfterLast() {
        if (mCursor == null) {
            return false;
        }
        return mCursor.isAfterLast();
    }
    public String getErrorReason() {
        return mErrorReason;
    }
}
