public class SyncManagerAccountTests extends AccountTestCase {
    EmailProvider mProvider;
    Context mMockContext;
    public SyncManagerAccountTests() {
        super();
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mMockContext = getMockContext();
        deleteTemporaryAccountManagerAccounts();
    }
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        deleteTemporaryAccountManagerAccounts();
    }
    public void testTestReconcileAccounts() {
        Account firstAccount = null;
        final String TEST_USER_ACCOUNT = "__user_account_test_1";
        Context context = getContext();
        try {
            createAccountManagerAccount(TEST_USER_ACCOUNT + TEST_ACCOUNT_SUFFIX);
            firstAccount = ProviderTestUtils.setupAccount(TEST_USER_ACCOUNT, true, context);
            testReconcileAccounts();
        } finally {
            if (firstAccount != null) {
                boolean firstAccountFound = false;
                context.getContentResolver().delete(firstAccount.getUri(), null, null);
                android.accounts.Account[] accountManagerAccounts = AccountManager.get(context)
                        .getAccountsByType(Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
                for (android.accounts.Account accountManagerAccount: accountManagerAccounts) {
                    if ((TEST_USER_ACCOUNT + TEST_ACCOUNT_SUFFIX)
                            .equals(accountManagerAccount.name)) {
                        deleteAccountManagerAccount(accountManagerAccount);
                        firstAccountFound = true;
                    }
                }
                assertTrue(firstAccountFound);
            }
        }
    }
    public void testReconcileAccounts() {
        Context context = getContext();
        android.accounts.Account[] baselineAccounts =
            AccountManager.get(context).getAccountsByType(Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
        Account firstAccount = setupProviderAndAccountManagerAccount(getTestAccountName("1"));
        setupProviderAndAccountManagerAccount(getTestAccountName("2"));
        setupProviderAndAccountManagerAccount(getTestAccountName("3"));
        assertEquals(3, EmailContent.count(mMockContext, Account.CONTENT_URI, null, null));
        android.accounts.Account[] accountManagerAccounts =
                getAccountManagerAccounts(baselineAccounts);
        assertEquals(3, accountManagerAccounts.length);
        android.accounts.Account removedAccount =
            makeAccountManagerAccount(getTestAccountEmailAddress("2"));
        deleteAccountManagerAccount(removedAccount);
        accountManagerAccounts = getAccountManagerAccounts(baselineAccounts);
        assertEquals(2, accountManagerAccounts.length);
        ContentResolver resolver = mMockContext.getContentResolver();
        SyncManager.reconcileAccountsWithAccountManager(context,
                makeSyncManagerAccountList(), accountManagerAccounts, true, resolver);
        assertEquals(2, EmailContent.count(mMockContext, Account.CONTENT_URI, null, null));
        resolver.delete(ContentUris.withAppendedId(Account.CONTENT_URI, firstAccount.mId),
                null, null);
        assertEquals(1, EmailContent.count(mMockContext, Account.CONTENT_URI, null, null));
        SyncManager.reconcileAccountsWithAccountManager(context,
                makeSyncManagerAccountList(), accountManagerAccounts, true, resolver);
        accountManagerAccounts = getAccountManagerAccounts(baselineAccounts);
        assertEquals(1, accountManagerAccounts.length);
        assertEquals(getTestAccountEmailAddress("3"), accountManagerAccounts[0].name);
    }
    public void testReleaseSyncHolds() {
        Context context = mMockContext;
        SyncManager syncManager = new SyncManager();
        SyncError securityErrorAccount1 =
            syncManager.new SyncError(AbstractSyncService.EXIT_SECURITY_FAILURE, false);
        SyncError ioError =
            syncManager.new SyncError(AbstractSyncService.EXIT_IO_ERROR, false);
        SyncError securityErrorAccount2 =
            syncManager.new SyncError(AbstractSyncService.EXIT_SECURITY_FAILURE, false);
        Account acct1 = ProviderTestUtils.setupAccount("acct1", true, context);
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", acct1.mId, true, context);
        Mailbox box2 = ProviderTestUtils.setupMailbox("box2", acct1.mId, true, context);
        Account acct2 = ProviderTestUtils.setupAccount("acct2", true, context);
        Mailbox box3 = ProviderTestUtils.setupMailbox("box3", acct2.mId, true, context);
        Mailbox box4 = ProviderTestUtils.setupMailbox("box4", acct2.mId, true, context);
        HashMap<Long, SyncError> errorMap = syncManager.mSyncErrorMap;
        errorMap.put(box1.mId, securityErrorAccount1);
        errorMap.put(box2.mId, ioError);
        errorMap.put(box3.mId, securityErrorAccount2);
        errorMap.put(box4.mId, securityErrorAccount2);
        assertEquals(4, errorMap.keySet().size());
        syncManager.releaseSyncHolds(context, AbstractSyncService.EXIT_SECURITY_FAILURE, acct2);
        assertEquals(2, errorMap.keySet().size());
        assertNotNull(errorMap.get(box2.mId));
        assertNotNull(errorMap.get(box1.mId));
        errorMap.put(box3.mId, securityErrorAccount2);
        errorMap.put(box4.mId, securityErrorAccount2);
        assertEquals(4, errorMap.keySet().size());
        syncManager.releaseSyncHolds(context, AbstractSyncService.EXIT_SECURITY_FAILURE, null);
        assertEquals(1, errorMap.keySet().size());
        assertNotNull(errorMap.get(box2.mId));
        syncManager.releaseSyncHolds(context, AbstractSyncService.EXIT_IO_ERROR, acct2);
        assertEquals(1, errorMap.keySet().size());
        syncManager.releaseSyncHolds(context, AbstractSyncService.EXIT_IO_ERROR, acct1);
        assertEquals(0, errorMap.keySet().size());
    }
}
