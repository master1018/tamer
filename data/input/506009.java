public class DownloadVoiceData extends Activity {
    private final static String MARKET_URI = "market:
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri marketUri = Uri.parse(MARKET_URI);
        Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
        startActivityForResult(marketIntent, 0);
        finish();
    }
}
