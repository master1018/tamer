@TestTargetClass(Photo.class)
public class ContactsContract_PhotoTest extends InstrumentationTestCase {
    private ContactsContract_TestDataBuilder mBuilder;
    private static final byte[] TEST_PHOTO_DATA = "ABCDEFG".getBytes();
    private static final byte[] EMPTY_TEST_PHOTO_DATA = "".getBytes();
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ContentResolver contentResolver =
                getInstrumentation().getTargetContext().getContentResolver();
        IContentProvider provider = contentResolver.acquireProvider(ContactsContract.AUTHORITY);
        mBuilder = new ContactsContract_TestDataBuilder(provider);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mBuilder.cleanup();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Tests INSERT operation for photo"
        )
    })
    public void testAddPhoto() throws Exception {
        TestRawContact rawContact = mBuilder.newRawContact().insert();
        TestData photoData = rawContact.newDataRow(Photo.CONTENT_ITEM_TYPE)
                .with(Photo.PHOTO, TEST_PHOTO_DATA)
                .insert();
        photoData.load();
        photoData.assertColumn(Photo.RAW_CONTACT_ID, rawContact.getId());
        photoData.assertColumn(Photo.PHOTO, TEST_PHOTO_DATA);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Tests INSERT operation for empty photo"
        )
    })
    public void testAddEmptyPhoto() throws Exception {
        TestRawContact rawContact = mBuilder.newRawContact().insert();
        TestData photoData = rawContact.newDataRow(Photo.CONTENT_ITEM_TYPE)
                .with(Photo.PHOTO, EMPTY_TEST_PHOTO_DATA)
                .insert();
        photoData.load();
        photoData.assertColumn(Photo.RAW_CONTACT_ID, rawContact.getId());
        photoData.assertColumn(Photo.PHOTO, EMPTY_TEST_PHOTO_DATA);
    }
}
