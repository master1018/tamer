public class ContactsSelectInstrumentation extends Instrumentation {
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
        Log.i("ContactsSelectInstrumentation", "Started: " + activity);
        ActivityMonitor am = addMonitor(IntentFilter.create(
            Intent.ACTION_VIEW, Contacts.People.CONTENT_ITEM_TYPE), null, true);
        sendKeySync(new KeyEvent(
            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
        sendKeySync(new KeyEvent(
            KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN));
        sendKeySync(new KeyEvent(
            KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER));
        sendKeySync(new KeyEvent(
            KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER));
        if (checkMonitorHit(am, 1)) {
            Log.i("ContactsSelectInstrumentation", "Activity started!");
        } else {
            Log.i("ContactsSelectInstrumentation", "*** ACTIVITY NOT STARTED!");
        }
        Log.i("ContactsSelectInstrumentation", "Done!");
        finish(Activity.RESULT_OK, null);
    }
}
