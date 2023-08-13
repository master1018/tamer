public class LocalSampleInstrumentation extends Instrumentation {
    public abstract static class ActivityRunnable implements Runnable {
        public final Activity activity;
        public ActivityRunnable(Activity _activity) {
            activity = _activity;
        }
    }
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(getTargetContext(), SaveRestoreState.class);
        SaveRestoreState activity = (SaveRestoreState)startActivitySync(intent);
        Log.i("LocalSampleInstrumentation",
              "Initial text: " + activity.getSavedText());
        runOnMainSync(new ActivityRunnable(activity) {
            public void run() {
                ((SaveRestoreState)activity).setSavedText("");
            }
        });
        sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT));
        sendCharacterSync(KeyEvent.KEYCODE_H);
        sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SHIFT_LEFT));
        sendCharacterSync(KeyEvent.KEYCODE_E);
        sendCharacterSync(KeyEvent.KEYCODE_L);
        sendCharacterSync(KeyEvent.KEYCODE_L);
        sendCharacterSync(KeyEvent.KEYCODE_O);
        waitForIdleSync();
        Log.i("LocalSampleInstrumentation",
              "Final text: " + activity.getSavedText());
        Log.i("ContactsFilterInstrumentation", "Done!");
        finish(Activity.RESULT_OK, null);
    }
}
