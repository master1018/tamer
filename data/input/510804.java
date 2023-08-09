public class PreconditionActivityHelper {
    public static Intent createPreconditionIntent(Activity activity,
            Class preconditionActivityClazz) {
        Intent newIntent = new Intent();
        newIntent.setClass(activity, preconditionActivityClazz);
        newIntent.putExtra(EXTRA_WRAPPED_INTENT, activity.getIntent());
        return newIntent;
    }
    public static void startPreconditionActivityAndFinish(Activity activity,
            Intent intent) {
        activity.startActivity(intent);
        activity.finish();
    }
    public static void startOriginalActivityAndFinish(
            Activity preconditionActivity) {
        preconditionActivity.startActivity(
                (Intent) preconditionActivity.getIntent()
                    .getParcelableExtra(EXTRA_WRAPPED_INTENT));
        preconditionActivity.finish();
    }
    static private final String EXTRA_WRAPPED_INTENT =
        "PreconditionActivityHelper_wrappedIntent";
}
