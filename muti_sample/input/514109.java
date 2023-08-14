public class SettingsProviderTest extends AndroidTestCase {
    @MediumTest
    public void testNameValueCache() {
        ContentResolver r = getContext().getContentResolver();
        Settings.Secure.putString(r, "test_service", "Value");
        assertEquals("Value", Settings.Secure.getString(r, "test_service"));
        Settings.Secure.putString(r, "test_service", "New");
        assertEquals("New", Settings.Secure.getString(r, "test_service"));
        assertEquals(1, r.delete(Settings.Secure.getUriFor("test_service"), null, null));
        assertEquals(null, Settings.Secure.getString(r, "test_service"));
        Settings.System.putString(r, "test_setting", "Value");
        assertEquals("Value", Settings.System.getString(r, "test_setting"));
        Settings.System.putString(r, "test_setting", "New");
        assertEquals("New", Settings.System.getString(r, "test_setting"));
        assertEquals(1, r.delete(Settings.System.getUriFor("test_setting"), null, null));
        assertEquals(null, Settings.System.getString(r, "test_setting"));
    }
    @MediumTest
    public void testRowNameContentUri() {
        ContentResolver r = getContext().getContentResolver();
        assertEquals("content:
                Settings.System.getUriFor("test_setting").toString());
        assertEquals("content:
                Settings.Secure.getUriFor("test_service").toString());
        Uri tables[] = { Settings.System.CONTENT_URI, Settings.Secure.CONTENT_URI };
        for (Uri table : tables) {
            ContentValues v = new ContentValues();
            v.put(Settings.System.NAME, "test_key");
            v.put(Settings.System.VALUE, "Test");
            Uri uri = r.insert(table, v);
            assertEquals(table.toString() + "/test_key", uri.toString());
            Cursor c = r.query(uri, null, null, null, null);
            try {
                assertTrue(c.moveToNext());
                assertEquals("test_key", c.getString(c.getColumnIndex(Settings.System.NAME)));
                assertEquals("Test", c.getString(c.getColumnIndex(Settings.System.VALUE)));
                assertFalse(c.moveToNext());
            } finally {
                c.close();
            }
            try {
                r.query(uri, null, "1", null, null);
                fail("UnsupportedOperationException expected");
            } catch (UnsupportedOperationException e) {
                if (!e.toString().contains("WHERE clause")) throw e;
            }
            c = r.query(table, null, "name='test_key'", null, null);
            try {
                assertTrue(c.moveToNext());
                assertEquals("test_key", c.getString(c.getColumnIndex(Settings.System.NAME)));
                assertEquals("Test", c.getString(c.getColumnIndex(Settings.System.VALUE)));
                assertFalse(c.moveToNext());
            } finally {
                c.close();
            }
            v = new ContentValues();
            v.put(Settings.System.VALUE, "Toast");
            assertEquals(1, r.update(uri, v, null, null));
            c = r.query(uri, null, null, null, null);
            try {
                assertTrue(c.moveToNext());
                assertEquals("test_key", c.getString(c.getColumnIndex(Settings.System.NAME)));
                assertEquals("Toast", c.getString(c.getColumnIndex(Settings.System.VALUE)));
                assertFalse(c.moveToNext());
            } finally {
                c.close();
            }
            assertEquals(1, r.delete(uri, null, null));
        }
        assertEquals(null, Settings.System.getString(r, "test_key"));
        assertEquals(null, Settings.Secure.getString(r, "test_key"));
    }
    @MediumTest
    public void testRowNumberContentUri() {
        ContentResolver r = getContext().getContentResolver();
        Uri uri = Settings.Bookmarks.add(r, new Intent("TEST"),
                "Test Title", "Test Folder", '*', 123);
        assertTrue(ContentUris.parseId(uri) > 0);
        assertEquals("TEST", Settings.Bookmarks.getIntentForShortcut(r, '*').getAction());
        ContentValues v = new ContentValues();
        v.put(Settings.Bookmarks.INTENT, "#Intent;action=TOAST;end");
        assertEquals(1, r.update(uri, v, null, null));
        assertEquals("TOAST", Settings.Bookmarks.getIntentForShortcut(r, '*').getAction());
        assertEquals(1, r.delete(uri, null, null));
        assertEquals(null, Settings.Bookmarks.getIntentForShortcut(r, '*'));
    }
}
