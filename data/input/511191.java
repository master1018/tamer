public class FillInWrap extends Activity {
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.fill_in_wrap);
        ((TextView) findViewById(R.id.data)).setText("1\n2\n3\n4\n5");
    }
}
