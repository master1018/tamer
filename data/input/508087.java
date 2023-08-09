public class LocalSample extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_sample);
        Button button = (Button)findViewById(R.id.go);
        button.setOnClickListener(mGoListener);
    }
    private OnClickListener mGoListener = new OnClickListener() {
        public void onClick(View v) {
            startInstrumentation(new ComponentName(LocalSample.this,
                    LocalSampleInstrumentation.class), null, null);
        }
    };
}
