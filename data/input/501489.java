@TestTargetClass(ContactsContract.Data.class)
public class ContactsContract_DataTest extends InstrumentationTestCase {
    private ContentResolver mResolver;
    private ContactsContract_TestDataBuilder mBuilder;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResolver = getInstrumentation().getTargetContext().getContentResolver();
        IContentProvider provider = mResolver.acquireProvider(ContactsContract.AUTHORITY);
        mBuilder = new ContactsContract_TestDataBuilder(provider);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mBuilder.cleanup();
    }
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Test Data#getContactLookupUri(ContentResolver resolver, Uri dataUri) " +
                    "using source id",
            method = "Data#getContactLookupUri",
            args = {android.content.ContentResolver.class, Uri.class}
    )
    public void testGetLookupUriBySourceId() throws Exception {
        TestRawContact rawContact = mBuilder.newRawContact()
                .with(RawContacts.ACCOUNT_TYPE, "test_type")
                .with(RawContacts.ACCOUNT_NAME, "test_name")
                .with(RawContacts.SOURCE_ID, "source_id")
                .insert();
        TestData data = rawContact.newDataRow(StructuredName.CONTENT_ITEM_TYPE)
                .with(StructuredName.DISPLAY_NAME, "test name")
                .insert();
        Uri lookupUri = Data.getContactLookupUri(mResolver, data.getUri());
        assertNotNull("Could not produce a lookup URI", lookupUri);
        TestContact lookupContact = mBuilder.newContact().setUri(lookupUri).load();
        assertEquals("Lookup URI matched the wrong contact",
                lookupContact.getId(), data.load().getRawContact().load().getContactId());
    }
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Test Data#getContactLookupUri(ContentResolver resolver, Uri dataUri) " +
                    "using display name",
            method = "Data#getContactLookupUri",
            args = {android.content.ContentResolver.class, Uri.class}
    )
    public void testGetLookupUriByDisplayName() throws Exception {
        TestRawContact rawContact = mBuilder.newRawContact()
                .with(RawContacts.ACCOUNT_TYPE, "test_type")
                .with(RawContacts.ACCOUNT_NAME, "test_name")
                .insert();
        TestData data = rawContact.newDataRow(StructuredName.CONTENT_ITEM_TYPE)
                .with(StructuredName.DISPLAY_NAME, "test name")
                .insert();
        Uri lookupUri = Data.getContactLookupUri(mResolver, data.getUri());
        assertNotNull("Could not produce a lookup URI", lookupUri);
        TestContact lookupContact = mBuilder.newContact().setUri(lookupUri).load();
        assertEquals("Lookup URI matched the wrong contact",
                lookupContact.getId(), data.load().getRawContact().load().getContactId());
    }
}
