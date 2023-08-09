public class PreferencesUnitTests extends AndroidTestCase {
    private Preferences mPreferences;
    private Account mAccount;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPreferences = Preferences.getPreferences(getContext());
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (mAccount != null && mPreferences != null) {
            mAccount.delete(mPreferences);
        }
    }
    public void testGetAccountByContentUri() {
        createTestAccount();
        Uri testAccountUri = mAccount.getContentUri();
        Account lookup = mPreferences.getAccountByContentUri(testAccountUri);
        assertEquals(mAccount, lookup);
        testAccountUri = Uri.parse("bogus:
        lookup = mPreferences.getAccountByContentUri(testAccountUri);
        assertNull(lookup);
        testAccountUri = Uri.parse("content:
        lookup = mPreferences.getAccountByContentUri(testAccountUri);
        assertNull(lookup);
        testAccountUri = Uri.parse("content:
        lookup = mPreferences.getAccountByContentUri(testAccountUri);
        assertNull(lookup);
    }
    private void createTestAccount() {
        mAccount = new Account(getContext());
        mAccount.save(mPreferences);
    }
}
