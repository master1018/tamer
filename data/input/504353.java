public class ClearTop extends Activity {
    public static final String WAIT_CLEAR_TASK = "waitClearTask";
    public ClearTop() {
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = new Intent(getIntent()).setAction(LocalScreen.CLEAR_TASK)
                .setClass(this, LocalScreen.class);
        startActivity(intent);
    }
    @Override
    public void onNewIntent(Intent intent) {
        if (LocalScreen.CLEAR_TASK.equals(intent.getAction())) {
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED, new Intent().setAction(
                    "New intent received " + intent + ", expecting action "
                    + TestedScreen.CLEAR_TASK));
        }
        finish();
    }
}
