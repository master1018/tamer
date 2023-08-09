public class DeleteItems extends Activity
{
    private TextView mPrompt;
    private Button mButton;
    private long [] mItemList;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.confirm_delete);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                                    WindowManager.LayoutParams.WRAP_CONTENT);
        mPrompt = (TextView)findViewById(R.id.prompt);
        mButton = (Button) findViewById(R.id.delete);
        mButton.setOnClickListener(mButtonClicked);
        ((Button)findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        Bundle b = getIntent().getExtras();
        String desc = b.getString("description");
        mItemList = b.getLongArray("items");
        mPrompt.setText(desc);
    }
    private View.OnClickListener mButtonClicked = new View.OnClickListener() {
        public void onClick(View v) {
            MusicUtils.deleteTracks(DeleteItems.this, mItemList);
            finish();
        }
    };
}
