public class ContactsLaunchPerformance extends LaunchPerformanceBase {
    @Override
    public void onCreate(Bundle arguments) {
        mIntent.setAction(Intent.ACTION_MAIN);
        mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mIntent.setComponent(new ComponentName("com.android.contacts",
                "com.android.contacts.DialtactsContactsEntryActivity"));
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}
