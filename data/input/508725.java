public class IsolatedContext extends ContextWrapper {
    private ContentResolver mResolver;
    private final MockAccountManager mMockAccountManager;
    private List<Intent> mBroadcastIntents = Lists.newArrayList();
    public IsolatedContext(
            ContentResolver resolver, Context targetContext) {
        super(targetContext);
        mResolver = resolver;
        mMockAccountManager = new MockAccountManager();
    }
    public List<Intent> getAndClearBroadcastIntents() {
        List<Intent> intents = mBroadcastIntents;
        mBroadcastIntents = Lists.newArrayList();
        return intents;
    }
    @Override
    public ContentResolver getContentResolver() {
        return mResolver;
    }
    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return false;
    }
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return null;
    }
    @Override
    public void sendBroadcast(Intent intent) {
        mBroadcastIntents.add(intent);
    }
    @Override
    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        mBroadcastIntents.add(intent);
    }
    @Override
    public int checkUriPermission(
            Uri uri, String readPermission, String writePermission, int pid,
            int uid, int modeFlags) {
        return PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        return PackageManager.PERMISSION_GRANTED;
    }
    @Override
    public Object getSystemService(String name) {
        if (Context.ACCOUNT_SERVICE.equals(name)) {
            return mMockAccountManager;
        }
        return null;
    }
    private class MockAccountManager extends AccountManager {
        public MockAccountManager() {
            super(IsolatedContext.this, null , null );
        }
        public void addOnAccountsUpdatedListener(OnAccountsUpdateListener listener,
                Handler handler, boolean updateImmediately) {
        }
        public Account[] getAccounts() {
            return new Account[]{};
        }
        public AccountManagerFuture<Account[]> getAccountsByTypeAndFeatures(
                final String type, final String[] features,
                AccountManagerCallback<Account[]> callback, Handler handler) {
            return new MockAccountManagerFuture<Account[]>(new Account[0]);
        }
        public String blockingGetAuthToken(Account account, String authTokenType,
                boolean notifyAuthFailure)
                throws OperationCanceledException, IOException, AuthenticatorException {
            return null;
        }
        private class MockAccountManagerFuture<T>
                implements AccountManagerFuture<T> {
            T mResult;
            public MockAccountManagerFuture(T result) {
                mResult = result;
            }
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }
            public boolean isCancelled() {
                return false;
            }
            public boolean isDone() {
                return true;
            }
            public T getResult()
                    throws OperationCanceledException, IOException, AuthenticatorException {
                return mResult;
            }
            public T getResult(long timeout, TimeUnit unit)
                    throws OperationCanceledException, IOException, AuthenticatorException {
                return getResult();
            }
        }
    }
    @Override
    public File getFilesDir() {
        return new File("/dev/null");
    }
}
