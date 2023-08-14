public class CallerInfoAsyncQuery {
    private static final boolean DBG = false;
    private static final String LOG_TAG = "CallerInfoAsyncQuery";
    private static final int EVENT_NEW_QUERY = 1;
    private static final int EVENT_ADD_LISTENER = 2;
    private static final int EVENT_END_OF_QUEUE = 3;
    private static final int EVENT_EMERGENCY_NUMBER = 4;
    private static final int EVENT_VOICEMAIL_NUMBER = 5;
    private CallerInfoAsyncQueryHandler mHandler;
    public interface OnQueryCompleteListener {
        public void onQueryComplete(int token, Object cookie, CallerInfo ci);
    }
    private static final class CookieWrapper {
        public OnQueryCompleteListener listener;
        public Object cookie;
        public int event;
        public String number;
    }
    public static class QueryPoolException extends SQLException {
        public QueryPoolException(String error) {
            super(error);
        }
    }
    private class CallerInfoAsyncQueryHandler extends AsyncQueryHandler {
        private Context mQueryContext;
        private Uri mQueryUri;
        private CallerInfo mCallerInfo;
        protected class CallerInfoWorkerHandler extends WorkerHandler {
            public CallerInfoWorkerHandler(Looper looper) {
                super(looper);
            }
            @Override
            public void handleMessage(Message msg) {
                WorkerArgs args = (WorkerArgs) msg.obj;
                CookieWrapper cw = (CookieWrapper) args.cookie;
                if (cw == null) {
                    if (DBG) log("Unexpected command (CookieWrapper is null): " + msg.what +
                            " ignored by CallerInfoWorkerHandler, passing onto parent.");
                    super.handleMessage(msg);
                } else {
                    if (DBG) log("Processing event: " + cw.event + " token (arg1): " + msg.arg1 +
                            " command: " + msg.what + " query URI: " + args.uri);
                    switch (cw.event) {
                        case EVENT_NEW_QUERY:
                            super.handleMessage(msg);
                            break;
                        case EVENT_EMERGENCY_NUMBER:
                        case EVENT_VOICEMAIL_NUMBER:
                        case EVENT_ADD_LISTENER:
                        case EVENT_END_OF_QUEUE:
                            Message reply = args.handler.obtainMessage(msg.what);
                            reply.obj = args;
                            reply.arg1 = msg.arg1;
                            reply.sendToTarget();
                            break;
                        default:
                    }
                }
            }
        }
        private CallerInfoAsyncQueryHandler(Context context) {
            super(context.getContentResolver());
        }
        @Override
        protected Handler createHandler(Looper looper) {
            return new CallerInfoWorkerHandler(looper);
        }
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (DBG) log("query complete for token: " + token);
            CookieWrapper cw = (CookieWrapper) cookie;
            if (cw == null) {
                if (DBG) log("Cookie is null, ignoring onQueryComplete() request.");
                return;
            }
            if (cw.event == EVENT_END_OF_QUEUE) {
                release();
                return;
            }
            if (mCallerInfo == null) {
                if ((mQueryContext == null) || (mQueryUri == null)) {
                    throw new QueryPoolException
                            ("Bad context or query uri, or CallerInfoAsyncQuery already released.");
                }
                if (cw.event == EVENT_EMERGENCY_NUMBER) {
                    mCallerInfo = new CallerInfo().markAsEmergency(mQueryContext);
                } else if (cw.event == EVENT_VOICEMAIL_NUMBER) {
                    mCallerInfo = new CallerInfo().markAsVoiceMail();
                } else {
                    mCallerInfo = CallerInfo.getCallerInfo(mQueryContext, mQueryUri, cursor);
                    if (!TextUtils.isEmpty(cw.number)) {
                        mCallerInfo.phoneNumber = PhoneNumberUtils.formatNumber(cw.number);
                    }
                }
                if (DBG) log("constructing CallerInfo object for token: " + token);
                CookieWrapper endMarker = new CookieWrapper();
                endMarker.event = EVENT_END_OF_QUEUE;
                startQuery (token, endMarker, null, null, null, null, null);
            }
            if (cw.listener != null) {
                if (DBG) log("notifying listener: " + cw.listener.getClass().toString() +
                             " for token: " + token + mCallerInfo);
                cw.listener.onQueryComplete(token, cw.cookie, mCallerInfo);
            }
        }
    }
    private CallerInfoAsyncQuery() {
    }
    public static CallerInfoAsyncQuery startQuery(int token, Context context, Uri contactRef,
            OnQueryCompleteListener listener, Object cookie) {
        CallerInfoAsyncQuery c = new CallerInfoAsyncQuery();
        c.allocate(context, contactRef);
        if (DBG) log("starting query for URI: " + contactRef + " handler: " + c.toString());
        CookieWrapper cw = new CookieWrapper();
        cw.listener = listener;
        cw.cookie = cookie;
        cw.event = EVENT_NEW_QUERY;
        c.mHandler.startQuery (token, cw, contactRef, null, null, null, null);
        return c;
    }
    public static CallerInfoAsyncQuery startQuery(int token, Context context, String number,
            OnQueryCompleteListener listener, Object cookie) {
        Uri contactRef = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        CallerInfoAsyncQuery c = new CallerInfoAsyncQuery();
        c.allocate(context, contactRef);
        if (DBG) log("starting query for number: " + number + " handler: " + c.toString());
        CookieWrapper cw = new CookieWrapper();
        cw.listener = listener;
        cw.cookie = cookie;
        cw.number = number;
        if (PhoneNumberUtils.isEmergencyNumber(number)) {
            cw.event = EVENT_EMERGENCY_NUMBER;
        } else if (PhoneNumberUtils.isVoiceMailNumber(number)) {
            cw.event = EVENT_VOICEMAIL_NUMBER;
        } else {
            cw.event = EVENT_NEW_QUERY;
        }
        c.mHandler.startQuery (token, cw, contactRef, null, null, null, null);
        return c;
   }
    public void addQueryListener(int token, OnQueryCompleteListener listener, Object cookie) {
        if (DBG) log("adding listener to query: " + mHandler.mQueryUri + " handler: " +
                mHandler.toString());
        CookieWrapper cw = new CookieWrapper();
        cw.listener = listener;
        cw.cookie = cookie;
        cw.event = EVENT_ADD_LISTENER;
        mHandler.startQuery (token, cw, null, null, null, null, null);
    }
    private void allocate (Context context, Uri contactRef) {
        if ((context == null) || (contactRef == null)){
            throw new QueryPoolException("Bad context or query uri.");
        }
        mHandler = new CallerInfoAsyncQueryHandler(context);
        mHandler.mQueryContext = context;
        mHandler.mQueryUri = contactRef;
    }
    private void release () {
        mHandler.mQueryContext = null;
        mHandler.mQueryUri = null;
        mHandler.mCallerInfo = null;
        mHandler = null;
    }
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
