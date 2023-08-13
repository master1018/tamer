public class MockApplicationActivity extends Activity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        TextView textView = new TextView(this);
        textView.setText("Test");
        setContentView(textView);
    }
}
