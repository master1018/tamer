@TestTargetClass(android.provider.Settings.NameValueTable.class)
public class Settings_NameValueTableTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "putString",
        args = {android.content.ContentResolver.class, android.net.Uri.class,
                java.lang.String.class, java.lang.String.class}
    )
    public void testPutString() {
        ContentResolver cr = mContext.getContentResolver();
        Uri uri = Settings.System.CONTENT_URI;
        String name = "name1";
        String value = "value1";
        Cursor c = cr.query(uri, null, null, null, null);
        try {
            assertNotNull(c);
            int origCount = c.getCount();
            c.close();
            MyNameValueTable.putString(cr, uri, name, value);
            c = cr.query(uri, null, null, null, null);
            assertNotNull(c);
            assertEquals(origCount + 1, c.getCount());
            c.close();
            String selection = NameValueTable.NAME + "=\"" + name + "\"";
            c = cr.query(uri, null, selection, null, null);
            assertNotNull(c);
            assertEquals(1, c.getCount());
            c.moveToFirst();
            assertEquals("name1", c.getString(c.getColumnIndexOrThrow(NameValueTable.NAME)));
            assertEquals("value1", c.getString(c.getColumnIndexOrThrow(NameValueTable.VALUE)));
            c.close();
            cr.delete(uri, selection, null);
            c = cr.query(uri, null, null, null, null);
            assertNotNull(c);
            assertEquals(origCount, c.getCount());
        } finally {
            c.close();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getUriFor",
        args = {android.net.Uri.class, java.lang.String.class}
    )
    public void testGetUriFor() {
        Uri uri = Uri.parse("content:
        String name = "table";
        Uri res = NameValueTable.getUriFor(uri, name);
        assertNotNull(res);
        assertEquals(Uri.withAppendedPath(uri, name), res);
    }
    private static class MyNameValueTable extends NameValueTable {
        protected static boolean putString(ContentResolver resolver, Uri uri, String name,
                String value) {
            return NameValueTable.putString(resolver, uri, name, value);
        }
    }
}
