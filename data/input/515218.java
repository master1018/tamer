public class SeekBarStubActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seekbar);
        View v = findViewById(R.id.seekBar);
        v.setEnabled(true);
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        v.requestFocus();
    }
}
