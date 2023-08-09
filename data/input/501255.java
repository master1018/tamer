@TestTargetClass(ContactsContract.Contacts.class)
public class ContactsContract_ContactsTest extends InstrumentationTestCase {
    private ContentResolver mContentResolver;
    private ContactsContract_TestDataBuilder mBuilder;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentResolver = getInstrumentation().getTargetContext().getContentResolver();
        IContentProvider provider = mContentResolver.acquireProvider(ContactsContract.AUTHORITY);
        mBuilder = new ContactsContract_TestDataBuilder(provider);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mBuilder.cleanup();
    }
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test markAsContacted(ContentResolver resolver, long contactId)",
            method = "markAsContacted",
            args = {android.content.ContentResolver.class, long.class}
    )
    public void testMarkAsContacted() throws Exception {
        TestRawContact rawContact = mBuilder.newRawContact().insert().load();
        TestContact contact = rawContact.getContact().load();
        long oldLastContacted = contact.getLong(Contacts.LAST_TIME_CONTACTED);
        Contacts.markAsContacted(mContentResolver, contact.getId());
        contact.load(); 
        long lastContacted = contact.getLong(Contacts.LAST_TIME_CONTACTED);
        assertTrue(oldLastContacted < lastContacted);
        oldLastContacted = lastContacted;
        Contacts.markAsContacted(mContentResolver, contact.getId());
        contact.load();
        lastContacted = contact.getLong(Contacts.LAST_TIME_CONTACTED);
        assertTrue(oldLastContacted < lastContacted);
    }
}
