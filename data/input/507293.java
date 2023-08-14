 public class ExportTestResolver extends MockContentResolver {
    ExportTestProvider mProvider;
    public ExportTestResolver(TestCase testCase) {
        mProvider = new ExportTestProvider(testCase);
        addProvider(VCardComposer.VCARD_TEST_AUTHORITY, mProvider);
        addProvider(RawContacts.CONTENT_URI.getAuthority(), mProvider);
    }
    public ContactEntry addInputContactEntry() {
        return mProvider.buildInputEntry();
    }
}
 class MockEntityIterator implements EntityIterator {
    List<Entity> mEntityList;
    Iterator<Entity> mIterator;
    public MockEntityIterator(List<ContentValues> contentValuesList) {
        mEntityList = new ArrayList<Entity>();
        Entity entity = new Entity(new ContentValues());
        for (ContentValues contentValues : contentValuesList) {
                entity.addSubValue(Data.CONTENT_URI, contentValues);
        }
        mEntityList.add(entity);
        mIterator = mEntityList.iterator();
    }
    public boolean hasNext() {
        return mIterator.hasNext();
    }
    public Entity next() {
        return mIterator.next();
    }
    public void remove() {
        throw new UnsupportedOperationException("remove not supported");
    }
    public void reset() {
        mIterator = mEntityList.iterator();
    }
    public void close() {
    }
}
 class ContactEntry {
    private final List<ContentValues> mContentValuesList = new ArrayList<ContentValues>();
    public ContentValuesBuilder addContentValues(String mimeType) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Data.MIMETYPE, mimeType);
        mContentValuesList.add(contentValues);
        return new ContentValuesBuilder(contentValues);
    }
    public List<ContentValues> getList() {
        return mContentValuesList;
    }
}
 class ExportTestProvider extends MockContentProvider {
    final private TestCase mTestCase;
    final private ArrayList<ContactEntry> mContactEntryList = new ArrayList<ContactEntry>();
    public ExportTestProvider(TestCase testCase) {
        mTestCase = testCase;
    }
    public ContactEntry buildInputEntry() {
        ContactEntry contactEntry = new ContactEntry();
        mContactEntryList.add(contactEntry);
        return contactEntry;
    }
    public EntityIterator queryEntities(Uri uri,
            String selection, String[] selectionArgs, String sortOrder) {
        mTestCase.assertTrue(uri != null);
        mTestCase.assertTrue(ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()));
        final String authority = uri.getAuthority();
        mTestCase.assertTrue(RawContacts.CONTENT_URI.getAuthority().equals(authority));
        mTestCase.assertTrue((Data.CONTACT_ID + "=?").equals(selection));
        mTestCase.assertEquals(1, selectionArgs.length);
        final int id = Integer.parseInt(selectionArgs[0]);
        mTestCase.assertTrue(id >= 0 && id < mContactEntryList.size());
        return new MockEntityIterator(mContactEntryList.get(id).getList());
    }
    @Override
    public Cursor query(Uri uri,String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        mTestCase.assertTrue(VCardComposer.CONTACTS_TEST_CONTENT_URI.equals(uri));
        mTestCase.assertNull(selection);
        mTestCase.assertNull(selectionArgs);
        mTestCase.assertNull(sortOrder);
        return new MockCursor() {
            int mCurrentPosition = -1;
            @Override
            public int getCount() {
                return mContactEntryList.size();
            }
            @Override
            public boolean moveToFirst() {
                mCurrentPosition = 0;
                return true;
            }
            @Override
            public boolean moveToNext() {
                if (mCurrentPosition < mContactEntryList.size()) {
                    mCurrentPosition++;
                    return true;
                } else {
                    return false;
                }
            }
            @Override
            public boolean isBeforeFirst() {
                return mCurrentPosition < 0;
            }
            @Override
            public boolean isAfterLast() {
                return mCurrentPosition >= mContactEntryList.size();
            }
            @Override
            public int getColumnIndex(String columnName) {
                mTestCase.assertEquals(Contacts._ID, columnName);
                return 0;
            }
            @Override
            public int getInt(int columnIndex) {
                mTestCase.assertEquals(0, columnIndex);
                mTestCase.assertTrue(mCurrentPosition >= 0
                        && mCurrentPosition < mContactEntryList.size());
                return mCurrentPosition;
            }
            @Override
            public String getString(int columnIndex) {
                return String.valueOf(getInt(columnIndex));
            }
            @Override
            public void close() {
            }
        };
    }
}
