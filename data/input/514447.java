public class Client extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlatformLibrary pl = new PlatformLibrary();
        int res = pl.getInt(false);
        TextView tv = new TextView(this);
        tv.setText("Got from lib: " + res);
        setContentView(tv);
    }
}
