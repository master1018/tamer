public class TestedActivity extends Activity
{
    public TestedActivity()
    {
    }
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
    }
    protected void onRestoreInstanceState(Bundle state)
    {
        super.onRestoreInstanceState(state);
    }
    protected void onResume()
    {
        super.onResume();
        Looper.myLooper().myQueue().addIdleHandler(new Idler());
    }
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }
    protected void onStop()
    {
        super.onStop();
    }
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            setResult(RESULT_OK);
            finish();
        }
    };
    private class Idler implements MessageQueue.IdleHandler
    {
        public final boolean queueIdle()
        {
            setResult(RESULT_OK);
            finish();
            return false;
        }
    }
}
