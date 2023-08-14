@TestTargetClass(OrientationListener.class)
public class OrientationListenerTest extends AndroidTestCase {
    private Context mContext;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor OrientationListener#OrientationListener(Context).",
            method = "OrientationListener",
            args = {Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor OrientationListener#OrientationListener(Context, int).",
            method = "OrientationListener",
            args = {Context.class, int.class}
        )
    })
    public void testConstructor() {
        new MockOrientationListener(mContext);
        new MockOrientationListener(mContext, SensorManager.SENSOR_DELAY_UI);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link OrientationListener#enable()}. "
                    + "This method is simply called to make sure that no exception is thrown. "
                    + "The registeration of the listener can not be tested becuase there is no way "
                    + "to simulate sensor events on the emulator",
            method = "enable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link OrientationListener#disable()}. "
                    + "This method is simply called to make sure that no exception is thrown. "
                    + "The registeration of the listener can not be tested becuase there is no way "
                    + "to simulate sensor events on the emulator",
            method = "disable",
            args = {}
        )
    })
    @ToBeFixed(explanation = "Can not simulate sensor events on the emulator.")
    public void testRegisterationOfOrientationListener() {
        MockOrientationListener listener = new MockOrientationListener(mContext);
        listener.disable();
        listener.enable();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link OrientationListener#onAccuracyChanged(int, int)}.",
        method = "onAccuracyChanged",
        args = {int.class, int.class}
    )
    public void testOnAccuracyChanged() {
        new MockOrientationListener(mContext).onAccuracyChanged(SensorManager.SENSOR_ACCELEROMETER,
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
        new MockOrientationListener(mContext).onAccuracyChanged(SensorManager.SENSOR_ORIENTATION,
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link OrientationListener#onSensorChanged(int , float[])}.",
        method = "onSensorChanged",
        args = {int.class, float[].class}
    )
    public void testOnSensorChanged() {
        MockOrientationListener listener = new MockOrientationListener(mContext);
        float[] mockData = new float[SensorManager.RAW_DATA_Z + 1];
        mockData[SensorManager.RAW_DATA_X] = 3.0f;
        mockData[SensorManager.RAW_DATA_Y] = 4.0f;
        mockData[SensorManager.RAW_DATA_Z] = 5.0f * 2.0f + 0.1f;
        new MockOrientationListener(mContext).onSensorChanged(SensorManager.SENSOR_ACCELEROMETER,
                mockData);
        mockData[SensorManager.RAW_DATA_X] = 4.0f;
        mockData[SensorManager.RAW_DATA_Y] = 4.0f;
        mockData[SensorManager.RAW_DATA_Z] = 5.0f * 2.0f;
        listener.reset();
        new MockOrientationListener(mContext).onSensorChanged(SensorManager.SENSOR_MAGNETIC_FIELD,
                mockData);
    }
    @TestTargetNew(
        level = TestLevel.TODO,
        notes = "Test {@link OrientationListener#onOrientationChanged(int)}. "
                + "This test does not check callback of the "
                + "{@link OrientationListener#onOrientationChanged(int)} "
                + "because there is no way to simulate the sensor events on the emulator.",
        method = "onSensorChanged",
        args = {int.class, float[].class}
    )
    @ToBeFixed(explanation = "Can not simulate sensor events on the emulator.")
    public void testOnOrientationChanged() {
        MockOrientationListener listener = new MockOrientationListener(mContext);
        listener.enable();
    }
    private class MockOrientationListener extends OrientationListener {
        private boolean mHasCalledOnOrientationChanged;
        public boolean hasCalledOnOrientationChanged() {
            return mHasCalledOnOrientationChanged;
        }
        public void reset() {
            mHasCalledOnOrientationChanged = false;
        }
        public MockOrientationListener(Context context) {
            super(context);
        }
        public MockOrientationListener(Context context, int rate) {
            super(context, rate);
        }
        @Override
        public void onOrientationChanged(int orientation) {
            mHasCalledOnOrientationChanged = true;
        }
    }
}
