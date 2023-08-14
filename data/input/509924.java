public class MockReceiverAbort extends BroadcastReceiver {
    public static final int RESULT_CODE = 3;
    public static final String RESULT_DATA = "abort";
    public static final String RESULT_EXTRAS_ABORT_KEY = "abort";
    public static final String RESULT_EXTRAS_ABORT_VALUE = "abort value";
    public void onReceive(Context context, Intent intent) {
        Bundle map = getResultExtras(false);
        map.putString(RESULT_EXTRAS_ABORT_KEY, RESULT_EXTRAS_ABORT_VALUE);
        setResult(RESULT_CODE, RESULT_DATA, map);
        abortBroadcast();
    }
}
