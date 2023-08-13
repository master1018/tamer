public class BluetoothRebootStressTest extends InstrumentationTestCase {
    private static final String TAG = "BluetoothRebootStressTest";
    private static final String OUTPUT_FILE = "BluetoothRebootStressTestOutput.txt";
    private BluetoothTestUtils mTestUtils;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Context context = getInstrumentation().getTargetContext();
        mTestUtils = new BluetoothTestUtils(context, TAG, OUTPUT_FILE);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mTestUtils.close();
    }
    public void testStart() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        mTestUtils.enable(adapter);
    }
    public void testMiddleNoToggle() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        assertTrue(adapter.isEnabled());
    }
    public void testMiddleToggle() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        assertTrue(adapter.isEnabled());
        mTestUtils.disable(adapter);
        mTestUtils.enable(adapter);
    }
    public void testStop() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        assertTrue(adapter.isEnabled());
        mTestUtils.disable(adapter);
    }
}
