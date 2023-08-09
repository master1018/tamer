public class EntitySetTests extends AndroidTestCase {
    public static final String TAG = "EntitySetTests";
    private static final long CONTACT_FIRST = 1;
    private static final long CONTACT_SECOND = 2;
    public static final long CONTACT_BOB = 10;
    public static final long CONTACT_MARY = 11;
    public static final long PHONE_RED = 20;
    public static final long PHONE_GREEN = 21;
    public static final long PHONE_BLUE = 22;
    public static final long EMAIL_YELLOW = 25;
    public static final long VER_FIRST = 100;
    public static final long VER_SECOND = 200;
    public static final String TEST_PHONE = "555-1212";
    public static final String TEST_ACCOUNT = "org.example.test";
    public EntitySetTests() {
        super();
    }
    @Override
    public void setUp() {
        mContext = getContext();
    }
    protected ContactsSource getSource() {
        final ContactsSource source = new MockContactsSource();
        source.ensureInflated(getContext(), ContactsSource.LEVEL_CONSTRAINTS);
        return source;
    }
    static ContentValues getValues(ContentProviderOperation operation)
            throws NoSuchFieldException, IllegalAccessException {
        final Field field = ContentProviderOperation.class.getDeclaredField("mValues");
        field.setAccessible(true);
        return (ContentValues) field.get(operation);
    }
    static EntityDelta getUpdate(long rawContactId) {
        final Entity before = EntityDeltaTests.getEntity(rawContactId,
                EntityDeltaTests.TEST_PHONE_ID);
        return EntityDelta.fromBefore(before);
    }
    static EntityDelta getInsert() {
        final ContentValues after = new ContentValues();
        after.put(RawContacts.ACCOUNT_NAME, EntityDeltaTests.TEST_ACCOUNT_NAME);
        after.put(RawContacts.SEND_TO_VOICEMAIL, 1);
        final ValuesDelta values = ValuesDelta.fromAfter(after);
        return new EntityDelta(values);
    }
    static EntitySet buildSet(EntityDelta... deltas) {
        final EntitySet set = EntitySet.fromSingle(deltas[0]);
        for (int i = 1; i < deltas.length; i++) {
            set.add(deltas[i]);
        }
        return set;
    }
    static EntityDelta buildBeforeEntity(long rawContactId, long version,
            ContentValues... entries) {
        final ContentValues contact = new ContentValues();
        contact.put(RawContacts.VERSION, version);
        contact.put(RawContacts._ID, rawContactId);
        final Entity before = new Entity(contact);
        for (ContentValues entry : entries) {
            before.addSubValue(Data.CONTENT_URI, entry);
        }
        return EntityDelta.fromBefore(before);
    }
    static EntityDelta buildAfterEntity(ContentValues... entries) {
        final ContentValues contact = new ContentValues();
        contact.put(RawContacts.ACCOUNT_TYPE, TEST_ACCOUNT);
        final EntityDelta after = new EntityDelta(ValuesDelta.fromAfter(contact));
        for (ContentValues entry : entries) {
            after.addEntry(ValuesDelta.fromAfter(entry));
        }
        return after;
    }
    static ContentValues buildPhone(long phoneId) {
        return buildPhone(phoneId, Long.toString(phoneId));
    }
    static ContentValues buildPhone(long phoneId, String value) {
        final ContentValues values = new ContentValues();
        values.put(Data._ID, phoneId);
        values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        values.put(Phone.NUMBER, value);
        values.put(Phone.TYPE, Phone.TYPE_HOME);
        return values;
    }
    static ContentValues buildEmail(long emailId) {
        final ContentValues values = new ContentValues();
        values.put(Data._ID, emailId);
        values.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
        values.put(Email.DATA, Long.toString(emailId));
        values.put(Email.TYPE, Email.TYPE_HOME);
        return values;
    }
    static void insertPhone(EntitySet set, long rawContactId, ContentValues values) {
        final EntityDelta match = set.getByRawContactId(rawContactId);
        match.addEntry(ValuesDelta.fromAfter(values));
    }
    static ValuesDelta getPhone(EntitySet set, long rawContactId, long dataId) {
        final EntityDelta match = set.getByRawContactId(rawContactId);
        return match.getEntry(dataId);
    }
    static void assertDiffPattern(EntityDelta delta, ContentProviderOperation... pattern) {
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        delta.buildAssert(diff);
        delta.buildDiff(diff);
        assertDiffPattern(diff, pattern);
    }
    static void assertDiffPattern(EntitySet set, ContentProviderOperation... pattern) {
        assertDiffPattern(set.buildDiff(), pattern);
    }
    static void assertDiffPattern(ArrayList<ContentProviderOperation> diff,
            ContentProviderOperation... pattern) {
        assertEquals("Unexpected operations", pattern.length, diff.size());
        for (int i = 0; i < pattern.length; i++) {
            final ContentProviderOperation expected = pattern[i];
            final ContentProviderOperation found = diff.get(i);
            assertEquals("Unexpected uri", expected.getUri(), found.getUri());
            final String expectedType = getStringForType(expected.getType());
            final String foundType = getStringForType(found.getType());
            assertEquals("Unexpected type", expectedType, foundType);
            if (expected.getType() == TYPE_DELETE) continue;
            try {
                final ContentValues expectedValues = getValues(expected);
                final ContentValues foundValues = getValues(found);
                expectedValues.remove(BaseColumns._ID);
                foundValues.remove(BaseColumns._ID);
                assertEquals("Unexpected values", expectedValues, foundValues);
            } catch (NoSuchFieldException e) {
                fail(e.toString());
            } catch (IllegalAccessException e) {
                fail(e.toString());
            }
        }
    }
    static String getStringForType(int type) {
        switch (type) {
            case TYPE_ASSERT: return "TYPE_ASSERT";
            case TYPE_INSERT: return "TYPE_INSERT";
            case TYPE_UPDATE: return "TYPE_UPDATE";
            case TYPE_DELETE: return "TYPE_DELETE";
            default: return Integer.toString(type);
        }
    }
    static ContentProviderOperation buildAssertVersion(long version) {
        final ContentValues values = new ContentValues();
        values.put(RawContacts.VERSION, version);
        return buildOper(RawContacts.CONTENT_URI, TYPE_ASSERT, values);
    }
    static ContentProviderOperation buildAggregationModeUpdate(int mode) {
        final ContentValues values = new ContentValues();
        values.put(RawContacts.AGGREGATION_MODE, mode);
        return buildOper(RawContacts.CONTENT_URI, TYPE_UPDATE, values);
    }
    static ContentProviderOperation buildUpdateAggregationSuspended() {
        return buildAggregationModeUpdate(RawContacts.AGGREGATION_MODE_SUSPENDED);
    }
    static ContentProviderOperation buildUpdateAggregationDefault() {
        return buildAggregationModeUpdate(RawContacts.AGGREGATION_MODE_DEFAULT);
    }
    static ContentProviderOperation buildUpdateAggregationKeepTogether(long rawContactId) {
        final ContentValues values = new ContentValues();
        values.put(AggregationExceptions.RAW_CONTACT_ID1, rawContactId);
        values.put(AggregationExceptions.TYPE, AggregationExceptions.TYPE_KEEP_TOGETHER);
        return buildOper(AggregationExceptions.CONTENT_URI, TYPE_UPDATE, values);
    }
    static ContentValues buildDataInsert(ValuesDelta values, long rawContactId) {
        final ContentValues insertValues = values.getCompleteValues();
        insertValues.put(Data.RAW_CONTACT_ID, rawContactId);
        return insertValues;
    }
    static ContentProviderOperation buildDelete(Uri uri) {
        return buildOper(uri, TYPE_DELETE, (ContentValues)null);
    }
    static ContentProviderOperation buildOper(Uri uri, int type, ValuesDelta values) {
        return buildOper(uri, type, values.getCompleteValues());
    }
    static ContentProviderOperation buildOper(Uri uri, int type, ContentValues values) {
        switch (type) {
            case TYPE_ASSERT:
                return ContentProviderOperation.newAssertQuery(uri).withValues(values).build();
            case TYPE_INSERT:
                return ContentProviderOperation.newInsert(uri).withValues(values).build();
            case TYPE_UPDATE:
                return ContentProviderOperation.newUpdate(uri).withValues(values).build();
            case TYPE_DELETE:
                return ContentProviderOperation.newDelete(uri).build();
        }
        return null;
    }
    static Long getVersion(EntitySet set, Long rawContactId) {
        return set.getByRawContactId(rawContactId).getValues().getAsLong(RawContacts.VERSION);
    }
    static int countExceptionUpdates(ArrayList<ContentProviderOperation> diff) {
        int updateCount = 0;
        for (ContentProviderOperation oper : diff) {
            if (AggregationExceptions.CONTENT_URI.equals(oper.getUri())
                    && oper.getType() == ContentProviderOperation.TYPE_UPDATE) {
                updateCount++;
            }
        }
        return updateCount;
    }
    public void testInsert() {
        final EntityDelta insert = getInsert();
        final EntitySet set = buildSet(insert);
        final ArrayList<ContentProviderOperation> diff = set.buildDiff();
        final int exceptionCount = countExceptionUpdates(diff);
        assertEquals("Unexpected exception updates", 0, exceptionCount);
    }
    public void testUpdateUpdate() {
        final EntityDelta updateFirst = getUpdate(CONTACT_FIRST);
        final EntityDelta updateSecond = getUpdate(CONTACT_SECOND);
        final EntitySet set = buildSet(updateFirst, updateSecond);
        final ArrayList<ContentProviderOperation> diff = set.buildDiff();
        final int exceptionCount = countExceptionUpdates(diff);
        assertEquals("Unexpected exception updates", 0, exceptionCount);
    }
    public void testUpdateInsert() {
        final EntityDelta update = getUpdate(CONTACT_FIRST);
        final EntityDelta insert = getInsert();
        final EntitySet set = buildSet(update, insert);
        final ArrayList<ContentProviderOperation> diff = set.buildDiff();
        final int exceptionCount = countExceptionUpdates(diff);
        assertEquals("Unexpected exception updates", 1, exceptionCount);
    }
    public void testInsertUpdateInsert() {
        final EntityDelta insertFirst = getInsert();
        final EntityDelta update = getUpdate(CONTACT_FIRST);
        final EntityDelta insertSecond = getInsert();
        final EntitySet set = buildSet(insertFirst, update, insertSecond);
        final ArrayList<ContentProviderOperation> diff = set.buildDiff();
        final int exceptionCount = countExceptionUpdates(diff);
        assertEquals("Unexpected exception updates", 2, exceptionCount);
    }
    public void testInsertInsertInsert() {
        final EntityDelta insertFirst = getInsert();
        final EntityDelta insertSecond = getInsert();
        final EntityDelta insertThird = getInsert();
        final EntitySet set = buildSet(insertFirst, insertSecond, insertThird);
        final ArrayList<ContentProviderOperation> diff = set.buildDiff();
        final int exceptionCount = countExceptionUpdates(diff);
        assertEquals("Unexpected exception updates", 2, exceptionCount);
    }
    public void testMergeDataRemoteInsert() {
        final EntitySet first = buildSet(buildBeforeEntity(CONTACT_BOB, VER_FIRST,
                buildPhone(PHONE_RED)));
        final EntitySet second = buildSet(buildBeforeEntity(CONTACT_BOB, VER_SECOND,
                buildPhone(PHONE_RED), buildPhone(PHONE_GREEN)));
        final EntitySet merged = EntitySet.mergeAfter(second, first);
        assertEquals("Unexpected change when merging", second, merged);
    }
    public void testMergeDataLocalUpdateRemoteInsert() {
        final EntitySet first = buildSet(buildBeforeEntity(CONTACT_BOB, VER_FIRST,
                buildPhone(PHONE_RED)));
        final EntitySet second = buildSet(buildBeforeEntity(CONTACT_BOB, VER_SECOND,
                buildPhone(PHONE_RED), buildPhone(PHONE_GREEN)));
        final ValuesDelta phone = getPhone(first, CONTACT_BOB, PHONE_RED);
        phone.put(Phone.NUMBER, TEST_PHONE);
        assertDiffPattern(first,
                buildAssertVersion(VER_FIRST),
                buildUpdateAggregationSuspended(),
                buildOper(Data.CONTENT_URI, TYPE_UPDATE, phone.getAfter()),
                buildUpdateAggregationDefault());
        final EntitySet merged = EntitySet.mergeAfter(second, first);
        assertDiffPattern(merged,
                buildAssertVersion(VER_SECOND),
                buildUpdateAggregationSuspended(),
                buildOper(Data.CONTENT_URI, TYPE_UPDATE, phone.getAfter()),
                buildUpdateAggregationDefault());
    }
    public void testMergeDataLocalUpdateRemoteDelete() {
        final EntitySet first = buildSet(buildBeforeEntity(CONTACT_BOB, VER_FIRST,
                buildPhone(PHONE_RED)));
        final EntitySet second = buildSet(buildBeforeEntity(CONTACT_BOB, VER_SECOND,
                buildPhone(PHONE_GREEN)));
        final ValuesDelta phone = getPhone(first, CONTACT_BOB, PHONE_RED);
        phone.put(Phone.NUMBER, TEST_PHONE);
        assertDiffPattern(first,
                buildAssertVersion(VER_FIRST),
                buildUpdateAggregationSuspended(),
                buildOper(Data.CONTENT_URI, TYPE_UPDATE, phone.getAfter()),
                buildUpdateAggregationDefault());
        final EntitySet merged = EntitySet.mergeAfter(second, first);
        assertDiffPattern(merged,
                buildAssertVersion(VER_SECOND),
                buildUpdateAggregationSuspended(),
                buildOper(Data.CONTENT_URI, TYPE_INSERT, buildDataInsert(phone, CONTACT_BOB)),
                buildUpdateAggregationDefault());
    }
    public void testMergeDataLocalDeleteRemoteUpdate() {
        final EntitySet first = buildSet(buildBeforeEntity(CONTACT_BOB, VER_FIRST,
                buildPhone(PHONE_RED)));
        final EntitySet second = buildSet(buildBeforeEntity(CONTACT_BOB, VER_SECOND,
                buildPhone(PHONE_RED, TEST_PHONE)));
        final ValuesDelta phone = getPhone(first, CONTACT_BOB, PHONE_RED);
        phone.markDeleted();
        assertDiffPattern(first,
                buildAssertVersion(VER_FIRST),
                buildUpdateAggregationSuspended(),
                buildDelete(Data.CONTENT_URI),
                buildUpdateAggregationDefault());
        final EntitySet merged = EntitySet.mergeAfter(second, first);
        assertDiffPattern(merged,
                buildAssertVersion(VER_SECOND),
                buildUpdateAggregationSuspended(),
                buildDelete(Data.CONTENT_URI),
                buildUpdateAggregationDefault());
    }
    public void testMergeDataLocalInsertRemoteInsert() {
        final EntitySet first = buildSet(buildBeforeEntity(CONTACT_BOB, VER_FIRST,
                buildPhone(PHONE_RED)));
        final EntitySet second = buildSet(buildBeforeEntity(CONTACT_BOB, VER_SECOND,
                buildPhone(PHONE_RED), buildPhone(PHONE_GREEN)));
        final ValuesDelta bluePhone = ValuesDelta.fromAfter(buildPhone(PHONE_BLUE));
        first.getByRawContactId(CONTACT_BOB).addEntry(bluePhone);
        assertDiffPattern(first,
                buildAssertVersion(VER_FIRST),
                buildUpdateAggregationSuspended(),
                buildOper(Data.CONTENT_URI, TYPE_INSERT, buildDataInsert(bluePhone, CONTACT_BOB)),
                buildUpdateAggregationDefault());
        final EntitySet merged = EntitySet.mergeAfter(second, first);
        assertDiffPattern(merged,
                buildAssertVersion(VER_SECOND),
                buildUpdateAggregationSuspended(),
                buildOper(Data.CONTENT_URI, TYPE_INSERT, buildDataInsert(bluePhone, CONTACT_BOB)),
                buildUpdateAggregationDefault());
    }
    public void testMergeRawContactLocalInsertRemoteInsert() {
        final EntitySet first = buildSet(buildBeforeEntity(CONTACT_BOB, VER_FIRST,
                buildPhone(PHONE_RED)));
        final EntitySet second = buildSet(buildBeforeEntity(CONTACT_BOB, VER_SECOND,
                buildPhone(PHONE_RED)), buildBeforeEntity(CONTACT_MARY, VER_SECOND,
                buildPhone(PHONE_RED)));
        final ContentValues joePhoneInsert = buildPhone(PHONE_BLUE);
        final EntityDelta joeContact = buildAfterEntity(joePhoneInsert);
        final ContentValues joeContactInsert = joeContact.getValues().getCompleteValues();
        joeContactInsert.put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_SUSPENDED);
        first.add(joeContact);
        assertDiffPattern(first,
                buildAssertVersion(VER_FIRST),
                buildOper(RawContacts.CONTENT_URI, TYPE_INSERT, joeContactInsert),
                buildOper(Data.CONTENT_URI, TYPE_INSERT, joePhoneInsert),
                buildAggregationModeUpdate(RawContacts.AGGREGATION_MODE_DEFAULT),
                buildUpdateAggregationKeepTogether(CONTACT_BOB));
        final EntitySet merged = EntitySet.mergeAfter(second, first);
        assertDiffPattern(merged,
                buildAssertVersion(VER_SECOND),
                buildAssertVersion(VER_SECOND),
                buildOper(RawContacts.CONTENT_URI, TYPE_INSERT, joeContactInsert),
                buildOper(Data.CONTENT_URI, TYPE_INSERT, joePhoneInsert),
                buildAggregationModeUpdate(RawContacts.AGGREGATION_MODE_DEFAULT),
                buildUpdateAggregationKeepTogether(CONTACT_BOB));
    }
    public void testMergeRawContactLocalDeleteRemoteDelete() {
        final EntitySet first = buildSet(
                buildBeforeEntity(CONTACT_BOB, VER_FIRST, buildPhone(PHONE_RED)),
                buildBeforeEntity(CONTACT_MARY, VER_FIRST, buildPhone(PHONE_RED)));
        final EntitySet second = buildSet(
                buildBeforeEntity(CONTACT_BOB, VER_SECOND, buildPhone(PHONE_RED)));
        first.getByRawContactId(CONTACT_MARY).markDeleted();
        assertDiffPattern(first,
                buildAssertVersion(VER_FIRST),
                buildAssertVersion(VER_FIRST),
                buildDelete(RawContacts.CONTENT_URI));
        final EntitySet merged = EntitySet.mergeAfter(second, first);
        assertDiffPattern(merged);
    }
    public void testMergeRawContactLocalUpdateRemoteDelete() {
        final EntitySet first = buildSet(
                buildBeforeEntity(CONTACT_BOB, VER_FIRST, buildPhone(PHONE_RED)),
                buildBeforeEntity(CONTACT_MARY, VER_FIRST, buildPhone(PHONE_RED)));
        final EntitySet second = buildSet(
                buildBeforeEntity(CONTACT_BOB, VER_SECOND, buildPhone(PHONE_RED)));
        final ValuesDelta phone = getPhone(first, CONTACT_MARY, PHONE_RED);
        phone.put(Phone.NUMBER, TEST_PHONE);
        assertDiffPattern(first,
                buildAssertVersion(VER_FIRST),
                buildAssertVersion(VER_FIRST),
                buildUpdateAggregationSuspended(),
                buildOper(Data.CONTENT_URI, TYPE_UPDATE, phone.getAfter()),
                buildUpdateAggregationDefault());
        final ContentValues phoneInsert = phone.getCompleteValues();
        final ContentValues contactInsert = first.getByRawContactId(CONTACT_MARY).getValues()
                .getCompleteValues();
        contactInsert.put(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_SUSPENDED);
        final EntitySet merged = EntitySet.mergeAfter(second, first);
        assertDiffPattern(merged,
                buildAssertVersion(VER_SECOND),
                buildOper(RawContacts.CONTENT_URI, TYPE_INSERT, contactInsert),
                buildOper(Data.CONTENT_URI, TYPE_INSERT, phoneInsert),
                buildAggregationModeUpdate(RawContacts.AGGREGATION_MODE_DEFAULT),
                buildUpdateAggregationKeepTogether(CONTACT_BOB));
    }
    public void testMergeUsesNewVersion() {
        final EntitySet first = buildSet(buildBeforeEntity(CONTACT_BOB, VER_FIRST,
                buildPhone(PHONE_RED)));
        final EntitySet second = buildSet(buildBeforeEntity(CONTACT_BOB, VER_SECOND,
                buildPhone(PHONE_RED)));
        assertEquals((Long)VER_FIRST, getVersion(first, CONTACT_BOB));
        assertEquals((Long)VER_SECOND, getVersion(second, CONTACT_BOB));
        final EntitySet merged = EntitySet.mergeAfter(second, first);
        assertEquals((Long)VER_SECOND, getVersion(merged, CONTACT_BOB));
    }
    public void testMergeAfterEnsureAndTrim() {
        final EntitySet first = buildSet(buildBeforeEntity(CONTACT_BOB, VER_FIRST,
                buildEmail(EMAIL_YELLOW)));
        final EntitySet second = buildSet(buildBeforeEntity(CONTACT_BOB, VER_SECOND,
                buildEmail(EMAIL_YELLOW)));
        final ContactsSource source = getSource();
        final EntityDelta bobContact = first.getByRawContactId(CONTACT_BOB);
        EntityModifier.ensureKindExists(bobContact, source, Phone.CONTENT_ITEM_TYPE);
        final ValuesDelta bobPhone = bobContact.getSuperPrimaryEntry(Phone.CONTENT_ITEM_TYPE, true);
        assertDiffPattern(first,
                buildAssertVersion(VER_FIRST),
                buildUpdateAggregationSuspended(),
                buildOper(Data.CONTENT_URI, TYPE_INSERT, buildDataInsert(bobPhone, CONTACT_BOB)),
                buildUpdateAggregationDefault());
        EntityModifier.trimEmpty(bobContact, source);
        assertDiffPattern(first);
        final EntitySet merged = EntitySet.mergeAfter(second, first);
        assertDiffPattern(merged);
    }
}
