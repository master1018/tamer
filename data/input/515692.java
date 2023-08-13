public class EntityDeltaTests extends AndroidTestCase {
    public static final String TAG = "EntityDeltaTests";
    public static final long TEST_CONTACT_ID = 12;
    public static final long TEST_PHONE_ID = 24;
    public static final String TEST_PHONE_NUMBER_1 = "218-555-1111";
    public static final String TEST_PHONE_NUMBER_2 = "218-555-2222";
    public static final String TEST_ACCOUNT_NAME = "TEST";
    public EntityDeltaTests() {
        super();
    }
    @Override
    public void setUp() {
        mContext = getContext();
    }
    public static Entity getEntity(long contactId, long phoneId) {
        final ContentValues contact = new ContentValues();
        contact.put(RawContacts.VERSION, 43);
        contact.put(RawContacts._ID, contactId);
        final ContentValues phone = new ContentValues();
        phone.put(Data._ID, phoneId);
        phone.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        phone.put(Phone.NUMBER, TEST_PHONE_NUMBER_1);
        phone.put(Phone.TYPE, Phone.TYPE_HOME);
        final Entity before = new Entity(contact);
        before.addSubValue(Data.CONTENT_URI, phone);
        return before;
    }
    public void testParcelChangesNone() {
        final Entity before = getEntity(TEST_CONTACT_ID, TEST_PHONE_ID);
        final EntityDelta source = EntityDelta.fromBefore(before);
        final EntityDelta dest = EntityDelta.fromBefore(before);
        final EntityDelta merged = EntityDelta.mergeAfter(dest, source);
        assertEquals("Unexpected change when merging", source, merged);
    }
    public void testParcelChangesInsert() {
        final Entity before = getEntity(TEST_CONTACT_ID, TEST_PHONE_ID);
        final EntityDelta source = EntityDelta.fromBefore(before);
        final EntityDelta dest = EntityDelta.fromBefore(before);
        final ContentValues phone = new ContentValues();
        phone.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        phone.put(Phone.NUMBER, TEST_PHONE_NUMBER_2);
        phone.put(Phone.TYPE, Phone.TYPE_WORK);
        source.addEntry(ValuesDelta.fromAfter(phone));
        final EntityDelta merged = EntityDelta.mergeAfter(dest, source);
        assertEquals("Unexpected change when merging", source, merged);
    }
    public void testParcelChangesUpdate() {
        final Entity before = getEntity(TEST_CONTACT_ID, TEST_PHONE_ID);
        final EntityDelta source = EntityDelta.fromBefore(before);
        final EntityDelta dest = EntityDelta.fromBefore(before);
        final ValuesDelta child = source.getEntry(TEST_PHONE_ID);
        child.put(Phone.NUMBER, TEST_PHONE_NUMBER_2);
        final EntityDelta merged = EntityDelta.mergeAfter(dest, source);
        assertEquals("Unexpected change when merging", source, merged);
    }
    public void testParcelChangesDelete() {
        final Entity before = getEntity(TEST_CONTACT_ID, TEST_PHONE_ID);
        final EntityDelta source = EntityDelta.fromBefore(before);
        final EntityDelta dest = EntityDelta.fromBefore(before);
        final ValuesDelta child = source.getEntry(TEST_PHONE_ID);
        child.markDeleted();
        final EntityDelta merged = EntityDelta.mergeAfter(dest, source);
        assertEquals("Unexpected change when merging", source, merged);
    }
    public void testValuesDiffNone() {
        final ContentValues before = new ContentValues();
        before.put(Data._ID, TEST_PHONE_ID);
        before.put(Phone.NUMBER, TEST_PHONE_NUMBER_1);
        final ValuesDelta values = ValuesDelta.fromBefore(before);
        final Builder builder = values.buildDiff(Data.CONTENT_URI);
        assertNull("None action produced a builder", builder);
    }
    public void testValuesDiffInsert() {
        final ContentValues after = new ContentValues();
        after.put(Phone.NUMBER, TEST_PHONE_NUMBER_2);
        final ValuesDelta values = ValuesDelta.fromAfter(after);
        final Builder builder = values.buildDiff(Data.CONTENT_URI);
        final int type = builder.build().getType();
        assertEquals("Didn't produce insert action", TYPE_INSERT, type);
    }
    public void testValuesDiffUpdate() {
        final ContentValues before = new ContentValues();
        before.put(Data._ID, TEST_PHONE_ID);
        before.put(Phone.NUMBER, TEST_PHONE_NUMBER_1);
        final ValuesDelta values = ValuesDelta.fromBefore(before);
        values.put(Phone.NUMBER, TEST_PHONE_NUMBER_2);
        final Builder builder = values.buildDiff(Data.CONTENT_URI);
        final int type = builder.build().getType();
        assertEquals("Didn't produce update action", TYPE_UPDATE, type);
    }
    public void testValuesDiffDelete() {
        final ContentValues before = new ContentValues();
        before.put(Data._ID, TEST_PHONE_ID);
        before.put(Phone.NUMBER, TEST_PHONE_NUMBER_1);
        final ValuesDelta values = ValuesDelta.fromBefore(before);
        values.markDeleted();
        final Builder builder = values.buildDiff(Data.CONTENT_URI);
        final int type = builder.build().getType();
        assertEquals("Didn't produce delete action", TYPE_DELETE, type);
    }
    public void testEntityDiffNone() {
        final Entity before = getEntity(TEST_CONTACT_ID, TEST_PHONE_ID);
        final EntityDelta source = EntityDelta.fromBefore(before);
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        source.buildDiff(diff);
        assertTrue("Created changes when none needed", (diff.size() == 0));
    }
    public void testEntityDiffNoneInsert() {
        final Entity before = getEntity(TEST_CONTACT_ID, TEST_PHONE_ID);
        final EntityDelta source = EntityDelta.fromBefore(before);
        final ContentValues phone = new ContentValues();
        phone.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        phone.put(Phone.NUMBER, TEST_PHONE_NUMBER_2);
        phone.put(Phone.TYPE, Phone.TYPE_WORK);
        source.addEntry(ValuesDelta.fromAfter(phone));
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        source.buildAssert(diff);
        source.buildDiff(diff);
        assertEquals("Unexpected operations", 4, diff.size());
        {
            final ContentProviderOperation oper = diff.get(0);
            assertEquals("Expected version enforcement", TYPE_ASSERT, oper.getType());
        }
        {
            final ContentProviderOperation oper = diff.get(1);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(2);
            assertEquals("Incorrect type", TYPE_INSERT, oper.getType());
            assertEquals("Incorrect target", Data.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(3);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
    }
    public void testEntityDiffUpdateInsert() {
        final Entity before = getEntity(TEST_CONTACT_ID, TEST_PHONE_ID);
        final EntityDelta source = EntityDelta.fromBefore(before);
        source.getValues().put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DISABLED);
        final ContentValues phone = new ContentValues();
        phone.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        phone.put(Phone.NUMBER, TEST_PHONE_NUMBER_2);
        phone.put(Phone.TYPE, Phone.TYPE_WORK);
        source.addEntry(ValuesDelta.fromAfter(phone));
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        source.buildAssert(diff);
        source.buildDiff(diff);
        assertEquals("Unexpected operations", 5, diff.size());
        {
            final ContentProviderOperation oper = diff.get(0);
            assertEquals("Expected version enforcement", TYPE_ASSERT, oper.getType());
        }
        {
            final ContentProviderOperation oper = diff.get(1);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(2);
            assertEquals("Incorrect type", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(3);
            assertEquals("Incorrect type", TYPE_INSERT, oper.getType());
            assertEquals("Incorrect target", Data.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(4);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
    }
    public void testEntityDiffNoneUpdate() {
        final Entity before = getEntity(TEST_CONTACT_ID, TEST_PHONE_ID);
        final EntityDelta source = EntityDelta.fromBefore(before);
        final ValuesDelta child = source.getEntry(TEST_PHONE_ID);
        child.put(Phone.NUMBER, TEST_PHONE_NUMBER_2);
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        source.buildAssert(diff);
        source.buildDiff(diff);
        assertEquals("Unexpected operations", 4, diff.size());
        {
            final ContentProviderOperation oper = diff.get(0);
            assertEquals("Expected version enforcement", TYPE_ASSERT, oper.getType());
        }
        {
            final ContentProviderOperation oper = diff.get(1);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(2);
            assertEquals("Incorrect type", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", Data.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(3);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
    }
    public void testEntityDiffDelete() {
        final Entity before = getEntity(TEST_CONTACT_ID, TEST_PHONE_ID);
        final EntityDelta source = EntityDelta.fromBefore(before);
        source.getValues().markDeleted();
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        source.buildAssert(diff);
        source.buildDiff(diff);
        assertEquals("Unexpected operations", 2, diff.size());
        {
            final ContentProviderOperation oper = diff.get(0);
            assertEquals("Expected version enforcement", TYPE_ASSERT, oper.getType());
        }
        {
            final ContentProviderOperation oper = diff.get(1);
            assertEquals("Incorrect type", TYPE_DELETE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
    }
    public void testEntityDiffInsert() {
        final ContentValues after = new ContentValues();
        after.put(RawContacts.ACCOUNT_NAME, TEST_ACCOUNT_NAME);
        after.put(RawContacts.SEND_TO_VOICEMAIL, 1);
        final ValuesDelta values = ValuesDelta.fromAfter(after);
        final EntityDelta source = new EntityDelta(values);
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        source.buildAssert(diff);
        source.buildDiff(diff);
        assertEquals("Unexpected operations", 2, diff.size());
        {
            final ContentProviderOperation oper = diff.get(0);
            assertEquals("Incorrect type", TYPE_INSERT, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
    }
    public void testEntityDiffInsertInsert() {
        final ContentValues after = new ContentValues();
        after.put(RawContacts.ACCOUNT_NAME, TEST_ACCOUNT_NAME);
        after.put(RawContacts.SEND_TO_VOICEMAIL, 1);
        final ValuesDelta values = ValuesDelta.fromAfter(after);
        final EntityDelta source = new EntityDelta(values);
        final ContentValues phone = new ContentValues();
        phone.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        phone.put(Phone.NUMBER, TEST_PHONE_NUMBER_2);
        phone.put(Phone.TYPE, Phone.TYPE_WORK);
        source.addEntry(ValuesDelta.fromAfter(phone));
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        source.buildAssert(diff);
        source.buildDiff(diff);
        assertEquals("Unexpected operations", 3, diff.size());
        {
            final ContentProviderOperation oper = diff.get(0);
            assertEquals("Incorrect type", TYPE_INSERT, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(1);
            assertEquals("Incorrect type", TYPE_INSERT, oper.getType());
            assertEquals("Incorrect target", Data.CONTENT_URI, oper.getUri());
        }
    }
}
