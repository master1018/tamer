public class ContentResolverSyncTestCase extends AndroidTestCase {
    private static final String AUTHORITY = "android.content.cts.authority";
    private static final Account ACCOUNT = new Account(MockAccountAuthenticator.ACCOUNT_NAME,
            MockAccountAuthenticator.ACCOUNT_TYPE);
    private static final int LATCH_TIMEOUT_MS = 5000;
    private static AccountManager sAccountManager;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        getMockSyncAdapter();
        sAccountManager = AccountManager.get(getContext());
    }
    @Override
    public void tearDown() throws Exception {
        getMockSyncAdapter().clearData();
        removeAccount(sAccountManager, ACCOUNT, null );
        super.tearDown();
    }
    public static synchronized MockSyncAdapter getMockSyncAdapter() {
        return MockSyncAdapter.getMockSyncAdapter();
    }
    public static synchronized MockAccountAuthenticator getMockAuthenticator(Context context) {
        return MockAccountAuthenticator.getMockAuthenticator(context);
    }
    private void addAccountExplicitly(Account account, String password, Bundle userdata) {
        assertTrue(sAccountManager.addAccountExplicitly(account, password, userdata));
    }
    private boolean removeAccount(AccountManager am, Account account,
            AccountManagerCallback<Boolean> callback) throws IOException, AuthenticatorException,
                OperationCanceledException {
        AccountManagerFuture<Boolean> futureBoolean = am.removeAccount(account,
                callback,
                null );
        Boolean resultBoolean = futureBoolean.getResult();
        assertTrue(futureBoolean.isDone());
        return resultBoolean;
    }
    private CountDownLatch setNewLatch(CountDownLatch latch) {
        getMockSyncAdapter().setLatch(latch);
        getMockSyncAdapter().clearData();
        return latch;
    }
    private void addAccountAndVerifyInitSync(Account account, String password,
            String authority, int latchTimeoutMs) {
        CountDownLatch latch = setNewLatch(new CountDownLatch(1));
        addAccountExplicitly(account, password, null );
        try {
            latch.await(latchTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("should not throw an InterruptedException");
        }
        assertFalse(getMockSyncAdapter().isStartSync());
        assertFalse(getMockSyncAdapter().isCancelSync());
        assertTrue(getMockSyncAdapter().isInitialized());
        assertEquals(account, getMockSyncAdapter().getAccount());
        assertEquals(authority, getMockSyncAdapter().getAuthority());
    }
    private void cancelSync(Account account, String authority, int latchTimeoutMillis) {
        CountDownLatch latch = setNewLatch(new CountDownLatch(1));
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.cancelSync(account, authority);
        try {
            latch.await(latchTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("should not throw an InterruptedException");
        }
    }
    private void requestSync(Account account, String authority, int latchTimeoutMillis) {
        CountDownLatch latch = setNewLatch(new CountDownLatch(1));
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(account, authority, extras);
        try {
            latch.await(latchTimeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("should not throw an InterruptedException");
        }
    }
    private void setIsSyncable(Account account, String authority, boolean b) {
        ContentResolver.setIsSyncable(account, authority, (b) ? 1 : 0);
    }
    public void testRequestSync() throws IOException, AuthenticatorException,
            OperationCanceledException {
        ContentResolver.setMasterSyncAutomatically(false);
        assertEquals(false, ContentResolver.getMasterSyncAutomatically());
        addAccountAndVerifyInitSync(ACCOUNT, MockAccountAuthenticator.ACCOUNT_PASSWORD, AUTHORITY,
                LATCH_TIMEOUT_MS);
        getMockSyncAdapter().clearData();
        setIsSyncable(ACCOUNT, AUTHORITY, true);
        cancelSync(ACCOUNT, AUTHORITY, LATCH_TIMEOUT_MS);
        getMockSyncAdapter().clearData();
        requestSync(ACCOUNT, AUTHORITY, LATCH_TIMEOUT_MS);
        assertTrue(getMockSyncAdapter().isStartSync());
        assertFalse(getMockSyncAdapter().isCancelSync());
        assertFalse(getMockSyncAdapter().isInitialized());
        assertEquals(ACCOUNT, getMockSyncAdapter().getAccount());
        assertEquals(AUTHORITY, getMockSyncAdapter().getAuthority());
    }
    public void testCancelSync() throws IOException, AuthenticatorException,
            OperationCanceledException {
        ContentResolver.setMasterSyncAutomatically(false);
        assertEquals(false, ContentResolver.getMasterSyncAutomatically());
        addAccountAndVerifyInitSync(ACCOUNT, MockAccountAuthenticator.ACCOUNT_PASSWORD, AUTHORITY,
                LATCH_TIMEOUT_MS);
        getMockSyncAdapter().clearData();
        setIsSyncable(ACCOUNT, AUTHORITY, true);
        requestSync(ACCOUNT, AUTHORITY, LATCH_TIMEOUT_MS);
        getMockSyncAdapter().clearData();
        cancelSync(ACCOUNT, AUTHORITY, LATCH_TIMEOUT_MS);
        assertFalse(getMockSyncAdapter().isStartSync());
        assertTrue(getMockSyncAdapter().isCancelSync());
        assertFalse(getMockSyncAdapter().isInitialized());
        assertFalse(ContentResolver.isSyncActive(ACCOUNT, AUTHORITY));
        assertFalse(ContentResolver.isSyncPending(ACCOUNT, AUTHORITY));
    }
    public void testGetAndSetMasterSyncAutomatically() {
        ContentResolver.setMasterSyncAutomatically(true);
        assertEquals(true, ContentResolver.getMasterSyncAutomatically());
        ContentResolver.setMasterSyncAutomatically(false);
        assertEquals(false, ContentResolver.getMasterSyncAutomatically());
    }
    public void testGetAndSetSyncAutomatically() {
        ContentResolver.setMasterSyncAutomatically(false);
        assertEquals(false, ContentResolver.getMasterSyncAutomatically());
        ContentResolver.setSyncAutomatically(ACCOUNT, AUTHORITY, false);
        assertEquals(false, ContentResolver.getSyncAutomatically(ACCOUNT, AUTHORITY));
        ContentResolver.setSyncAutomatically(ACCOUNT, AUTHORITY, true);
        assertEquals(true, ContentResolver.getSyncAutomatically(ACCOUNT, AUTHORITY));
    }
    public void testGetAndSetIsSyncable() {
        ContentResolver.setMasterSyncAutomatically(false);
        assertEquals(false, ContentResolver.getMasterSyncAutomatically());
        addAccountExplicitly(ACCOUNT, MockAccountAuthenticator.ACCOUNT_PASSWORD, null );
        ContentResolver.setIsSyncable(ACCOUNT, AUTHORITY, 2);
        assertTrue(ContentResolver.getIsSyncable(ACCOUNT, AUTHORITY) > 0);
        ContentResolver.setIsSyncable(ACCOUNT, AUTHORITY, 1);
        assertTrue(ContentResolver.getIsSyncable(ACCOUNT, AUTHORITY) > 0);
        ContentResolver.setIsSyncable(ACCOUNT, AUTHORITY, 0);
        assertEquals(0, ContentResolver.getIsSyncable(ACCOUNT, AUTHORITY));
        ContentResolver.setIsSyncable(ACCOUNT, AUTHORITY, -1);
        assertTrue(ContentResolver.getIsSyncable(ACCOUNT, AUTHORITY) < 0);
        ContentResolver.setIsSyncable(ACCOUNT, AUTHORITY, -2);
        assertTrue(ContentResolver.getIsSyncable(ACCOUNT, AUTHORITY) < 0);
    }
    public void testGetSyncAdapterTypes() {
        SyncAdapterType[] types = ContentResolver.getSyncAdapterTypes();
        assertNotNull(types);
        int length = types.length;
        assertTrue(length > 0);
        boolean found = false;
        for (int n=0; n < length; n++) {
            SyncAdapterType type = types[n];
            if (MockAccountAuthenticator.ACCOUNT_TYPE.equals(type.accountType) &&
                    AUTHORITY.equals(type.authority)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
    public void testStartSyncFailure() {
        try {
            ContentResolver.requestSync(null, null, null);
            fail("did not throw IllegalArgumentException when extras is null.");
        } catch (IllegalArgumentException e) {
        }
    }
    public void testValidateSyncExtrasBundle() {
        Bundle extras = new Bundle();
        extras.putInt("Integer", 20);
        extras.putLong("Long", 10l);
        extras.putBoolean("Boolean", true);
        extras.putFloat("Float", 5.5f);
        extras.putDouble("Double", 2.5);
        extras.putString("String", MockAccountAuthenticator.ACCOUNT_NAME);
        extras.putCharSequence("CharSequence", null);
        ContentResolver.validateSyncExtrasBundle(extras);
        extras.putChar("Char", 'a'); 
        try {
            ContentResolver.validateSyncExtrasBundle(extras);
            fail("did not throw IllegalArgumentException when extras is invalide.");
        } catch (IllegalArgumentException e) {
        }
    }
}
