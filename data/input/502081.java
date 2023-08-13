public class AccountManagerTest extends AndroidTestCase {
    public static final String ACCOUNT_NAME = "android.accounts.cts.account.name";
    public static final String ACCOUNT_NAME_OTHER = "android.accounts.cts.account.name.other";
    public static final String ACCOUNT_TYPE = "android.accounts.cts.account.type";
    public static final String ACCOUNT_TYPE_OTHER = "android.accounts.cts.account.type.other";
    public static final String ACCOUNT_PASSWORD = "android.accounts.cts.account.password";
    public static final String AUTH_TOKEN = "mockAuthToken";
    public static final String AUTH_TOKEN_TYPE = "mockAuthTokenType";
    public static final String AUTH_TOKEN_LABEL = "mockAuthTokenLabel";
    public static final String FEATURE_1 = "feature.1";
    public static final String FEATURE_2 = "feature.2";
    public static final String NON_EXISTING_FEATURE = "feature.3";
    public static final String OPTION_NAME_1 = "option.name.1";
    public static final String OPTION_VALUE_1 = "option.value.1";
    public static final String OPTION_NAME_2 = "option.name.2";
    public static final String OPTION_VALUE_2 = "option.value.2";
    public static final String[] REQUIRED_FEATURES = new String[] { FEATURE_1, FEATURE_2 };
    public static final Activity ACTIVITY = new Activity();
    public static final Bundle OPTIONS_BUNDLE = new Bundle();
    public static final Bundle USERDATA_BUNDLE = new Bundle();
    public static final String USERDATA_NAME_1 = "user.data.name.1";
    public static final String USERDATA_NAME_2 = "user.data.name.2";
    public static final String USERDATA_VALUE_1 = "user.data.value.1";
    public static final String USERDATA_VALUE_2 = "user.data.value.2";
    public static final Account ACCOUNT = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
    public static final Account ACCOUNT_SAME_TYPE = new Account(ACCOUNT_NAME_OTHER, ACCOUNT_TYPE);
    private static MockAccountAuthenticator mockAuthenticator;
    private static final int LATCH_TIMEOUT_MS = 500;
    private static AccountManager am;
    public synchronized static MockAccountAuthenticator getMockAuthenticator(Context context) {
        if (null == mockAuthenticator) {
            mockAuthenticator = new MockAccountAuthenticator(context);
        }
        return mockAuthenticator;
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        OPTIONS_BUNDLE.putString(OPTION_NAME_1, OPTION_VALUE_1);
        OPTIONS_BUNDLE.putString(OPTION_NAME_2, OPTION_VALUE_2);
        USERDATA_BUNDLE.putString(USERDATA_NAME_1, USERDATA_VALUE_1);
        getMockAuthenticator(getContext());
        am = AccountManager.get(getContext());
    }
    @Override
    public void tearDown() throws Exception, AuthenticatorException, OperationCanceledException {
        mockAuthenticator.clearData();
        assertTrue(removeAccount(am, ACCOUNT, null ));
        assertTrue(removeAccount(am, ACCOUNT_SAME_TYPE, null ));
        mockAuthenticator.clearData();
        super.tearDown();
    }
    private void validateAccountAndAuthTokenResult(Bundle result) {
        assertEquals(ACCOUNT_NAME, result.get(AccountManager.KEY_ACCOUNT_NAME));
        assertEquals(ACCOUNT_TYPE, result.get(AccountManager.KEY_ACCOUNT_TYPE));
        assertEquals(AUTH_TOKEN, result.get(AccountManager.KEY_AUTHTOKEN));
    }
    private void validateAccountAndNoAuthTokenResult(Bundle result) {
        assertEquals(ACCOUNT_NAME, result.get(AccountManager.KEY_ACCOUNT_NAME));
        assertEquals(ACCOUNT_TYPE, result.get(AccountManager.KEY_ACCOUNT_TYPE));
        assertNull(result.get(AccountManager.KEY_AUTHTOKEN));
    }
    private void validateNullResult(Bundle resultBundle) {
        assertNull(resultBundle.get(AccountManager.KEY_ACCOUNT_NAME));
        assertNull(resultBundle.get(AccountManager.KEY_ACCOUNT_TYPE));
        assertNull(resultBundle.get(AccountManager.KEY_AUTHTOKEN));
    }
    private void validateAccountAndAuthTokenType() {
        assertEquals(ACCOUNT_TYPE, mockAuthenticator.getAccountType());
        assertEquals(AUTH_TOKEN_TYPE, mockAuthenticator.getAuthTokenType());
    }
    private void validateFeatures() {
        assertEquals(REQUIRED_FEATURES[0], mockAuthenticator.getRequiredFeatures()[0]);
        assertEquals(REQUIRED_FEATURES[1], mockAuthenticator.getRequiredFeatures()[1]);
    }
    private void validateOptions(Bundle expectedOptions, Bundle actualOptions) {
        if (expectedOptions == null) {
            assertNull(actualOptions);
        } else {
            assertNotNull(actualOptions);
            assertEquals(expectedOptions.get(OPTION_NAME_1), actualOptions.get(OPTION_NAME_1));
            assertEquals(expectedOptions.get(OPTION_NAME_2), actualOptions.get(OPTION_NAME_2));
        }
    }
    private void validateCredentials() {
        assertEquals(ACCOUNT, mockAuthenticator.getAccount());
    }
    private int getAccountsCount() {
        Account[] accounts = am.getAccounts();
        assertNotNull(accounts);
        return accounts.length;
    }
    private Bundle addAccount(AccountManager am, String accountType, String authTokenType,
            String[] requiredFeatures, Bundle options, Activity activity,
            AccountManagerCallback<Bundle> callback, Handler handler) throws
                IOException, AuthenticatorException, OperationCanceledException {
        AccountManagerFuture<Bundle> futureBundle = am.addAccount(
                accountType,
                authTokenType,
                requiredFeatures,
                options,
                activity,
                callback,
                handler);
        Bundle resultBundle = futureBundle.getResult();
        assertTrue(futureBundle.isDone());
        assertNotNull(resultBundle);
        return resultBundle;
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
    private void addAccountExplicitly(Account account, String password, Bundle userdata) {
        assertTrue(am.addAccountExplicitly(account, password, userdata));
    }
    private Bundle getAuthTokenByFeature(String[] features, Activity activity)
            throws IOException, AuthenticatorException, OperationCanceledException {
        AccountManagerFuture<Bundle> futureBundle = am.getAuthTokenByFeatures(ACCOUNT_TYPE,
                AUTH_TOKEN_TYPE,
                features,
                activity,
                OPTIONS_BUNDLE,
                OPTIONS_BUNDLE,
                null ,
                null 
        );
        Bundle resultBundle = futureBundle.getResult();
        assertTrue(futureBundle.isDone());
        assertNotNull(resultBundle);
        return resultBundle;
    }
    private boolean isAccountPresent(Account[] accounts, Account accountToCheck) {
        if (null == accounts || null == accountToCheck) {
            return false;
        }
        boolean result = false;
        int length = accounts.length;
        for (int n=0; n<length; n++) {
            if(accountToCheck.equals(accounts[n])) {
                result = true;
                break;
            }
        }
        return result;
    }
    public void testGet() {
        assertNotNull(AccountManager.get(getContext()));
    }
    public void testAddAccount() throws IOException, AuthenticatorException,
            OperationCanceledException {
        Bundle resultBundle = addAccount(am,
                ACCOUNT_TYPE,
                AUTH_TOKEN_TYPE,
                REQUIRED_FEATURES,
                OPTIONS_BUNDLE,
                ACTIVITY,
                null ,
                null );
        validateAccountAndAuthTokenType();
        validateFeatures();
        validateOptions(OPTIONS_BUNDLE, mockAuthenticator.mOptionsAddAccount);
        validateOptions(null, mockAuthenticator.mOptionsUpdateCredentials);
        validateOptions(null, mockAuthenticator.mOptionsConfirmCredentials);
        validateOptions(null, mockAuthenticator.mOptionsGetAuthToken);
        validateAccountAndNoAuthTokenResult(resultBundle);
    }
    public void testAddAccountWithCallbackAndHandler() throws IOException,
            AuthenticatorException, OperationCanceledException {
        testAddAccountWithCallbackAndHandler(null );
        testAddAccountWithCallbackAndHandler(new Handler());
    }
    private void testAddAccountWithCallbackAndHandler(Handler handler) throws IOException,
            AuthenticatorException, OperationCanceledException {
        final CountDownLatch latch = new CountDownLatch(1);
        AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> bundleFuture) {
                Bundle resultBundle = null;
                try {
                    resultBundle = bundleFuture.getResult();
                } catch (OperationCanceledException e) {
                    fail("should not throw an OperationCanceledException");
                } catch (IOException e) {
                    fail("should not throw an IOException");
                } catch (AuthenticatorException e) {
                    fail("should not throw an AuthenticatorException");
                }
                validateAccountAndAuthTokenType();
                validateFeatures();
                validateOptions(OPTIONS_BUNDLE, mockAuthenticator.mOptionsAddAccount);
                validateOptions(null, mockAuthenticator.mOptionsUpdateCredentials);
                validateOptions(null, mockAuthenticator.mOptionsConfirmCredentials);
                validateOptions(null, mockAuthenticator.mOptionsGetAuthToken);
                validateAccountAndNoAuthTokenResult(resultBundle);
                latch.countDown();
            }
        };
        addAccount(am,
                ACCOUNT_TYPE,
                AUTH_TOKEN_TYPE,
                REQUIRED_FEATURES,
                OPTIONS_BUNDLE,
                ACTIVITY,
                callback,
                handler);
        try {
            latch.await(LATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("should not throw an InterruptedException");
        }
    }
    public void testAddAccountExplicitlyAndRemoveAccount() throws IOException,
            AuthenticatorException, OperationCanceledException {
        final int accountsCount = getAccountsCount();
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        Account[] accounts = am.getAccounts();
        assertNotNull(accounts);
        assertEquals(1 + accountsCount, accounts.length);
        assertTrue(isAccountPresent(am.getAccounts(), ACCOUNT));
        assertTrue(removeAccount(am, ACCOUNT, null ));
        accounts = am.getAccounts();
        assertNotNull(accounts);
        assertEquals(accountsCount, accounts.length);
    }
    public void testGetAccountsAndGetAccountsByType() throws IOException, AuthenticatorException,
            OperationCanceledException {
        assertEquals(false, isAccountPresent(am.getAccounts(), ACCOUNT));
        assertEquals(false, isAccountPresent(am.getAccounts(), ACCOUNT_SAME_TYPE));
        final int accountsCount = getAccountsCount();
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        Account[] accounts = am.getAccounts();
        assertEquals(1 + accountsCount, accounts.length);
        assertEquals(true, isAccountPresent(accounts, ACCOUNT));
        addAccountExplicitly(ACCOUNT_SAME_TYPE, ACCOUNT_PASSWORD, null );
        accounts = am.getAccounts();
        assertEquals(2 + accountsCount, accounts.length);
        assertEquals(true, isAccountPresent(accounts, ACCOUNT_SAME_TYPE));
        accounts = am.getAccountsByType(ACCOUNT_TYPE);
        assertEquals(2, accounts.length);
        accounts = am.getAccountsByType(ACCOUNT_TYPE_OTHER);
        assertEquals(0, accounts.length);
    }
    public void testGetAuthenticatorTypes() {
        AuthenticatorDescription[] types = am.getAuthenticatorTypes();
        for(AuthenticatorDescription description: types) {
            if (description.type.equals(ACCOUNT_TYPE)) {
                return;
            }
        }
        fail("should have found Authenticator type: " + ACCOUNT_TYPE);
    }
    public void testSetAndGetAndClearPassword() {
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        assertEquals(ACCOUNT_PASSWORD, am.getPassword(ACCOUNT));
        am.clearPassword(ACCOUNT);
        assertNull(am.getPassword(ACCOUNT));
        am.setPassword(ACCOUNT, ACCOUNT_PASSWORD);
        assertEquals(ACCOUNT_PASSWORD, am.getPassword(ACCOUNT));
    }
    public void testSetAndGetUserData() {
        boolean result = am.addAccountExplicitly(ACCOUNT,
                                ACCOUNT_PASSWORD,
                                USERDATA_BUNDLE);
        assertTrue(result);
        assertEquals(USERDATA_VALUE_1, am.getUserData(ACCOUNT, USERDATA_NAME_1));
        am.setUserData(ACCOUNT, USERDATA_NAME_2, USERDATA_VALUE_2);
        assertEquals(USERDATA_VALUE_2, am.getUserData(ACCOUNT, USERDATA_NAME_2));
    }
    public void testGetAccountsByTypeAndFeatures() throws IOException,
            AuthenticatorException, OperationCanceledException {
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        AccountManagerFuture<Account[]> futureAccounts = am.getAccountsByTypeAndFeatures(
                ACCOUNT_TYPE, REQUIRED_FEATURES, null, null);
        Account[] accounts = futureAccounts.getResult();
        assertNotNull(accounts);
        assertEquals(1, accounts.length);
        assertEquals(true, isAccountPresent(accounts, ACCOUNT));
        futureAccounts = am.getAccountsByTypeAndFeatures(ACCOUNT_TYPE,
                new String[] { NON_EXISTING_FEATURE },
                null ,
                null );
        accounts = futureAccounts.getResult();
        assertNotNull(accounts);
        assertEquals(0, accounts.length);
    }
    public void testGetAccountsByTypeAndFeaturesWithCallbackAndHandler() throws IOException,
            AuthenticatorException, OperationCanceledException {
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        testGetAccountsByTypeAndFeaturesWithCallbackAndHandler(null );
        testGetAccountsByTypeAndFeaturesWithCallbackAndHandler(new Handler());
    }
    private void testGetAccountsByTypeAndFeaturesWithCallbackAndHandler(Handler handler) throws
            IOException, AuthenticatorException, OperationCanceledException {
        final CountDownLatch latch1 = new CountDownLatch(1);
        AccountManagerCallback<Account[]> callback1 = new AccountManagerCallback<Account[]>() {
            public void run(AccountManagerFuture<Account[]> accountsFuture) {
                try {
                    Account[] accounts = accountsFuture.getResult();
                    assertNotNull(accounts);
                    assertEquals(1, accounts.length);
                    assertEquals(true, isAccountPresent(accounts, ACCOUNT));
                } catch (OperationCanceledException e) {
                    fail("should not throw an OperationCanceledException");
                } catch (IOException e) {
                    fail("should not throw an IOException");
                } catch (AuthenticatorException e) {
                    fail("should not throw an AuthenticatorException");
                } finally {
                  latch1.countDown();
                }
            }
        };
        AccountManagerFuture<Account[]> futureAccounts = am.getAccountsByTypeAndFeatures(
                ACCOUNT_TYPE,
                REQUIRED_FEATURES,
                callback1,
                handler);
        Account[] accounts = futureAccounts.getResult();
        assertNotNull(accounts);
        assertEquals(1, accounts.length);
        assertEquals(true, isAccountPresent(accounts, ACCOUNT));
        try {
            latch1.await(LATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("should not throw an InterruptedException");
        }
        final CountDownLatch latch2 = new CountDownLatch(1);
        AccountManagerCallback<Account[]> callback2 = new AccountManagerCallback<Account[]>() {
            public void run(AccountManagerFuture<Account[]> accountsFuture) {
                try {
                    Account[] accounts = accountsFuture.getResult();
                    assertNotNull(accounts);
                    assertEquals(0, accounts.length);
                } catch (OperationCanceledException e) {
                    fail("should not throw an OperationCanceledException");
                } catch (IOException e) {
                    fail("should not throw an IOException");
                } catch (AuthenticatorException e) {
                    fail("should not throw an AuthenticatorException");
                } finally {
                  latch2.countDown();
                }
            }
        };
        accounts = null;
        futureAccounts = am.getAccountsByTypeAndFeatures(ACCOUNT_TYPE,
                new String[] { NON_EXISTING_FEATURE },
                callback2,
                handler);
        accounts = futureAccounts.getResult();
        assertNotNull(accounts);
        assertEquals(0, accounts.length);
        try {
            latch2.await(LATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("should not throw an InterruptedException");
        }
    }
    public void testSetAndPeekAndInvalidateAuthToken() {
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        am.setAuthToken(ACCOUNT, AUTH_TOKEN_TYPE, AUTH_TOKEN);
        String token = am.peekAuthToken(ACCOUNT, AUTH_TOKEN_TYPE);
        assertNotNull(token);
        assertEquals(AUTH_TOKEN, token);
        am.invalidateAuthToken(ACCOUNT_TYPE, AUTH_TOKEN);
        token = am.peekAuthToken(ACCOUNT, AUTH_TOKEN_TYPE);
        assertNull(token);
    }
    public void testBlockingGetAuthToken() throws IOException, AuthenticatorException,
            OperationCanceledException {
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null);
        String token = am.blockingGetAuthToken(ACCOUNT,
                AUTH_TOKEN_TYPE,
                false );
        assertNotNull(token);
        assertEquals(AUTH_TOKEN, token);
    }
    public void testGetAuthToken() throws IOException, AuthenticatorException,
            OperationCanceledException {
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        AccountManagerFuture<Bundle> futureBundle = am.getAuthToken(ACCOUNT,
                AUTH_TOKEN_TYPE,
                false ,
                null ,
                null 
        );
        Bundle resultBundle = futureBundle.getResult();
        assertTrue(futureBundle.isDone());
        assertNotNull(resultBundle);
        validateAccountAndAuthTokenResult(resultBundle);
    }
    public void testGetAuthTokenWithCallbackAndHandler() throws IOException, AuthenticatorException,
            OperationCanceledException {
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        testGetAuthTokenWithCallbackAndHandler(null );
        testGetAuthTokenWithCallbackAndHandler(new Handler());
    }
    private void testGetAuthTokenWithCallbackAndHandler(Handler handler) throws IOException,
            AuthenticatorException, OperationCanceledException {
        final CountDownLatch latch = new CountDownLatch(1);
        AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> bundleFuture) {
                Bundle resultBundle = null;
                try {
                    resultBundle = bundleFuture.getResult();
                    validateAccountAndAuthTokenResult(resultBundle);
                } catch (OperationCanceledException e) {
                    fail("should not throw an OperationCanceledException");
                } catch (IOException e) {
                    fail("should not throw an IOException");
                } catch (AuthenticatorException e) {
                    fail("should not throw an AuthenticatorException");
                }
                finally {
                    latch.countDown();
                }
            }
        };
        AccountManagerFuture<Bundle> futureBundle = am.getAuthToken(ACCOUNT,
                AUTH_TOKEN_TYPE,
                false ,
                callback,
                handler
        );
        Bundle resultBundle = futureBundle.getResult();
        assertTrue(futureBundle.isDone());
        assertNotNull(resultBundle);
        try {
            latch.await(LATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("should not throw an InterruptedException");
        }
    }
    public void testGetAuthTokenWithOptions() throws IOException, AuthenticatorException,
            OperationCanceledException {
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        AccountManagerFuture<Bundle> futureBundle = am.getAuthToken(ACCOUNT,
                AUTH_TOKEN_TYPE,
                OPTIONS_BUNDLE,
                ACTIVITY,
                null ,
                null 
        );
        Bundle resultBundle = futureBundle.getResult();
        assertTrue(futureBundle.isDone());
        assertNotNull(resultBundle);
        validateAccountAndAuthTokenResult(resultBundle);
        validateOptions(null, mockAuthenticator.mOptionsAddAccount);
        validateOptions(null, mockAuthenticator.mOptionsUpdateCredentials);
        validateOptions(null, mockAuthenticator.mOptionsConfirmCredentials);
        validateOptions(OPTIONS_BUNDLE, mockAuthenticator.mOptionsGetAuthToken);
    }
    public void testGetAuthTokenWithOptionsAndCallback() throws IOException,
            AuthenticatorException, OperationCanceledException {
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        testGetAuthTokenWithOptionsAndCallbackAndHandler(null );
        testGetAuthTokenWithOptionsAndCallbackAndHandler(new Handler());
    }
    private void testGetAuthTokenWithOptionsAndCallbackAndHandler(Handler handler) throws
            IOException, AuthenticatorException, OperationCanceledException {
        final CountDownLatch latch = new CountDownLatch(1);
        AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> bundleFuture) {
                Bundle resultBundle = null;
                try {
                    resultBundle = bundleFuture.getResult();
                    validateAccountAndAuthTokenResult(resultBundle);
                } catch (OperationCanceledException e) {
                    fail("should not throw an OperationCanceledException");
                } catch (IOException e) {
                    fail("should not throw an IOException");
                } catch (AuthenticatorException e) {
                    fail("should not throw an AuthenticatorException");
                }
                finally {
                    latch.countDown();
                }
            }
        };
        AccountManagerFuture<Bundle> futureBundle = am.getAuthToken(ACCOUNT,
                AUTH_TOKEN_TYPE,
                OPTIONS_BUNDLE,
                ACTIVITY,
                callback,
                handler
        );
        Bundle resultBundle = futureBundle.getResult();
        assertTrue(futureBundle.isDone());
        assertNotNull(resultBundle);
        try {
            latch.await(LATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("should not throw an InterruptedException");
        }
    }
    public void testGetAuthTokenByFeatures() throws IOException, AuthenticatorException,
            OperationCanceledException {
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        Bundle resultBundle = getAuthTokenByFeature(
                new String[] { NON_EXISTING_FEATURE },
                null 
        );
        validateNullResult(resultBundle);
        validateOptions(null, mockAuthenticator.mOptionsAddAccount);
        validateOptions(null, mockAuthenticator.mOptionsUpdateCredentials);
        validateOptions(null, mockAuthenticator.mOptionsConfirmCredentials);
        validateOptions(null, mockAuthenticator.mOptionsGetAuthToken);
        mockAuthenticator.clearData();
        resultBundle = getAuthTokenByFeature(
                new String[] { NON_EXISTING_FEATURE },
                ACTIVITY
        );
        validateAccountAndAuthTokenResult(resultBundle);
        validateOptions(OPTIONS_BUNDLE, mockAuthenticator.mOptionsAddAccount);
        validateOptions(null, mockAuthenticator.mOptionsUpdateCredentials);
        validateOptions(null, mockAuthenticator.mOptionsConfirmCredentials);
        validateOptions(null, mockAuthenticator.mOptionsGetAuthToken);
        mockAuthenticator.clearData();
        resultBundle = getAuthTokenByFeature(
                REQUIRED_FEATURES,
                null 
        );
        validateAccountAndAuthTokenResult(resultBundle);
        validateOptions(null, mockAuthenticator.mOptionsAddAccount);
        validateOptions(null, mockAuthenticator.mOptionsUpdateCredentials);
        validateOptions(null, mockAuthenticator.mOptionsConfirmCredentials);
        validateOptions(null, mockAuthenticator.mOptionsGetAuthToken);
        mockAuthenticator.clearData();
        resultBundle = getAuthTokenByFeature(
                REQUIRED_FEATURES,
                ACTIVITY
        );
        validateAccountAndAuthTokenResult(resultBundle);
        validateOptions(null, mockAuthenticator.mOptionsAddAccount);
        validateOptions(null, mockAuthenticator.mOptionsUpdateCredentials);
        validateOptions(null, mockAuthenticator.mOptionsConfirmCredentials);
        validateOptions(null, mockAuthenticator.mOptionsGetAuthToken);
    }
    public void testConfirmCredentials() throws IOException, AuthenticatorException,
            OperationCanceledException {
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        AccountManagerFuture<Bundle> futureBundle = am.confirmCredentials(ACCOUNT,
                OPTIONS_BUNDLE,
                ACTIVITY,
                null ,
                null );
        futureBundle.getResult();
        validateCredentials();
    }
    public void testConfirmCredentialsWithCallbackAndHandler() throws IOException,
            AuthenticatorException, OperationCanceledException {
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        testConfirmCredentialsWithCallbackAndHandler(null );
        testConfirmCredentialsWithCallbackAndHandler(new Handler());
    }
    private void testConfirmCredentialsWithCallbackAndHandler(Handler handler) throws IOException,
            AuthenticatorException, OperationCanceledException {
        final CountDownLatch latch = new CountDownLatch(1);
        AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> bundleFuture) {
                Bundle resultBundle = null;
                try {
                    resultBundle = bundleFuture.getResult();
                    validateCredentials();
                    assertNull(resultBundle.get(AccountManager.KEY_AUTHTOKEN));
                } catch (OperationCanceledException e) {
                    fail("should not throw an OperationCanceledException");
                } catch (IOException e) {
                    fail("should not throw an IOException");
                } catch (AuthenticatorException e) {
                    fail("should not throw an AuthenticatorException");
                }
                finally {
                    latch.countDown();
                }
            }
        };
        AccountManagerFuture<Bundle> futureBundle = am.confirmCredentials(ACCOUNT,
                OPTIONS_BUNDLE,
                ACTIVITY,
                callback,
                handler);
        futureBundle.getResult();
        try {
            latch.await(LATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("should not throw an InterruptedException");
        }
    }
    public void testUpdateCredentials() throws IOException, AuthenticatorException,
            OperationCanceledException {
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        AccountManagerFuture<Bundle> futureBundle = am.updateCredentials(ACCOUNT,
                AUTH_TOKEN_TYPE,
                OPTIONS_BUNDLE,
                ACTIVITY,
                null ,
                null );
        Bundle result = futureBundle.getResult();
        validateAccountAndNoAuthTokenResult(result);
        validateCredentials();
    }
    public void testUpdateCredentialsWithCallbackAndHandler() throws IOException,
            AuthenticatorException, OperationCanceledException {
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        testUpdateCredentialsWithCallbackAndHandler(null );
        testUpdateCredentialsWithCallbackAndHandler(new Handler());
    }
    private void testUpdateCredentialsWithCallbackAndHandler(Handler handler) throws IOException,
            AuthenticatorException, OperationCanceledException {
        final CountDownLatch latch = new CountDownLatch(1);
        AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> bundleFuture) {
                Bundle resultBundle = null;
                try {
                    resultBundle = bundleFuture.getResult();
                    validateCredentials();
                    assertNull(resultBundle.get(AccountManager.KEY_AUTHTOKEN));
                } catch (OperationCanceledException e) {
                    fail("should not throw an OperationCanceledException");
                } catch (IOException e) {
                    fail("should not throw an IOException");
                } catch (AuthenticatorException e) {
                    fail("should not throw an AuthenticatorException");
                }
                finally {
                    latch.countDown();
                }
            }
        };
        AccountManagerFuture<Bundle> futureBundle = am.updateCredentials(ACCOUNT,
                AUTH_TOKEN_TYPE,
                OPTIONS_BUNDLE,
                ACTIVITY,
                callback,
                handler);
        futureBundle.getResult();
        try {
            latch.await(LATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("should not throw an InterruptedException");
        }
    }
    public void testEditProperties() throws IOException, AuthenticatorException,
            OperationCanceledException {
        AccountManagerFuture<Bundle> futureBundle = am.editProperties(ACCOUNT_TYPE,
                ACTIVITY,
                null ,
                null );
        Bundle result = futureBundle.getResult();
        validateAccountAndNoAuthTokenResult(result);
        assertEquals(ACCOUNT_TYPE, mockAuthenticator.getAccountType());
    }
    public void testEditPropertiesWithCallbackAndHandler() {
        testEditPropertiesWithCallbackAndHandler(null );
        testEditPropertiesWithCallbackAndHandler(new Handler());
    }
    private void testEditPropertiesWithCallbackAndHandler(Handler handler) {
        final CountDownLatch latch = new CountDownLatch(1);
        AccountManagerCallback<Bundle> callback = new AccountManagerCallback<Bundle>() {
            public void run(AccountManagerFuture<Bundle> bundleFuture) {
                try {
                    assertEquals(ACCOUNT_TYPE, mockAuthenticator.getAccountType());
                }
                finally {
                    latch.countDown();
                }
            }
        };
        AccountManagerFuture<Bundle> futureBundle = am.editProperties(ACCOUNT_TYPE,
                ACTIVITY,
                callback,
                handler);
        try {
            latch.await(LATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("should not throw an InterruptedException");
        }
    }
    public void testAddOnAccountsUpdatedListenerWithHandler() throws IOException,
            AuthenticatorException, OperationCanceledException {
        testAddOnAccountsUpdatedListenerWithHandler(null ,
                false );
        assertTrue(removeAccount(am, ACCOUNT, null ));
        testAddOnAccountsUpdatedListenerWithHandler(null ,
                true );
        assertTrue(removeAccount(am, ACCOUNT, null ));
        testAddOnAccountsUpdatedListenerWithHandler(new Handler(),
                false );
        assertTrue(removeAccount(am, ACCOUNT, null ));
        testAddOnAccountsUpdatedListenerWithHandler(new Handler(),
                true );
    }
    private void testAddOnAccountsUpdatedListenerWithHandler(Handler handler,
            boolean updateImmediately) {
        final CountDownLatch latch = new CountDownLatch(1);
        OnAccountsUpdateListener listener = new OnAccountsUpdateListener() {
            public void onAccountsUpdated(Account[] accounts) {
                latch.countDown();
            }
        };
        am.addOnAccountsUpdatedListener(listener,
                handler,
                updateImmediately);
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        try {
            latch.await(LATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("should not throw an InterruptedException");
        }
        am.removeOnAccountsUpdatedListener(listener);
    }
    public void testRemoveOnAccountsUpdatedListener() throws IOException, AuthenticatorException,
            OperationCanceledException {
        testRemoveOnAccountsUpdatedListenerWithHandler(null );
        assertTrue(removeAccount(am, ACCOUNT, null ));
        testRemoveOnAccountsUpdatedListenerWithHandler(new Handler());
    }
    private void testRemoveOnAccountsUpdatedListenerWithHandler(Handler handler) {
        final CountDownLatch latch = new CountDownLatch(1);
        OnAccountsUpdateListener listener = new OnAccountsUpdateListener() {
            public void onAccountsUpdated(Account[] accounts) {
                fail("should not be called");
            }
        };
        am.addOnAccountsUpdatedListener(listener,
                handler,
                false );
        am.removeOnAccountsUpdatedListener(listener);
        addAccountExplicitly(ACCOUNT, ACCOUNT_PASSWORD, null );
        try {
            latch.await(LATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("should not throw an InterruptedException");
        }
    }
    public void testHasFeature()
            throws IOException, AuthenticatorException, OperationCanceledException {
        testHasFeature(null );
        testHasFeature(new Handler());
        testHasFeatureWithCallback(null );
        testHasFeatureWithCallback(new Handler());
    }
    public void testHasFeature(Handler handler)
            throws IOException, AuthenticatorException, OperationCanceledException {
        Bundle resultBundle = addAccount(am,
                ACCOUNT_TYPE,
                AUTH_TOKEN_TYPE,
                REQUIRED_FEATURES,
                OPTIONS_BUNDLE,
                ACTIVITY,
                null ,
                null );
        validateAccountAndAuthTokenType();
        validateFeatures();
        AccountManagerFuture<Boolean> booleanFuture = am.hasFeatures(ACCOUNT,
                new String[]{FEATURE_1},
                null ,
                handler);
        assertTrue(booleanFuture.getResult());
        booleanFuture = am.hasFeatures(ACCOUNT,
                new String[]{FEATURE_2},
                null ,
                handler);
        assertTrue(booleanFuture.getResult());
        booleanFuture = am.hasFeatures(ACCOUNT,
                new String[]{FEATURE_1, FEATURE_2},
                null ,
                handler);
        assertTrue(booleanFuture.getResult());
        booleanFuture = am.hasFeatures(ACCOUNT,
                new String[]{NON_EXISTING_FEATURE},
                null ,
                handler);
        assertFalse(booleanFuture.getResult());
        booleanFuture = am.hasFeatures(ACCOUNT,
                new String[]{NON_EXISTING_FEATURE, FEATURE_1},
                null ,
                handler);
        assertFalse(booleanFuture.getResult());
        booleanFuture = am.hasFeatures(ACCOUNT,
                new String[]{NON_EXISTING_FEATURE, FEATURE_1, FEATURE_2},
                null ,
                handler);
        assertFalse(booleanFuture.getResult());
    }
    private AccountManagerCallback<Boolean> getAssertTrueCallback(final CountDownLatch latch) {
        return new AccountManagerCallback<Boolean>() {
            public void run(AccountManagerFuture<Boolean> booleanFuture) {
                try {
                    assertTrue(booleanFuture.getResult());
                } catch (Exception e) {
                    fail("Exception: " + e);
                } finally {
                    latch.countDown();
                }
            }
        };
    }
    private AccountManagerCallback<Boolean> getAssertFalseCallback(final CountDownLatch latch) {
        return new AccountManagerCallback<Boolean>() {
            public void run(AccountManagerFuture<Boolean> booleanFuture) {
                try {
                    assertFalse(booleanFuture.getResult());
                } catch (Exception e) {
                    fail("Exception: " + e);
                } finally {
                    latch.countDown();
                }
            }
        };
    }
    private void waitForLatch(final CountDownLatch latch) {
        try {
            latch.await(LATCH_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("should not throw an InterruptedException");
        }
    }
    public void testHasFeatureWithCallback(Handler handler)
            throws IOException, AuthenticatorException, OperationCanceledException {
        Bundle resultBundle = addAccount(am,
                ACCOUNT_TYPE,
                AUTH_TOKEN_TYPE,
                REQUIRED_FEATURES,
                OPTIONS_BUNDLE,
                ACTIVITY,
                null ,
                null );
        validateAccountAndAuthTokenType();
        validateFeatures();
        CountDownLatch latch = new CountDownLatch(1);
        am.hasFeatures(ACCOUNT,
                new String[]{FEATURE_1},
                getAssertTrueCallback(latch),
                handler);
        waitForLatch(latch);
        latch = new CountDownLatch(1);
        am.hasFeatures(ACCOUNT,
                new String[]{FEATURE_2},
                getAssertTrueCallback(latch),
                handler);
        waitForLatch(latch);
        latch = new CountDownLatch(1);
        am.hasFeatures(ACCOUNT,
                new String[]{FEATURE_1, FEATURE_2},
                getAssertTrueCallback(latch),
                handler);
        waitForLatch(latch);
        latch = new CountDownLatch(1);
        am.hasFeatures(ACCOUNT,
                new String[]{NON_EXISTING_FEATURE},
                getAssertFalseCallback(latch),
                handler);
        waitForLatch(latch);
        latch = new CountDownLatch(1);
        am.hasFeatures(ACCOUNT,
                new String[]{NON_EXISTING_FEATURE, FEATURE_1},
                getAssertFalseCallback(latch),
                handler);
        waitForLatch(latch);
        latch = new CountDownLatch(1);
        am.hasFeatures(ACCOUNT,
                new String[]{NON_EXISTING_FEATURE, FEATURE_1, FEATURE_2},
                getAssertFalseCallback(latch),
                handler);
        waitForLatch(latch);
    }
}
