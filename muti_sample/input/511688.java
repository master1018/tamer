public class BluetoothStressTest extends InstrumentationTestCase {
    private static final String TAG = "BluetoothStressTest";
    private static final String OUTPUT_FILE = "BluetoothStressTestOutput.txt";
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
    public void testEnable() {
        int iterations = BluetoothTestRunner.sEnableIterations;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        for (int i = 0; i < iterations; i++) {
            mTestUtils.writeOutput("enable iteration " + (i + 1) + " of " + iterations);
            mTestUtils.enable(adapter);
            mTestUtils.disable(adapter);
        }
    }
    public void testDiscoverable() {
        int iterations = BluetoothTestRunner.sDiscoverableIterations;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        mTestUtils.enable(adapter);
        for (int i = 0; i < iterations; i++) {
            mTestUtils.writeOutput("discoverable iteration " + (i + 1) + " of " + iterations);
            mTestUtils.discoverable(adapter);
            mTestUtils.undiscoverable(adapter);
        }
        mTestUtils.disable(adapter);
    }
    public void testScan() {
        int iterations = BluetoothTestRunner.sScanIterations;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        mTestUtils.enable(adapter);
        for (int i = 0; i < iterations; i++) {
            mTestUtils.writeOutput("scan iteration " + (i + 1) + " of " + iterations);
            mTestUtils.startScan(adapter);
            mTestUtils.stopScan(adapter);
        }
        mTestUtils.disable(adapter);
    }
}
