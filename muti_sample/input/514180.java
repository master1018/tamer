public class SendResult extends Activity
{
    @Override
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_result);
        Button button = (Button)findViewById(R.id.corky);
        button.setOnClickListener(mCorkyListener);
        button = (Button)findViewById(R.id.violet);
        button.setOnClickListener(mVioletListener);
    }
    private OnClickListener mCorkyListener = new OnClickListener()
    {
        public void onClick(View v)
        {
            setResult(RESULT_OK, (new Intent()).setAction("Corky!"));
            finish();
        }
    };
    private OnClickListener mVioletListener = new OnClickListener()
    {
        public void onClick(View v)
        {
            setResult(RESULT_OK, (new Intent()).setAction("Violet!"));
            finish();
        }
    };
}
