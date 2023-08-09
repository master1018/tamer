public class SeekBar1 extends Activity implements SeekBar.OnSeekBarChangeListener {
    SeekBar mSeekBar;
    TextView mProgressText;
    TextView mTrackingText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seekbar_1);
        mSeekBar = (SeekBar)findViewById(R.id.seek);
        mSeekBar.setOnSeekBarChangeListener(this);
        mProgressText = (TextView)findViewById(R.id.progress);
        mTrackingText = (TextView)findViewById(R.id.tracking);
    }
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        mProgressText.setText(progress + " " + 
                getString(R.string.seekbar_from_touch) + "=" + fromTouch);
    }
    public void onStartTrackingTouch(SeekBar seekBar) {
        mTrackingText.setText(getString(R.string.seekbar_tracking_on));
    }
    public void onStopTrackingTouch(SeekBar seekBar) {
        mTrackingText.setText(getString(R.string.seekbar_tracking_off));
    }
}
