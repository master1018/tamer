public class Forwarding extends Activity
{
    @Override
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forwarding);
        Button goButton = (Button)findViewById(R.id.go);
        goButton.setOnClickListener(mGoListener);
    }
    private OnClickListener mGoListener = new OnClickListener()
    {
        public void onClick(View v)
        {
            Intent intent = new Intent();
            intent.setClass(Forwarding.this, ForwardTarget.class);
            startActivity(intent);
            finish();
        }
    };
}
