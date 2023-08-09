public class MockReceiverFirst extends BroadcastReceiver {
    public static final int RESULT_CODE = 2;
    public static final String RESULT_DATA = "first";
    public static final String RESULT_EXTRAS_FIRST_KEY = "first";
    public static final String RESULT_EXTRAS_FIRST_VALUE = "first value";
    public void onReceive(Context context, Intent intent) {
        Bundle map = getResultExtras(false);
        map.putString(RESULT_EXTRAS_FIRST_KEY, RESULT_EXTRAS_FIRST_VALUE);
        setResult(RESULT_CODE, RESULT_DATA, map);
    }
}
