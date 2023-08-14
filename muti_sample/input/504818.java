@TestTargetClass(android.provider.Settings.System.class)
public class Settings_SystemTest extends AndroidTestCase {
    private ContentResolver cr;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cr = mContext.getContentResolver();
        assertNotNull(cr);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "putInt",
            args = {android.content.ContentResolver.class, java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "putLong",
            args = {android.content.ContentResolver.class, java.lang.String.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "putFloat",
            args = {android.content.ContentResolver.class, java.lang.String.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "putString",
            args = {android.content.ContentResolver.class, java.lang.String.class,
                    java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "putConfiguration",
            args = {android.content.ContentResolver.class, android.content.res.Configuration.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setShowGTalkServiceStatus",
            args = {android.content.ContentResolver.class, boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInt",
            args = {android.content.ContentResolver.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLong",
            args = {android.content.ContentResolver.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFloat",
            args = {android.content.ContentResolver.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getString",
            args = {android.content.ContentResolver.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getConfiguration",
            args = {android.content.ContentResolver.class, android.content.res.Configuration.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getShowGTalkServiceStatus",
            args = {android.content.ContentResolver.class}
        )
    })
    public void testSystemSettings() throws SettingNotFoundException {
        Cursor c = cr.query(System.CONTENT_URI, null, null, null, null);
        try {
            assertNotNull(c);
            int origCount = c.getCount();
            c.close();
            String intField = "IntField";
            String longField = "LongField";
            String floatField = "FloatField";
            String stringField = "StringField";
            String stringValue = "cts";
            assertTrue(System.putInt(cr, intField, 10));
            assertTrue(System.putLong(cr, longField, 20l));
            assertTrue(System.putFloat(cr, floatField, 30.0f));
            assertTrue(System.putString(cr, stringField, stringValue));
            System.setShowGTalkServiceStatus(cr, true);
            c = cr.query(System.CONTENT_URI, null, null, null, null);
            assertNotNull(c);
            assertEquals(origCount + 5, c.getCount());
            c.close();
            assertEquals(10, System.getInt(cr, intField));
            assertEquals(20l, System.getLong(cr, longField));
            assertEquals(30.0f, System.getFloat(cr, floatField), 0.001);
            assertEquals(stringValue, System.getString(cr, stringField));
            assertTrue(System.getShowGTalkServiceStatus(cr));
            String selection = System.NAME + "=\"" + intField + "\"";
            cr.delete(System.CONTENT_URI, selection, null);
            selection = System.NAME + "=\"" + longField + "\"";
            cr.delete(System.CONTENT_URI, selection, null);
            selection = System.NAME + "=\"" + floatField + "\"";
            cr.delete(System.CONTENT_URI, selection, null);
            selection = System.NAME + "=\"" + stringField + "\"";
            cr.delete(System.CONTENT_URI, selection, null);
            selection = System.NAME + "=\"" + System.SHOW_GTALK_SERVICE_STATUS + "\"";
            cr.delete(System.CONTENT_URI, selection, null);
            c = cr.query(System.CONTENT_URI, null, null, null, null);
            assertNotNull(c);
            assertEquals(origCount, c.getCount());
            Configuration cfg = new Configuration();
            System.getConfiguration(cr, cfg);
            float store = cfg.fontScale;
            cfg = new Configuration();
            cfg.fontScale = 10.0f;
            assertTrue(System.putConfiguration(cr, cfg));
            System.getConfiguration(cr, cfg);
            assertEquals(10.0f, cfg.fontScale);
            cfg.fontScale = store;
            assertTrue(System.putConfiguration(cr, cfg));
        } finally {
            c.close();
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInt",
            args = {android.content.ContentResolver.class, java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLong",
            args = {android.content.ContentResolver.class, java.lang.String.class, long.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFloat",
            args = {android.content.ContentResolver.class, java.lang.String.class, float.class}
        )
    })
    public void testGetDefaultValues() {
        assertEquals(10, System.getInt(cr, "int", 10));
        assertEquals(20, System.getLong(cr, "long", 20l));
        assertEquals(30.0f, System.getFloat(cr, "float", 30.0f), 0.001);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getUriFor",
        args = {java.lang.String.class}
    )
    public void testGetUriFor() {
        String name = "table";
        Uri uri = System.getUriFor(name);
        assertNotNull(uri);
        assertEquals(Uri.withAppendedPath(System.CONTENT_URI, name), uri);
    }
}
