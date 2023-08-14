public class ConfirmRateLimitActivity extends Activity {
    private static final String TAG = "ConfirmRateLimitActivity";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private long mCreateTime;
    private Handler mHandler;
    private Runnable mRunnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.confirm_rate_limit_activity);
        Button button = (Button) findViewById(R.id.btn_yes);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doAnswer(true);
            }
        });
        button = (Button) findViewById(R.id.btn_no);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doAnswer(false);
            }
        });
        mHandler = new Handler();
        mRunnable = new Runnable() {
            public void run() {
                if (LOCAL_LOGV) {
                    Log.v(TAG, "Runnable executed.");
                }
                doAnswer(false);
            }
        };
        mCreateTime = System.currentTimeMillis();
    }
    @Override
    protected void onResume() {
        super.onResume();
        long delay = mCreateTime - System.currentTimeMillis()
                        + (RateController.ANSWER_TIMEOUT - 500);
        if (delay <= 0) {
            doAnswer(false);
        } else if (mHandler != null) {
            mHandler.postDelayed(mRunnable, delay);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)
                && (event.getRepeatCount() == 0)) {
            doAnswer(false);
        }
        return super.onKeyDown(keyCode, event);
    }
    private void doAnswer(boolean answer) {
        Intent intent = new Intent(RATE_LIMIT_CONFIRMED_ACTION);
        intent.putExtra("answer", answer);
        sendBroadcast(intent);
        finish();
    }
}
