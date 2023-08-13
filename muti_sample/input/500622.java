public abstract class AlertActivity extends Activity implements DialogInterface {
    protected AlertController mAlert;
    protected AlertController.AlertParams mAlertParams;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAlert = new AlertController(this, this, getWindow());
        mAlertParams = new AlertController.AlertParams(this);        
    }
    public void cancel() {
        finish();
    }
    public void dismiss() {
        if (!isFinishing()) {
            finish();
        }
    }
    protected void setupAlert() {
        mAlertParams.apply(mAlert);
        mAlert.installContent();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mAlert.onKeyDown(keyCode, event)) return true;
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mAlert.onKeyUp(keyCode, event)) return true;
        return super.onKeyUp(keyCode, event);
    }
}
