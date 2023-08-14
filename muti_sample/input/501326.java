public class MorseCode extends Activity
{
    private static final String TAG = "MorseCode";
    private TextView mTextView;
    ;
    @Override
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.morse_code);
        findViewById(R.id.button).setOnClickListener(mClickListener);
        mTextView = (TextView)findViewById(R.id.text);
    }
    View.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            String text = mTextView.getText().toString();
            long[] pattern = MorseCodeConverter.pattern(text);
            Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(pattern, -1);
        }
    };
}
