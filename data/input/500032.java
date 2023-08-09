@TestTargetClass(GroupMembership.class)
public class ContactsContract_GroupMembershipTest extends InstrumentationTestCase {
    private ContactsContract_TestDataBuilder mBuilder;
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
            notes = "Tests INSERT operation for group membership using group row ID"
        )
    })
    public void testAddGroupMembershipWithGroupRowId() throws Exception {
        TestRawContact rawContact = mBuilder.newRawContact().insert();
        TestGroup group = mBuilder.newGroup().insert();
        TestData groupMembership = rawContact.newDataRow(GroupMembership.CONTENT_ITEM_TYPE)
                .with(GroupMembership.GROUP_ROW_ID, group.getId())
                .insert();
        groupMembership.load();
        groupMembership.assertColumn(GroupMembership.RAW_CONTACT_ID, rawContact.getId());
        groupMembership.assertColumn(GroupMembership.GROUP_ROW_ID, group.getId());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Tests INSERT operation for group membership using group source ID"
        )
    })
    public void testAddGroupMembershipWithGroupSourceId() throws Exception {
        TestRawContact rawContact = mBuilder.newRawContact()
                .with(RawContacts.ACCOUNT_TYPE, "test_type")
                .with(RawContacts.ACCOUNT_NAME, "test_name")
                .insert();
        TestGroup group = mBuilder.newGroup()
                .with(Groups.SOURCE_ID, "test_source_id")
                .with(Groups.ACCOUNT_TYPE, "test_type")
                .with(Groups.ACCOUNT_NAME, "test_name")
                .insert();
        TestData groupMembership = rawContact.newDataRow(GroupMembership.CONTENT_ITEM_TYPE)
                .with(GroupMembership.GROUP_SOURCE_ID, "test_source_id")
                .insert();
        groupMembership.load();
        groupMembership.assertColumn(GroupMembership.RAW_CONTACT_ID, rawContact.getId());
        groupMembership.assertColumn(GroupMembership.GROUP_SOURCE_ID, "test_source_id");
        groupMembership.assertColumn(GroupMembership.GROUP_ROW_ID, group.getId());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Tests INSERT operation for group membership using an unknown group source ID"
        )
    })
    public void testAddGroupMembershipWithUnknownGroupSourceId() throws Exception {
        TestRawContact rawContact = mBuilder.newRawContact()
                .with(RawContacts.ACCOUNT_TYPE, "test_type")
                .with(RawContacts.ACCOUNT_NAME, "test_name")
                .insert();
        TestData groupMembership = rawContact.newDataRow(GroupMembership.CONTENT_ITEM_TYPE)
                .with(GroupMembership.GROUP_SOURCE_ID, "test_source_id")
                .insert();
        TestGroup group = mBuilder.newGroup()
                .with(Groups.ACCOUNT_TYPE, "test_type")
                .with(Groups.ACCOUNT_NAME, "test_name")
                .with(Groups.SOURCE_ID, "test_source_id")
                .with(Groups.DELETED, 0)
                .loadUsingValues();
        groupMembership.load();
        groupMembership.assertColumn(GroupMembership.RAW_CONTACT_ID, rawContact.getId());
        groupMembership.assertColumn(GroupMembership.GROUP_SOURCE_ID, "test_source_id");
        groupMembership.assertColumn(GroupMembership.GROUP_ROW_ID, group.getId());
        group.deletePermanently();
    }
}
