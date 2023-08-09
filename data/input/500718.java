public class AccountUnitTests extends AndroidTestCase {
    private Preferences mPreferences;
    private String mUuid;
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
    public void testTransportToSenderUpdate() {
        final String TEST_VALUE = "This Is The Sender Uri";
        createTestAccount();
        SharedPreferences.Editor editor = mPreferences.mSharedPreferences.edit();
        editor.remove(mUuid + ".senderUri");
        editor.putString(mUuid + ".transportUri", Utility.base64Encode(TEST_VALUE));
        editor.commit();
        mAccount.refresh(mPreferences);
        assertEquals(TEST_VALUE, mAccount.getSenderUri());
        mAccount.save(mPreferences);
        String newString = mPreferences.mSharedPreferences.getString(mUuid + ".senderUri", null);
        assertEquals(TEST_VALUE, Utility.base64Decode(newString));
        String oldString = mPreferences.mSharedPreferences.getString(mUuid + ".transportUri", null);
        assertNull(oldString);
    }
    public void testImapDeletePolicyUpdate() {
        final String STORE_URI_IMAP = "imap:
        final String STORE_URI_POP3 = "pop3:
        createTestAccount();
        SharedPreferences.Editor editor = mPreferences.mSharedPreferences.edit();
        editor.putString(mUuid + ".storeUri", Utility.base64Encode(STORE_URI_POP3));
        editor.putInt(mUuid + ".deletePolicy", Account.DELETE_POLICY_NEVER);
        editor.commit();
        mAccount.refresh(mPreferences);
        assertEquals(Account.DELETE_POLICY_NEVER, mAccount.getDeletePolicy());
        mAccount.save(mPreferences);
        int storedPolicy = mPreferences.mSharedPreferences.getInt(mUuid + ".deletePolicy", -1);
        assertEquals(Account.DELETE_POLICY_NEVER, storedPolicy);
        mAccount.delete(mPreferences);
        createTestAccount();
        editor = mPreferences.mSharedPreferences.edit();
        editor.putString(mUuid + ".storeUri", Utility.base64Encode(STORE_URI_IMAP));
        editor.putInt(mUuid + ".deletePolicy", Account.DELETE_POLICY_NEVER);
        editor.commit();
        mAccount.refresh(mPreferences);
        assertEquals(Account.DELETE_POLICY_ON_DELETE, mAccount.getDeletePolicy());
        mAccount.save(mPreferences);
        storedPolicy = mPreferences.mSharedPreferences.getInt(mUuid + ".deletePolicy", -1);
        assertEquals(Account.DELETE_POLICY_ON_DELETE, storedPolicy);
    }
    public void testFlagsField() {
        createTestAccount();
        assertEquals(0, mAccount.mBackupFlags);
        mAccount.save(mPreferences);
        mAccount.mBackupFlags = -1;
        mAccount.refresh(mPreferences);
        assertEquals(0, mAccount.mBackupFlags);
        mAccount.mBackupFlags = Account.BACKUP_FLAGS_IS_BACKUP;
        mAccount.save(mPreferences);
        mAccount.mBackupFlags = -1;
        mAccount.refresh(mPreferences);
        assertEquals(Account.BACKUP_FLAGS_IS_BACKUP, mAccount.mBackupFlags);
        mAccount.mBackupFlags = Account.BACKUP_FLAGS_SYNC_CONTACTS;
        mAccount.save(mPreferences);
        mAccount.mBackupFlags = -1;
        mAccount.refresh(mPreferences);
        assertEquals(Account.BACKUP_FLAGS_SYNC_CONTACTS, mAccount.mBackupFlags);
        mAccount.mBackupFlags = Account.BACKUP_FLAGS_IS_DEFAULT;
        mAccount.save(mPreferences);
        mAccount.mBackupFlags = -1;
        mAccount.refresh(mPreferences);
        assertEquals(Account.BACKUP_FLAGS_IS_DEFAULT, mAccount.mBackupFlags);
    }
    private void createTestAccount() {
        mAccount = new Account(getContext());
        mAccount.save(mPreferences);
        mUuid = mAccount.getUuid();
    }
}
