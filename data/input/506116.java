public class GlobalSearchSupportTest extends BaseContactsProvider2Test {
    public void testSearchSuggestionsNotInVisibleGroup() throws Exception {
        Account account = new Account("actname", "acttype");
        long rawContactId = createRawContact(account);
        insertStructuredName(rawContactId, "Deer", "Dough");
        Uri searchUri = new Uri.Builder().scheme("content").authority(ContactsContract.AUTHORITY)
                .appendPath(SearchManager.SUGGEST_URI_PATH_QUERY).appendPath("D").build();
        Cursor c = mResolver.query(searchUri, null, null, null, null);
        assertEquals(0, c.getCount());
        c.close();
    }
    public void testSearchSuggestionsByNameWithPhoto() throws Exception {
        GoldenContact contact = new GoldenContactBuilder().name("Deer", "Dough").photo(
                loadTestPhoto()).build();
        new SuggestionTesterBuilder(contact).query("D").expectIcon1Uri(true).expectedText1(
                "Deer Dough").build().test();
    }
    public void testSearchSuggestionsByNameWithPhotoAndCompany() throws Exception {
        GoldenContact contact = new GoldenContactBuilder().name("Deer", "Dough").photo(
                loadTestPhoto()).company("Google").build();
        new SuggestionTesterBuilder(contact).query("D").expectIcon1Uri(true).expectedText1(
                "Deer Dough").expectedText2("Google").build().test();
    }
    public void testSearchSuggestionsByNameWithPhotoAndPhone() {
        GoldenContact contact = new GoldenContactBuilder().name("Deer", "Dough").photo(
                loadTestPhoto()).phone("1-800-4664-411").build();
        new SuggestionTesterBuilder(contact).query("D").expectIcon1Uri(true).expectedText1(
                "Deer Dough").expectedText2("1-800-4664-411").build().test();
    }
    public void testSearchSuggestionsByNameWithPhotoAndEmail() {
        GoldenContact contact = new GoldenContactBuilder().name("Deer", "Dough").photo(
                loadTestPhoto()).email("foo@acme.com").build();
        new SuggestionTesterBuilder(contact).query("D").expectIcon1Uri(true).expectedIcon2(
                String.valueOf(StatusUpdates.getPresenceIconResourceId(StatusUpdates.OFFLINE)))
                .expectedText1("Deer Dough").expectedText2("foo@acme.com").build().test();
    }
    public void testSearchSuggestionsByNameWithCompany() {
        GoldenContact contact = new GoldenContactBuilder().name("Deer", "Dough").company("Google")
                .build();
        new SuggestionTesterBuilder(contact).query("D").expectedText1("Deer Dough").expectedText2(
                "Google").build().test();
    }
    public void testSearchByNicknameWithCompany() {
        GoldenContact contact = new GoldenContactBuilder().name("Deer", "Dough").nickname(
                "Little Fawn").company("Google").build();
        new SuggestionTesterBuilder(contact).query("L").expectedText1("Deer Dough").expectedText2(
                "Google").build().test();
    }
    public void testSearchByCompany() {
        GoldenContact contact = new GoldenContactBuilder().name("Deer", "Dough").company("Google")
                .build();
        new SuggestionTesterBuilder(contact).query("G").expectedText1("Deer Dough").expectedText2(
                "Google").build().test();
    }
    public void testSearchByTitleWithCompany() {
        GoldenContact contact = new GoldenContactBuilder().name("Deer", "Dough").company("Google")
                .title("Software Engineer").build();
        new SuggestionTesterBuilder(contact).query("S").expectIcon1Uri(false).expectedText1(
                "Deer Dough").expectedText2("Google").build().test();
    }
    public void testSearchSuggestionsByPhoneNumber() throws Exception {
        ContentValues values = new ContentValues();
        Uri searchUri = new Uri.Builder().scheme("content").authority(ContactsContract.AUTHORITY)
                .appendPath(SearchManager.SUGGEST_URI_PATH_QUERY).appendPath("12345").build();
        Cursor c = mResolver.query(searchUri, null, null, null, null);
        DatabaseUtils.dumpCursor(c);
        assertEquals(2, c.getCount());
        c.moveToFirst();
        values.put(SearchManager.SUGGEST_COLUMN_TEXT_1, "Dial number");
        values.put(SearchManager.SUGGEST_COLUMN_TEXT_2, "using 12345");
        values.put(SearchManager.SUGGEST_COLUMN_ICON_1,
                String.valueOf(com.android.internal.R.drawable.call_contact));
        values.put(SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
                Intents.SEARCH_SUGGESTION_DIAL_NUMBER_CLICKED);
        values.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA, "tel:12345");
        values.putNull(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        assertCursorValues(c, values);
        c.moveToNext();
        values.clear();
        values.put(SearchManager.SUGGEST_COLUMN_TEXT_1, "Create contact");
        values.put(SearchManager.SUGGEST_COLUMN_TEXT_2, "using 12345");
        values.put(SearchManager.SUGGEST_COLUMN_ICON_1,
                String.valueOf(com.android.internal.R.drawable.create_contact));
        values.put(SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
                Intents.SEARCH_SUGGESTION_CREATE_CONTACT_CLICKED);
        values.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA, "tel:12345");
        values.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
                SearchManager.SUGGEST_NEVER_MAKE_SHORTCUT);
        assertCursorValues(c, values);
        c.close();
    }
    private final class SuggestionTester {
        private final GoldenContact contact;
        private final String query;
        private final boolean expectIcon1Uri;
        private final String expectedIcon2;
        private final String expectedText1;
        private final String expectedText2;
        public SuggestionTester(SuggestionTesterBuilder builder) {
            contact = builder.contact;
            query = builder.query;
            expectIcon1Uri = builder.expectIcon1Uri;
            expectedIcon2 = builder.expectedIcon2;
            expectedText1 = builder.expectedText1;
            expectedText2 = builder.expectedText2;
        }
        public void test() {
            testQsbSuggest();
            testContactIdQsbRefresh();
            testLookupKeyQsbRefresh();
            contact.delete();
        }
        private void testQsbSuggest() {
            Uri searchUri = new Uri.Builder().scheme("content").authority(
                    ContactsContract.AUTHORITY).appendPath(SearchManager.SUGGEST_URI_PATH_QUERY)
                    .appendPath(query).build();
            Cursor c = mResolver.query(searchUri, null, null, null, null);
            assertEquals(1, c.getCount());
            c.moveToFirst();
            String icon1 = c.getString(c.getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_1));
            if (expectIcon1Uri) {
                assertTrue(icon1.startsWith("content:"));
            } else {
                assertEquals(String.valueOf(com.android.internal.R.drawable.ic_contact_picture),
                        icon1);
            }
            ContentValues values = getContactValues();
            assertCursorValues(c, values);
            c.close();
        }
        private ContentValues getContactValues() {
            ContentValues values = new ContentValues();
            values.put("_id", contact.getContactId());
            values.put(SearchManager.SUGGEST_COLUMN_TEXT_1, expectedText1);
            values.put(SearchManager.SUGGEST_COLUMN_TEXT_2, expectedText2);
            values.put(SearchManager.SUGGEST_COLUMN_ICON_2, expectedIcon2);
            values.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, contact.getLookupKey());
            values.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, contact.getLookupKey());
            return values;
        }
        private Cursor refreshQuery(String refreshId) {
            Uri refershUri = ContactsContract.AUTHORITY_URI.buildUpon().appendPath(
                    SearchManager.SUGGEST_URI_PATH_SHORTCUT)
                    .appendPath(refreshId).build();
            String[] projection = new String[] {
                    SearchManager.SUGGEST_COLUMN_ICON_1, SearchManager.SUGGEST_COLUMN_ICON_2,
                    SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2,
                    SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
                    SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "_id",
            };
            return mResolver.query(refershUri, projection, null, null, null);
        }
        private void testContactIdQsbRefresh() {
            Cursor c = refreshQuery(String.valueOf(contact.getContactId()));
            try {
                assertEquals("Record count", 0, c.getCount());
            } finally {
                c.close();
            }
        }
        private void testLookupKeyQsbRefresh() {
            Cursor c = refreshQuery(contact.getLookupKey());
            try {
                assertEquals("Record count", 1, c.getCount());
                c.moveToFirst();
                assertCursorValues(c, getContactValues());
            } finally {
                c.close();
            }
        }
    }
    private final class SuggestionTesterBuilder {
        private final GoldenContact contact;
        private String query;
        private boolean expectIcon1Uri;
        private String expectedIcon2;
        private String expectedText1;
        private String expectedText2;
        public SuggestionTesterBuilder(GoldenContact contact) {
            this.contact = contact;
        }
        public SuggestionTester build() {
            return new SuggestionTester(this);
        }
        public SuggestionTesterBuilder query(String value) {
            query = value;
            return this;
        }
        public SuggestionTesterBuilder expectIcon1Uri(boolean value) {
            expectIcon1Uri = value;
            return this;
        }
        public SuggestionTesterBuilder expectedIcon2(String value) {
            expectedIcon2 = value;
            return this;
        }
        public SuggestionTesterBuilder expectedText1(String value) {
            expectedText1 = value;
            return this;
        }
        public SuggestionTesterBuilder expectedText2(String value) {
            expectedText2 = value;
            return this;
        }
    }
}
