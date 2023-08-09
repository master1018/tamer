public class RedirectEnter extends Activity
{
    @Override
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.redirect_enter);
        Button goButton = (Button)findViewById(R.id.go);
        goButton.setOnClickListener(mGoListener);
    }
    private OnClickListener mGoListener = new OnClickListener()
    {
        public void onClick(View v)
        {
            Intent intent = new Intent(RedirectEnter.this, RedirectMain.class);
            startActivity(intent);
        }
    };
}
