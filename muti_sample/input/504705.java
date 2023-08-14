public class ActivityManagerStubFooActivity extends Activity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final TextView tv = new TextView(this);
        tv.setText("Hello, Android");
        setContentView(tv);
    }
}
