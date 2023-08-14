public class ViewGroupStubActivity extends Activity {
    public static final String ACTION_INVALIDATE_CHILD = "invalidateChild";
    private final Handler mHandler = new Handler();
    private static CTSResult sResult;
    public static void setResult(CTSResult result) {
        sResult = result;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.android.cts.stub.R.layout.viewgrouptest_stub);
        TextView textView = (TextView)findViewById(com.android.cts.stub.R.id.viewgrouptest_stub);
        textView.setText("test");
    }
    @Override
    protected void onResume() {
        super.onResume();
        String action = getIntent().getAction();
        if (action.equals(ACTION_INVALIDATE_CHILD)) {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    MockLinearLayout mll =
                        (MockLinearLayout) findViewById(com.android.cts.stub.R.id.
                                                                        mocklinearlayout);
                    if (!mll.mIsInvalidateChildInParentCalled) {
                        fail();
                        return;
                    }
                    sResult.setResult(CTSResult.RESULT_OK);
                    finish();
                }
            }, 2000);
        }
    }
    private void fail() {
        sResult.setResult(CTSResult.RESULT_FAIL);
        finish();
    }
}
