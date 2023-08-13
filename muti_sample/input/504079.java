@TestTargetClass(android.provider.Settings.Secure.class)
public class Settings_SecureTest extends AndroidTestCase {
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
        )
    })
    @BrokenTest("Cannot access secure settings table")
    public void testSecureSettings() throws SettingNotFoundException {
        Cursor c = cr.query(Secure.CONTENT_URI, null, null, null, null);
        try {
            assertNotNull(c);
            int count = c.getCount();
            c.close();
            assertTrue(Secure.putInt(cr, "IntField", 10));
            assertTrue(Secure.putLong(cr, "LongField", 20));
            assertTrue(Secure.putFloat(cr, "FloatField", 30));
            assertTrue(Secure.putString(cr, "StringField", "cts"));
            c = cr.query(Secure.CONTENT_URI, null, null, null, null);
            assertNotNull(c);
            assertEquals(count + 4, c.getCount());
            c.close();
            assertEquals(10, Secure.getInt(cr, "IntField"));
            assertEquals(20, Secure.getLong(cr, "LongField"));
            assertEquals(30.0f, Secure.getFloat(cr, "FloatField"), 0.001);
            assertEquals("cts", Secure.getString(cr, "StringField"));
            String selection = Secure.NAME + "=\"" + "IntField" + "\"";
            cr.delete(Secure.CONTENT_URI, selection, null);
            selection = Secure.NAME + "=\"" + "LongField" + "\"";
            cr.delete(Secure.CONTENT_URI, selection, null);
            selection = Secure.NAME + "=\"" + "FloatField" + "\"";
            cr.delete(Secure.CONTENT_URI, selection, null);
            selection = Secure.NAME + "=\"" + "StringField" + "\"";
            cr.delete(Secure.CONTENT_URI, selection, null);
            c = cr.query(Secure.CONTENT_URI, null, null, null, null);
            assertNotNull(c);
            assertEquals(count, c.getCount());
            c.close();
            selection = "name=\""+ Secure.BLUETOOTH_ON + "\"";
            c = cr.query(Secure.CONTENT_URI, null, selection, null, null);
            assertNotNull(c);
            assertEquals(1, c.getCount());
            c.moveToFirst();
            String name = c.getString(c.getColumnIndexOrThrow(Secure.NAME));
            String store = Secure.getString(cr, name);
            c.close();
            assertTrue(Secure.putString(cr, name, "1"));
            assertEquals("1", Secure.getString(cr, name));
            c = cr.query(Secure.CONTENT_URI, null, null, null, null);
            assertNotNull(c);
            assertEquals(count, c.getCount()); 
            assertTrue(Secure.putString(cr, name, store));
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
        assertEquals(10, Secure.getInt(cr, "int", 10));
        assertEquals(20, Secure.getLong(cr, "long", 20));
        assertEquals(30.0f, Secure.getFloat(cr, "float", 30), 0.001);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getUriFor",
        args = {java.lang.String.class}
    )
    public void testGetUriFor() {
        String name = "table";
        Uri uri = Secure.getUriFor(name);
        assertNotNull(uri);
        assertEquals(Uri.withAppendedPath(Secure.CONTENT_URI, name), uri);
    }
}
