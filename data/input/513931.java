public class CallLogProviderTest extends BaseContactsProvider2Test {
    @Override
    protected Class<? extends ContentProvider> getProviderClass() {
       return SynchronousContactsProvider2.class;
    }
    @Override
    protected String getAuthority() {
        return ContactsContract.AUTHORITY;
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        addProvider(TestCallLogProvider.class, CallLog.AUTHORITY);
    }
    public void testInsert() {
        ContentValues values = new ContentValues();
        putCallValues(values);
        Uri uri = mResolver.insert(Calls.CONTENT_URI, values);
        assertStoredValues(uri, values);
        assertSelection(uri, values, Calls._ID, ContentUris.parseId(uri));
    }
    public void testUpdate() {
        ContentValues values = new ContentValues();
        putCallValues(values);
        Uri uri = mResolver.insert(Calls.CONTENT_URI, values);
        values.clear();
        values.put(Calls.TYPE, Calls.OUTGOING_TYPE);
        values.put(Calls.NUMBER, "1-800-263-7643");
        values.put(Calls.DATE, 2000);
        values.put(Calls.DURATION, 40);
        values.put(Calls.CACHED_NAME, "1-800-GOOG-411");
        values.put(Calls.CACHED_NUMBER_TYPE, Phone.TYPE_CUSTOM);
        values.put(Calls.CACHED_NUMBER_LABEL, "Directory");
        int count = mResolver.update(uri, values, null, null);
        assertEquals(1, count);
        assertStoredValues(uri, values);
    }
    public void testDelete() {
        ContentValues values = new ContentValues();
        putCallValues(values);
        Uri uri = mResolver.insert(Calls.CONTENT_URI, values);
        try {
            mResolver.delete(uri, null, null);
            fail();
        } catch (UnsupportedOperationException ex) {
        }
        int count = mResolver.delete(Calls.CONTENT_URI, Calls._ID + "="
                + ContentUris.parseId(uri), null);
        assertEquals(1, count);
        assertEquals(0, getCount(uri, null, null));
    }
    public void testCallLogFilter() {
        ContentValues values = new ContentValues();
        putCallValues(values);
        mResolver.insert(Calls.CONTENT_URI, values);
        Uri filterUri = Uri.withAppendedPath(Calls.CONTENT_FILTER_URI, "1-800-4664-411");
        Cursor c = mResolver.query(filterUri, null, null, null, null);
        assertEquals(1, c.getCount());
        c.moveToFirst();
        assertCursorValues(c, values);
        c.close();
        filterUri = Uri.withAppendedPath(Calls.CONTENT_FILTER_URI, "1-888-4664-411");
        c = mResolver.query(filterUri, null, null, null, null);
        assertEquals(0, c.getCount());
        c.close();
    }
    public void testAddCall() {
        CallerInfo ci = new CallerInfo();
        ci.name = "1-800-GOOG-411";
        ci.numberType = Phone.TYPE_CUSTOM;
        ci.numberLabel = "Directory";
        Uri uri = Calls.addCall(ci, getMockContext(), "1-800-263-7643",
                Connection.PRESENTATION_ALLOWED, Calls.OUTGOING_TYPE, 2000, 40);
        ContentValues values = new ContentValues();
        values.put(Calls.TYPE, Calls.OUTGOING_TYPE);
        values.put(Calls.NUMBER, "1-800-263-7643");
        values.put(Calls.DATE, 2000);
        values.put(Calls.DURATION, 40);
        values.put(Calls.CACHED_NAME, "1-800-GOOG-411");
        values.put(Calls.CACHED_NUMBER_TYPE, Phone.TYPE_CUSTOM);
        values.put(Calls.CACHED_NUMBER_LABEL, "Directory");
        assertStoredValues(uri, values);
    }
    private void putCallValues(ContentValues values) {
        values.put(Calls.TYPE, Calls.INCOMING_TYPE);
        values.put(Calls.NUMBER, "1-800-4664-411");
        values.put(Calls.DATE, 1000);
        values.put(Calls.DURATION, 30);
        values.put(Calls.NEW, 1);
    }
    public static class TestCallLogProvider extends CallLogProvider {
        private static ContactsDatabaseHelper mDbHelper;
        @Override
        protected ContactsDatabaseHelper getDatabaseHelper(final Context context) {
            if (mDbHelper == null) {
                mDbHelper = new ContactsDatabaseHelper(context);
            }
            return mDbHelper;
        }
    }
}
