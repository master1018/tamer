public class CallLogAsync {
    private static final String TAG = "CallLogAsync";
    public static class AddCallArgs {
        public AddCallArgs(Context context,
                           CallerInfo ci,
                           String number,
                           int presentation,
                           int callType,
                           long timestamp,
                           long durationInMillis) {
            this.context = context;
            this.ci = ci;
            this.number = number;
            this.presentation = presentation;
            this.callType = callType;
            this.timestamp = timestamp;
            this.durationInSec = (int)(durationInMillis / 1000);
        }
        public final Context context;
        public final CallerInfo ci;
        public final String number;
        public final int presentation;
        public final int callType;
        public final long timestamp;
        public final int durationInSec;
    }
    public static class GetLastOutgoingCallArgs {
        public GetLastOutgoingCallArgs(Context context,
                                       OnLastOutgoingCallComplete callback) {
            this.context = context;
            this.callback = callback;
        }
        public final Context context;
        public final OnLastOutgoingCallComplete callback;
    }
    public AsyncTask addCall(AddCallArgs args) {
        assertUiThread();
        return new AddCallTask().execute(args);
    }
    public interface OnLastOutgoingCallComplete {
        void lastOutgoingCall(String number);
    }
    public AsyncTask getLastOutgoingCall(GetLastOutgoingCallArgs args) {
        assertUiThread();
        return new GetLastOutgoingCallTask(args.callback).execute(args);
    }
    private class AddCallTask extends AsyncTask<AddCallArgs, Void, Uri[]> {
        @Override
        protected Uri[] doInBackground(AddCallArgs... callList) {
            int count = callList.length;
            Uri[] result = new Uri[count];
            for (int i = 0; i < count; i++) {
                AddCallArgs c = callList[i];
                result[i] = Calls.addCall(
                    c.ci, c.context, c.number, c.presentation,
                    c.callType, c.timestamp, c.durationInSec);
            }
            return result;
        }
        @Override
        protected void onPostExecute(Uri[] result) {
            for (Uri uri : result) {
                if (uri == null) {
                    Log.e(TAG, "Failed to write call to the log.");
                }
            }
        }
    }
    private class GetLastOutgoingCallTask extends AsyncTask<GetLastOutgoingCallArgs, Void, String> {
        private final OnLastOutgoingCallComplete mCallback;
        private String mNumber;
        public GetLastOutgoingCallTask(OnLastOutgoingCallComplete callback) {
            mCallback = callback;
        }
        @Override
        protected String doInBackground(GetLastOutgoingCallArgs... list) {
            int count = list.length;
            String number = "";
            for (GetLastOutgoingCallArgs args : list) {
                number = Calls.getLastOutgoingCall(args.context);
            }
            return number;  
        }
        @Override
        protected void onPostExecute(String number) {
            assertUiThread();
            mCallback.lastOutgoingCall(number);
        }
    }
    private void assertUiThread() {
        if (!Looper.getMainLooper().equals(Looper.myLooper())) {
            throw new RuntimeException("Not on the UI thread!");
        }
    }
}
