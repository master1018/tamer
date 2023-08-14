public class SignatureTestLog {
    private static final String TAG = "CTSSignatureTest";
    public static void e(String msg, Exception e) {
        Log.e(TAG, msg, e);
    }
    public static void d(String msg) {
        Log.d(TAG, msg);
    }
}
