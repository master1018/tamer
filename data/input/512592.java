@TestTargetClass(Sensor.class)
public class SensorTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getType",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getName",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getPower",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getResolution",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getVendor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getVersion",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            method = "getMaximumRange",
            args = {}
        )
    })
    public void testSensorOperations() {
        final SensorManager mSensorManager =
            (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        assertNotNull(sensors);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        assertEquals(Sensor.TYPE_ACCELEROMETER, sensor.getType());
        assertSensorValues(sensor);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        assertEquals(Sensor.TYPE_MAGNETIC_FIELD, sensor.getType());
        assertSensorValues(sensor);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        assertEquals(Sensor.TYPE_ORIENTATION, sensor.getType());
        assertSensorValues(sensor);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE);
        if (sensor != null) {
            assertEquals(Sensor.TYPE_TEMPERATURE, sensor.getType());
            assertSensorValues(sensor);
        }
    }
    private void assertSensorValues(Sensor sensor) {
        assertTrue(sensor.getMaximumRange() >= 0);
        assertTrue(sensor.getPower() >= 0);
        assertTrue(sensor.getResolution() >= 0);
        assertNotNull(sensor.getVendor());
        assertTrue(sensor.getVersion() > 0);
    }
}
