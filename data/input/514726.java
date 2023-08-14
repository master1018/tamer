@TestTargetClass(ResultReceiver.class)
public class ResultReceiverTest extends AndroidTestCase {
    private Handler mHandler = new Handler();
    private static final long DURATION = 100l;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "send",
            args = {int.class, Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onReceiveResult",
            args = {int.class, Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "ResultReceiver",
            args = {Handler.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "describeContents",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "writeToParcel",
            args = {Parcel.class, int.class}
        )
    })
    public void testResultReceiver() throws InterruptedException {
        MockResultReceiver sender = new MockResultReceiver(mHandler);
        Bundle bundle = new Bundle();
        int resultCode = 1;
        sender.send(resultCode, bundle);
        Thread.sleep(DURATION);
        assertEquals(resultCode, sender.getResultCode());
        assertSame(bundle, sender.getResultData());
        ResultReceiver receiver = new ResultReceiver(mHandler);
        assertEquals(0, receiver.describeContents());
        Parcel p = Parcel.obtain();
        receiver.writeToParcel(p, 0);
        p.setDataPosition(0);
        ResultReceiver target = ResultReceiver.CREATOR.createFromParcel(p);
        assertNotNull(target);
    }
    private class MockResultReceiver extends ResultReceiver {
        private Bundle mResultData;
        private int mResultCode;
        public MockResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            mResultData = resultData;
            mResultCode = resultCode;
        }
        public Bundle getResultData() {
            return mResultData;
        }
        public int getResultCode() {
            return mResultCode;
        }
    }
}
