public class EntityModifierTests extends AndroidTestCase {
    public static final String TAG = "EntityModifierTests";
    public static final long VER_FIRST = 100;
    private static final long TEST_ID = 4;
    private static final String TEST_PHONE = "218-555-1212";
    private static final String TEST_NAME = "Adam Young";
    private static final String TEST_NAME2 = "Breanne Duren";
    private static final String TEST_IM = "example@example.com";
    private static final String TEST_POSTAL = "1600 Amphitheatre Parkway";
    private static final String TEST_ACCOUNT_NAME = "unittest@example.com";
    private static final String TEST_ACCOUNT_TYPE = "com.example.unittest";
    public EntityModifierTests() {
        super();
    }
    @Override
    public void setUp() {
        mContext = getContext();
    }
    public static class MockContactsSource extends ContactsSource {
        @Override
        protected void inflate(Context context, int inflateLevel) {
            this.accountType = TEST_ACCOUNT_TYPE;
            this.setInflatedLevel(ContactsSource.LEVEL_CONSTRAINTS);
            DataKind kind = new DataKind(Phone.CONTENT_ITEM_TYPE, -1, -1, 10, true);
            kind.typeOverallMax = 5;
            kind.typeColumn = Phone.TYPE;
            kind.typeList = Lists.newArrayList();
            kind.typeList.add(new EditType(Phone.TYPE_HOME, -1).setSpecificMax(2));
            kind.typeList.add(new EditType(Phone.TYPE_WORK, -1).setSpecificMax(1));
            kind.typeList.add(new EditType(Phone.TYPE_FAX_WORK, -1).setSecondary(true));
            kind.typeList.add(new EditType(Phone.TYPE_OTHER, -1));
            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(Phone.NUMBER, -1, -1));
            kind.fieldList.add(new EditField(Phone.LABEL, -1, -1));
            addKind(kind);
            kind = new DataKind(Email.CONTENT_ITEM_TYPE, -1, -1, 10, true);
            kind.typeOverallMax = -1;
            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(Email.DATA, -1, -1));
            addKind(kind);
            kind = new DataKind(Im.CONTENT_ITEM_TYPE, -1, -1, 10, true);
            kind.typeOverallMax = 1;
            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(Im.DATA, -1, -1));
            addKind(kind);
            kind = new DataKind(Organization.CONTENT_ITEM_TYPE, -1, -1, 10, true);
            kind.typeOverallMax = 1;
            kind.fieldList = Lists.newArrayList();
            kind.fieldList.add(new EditField(Organization.COMPANY, -1, -1));
            kind.fieldList.add(new EditField(Organization.TITLE, -1, -1));
            addKind(kind);
        }
        @Override
        public int getHeaderColor(Context context) {
            return 0;
        }
        @Override
        public int getSideBarColor(Context context) {
            return 0xffffff;
        }
    }
    protected ContactsSource getSource() {
        final ContactsSource source = new MockContactsSource();
        source.ensureInflated(getContext(), ContactsSource.LEVEL_CONSTRAINTS);
        return source;
    }
    protected Sources getSources(ContactsSource... sources) {
        return new Sources(sources);
    }
    protected EntityDelta getEntity(Long existingId, ContentValues... entries) {
        final ContentValues contact = new ContentValues();
        if (existingId != null) {
            contact.put(RawContacts._ID, existingId);
        }
        contact.put(RawContacts.ACCOUNT_NAME, TEST_ACCOUNT_NAME);
        contact.put(RawContacts.ACCOUNT_TYPE, TEST_ACCOUNT_TYPE);
        final Entity before = new Entity(contact);
        for (ContentValues values : entries) {
            before.addSubValue(Data.CONTENT_URI, values);
        }
        return EntityDelta.fromBefore(before);
    }
    protected void assertContains(List<?> list, Object object) {
        assertTrue("Missing expected value", list.contains(object));
    }
    protected void assertNotContains(List<?> list, Object object) {
        assertFalse("Contained unexpected value", list.contains(object));
    }
    public void testValidTypes() {
        final ContactsSource source = getSource();
        final DataKind kindPhone = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
        final EditType typeHome = EntityModifier.getType(kindPhone, Phone.TYPE_HOME);
        final EditType typeWork = EntityModifier.getType(kindPhone, Phone.TYPE_WORK);
        final EditType typeOther = EntityModifier.getType(kindPhone, Phone.TYPE_OTHER);
        List<EditType> validTypes;
        final EntityDelta state = getEntity(TEST_ID);
        EntityModifier.insertChild(state, kindPhone, typeHome);
        EntityModifier.insertChild(state, kindPhone, typeWork);
        validTypes = EntityModifier.getValidTypes(state, kindPhone, null);
        assertContains(validTypes, typeHome);
        assertNotContains(validTypes, typeWork);
        assertContains(validTypes, typeOther);
        EntityModifier.insertChild(state, kindPhone, typeHome);
        validTypes = EntityModifier.getValidTypes(state, kindPhone, null);
        assertNotContains(validTypes, typeHome);
        assertNotContains(validTypes, typeWork);
        assertContains(validTypes, typeOther);
        EntityModifier.insertChild(state, kindPhone, typeHome);
        EntityModifier.insertChild(state, kindPhone, typeHome);
        validTypes = EntityModifier.getValidTypes(state, kindPhone, null);
        assertNotContains(validTypes, typeHome);
        assertNotContains(validTypes, typeWork);
        assertNotContains(validTypes, typeOther);
    }
    public void testCanInsert() {
        final ContactsSource source = getSource();
        final DataKind kindPhone = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
        final EditType typeHome = EntityModifier.getType(kindPhone, Phone.TYPE_HOME);
        final EditType typeWork = EntityModifier.getType(kindPhone, Phone.TYPE_WORK);
        final EditType typeOther = EntityModifier.getType(kindPhone, Phone.TYPE_OTHER);
        final EntityDelta state = getEntity(TEST_ID);
        EntityModifier.insertChild(state, kindPhone, typeHome);
        EntityModifier.insertChild(state, kindPhone, typeWork);
        assertTrue("Unable to insert", EntityModifier.canInsert(state, kindPhone));
        EntityModifier.insertChild(state, kindPhone, typeOther);
        EntityModifier.insertChild(state, kindPhone, typeOther);
        assertTrue("Unable to insert", EntityModifier.canInsert(state, kindPhone));
        EntityModifier.insertChild(state, kindPhone, typeHome);
        assertFalse("Able to insert", EntityModifier.canInsert(state, kindPhone));
    }
    public void testBestValidType() {
        final ContactsSource source = getSource();
        final DataKind kindPhone = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
        final EditType typeHome = EntityModifier.getType(kindPhone, Phone.TYPE_HOME);
        final EditType typeWork = EntityModifier.getType(kindPhone, Phone.TYPE_WORK);
        final EditType typeFaxWork = EntityModifier.getType(kindPhone, Phone.TYPE_FAX_WORK);
        final EditType typeOther = EntityModifier.getType(kindPhone, Phone.TYPE_OTHER);
        EditType suggested;
        final EntityDelta state = getEntity(TEST_ID);
        suggested = EntityModifier.getBestValidType(state, kindPhone, false, Integer.MIN_VALUE);
        assertEquals("Unexpected suggestion", typeHome, suggested);
        EntityModifier.insertChild(state, kindPhone, typeHome);
        suggested = EntityModifier.getBestValidType(state, kindPhone, false, Integer.MIN_VALUE);
        assertEquals("Unexpected suggestion", typeWork, suggested);
        EntityModifier.insertChild(state, kindPhone, typeFaxWork);
        suggested = EntityModifier.getBestValidType(state, kindPhone, false, Integer.MIN_VALUE);
        assertEquals("Unexpected suggestion", typeWork, suggested);
        EntityModifier.insertChild(state, kindPhone, typeOther);
        suggested = EntityModifier.getBestValidType(state, kindPhone, false, Integer.MIN_VALUE);
        assertEquals("Unexpected suggestion", typeWork, suggested);
        EntityModifier.insertChild(state, kindPhone, typeWork);
        suggested = EntityModifier.getBestValidType(state, kindPhone, false, Integer.MIN_VALUE);
        assertEquals("Unexpected suggestion", typeOther, suggested);
    }
    public void testIsEmptyEmpty() {
        final ContactsSource source = getSource();
        final DataKind kindPhone = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
        final ContentValues after = new ContentValues();
        final ValuesDelta values = ValuesDelta.fromAfter(after);
        assertTrue("Expected empty", EntityModifier.isEmpty(values, kindPhone));
    }
    public void testIsEmptyDirectFields() {
        final ContactsSource source = getSource();
        final DataKind kindPhone = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
        final EditType typeHome = EntityModifier.getType(kindPhone, Phone.TYPE_HOME);
        final EntityDelta state = getEntity(TEST_ID);
        final ValuesDelta values = EntityModifier.insertChild(state, kindPhone, typeHome);
        assertTrue("Expected empty", EntityModifier.isEmpty(values, kindPhone));
        values.put(Phone.NUMBER, TEST_PHONE);
        assertFalse("Expected non-empty", EntityModifier.isEmpty(values, kindPhone));
    }
    public void testTrimEmptySingle() {
        final ContactsSource source = getSource();
        final DataKind kindPhone = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
        final EditType typeHome = EntityModifier.getType(kindPhone, Phone.TYPE_HOME);
        final EntityDelta state = getEntity(TEST_ID);
        final ValuesDelta values = EntityModifier.insertChild(state, kindPhone, typeHome);
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 3, diff.size());
        {
            final ContentProviderOperation oper = diff.get(0);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(1);
            assertEquals("Incorrect type", TYPE_INSERT, oper.getType());
            assertEquals("Incorrect target", Data.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(2);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
        EntityModifier.trimEmpty(state, source);
        diff.clear();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 1, diff.size());
        {
            final ContentProviderOperation oper = diff.get(0);
            assertEquals("Incorrect type", TYPE_DELETE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
    }
    public void testTrimEmptySpaces() {
        final ContactsSource source = getSource();
        final DataKind kindPhone = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
        final EditType typeHome = EntityModifier.getType(kindPhone, Phone.TYPE_HOME);
        final EntityDelta state = EntitySetTests.buildBeforeEntity(TEST_ID, VER_FIRST);
        final ValuesDelta values = EntityModifier.insertChild(state, kindPhone, typeHome);
        values.put(Phone.NUMBER, "   ");
        EntitySetTests.assertDiffPattern(state,
                EntitySetTests.buildAssertVersion(VER_FIRST),
                EntitySetTests.buildUpdateAggregationSuspended(),
                EntitySetTests.buildOper(Data.CONTENT_URI, TYPE_INSERT,
                        EntitySetTests.buildDataInsert(values, TEST_ID)),
                EntitySetTests.buildUpdateAggregationDefault());
        EntityModifier.trimEmpty(state, source);
        EntitySetTests.assertDiffPattern(state,
                EntitySetTests.buildAssertVersion(VER_FIRST),
                EntitySetTests.buildDelete(RawContacts.CONTENT_URI));
    }
    public void testTrimLeaveValid() {
        final ContactsSource source = getSource();
        final DataKind kindPhone = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
        final EditType typeHome = EntityModifier.getType(kindPhone, Phone.TYPE_HOME);
        final EntityDelta state = EntitySetTests.buildBeforeEntity(TEST_ID, VER_FIRST);
        final ValuesDelta values = EntityModifier.insertChild(state, kindPhone, typeHome);
        values.put(Phone.NUMBER, TEST_PHONE);
        EntitySetTests.assertDiffPattern(state,
                EntitySetTests.buildAssertVersion(VER_FIRST),
                EntitySetTests.buildUpdateAggregationSuspended(),
                EntitySetTests.buildOper(Data.CONTENT_URI, TYPE_INSERT,
                        EntitySetTests.buildDataInsert(values, TEST_ID)),
                EntitySetTests.buildUpdateAggregationDefault());
        EntityModifier.trimEmpty(state, source);
        EntitySetTests.assertDiffPattern(state,
                EntitySetTests.buildAssertVersion(VER_FIRST),
                EntitySetTests.buildUpdateAggregationSuspended(),
                EntitySetTests.buildOper(Data.CONTENT_URI, TYPE_INSERT,
                        EntitySetTests.buildDataInsert(values, TEST_ID)),
                EntitySetTests.buildUpdateAggregationDefault());
    }
    public void testTrimEmptyUntouched() {
        final ContactsSource source = getSource();
        final DataKind kindPhone = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
        final EditType typeHome = EntityModifier.getType(kindPhone, Phone.TYPE_HOME);
        final EntityDelta state = getEntity(TEST_ID);
        final ContentValues before = new ContentValues();
        before.put(Data._ID, TEST_ID);
        before.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        state.addEntry(ValuesDelta.fromBefore(before));
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 0, diff.size());
        EntityModifier.trimEmpty(state, source);
        diff.clear();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 0, diff.size());
    }
    public void testTrimEmptyAfterUpdate() {
        final ContactsSource source = getSource();
        final DataKind kindPhone = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
        final EditType typeHome = EntityModifier.getType(kindPhone, Phone.TYPE_HOME);
        final ContentValues before = new ContentValues();
        before.put(Data._ID, TEST_ID);
        before.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        before.put(kindPhone.typeColumn, typeHome.rawValue);
        before.put(Phone.NUMBER, TEST_PHONE);
        final EntityDelta state = getEntity(TEST_ID, before);
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 0, diff.size());
        final ValuesDelta child = state.getEntry(TEST_ID);
        child.put(Phone.NUMBER, "");
        diff.clear();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 3, diff.size());
        {
            final ContentProviderOperation oper = diff.get(0);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(1);
            assertEquals("Incorrect type", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", Data.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(2);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
        EntityModifier.trimEmpty(state, source);
        diff.clear();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 1, diff.size());
        {
            final ContentProviderOperation oper = diff.get(0);
            assertEquals("Incorrect type", TYPE_DELETE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
    }
    public void testTrimInsertEmpty() {
        final ContactsSource source = getSource();
        final Sources sources = getSources(source);
        final DataKind kindPhone = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
        final EditType typeHome = EntityModifier.getType(kindPhone, Phone.TYPE_HOME);
        final EntityDelta state = getEntity(null);
        final EntitySet set = EntitySet.fromSingle(state);
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 2, diff.size());
        {
            final ContentProviderOperation oper = diff.get(0);
            assertEquals("Incorrect type", TYPE_INSERT, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
        EntityModifier.trimEmpty(set, sources);
        diff.clear();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 0, diff.size());
    }
    public void testTrimInsertInsert() {
        final ContactsSource source = getSource();
        final Sources sources = getSources(source);
        final DataKind kindPhone = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
        final EditType typeHome = EntityModifier.getType(kindPhone, Phone.TYPE_HOME);
        final EntityDelta state = getEntity(null);
        final ValuesDelta values = EntityModifier.insertChild(state, kindPhone, typeHome);
        final EntitySet set = EntitySet.fromSingle(state);
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        state.buildDiff(diff);
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
        EntityModifier.trimEmpty(set, sources);
        diff.clear();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 0, diff.size());
    }
    public void testTrimUpdateRemain() {
        final ContactsSource source = getSource();
        final Sources sources = getSources(source);
        final DataKind kindPhone = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
        final EditType typeHome = EntityModifier.getType(kindPhone, Phone.TYPE_HOME);
        final ContentValues first = new ContentValues();
        first.put(Data._ID, TEST_ID);
        first.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        first.put(kindPhone.typeColumn, typeHome.rawValue);
        first.put(Phone.NUMBER, TEST_PHONE);
        final ContentValues second = new ContentValues();
        second.put(Data._ID, TEST_ID);
        second.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        second.put(kindPhone.typeColumn, typeHome.rawValue);
        second.put(Phone.NUMBER, TEST_PHONE);
        final EntityDelta state = getEntity(TEST_ID, first, second);
        final EntitySet set = EntitySet.fromSingle(state);
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 0, diff.size());
        final ValuesDelta child = state.getEntry(TEST_ID);
        child.put(Phone.NUMBER, "");
        diff.clear();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 3, diff.size());
        {
            final ContentProviderOperation oper = diff.get(0);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(1);
            assertEquals("Incorrect type", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", Data.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(2);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
        EntityModifier.trimEmpty(set, sources);
        diff.clear();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 3, diff.size());
        {
            final ContentProviderOperation oper = diff.get(0);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(1);
            assertEquals("Incorrect type", TYPE_DELETE, oper.getType());
            assertEquals("Incorrect target", Data.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(2);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
    }
    public void testTrimUpdateUpdate() {
        final ContactsSource source = getSource();
        final Sources sources = getSources(source);
        final DataKind kindPhone = source.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
        final EditType typeHome = EntityModifier.getType(kindPhone, Phone.TYPE_HOME);
        final ContentValues first = new ContentValues();
        first.put(Data._ID, TEST_ID);
        first.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
        first.put(kindPhone.typeColumn, typeHome.rawValue);
        first.put(Phone.NUMBER, TEST_PHONE);
        final EntityDelta state = getEntity(TEST_ID, first);
        final EntitySet set = EntitySet.fromSingle(state);
        final ArrayList<ContentProviderOperation> diff = Lists.newArrayList();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 0, diff.size());
        final ValuesDelta child = state.getEntry(TEST_ID);
        child.put(Phone.NUMBER, "");
        diff.clear();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 3, diff.size());
        {
            final ContentProviderOperation oper = diff.get(0);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(1);
            assertEquals("Incorrect type", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", Data.CONTENT_URI, oper.getUri());
        }
        {
            final ContentProviderOperation oper = diff.get(2);
            assertEquals("Expected aggregation mode change", TYPE_UPDATE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
        EntityModifier.trimEmpty(set, sources);
        diff.clear();
        state.buildDiff(diff);
        assertEquals("Unexpected operations", 1, diff.size());
        {
            final ContentProviderOperation oper = diff.get(0);
            assertEquals("Incorrect type", TYPE_DELETE, oper.getType());
            assertEquals("Incorrect target", RawContacts.CONTENT_URI, oper.getUri());
        }
    }
    public void testParseExtrasExistingName() {
        final ContactsSource source = getSource();
        final DataKind kindName = source.getKindForMimetype(StructuredName.CONTENT_ITEM_TYPE);
        final ContentValues first = new ContentValues();
        first.put(Data._ID, TEST_ID);
        first.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        first.put(StructuredName.GIVEN_NAME, TEST_NAME);
        final EntityDelta state = getEntity(TEST_ID, first);
        final Bundle extras = new Bundle();
        extras.putString(Insert.NAME, TEST_NAME2);
        EntityModifier.parseExtras(mContext, source, state, extras);
        final int nameCount = state.getMimeEntriesCount(StructuredName.CONTENT_ITEM_TYPE, true);
        assertEquals("Unexpected names", 1, nameCount);
    }
    public void testParseExtrasIgnoreLimit() {
        final ContactsSource source = getSource();
        final DataKind kindIm = source.getKindForMimetype(Im.CONTENT_ITEM_TYPE);
        final ContentValues first = new ContentValues();
        first.put(Data._ID, TEST_ID);
        first.put(Data.MIMETYPE, Im.CONTENT_ITEM_TYPE);
        first.put(Im.DATA, TEST_IM);
        final EntityDelta state = getEntity(TEST_ID, first);
        final int beforeCount = state.getMimeEntries(Im.CONTENT_ITEM_TYPE).size();
        final Bundle extras = new Bundle();
        extras.putInt(Insert.IM_PROTOCOL, Im.PROTOCOL_GOOGLE_TALK);
        extras.putString(Insert.IM_HANDLE, TEST_IM);
        EntityModifier.parseExtras(mContext, source, state, extras);
        final int afterCount = state.getMimeEntries(Im.CONTENT_ITEM_TYPE).size();
        assertEquals("Broke source rules", beforeCount, afterCount);
    }
    public void testParseExtrasIgnoreUnhandled() {
        final ContactsSource source = getSource();
        final EntityDelta state = getEntity(TEST_ID);
        final Bundle extras = new Bundle();
        extras.putString(Insert.POSTAL, TEST_POSTAL);
        EntityModifier.parseExtras(mContext, source, state, extras);
        assertNull("Broke source rules", state.getMimeEntries(StructuredPostal.CONTENT_ITEM_TYPE));
    }
    public void testParseExtrasJobTitle() {
        final ContactsSource source = getSource();
        final EntityDelta state = getEntity(TEST_ID);
        final Bundle extras = new Bundle();
        extras.putString(Insert.JOB_TITLE, TEST_NAME);
        EntityModifier.parseExtras(mContext, source, state, extras);
        final int count = state.getMimeEntries(Organization.CONTENT_ITEM_TYPE).size();
        assertEquals("Expected to create organization", 1, count);
    }
}
