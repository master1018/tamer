@TestTargetClass(ContactsContract.RawContacts.class)
public class ContactsContract_RawContactsTest extends InstrumentationTestCase {
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
            notes = "Test RawContacts#getContactLookupUri(ContentResolver resolver, Uri " +
                    "rawContactUri) using source id",
            method = "RawContacts#getContactLookupUri",
            args = {android.content.ContentResolver.class, Uri.class}
    )
    public void testGetLookupUriBySourceId() throws Exception {
        TestRawContact rawContact = mBuilder.newRawContact()
                .with(RawContacts.ACCOUNT_TYPE, "test_type")
                .with(RawContacts.ACCOUNT_NAME, "test_name")
                .with(RawContacts.SOURCE_ID, "source_id")
                .insert();
        rawContact.newDataRow(StructuredName.CONTENT_ITEM_TYPE)
                .with(StructuredName.DISPLAY_NAME, "test name")
                .insert();
        Uri lookupUri = RawContacts.getContactLookupUri(mResolver, rawContact.getUri());
        assertNotNull("Could not produce a lookup URI", lookupUri);
        TestContact lookupContact = mBuilder.newContact().setUri(lookupUri).load();
        assertEquals("Lookup URI matched the wrong contact",
                lookupContact.getId(), rawContact.load().getContactId());
    }
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Test RawContacts#getContactLookupUri(ContentResolver resolver, Uri " +
                    "rawContactUri) using display name",
            method = "RawContacts#getContactLookupUri",
            args = {android.content.ContentResolver.class, Uri.class}
    )
    public void testGetLookupUriByDisplayName() throws Exception {
        TestRawContact rawContact = mBuilder.newRawContact()
                .with(RawContacts.ACCOUNT_TYPE, "test_type")
                .with(RawContacts.ACCOUNT_NAME, "test_name")
                .insert();
        rawContact.newDataRow(StructuredName.CONTENT_ITEM_TYPE)
                .with(StructuredName.DISPLAY_NAME, "test name")
                .insert();
        Uri lookupUri = RawContacts.getContactLookupUri(mResolver, rawContact.getUri());
        assertNotNull("Could not produce a lookup URI", lookupUri);
        TestContact lookupContact = mBuilder.newContact().setUri(lookupUri).load();
        assertEquals("Lookup URI matched the wrong contact",
                lookupContact.getId(), rawContact.load().getContactId());
    }
}
