public class AccountBackupRestoreTests extends ProviderTestCase2<EmailProvider> {
    private Preferences mPreferences;
    private Context mMockContext;
    public AccountBackupRestoreTests() {
        super(EmailProvider.class, EmailProvider.EMAIL_AUTHORITY);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMockContext = getMockContext();
        mPreferences = Preferences.getPreferences(mContext);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        deleteLegacyAccounts();
    }
    private void deleteLegacyAccounts() {
        Account[] oldAccounts = mPreferences.getAccounts();
        for (Account oldAccount : oldAccounts) {
            oldAccount.delete(mPreferences);
        }
    }
    public void testNoAccountBackup() {
        Account backupAccount = new Account(mMockContext);
        backupAccount.save(mPreferences);
        Account[] oldBackups = mPreferences.getAccounts();
        assertTrue(oldBackups.length >= 1);
        int numAccounts = EmailContent.count(mMockContext, EmailContent.Account.CONTENT_URI,
                null, null);
        assertEquals(0, numAccounts);
        AccountBackupRestore.doBackupAccounts(mMockContext, mPreferences);
        Account[] backups = mPreferences.getAccounts();
        assertEquals(0, backups.length);
    }
    public void testBackup() {
        deleteLegacyAccounts();
        EmailContent.Account liveAccount1 =
            ProviderTestUtils.setupAccount("testBackup1", false, mMockContext);
        liveAccount1.mHostAuthRecv =
            ProviderTestUtils.setupHostAuth("legacy-recv", 0, false, mMockContext);
        liveAccount1.mHostAuthSend =
            ProviderTestUtils.setupHostAuth("legacy-send", 0, false, mMockContext);
        liveAccount1.setDefaultAccount(true);
        liveAccount1.save(mMockContext);
        EmailContent.Account liveAccount2 =
            ProviderTestUtils.setupAccount("testBackup2", false, mMockContext);
        liveAccount2.mHostAuthRecv =
            ProviderTestUtils.setupHostAuth("legacy-recv", 0, false, mMockContext);
        liveAccount2.mHostAuthSend =
            ProviderTestUtils.setupHostAuth("legacy-send", 0, false, mMockContext);
        liveAccount2.setDefaultAccount(false);
        liveAccount2.save(mMockContext);
        AccountBackupRestore.doBackupAccounts(mMockContext, mPreferences);
        Account[] backups = mPreferences.getAccounts();
        assertEquals(2, backups.length);
        for (Account backup : backups) {
            if ("testBackup1".equals(backup.getDescription())) {
                assertTrue(0 != (backup.mBackupFlags & Account.BACKUP_FLAGS_IS_DEFAULT));
            } else if ("testBackup2".equals(backup.getDescription())) {
                assertFalse(0 != (backup.mBackupFlags & Account.BACKUP_FLAGS_IS_DEFAULT));
            } else {
                fail("unexpected backup name=" + backup.getDescription());
            }
        }
        Account backup1 = backups[0];
        assertTrue(0 != (backup1.mBackupFlags & Account.BACKUP_FLAGS_IS_BACKUP));
        assertEquals(liveAccount1.getDisplayName(), backup1.getDescription());
    }
    public void testNoAccountRestore1() {
        deleteLegacyAccounts();
        Account backupAccount1 = setupLegacyBackupAccount("backup1");
        backupAccount1.save(mPreferences);
        Account backupAccount2 = setupLegacyBackupAccount("backup2");
        backupAccount2.save(mPreferences);
        EmailContent.Account existing =
            ProviderTestUtils.setupAccount("existing", true, mMockContext);
        boolean anyRestored = AccountBackupRestore.doRestoreAccounts(mMockContext, mPreferences);
        assertFalse(anyRestored);
        int numAccounts = EmailContent.count(mMockContext, EmailContent.Account.CONTENT_URI,
                null, null);
        assertEquals(1, numAccounts);
    }
    public void testNoAccountRestore2() {
        deleteLegacyAccounts();
        int numAccounts = EmailContent.count(mMockContext, EmailContent.Account.CONTENT_URI,
                null, null);
        assertEquals(0, numAccounts);
        boolean anyRestored = AccountBackupRestore.doRestoreAccounts(mMockContext, mPreferences);
        assertFalse(anyRestored);
        numAccounts = EmailContent.count(mMockContext, EmailContent.Account.CONTENT_URI,
                null, null);
        assertEquals(0, numAccounts);
    }
    public void testAccountRestore() {
        deleteLegacyAccounts();
        Account backupAccount1 = setupLegacyBackupAccount("backup1");
        backupAccount1.mBackupFlags |= Account.BACKUP_FLAGS_IS_DEFAULT;
        backupAccount1.save(mPreferences);
        Account backupAccount2 = setupLegacyBackupAccount("backup2");
        backupAccount2.save(mPreferences);
        boolean anyRestored = AccountBackupRestore.doRestoreAccounts(mMockContext, mPreferences);
        assertTrue(anyRestored);
        Cursor c = mMockContext.getContentResolver().query(EmailContent.Account.CONTENT_URI,
                EmailContent.Account.CONTENT_PROJECTION, null, null, null);
        try {
            assertEquals(2, c.getCount());
            while (c.moveToNext()) {
                EmailContent.Account restored =
                    EmailContent.getContent(c, EmailContent.Account.class);
                if ("backup1".equals(restored.getDisplayName())) {
                    assertTrue(restored.mIsDefault);
                } else if ("backup2".equals(restored.getDisplayName())) {
                    assertFalse(restored.mIsDefault);
                } else {
                    fail("Unexpected restore account name=" + restored.getDisplayName());
                }
                checkRestoredTransientValues(restored);
            }
        } finally {
            c.close();
        }
        deleteLegacyAccounts();
        mMockContext.getContentResolver().delete(EmailContent.Account.CONTENT_URI, null, null);
        Account backupAccount3 = setupLegacyBackupAccount("backup3");
        backupAccount3.save(mPreferences);
        Account backupAccount4 = setupLegacyBackupAccount("backup4");
        backupAccount4.mBackupFlags |= Account.BACKUP_FLAGS_IS_DEFAULT;
        backupAccount4.save(mPreferences);
        AccountBackupRestore.doRestoreAccounts(mMockContext, mPreferences);
        c = mMockContext.getContentResolver().query(EmailContent.Account.CONTENT_URI,
                EmailContent.Account.CONTENT_PROJECTION, null, null, null);
        try {
            assertEquals(2, c.getCount());
            while (c.moveToNext()) {
                EmailContent.Account restored =
                    EmailContent.getContent(c, EmailContent.Account.class);
                if ("backup3".equals(restored.getDisplayName())) {
                    assertFalse(restored.mIsDefault);
                } else if ("backup4".equals(restored.getDisplayName())) {
                    assertTrue(restored.mIsDefault);
                } else {
                    fail("Unexpected restore account name=" + restored.getDisplayName());
                }
                checkRestoredTransientValues(restored);
            }
        } finally {
            c.close();
        }
    }
    private void checkRestoredTransientValues(EmailContent.Account restored) {
        assertNull(restored.mSyncKey);
        assertTrue(restored.mHostAuthKeyRecv > 0);
        assertTrue(restored.mHostAuthKeySend > 0);
        assertTrue(restored.mProtocolVersion == null || restored.mProtocolVersion.length() > 0);
    }
    private Account setupLegacyBackupAccount(String name) {
        Account backup = new Account(mMockContext);
        backup.mUuid = "test-uid-" + name;
        backup.mStoreUri = "store:
        backup.mLocalStoreUri = "local:
        backup.mSenderUri = "sender:
        backup.mDescription = name;
        backup.mName = "name " + name;
        backup.mEmail = "email " + name;
        backup.mAutomaticCheckIntervalMinutes = 100;
        backup.mLastAutomaticCheckTime = 200;
        backup.mNotifyNewMail = true;
        backup.mDraftsFolderName = "drafts " + name;
        backup.mSentFolderName = "sent " + name;
        backup.mTrashFolderName = "trash " + name;
        backup.mOutboxFolderName = "outbox " + name;
        backup.mAccountNumber = 300;
        backup.mVibrate = true;
        backup.mVibrateWhenSilent = false;
        backup.mRingtoneUri = "ringtone:
        backup.mSyncWindow = 400;
        backup.mBackupFlags = Account.BACKUP_FLAGS_IS_BACKUP;
        backup.mProtocolVersion = "proto version" + name;
        backup.mDeletePolicy = Account.DELETE_POLICY_NEVER;
        backup.mSecurityFlags = 500;
        return backup;
    }
}
