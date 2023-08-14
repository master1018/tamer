public class ProcessOutgoingCallTest extends BroadcastReceiver {
    private static final String TAG = "ProcessOutgoingCallTest";
    private static final String AREACODE = "617";
    private static final boolean LOGV = Config.LOGV;
    private static final boolean REDIRECT_411_TO_GOOG411 = true;
    private static final boolean SEVEN_DIGIT_DIALING = true;
    private static final boolean POUND_POUND_SEARCH = true;
    private static final boolean BLOCK_555 = true;
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            if (LOGV) Log.v(TAG, "Received intent " + intent + " (number = " + number + ".");
            if (REDIRECT_411_TO_GOOG411 && number.equals("411")) {
                setResultData("18004664411");
            }
            if (SEVEN_DIGIT_DIALING && number.length() == 7) {
                setResultData(AREACODE + number);
            }
            if (POUND_POUND_SEARCH && number.startsWith("##")) {
                Intent newIntent = new Intent(Intent.ACTION_SEARCH);
                newIntent.putExtra(SearchManager.QUERY, number.substring(2));
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(newIntent);
                setResultData(null);
            }
            int length = number.length();
            if (BLOCK_555 && length >= 7) {
                String exchange = number.substring(length - 7, length - 4);
                Log.v(TAG, "exchange = " + exchange);
                if (exchange.equals("555")) {
                    setResultData(null);
                }
            }
        }
    }
}
