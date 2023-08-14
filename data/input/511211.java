public class Intents extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intents);
        Button button = (Button)findViewById(R.id.get_music);
        button.setOnClickListener(mGetMusicListener);
    }
    private OnClickListener mGetMusicListener = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            startActivity(Intent.createChooser(intent, "Select music"));
        }
    };
}
