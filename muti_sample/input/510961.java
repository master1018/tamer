public class ToneDialog extends Activity {
    TextMessage toneMsg = null;
    ToneSettings settings = null;
    TonePlayer player = null;
    Handler mToneStopper = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_ID_STOP_TONE:
                sendResponse(StkAppService.RES_ID_DONE);
                finish();
                break;
            }
        }
    };
    Vibrator mVibrator = new Vibrator();
    private static final int MSG_ID_STOP_TONE = 0xda;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
       initFromIntent(getIntent());
        View title = findViewById(com.android.internal.R.id.title);
        title.setVisibility(View.GONE);
        setContentView(R.layout.stk_tone_dialog);
        TextView tv = (TextView) findViewById(R.id.message);
        ImageView iv = (ImageView) findViewById(R.id.icon);
        tv.setText(toneMsg.text);
        if (toneMsg.icon == null) {
            iv.setImageResource(com.android.internal.R.drawable.ic_volume);
        } else {
            iv.setImageBitmap(toneMsg.icon);
        }
        player = new TonePlayer();
        player.play(settings.tone);
        int timeout = StkApp.calculateDurationInMilis(settings.duration);
        if (timeout == 0) {
            timeout = StkApp.TONE_DFEAULT_TIMEOUT;
        }
        mToneStopper.sendEmptyMessageDelayed(MSG_ID_STOP_TONE, timeout);
        if (settings.vibrate) {
            mVibrator.vibrate(timeout);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mToneStopper.removeMessages(MSG_ID_STOP_TONE);
        player.stop();
        player.release();
        mVibrator.cancel();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            sendResponse(StkAppService.RES_ID_END_SESSION);
            finish();
            break;
        }
        return false;
    }
    private void initFromIntent(Intent intent) {
        if (intent == null) {
            finish();
        }
        toneMsg = intent.getParcelableExtra("TEXT");
        settings = intent.getParcelableExtra("TONE");
    }
    private void sendResponse(int resId) {
        Bundle args = new Bundle();
        args.putInt(StkAppService.OPCODE, StkAppService.OP_RESPONSE);
        args.putInt(StkAppService.RES_ID, resId);
        startService(new Intent(this, StkAppService.class).putExtras(args));
    }
}
