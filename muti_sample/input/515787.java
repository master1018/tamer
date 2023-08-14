public class InCallScreenShowActivation extends Activity {
    private static final String LOG_TAG = "InCallScreenShowActivation";
    private static final String EXTRA_USER_SKIP_PENDING_INTENT = "ota_user_skip_pending_intent";
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        if (intent.getAction().equals(InCallScreen.ACTION_SHOW_ACTIVATION)) {
            Intent newIntent = new Intent().setClass(this, InCallScreen.class)
                    .setAction(InCallScreen.ACTION_SHOW_ACTIVATION);
            PhoneApp app = PhoneApp.getInstance();
            app.cdmaOtaInCallScreenUiState.reportSkipPendingIntent = (PendingIntent) intent
                    .getParcelableExtra(EXTRA_USER_SKIP_PENDING_INTENT);
            startActivity(newIntent);
        } else {
            Log.e(LOG_TAG, "Inappropriate launch of InCallScreenShowActivation");
        }
        finish();
    }
}
