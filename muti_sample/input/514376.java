public class SetTimeZonePermissionsTest extends AndroidTestCase {
    private String[] mZones;
    private String mCurrentZone;
    private AlarmManager mAlarm;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mZones = TimeZone.getAvailableIDs();
        mCurrentZone = TimeZone.getDefault().getID();
        mAlarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
    }
    @LargeTest
    public void testSetTimeZonePermissions() {
        int max = (mZones.length > 10) ? mZones.length : 10;
        assertTrue("No system-defined time zones - test invalid", max > 0);
        for (int i = 0; i < max; i++) {
            String tz = mZones[i];
            try {
                mAlarm.setTimeZone(tz);
            } catch (SecurityException se) {
            }
            String newZone = TimeZone.getDefault().getID();
            assertEquals("AlarmManager.setTimeZone() succeeded despite lack of permission",
                    mCurrentZone,
                    newZone);
        }
    }
}
