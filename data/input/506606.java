public class JetBoy extends Activity implements View.OnClickListener {
    private JetBoyThread mJetBoyThread;
    private JetBoyView mJetBoyView;
    private Button mButton;
    private Button mButtonRetry;
    private TextView mTextView;
    private TextView mTimerView;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mJetBoyView = (JetBoyView)findViewById(R.id.JetBoyView);
        mJetBoyThread = mJetBoyView.getThread();
        mButton = (Button)findViewById(R.id.Button01);
        mButton.setOnClickListener(this);
        mButtonRetry = (Button)findViewById(R.id.Button02);
        mButtonRetry.setOnClickListener(this);
        mTextView = (TextView)findViewById(R.id.text);
        mTimerView = (TextView)findViewById(R.id.timer);
        mJetBoyView.setTimerView(mTimerView);
        mJetBoyView.SetButtonView(mButtonRetry);
        mJetBoyView.SetTextView(mTextView);
    }
    public void onClick(View v) {
        if (mJetBoyThread.getGameState() == JetBoyThread.STATE_START) {
            mButton.setText("PLAY!");
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(R.string.helpText);
            mJetBoyThread.setGameState(JetBoyThread.STATE_PLAY);
        }
        else if (mJetBoyThread.getGameState() == JetBoyThread.STATE_PLAY) {
            mButton.setVisibility(View.INVISIBLE);
            mTextView.setVisibility(View.INVISIBLE);
            mTimerView.setVisibility(View.VISIBLE);
            mJetBoyThread.setGameState(JetBoyThread.STATE_RUNNING);
        }
        else if (mButtonRetry.equals(v)) {
            mTextView.setText(R.string.helpText);
            mButton.setText("PLAY!");
            mButtonRetry.setVisibility(View.INVISIBLE);
            mTextView.setVisibility(View.VISIBLE);
            mButton.setText("PLAY!");
            mButton.setVisibility(View.VISIBLE);
            mJetBoyThread.setGameState(JetBoyThread.STATE_PLAY);
        } else {
            Log.d("JB VIEW", "unknown click " + v.getId());
            Log.d("JB VIEW", "state is  " + mJetBoyThread.mState);
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, msg);
        } else {
            return mJetBoyThread.doKeyDown(keyCode, msg);
        }
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent msg) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return super.onKeyUp(keyCode, msg);
        } else {
            return mJetBoyThread.doKeyUp(keyCode, msg);
        }
    }
}
