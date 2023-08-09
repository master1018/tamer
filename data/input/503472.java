public class ActivityManagerStubCrashActivity extends Activity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final TextView tv = new TextView(this);
        tv.setText("Hello, Android but crash");
        setContentView(tv);
        die();
    }
    public void die() {
        throw new NullPointerException("Expected NPE.");
    }
}
