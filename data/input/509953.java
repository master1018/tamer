public class ContactsFilterInstrumentation extends Instrumentation {
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(getTargetContext(),
                "com.android.phone.Dialer");
        Activity activity = startActivitySync(intent);
        Log.i("ContactsFilterInstrumentation", "Started: " + activity);
        sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_M));
        sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_M));
        sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A));
        sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_A));
        waitForIdleSync();
        Log.i("ContactsFilterInstrumentation", "Done!");
        finish(Activity.RESULT_OK, null);
    }
}
