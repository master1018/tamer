public class ReadAsset extends Activity
{
    @Override
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_asset);
        try {
            InputStream is = getAssets().open("read_asset.txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String text = new String(buffer);
            TextView tv = (TextView)findViewById(R.id.text);
            tv.setText(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
