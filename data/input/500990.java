public class StyledText extends Activity
{
    @Override
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.styled_text);
        CharSequence str = getText(R.string.styled_text);
        TextView tv = (TextView)findViewById(R.id.text);
        tv.setText(str);
    }
}
