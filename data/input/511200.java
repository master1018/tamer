public class ResourcesSample extends Activity
{
    @Override
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resources);
        TextView tv;
        CharSequence cs;
        String str;
        cs = getText(R.string.styled_text);
        tv = (TextView)findViewById(R.id.styled_text);
        tv.setText(cs);
        str = getString(R.string.styled_text);
        tv = (TextView)findViewById(R.id.plain_text);
        tv.setText(str);
        Context context = this;
        Resources res = context.getResources();
        cs = res.getText(R.string.styled_text);
        tv = (TextView)findViewById(R.id.res1);
        tv.setText(cs);
    }
}
