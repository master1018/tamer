 class ImportTestResolver extends MockContentResolver {
    final ImportTestProvider mProvider;
    public ImportTestResolver(TestCase testCase) {
        mProvider = new ImportTestProvider(testCase);
    }
    @Override
    public ContentProviderResult[] applyBatch(String authority,
            ArrayList<ContentProviderOperation> operations) {
        equalsString(authority, RawContacts.CONTENT_URI.toString());
        return mProvider.applyBatch(operations);
    }
    public void addExpectedContentValues(ContentValues expectedContentValues) {
        mProvider.addExpectedContentValues(expectedContentValues);
    }
    public void verify() {
        mProvider.verify();
    }
    private static boolean equalsString(String a, String b) {
        if (a == null || a.length() == 0) {
            return b == null || b.length() == 0;
        } else {
            return a.equals(b);
        }
    }
}
 class ImportTestProvider extends MockContentProvider {
    private static final Set<String> sKnownMimeTypeSet =
        new HashSet<String>(Arrays.asList(StructuredName.CONTENT_ITEM_TYPE,
                Nickname.CONTENT_ITEM_TYPE, Phone.CONTENT_ITEM_TYPE,
                Email.CONTENT_ITEM_TYPE, StructuredPostal.CONTENT_ITEM_TYPE,
                Im.CONTENT_ITEM_TYPE, Organization.CONTENT_ITEM_TYPE,
                Event.CONTENT_ITEM_TYPE, Photo.CONTENT_ITEM_TYPE,
                Note.CONTENT_ITEM_TYPE, Website.CONTENT_ITEM_TYPE,
                Relation.CONTENT_ITEM_TYPE, Event.CONTENT_ITEM_TYPE,
                GroupMembership.CONTENT_ITEM_TYPE));
    final Map<String, Collection<ContentValues>> mMimeTypeToExpectedContentValues;
    private final TestCase mTestCase;
    public ImportTestProvider(TestCase testCase) {
        mTestCase = testCase;
        mMimeTypeToExpectedContentValues =
            new HashMap<String, Collection<ContentValues>>();
        for (String acceptanbleMimeType : sKnownMimeTypeSet) {
            mMimeTypeToExpectedContentValues.put(
                    acceptanbleMimeType, new ArrayList<ContentValues>());
        }
    }
    public void addExpectedContentValues(ContentValues expectedContentValues) {
        final String mimeType = expectedContentValues.getAsString(Data.MIMETYPE);
        if (!sKnownMimeTypeSet.contains(mimeType)) {
            mTestCase.fail(String.format(
                    "Unknow MimeType %s in the test code. Test code should be broken.",
                    mimeType));
        }
        final Collection<ContentValues> contentValuesCollection =
                mMimeTypeToExpectedContentValues.get(mimeType);
        contentValuesCollection.add(expectedContentValues);
    }
    @Override
    public ContentProviderResult[] applyBatch(
            ArrayList<ContentProviderOperation> operations) {
        if (operations == null) {
            mTestCase.fail("There is no operation.");
        }
        final int size = operations.size();
        ContentProviderResult[] fakeResultArray = new ContentProviderResult[size];
        for (int i = 0; i < size; i++) {
            Uri uri = Uri.withAppendedPath(RawContacts.CONTENT_URI, String.valueOf(i));
            fakeResultArray[i] = new ContentProviderResult(uri);
        }
        for (int i = 0; i < size; i++) {
            ContentProviderOperation operation = operations.get(i);
            ContentValues contentValues = operation.resolveValueBackReferences(
                    fakeResultArray, i);
        }
        for (int i = 0; i < size; i++) {
            ContentProviderOperation operation = operations.get(i);
            ContentValues actualContentValues = operation.resolveValueBackReferences(
                    fakeResultArray, i);
            final Uri uri = operation.getUri();
            if (uri.equals(RawContacts.CONTENT_URI)) {
                mTestCase.assertNull(actualContentValues.get(RawContacts.ACCOUNT_NAME));
                mTestCase.assertNull(actualContentValues.get(RawContacts.ACCOUNT_TYPE));
            } else if (uri.equals(Data.CONTENT_URI)) {
                final String mimeType = actualContentValues.getAsString(Data.MIMETYPE);
                if (!sKnownMimeTypeSet.contains(mimeType)) {
                    mTestCase.fail(String.format(
                            "Unknown MimeType %s. Probably added after developing this test",
                            mimeType));
                }
                Set<String> keyToBeRemoved = new HashSet<String>();
                for (Entry<String, Object> entry : actualContentValues.valueSet()) {
                    Object value = entry.getValue();
                    if (value == null || TextUtils.isEmpty(value.toString())) {
                        keyToBeRemoved.add(entry.getKey());
                    }
                }
                for (String key: keyToBeRemoved) {
                    actualContentValues.remove(key);
                }
                if (actualContentValues.containsKey(Data.RAW_CONTACT_ID)) {
                    actualContentValues.remove(Data.RAW_CONTACT_ID);
                }
                final Collection<ContentValues> contentValuesCollection =
                    mMimeTypeToExpectedContentValues.get(mimeType);
                if (contentValuesCollection.isEmpty()) {
                    mTestCase.fail("ContentValues for MimeType " + mimeType
                            + " is not expected at all (" + actualContentValues + ")");
                }
                boolean checked = false;
                for (ContentValues expectedContentValues : contentValuesCollection) {
                    if (equalsForContentValues(expectedContentValues,
                            actualContentValues)) {
                        mTestCase.assertTrue(contentValuesCollection.remove(expectedContentValues));
                        checked = true;
                        break;
                    }
                }
                if (!checked) {
                    final StringBuilder builder = new StringBuilder();
                    builder.append("Unexpected: ");
                    builder.append(convertToEasilyReadableString(actualContentValues));
                    builder.append("\nExpected: ");
                    for (ContentValues expectedContentValues : contentValuesCollection) {
                        builder.append(convertToEasilyReadableString(expectedContentValues));
                    }
                    mTestCase.fail(builder.toString());
                }
            } else {
                mTestCase.fail("Unexpected Uri has come: " + uri);
            }
        }  
        return fakeResultArray;
    }
    public void verify() {
        StringBuilder builder = new StringBuilder();
        for (Collection<ContentValues> contentValuesCollection :
                mMimeTypeToExpectedContentValues.values()) {
            for (ContentValues expectedContentValues: contentValuesCollection) {
                builder.append(convertToEasilyReadableString(expectedContentValues));
                builder.append("\n");
            }
        }
        if (builder.length() > 0) {
            final String failMsg =
                "There is(are) remaining expected ContentValues instance(s): \n"
                    + builder.toString();
            mTestCase.fail(failMsg);
        }
    }
    private String convertToEasilyReadableString(ContentValues contentValues) {
        if (contentValues == null) {
            return "null";
        }
        String mimeTypeValue = "";
        SortedMap<String, String> sortedMap = new TreeMap<String, String>();
        for (Entry<String, Object> entry : contentValues.valueSet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            final String valueString = (value != null ? value.toString() : null);
            if (Data.MIMETYPE.equals(key)) {
                mimeTypeValue = valueString;
            } else {
                mTestCase.assertNotNull(key);
                sortedMap.put(key, valueString);
            }
        }
        StringBuilder builder = new StringBuilder();
        builder.append(Data.MIMETYPE);
        builder.append('=');
        builder.append(mimeTypeValue);
        for (Entry<String, String> entry : sortedMap.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            builder.append(' ');
            builder.append(key);
            builder.append("=\"");
            builder.append(value);
            builder.append('"');
        }
        return builder.toString();
    }
    private static boolean equalsForContentValues(
            ContentValues expected, ContentValues actual) {
        if (expected == actual) {
            return true;
        } else if (expected == null || actual == null || expected.size() != actual.size()) {
            return false;
        }
        for (Entry<String, Object> entry : expected.valueSet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (!actual.containsKey(key)) {
                return false;
            }
            if (value instanceof byte[]) {
                Object actualValue = actual.get(key);
                if (!Arrays.equals((byte[])value, (byte[])actualValue)) {
                    return false;
                }
            } else if (!value.equals(actual.get(key))) {
                    return false;
            }
        }
        return true;
    }
}
