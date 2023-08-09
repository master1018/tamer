public class ExternalSharedPermsBTTest extends InstrumentationTestCase
{
    private static final int REQUEST_ENABLE_BT = 2;
    public void testRunBluetooth()
    {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if ((mBluetoothAdapter != null) && (!mBluetoothAdapter.isEnabled())) {
            mBluetoothAdapter.getName();
        }
    }
}
