public class TestedScreen extends Activity
{
    public static final String WAIT_BEFORE_FINISH = "TestedScreen.WAIT_BEFORE_FINISH";
    public static final String DELIVER_RESULT = "TestedScreen.DELIVER_RESULT";
    public static final String CLEAR_TASK = "TestedScreen.CLEAR_TASK";
    public TestedScreen() {
    }
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (ActivityTests.DEBUG_LIFECYCLE) Log.v("test", "CREATE tested "
                + Integer.toHexString(System.identityHashCode(this)) + ": " + getIntent());
        if (LaunchpadActivity.FORWARD_RESULT.equals(getIntent().getAction())) {
            Intent intent = new Intent(getIntent());
            intent.setAction(DELIVER_RESULT);
            intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
            if (ActivityTests.DEBUG_LIFECYCLE) Log.v("test", "Finishing tested "
                    + Integer.toHexString(System.identityHashCode(this)) + ": " + getIntent());
            finish();
        } else if (DELIVER_RESULT.equals(getIntent().getAction())) {
            setResult(RESULT_OK, (new Intent()).setAction(
                    LaunchpadActivity.RETURNED_RESULT));
            if (ActivityTests.DEBUG_LIFECYCLE) Log.v("test", "Finishing tested "
                    + Integer.toHexString(System.identityHashCode(this)) + ": " + getIntent());
            finish();
        } else if (CLEAR_TASK.equals(getIntent().getAction())) {
            if (!getIntent().getBooleanExtra(ClearTop.WAIT_CLEAR_TASK, false)) {
                launchClearTask();
            }
        }
    }
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
    }
    protected void onResume() {
        super.onResume();
        if (ActivityTests.DEBUG_LIFECYCLE) Log.v("test", "RESUME tested "
                + Integer.toHexString(System.identityHashCode(this)) + ": " + getIntent());
        if (CLEAR_TASK.equals(getIntent().getAction())) {
            if (getIntent().getBooleanExtra(ClearTop.WAIT_CLEAR_TASK, false)) {
                Looper.myLooper().myQueue().addIdleHandler(new Idler());
            }
        } else {
            Looper.myLooper().myQueue().addIdleHandler(new Idler());
        }
    }
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    protected void onStop() {
        super.onStop();
        if (ActivityTests.DEBUG_LIFECYCLE) Log.v("test", "STOP tested "
                + Integer.toHexString(System.identityHashCode(this)) + ": " + getIntent());
    }
    private void launchClearTask() {
        Intent intent = new Intent(getIntent()).
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).
        setClass(this, ClearTop.class);
        startActivity(intent);
    }
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (CLEAR_TASK.equals(getIntent().getAction())) {
                launchClearTask();
            } else {
                if (ActivityTests.DEBUG_LIFECYCLE) Log.v("test", "Finishing tested "
                        + Integer.toHexString(System.identityHashCode(this)) + ": " + getIntent());
                setResult(RESULT_OK);
                finish();
            }
        }
    };
    private class Idler implements MessageQueue.IdleHandler {
        public final boolean queueIdle() {
            if (WAIT_BEFORE_FINISH.equals(getIntent().getAction())) {
                Message m = Message.obtain();
                mHandler.sendMessageAtTime(m, SystemClock.uptimeMillis()+1000);
            } else if (CLEAR_TASK.equals(getIntent().getAction())) {
                Message m = Message.obtain();
                mHandler.sendMessageAtTime(m, SystemClock.uptimeMillis()+1000);
            } else {
                if (ActivityTests.DEBUG_LIFECYCLE) Log.v("test", "Finishing tested "
                        + Integer.toHexString(System.identityHashCode(this)) + ": " + getIntent());
                setResult(RESULT_OK);
                finish();
            }
            return false;
        }
    }
}
