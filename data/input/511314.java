public class ExchangeStore extends Store {
    public static final String LOG_TAG = "ExchangeStore";
    private URI mUri;
    private final ExchangeTransport mTransport;
    public static Store newInstance(String uri, Context context, PersistentDataCallbacks callbacks)
            throws MessagingException {
        return new ExchangeStore(uri, context, callbacks);
    }
    private ExchangeStore(String _uri, Context context, PersistentDataCallbacks callbacks)
            throws MessagingException {
        try {
            mUri = new URI(_uri);
        } catch (URISyntaxException e) {
            throw new MessagingException("Invalid uri for ExchangeStore");
        }
        mTransport = ExchangeTransport.getInstance(mUri, context);
    }
    @Override
    public void checkSettings() throws MessagingException {
        mTransport.checkSettings(mUri);
    }
    static public AccountManagerFuture<Bundle> addSystemAccount(Context context, Account acct,
            boolean syncContacts, boolean syncCalendar, AccountManagerCallback<Bundle> callback) {
        Bundle options = new Bundle();
        options.putString(EasAuthenticatorService.OPTIONS_USERNAME, acct.mEmailAddress);
        options.putString(EasAuthenticatorService.OPTIONS_PASSWORD, acct.mHostAuthRecv.mPassword);
        options.putBoolean(EasAuthenticatorService.OPTIONS_CONTACTS_SYNC_ENABLED, syncContacts);
        options.putBoolean(EasAuthenticatorService.OPTIONS_CALENDAR_SYNC_ENABLED, syncCalendar);
        return AccountManager.get(context).addAccount(Email.EXCHANGE_ACCOUNT_MANAGER_TYPE,
                null, null, options, null, callback, null);
    }
    static public AccountManagerFuture<Boolean> removeSystemAccount(Context context, Account acct,
            AccountManagerCallback<Bundle> callback) {
        android.accounts.Account systemAccount =
            new android.accounts.Account(acct.mEmailAddress, Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
        return AccountManager.get(context).removeAccount(systemAccount, null, null);
    }
    @Override
    public Folder getFolder(String name) {
        return null;
    }
    @Override
    public Folder[] getPersonalNamespaces() {
        return null;
    }
    @Override
    public Class<? extends android.app.Activity> getSettingActivityClass() {
        return com.android.email.activity.setup.AccountSetupExchange.class;
    }
    @Override
    public StoreSynchronizer getMessageSynchronizer() {
        return null;
    }
    @Override
    public boolean requireStructurePrefetch() {
        return true;
    }
    @Override
    public boolean requireCopyMessageToSentFolder() {
        return false;
    }
    public static class ExchangeTransport {
        private final Context mContext;
        private String mHost;
        private String mDomain;
        private String mUsername;
        private String mPassword;
        private static HashMap<String, ExchangeTransport> sUriToInstanceMap =
            new HashMap<String, ExchangeTransport>();
        public synchronized static ExchangeTransport getInstance(URI uri, Context context)
        throws MessagingException {
            if (!uri.getScheme().equals("eas") && !uri.getScheme().equals("eas+ssl+") &&
                    !uri.getScheme().equals("eas+ssl+trustallcerts")) {
                throw new MessagingException("Invalid scheme");
            }
            final String key = uri.toString();
            ExchangeTransport transport = sUriToInstanceMap.get(key);
            if (transport == null) {
                transport = new ExchangeTransport(uri, context);
                sUriToInstanceMap.put(key, transport);
            }
            return transport;
        }
        private ExchangeTransport(URI uri, Context context) throws MessagingException {
            mContext = context;
            setUri(uri);
        }
        private void setUri(final URI uri) throws MessagingException {
            mHost = uri.getHost();
            if (mHost == null) {
                throw new MessagingException("host not specified");
            }
            mDomain = uri.getPath();
            if (!TextUtils.isEmpty(mDomain)) {
                mDomain = mDomain.substring(1);
            }
            final String userInfo = uri.getUserInfo();
            if (userInfo == null) {
                throw new MessagingException("user information not specifed");
            }
            final String[] uinfo = userInfo.split(":", 2);
            if (uinfo.length != 2) {
                throw new MessagingException("user name and password not specified");
            }
            mUsername = uinfo[0];
            mPassword = uinfo[1];
        }
        public void checkSettings(URI uri) throws MessagingException {
            setUri(uri);
            boolean ssl = uri.getScheme().contains("+ssl");
            boolean tssl = uri.getScheme().contains("+trustallcerts");
            try {
                int port = ssl ? 443 : 80;
                IEmailService svc = ExchangeUtils.getExchangeEmailService(mContext, null);
                if (svc instanceof EmailServiceProxy) {
                    ((EmailServiceProxy)svc).setTimeout(90);
                }
                int result = svc.validate("eas", mHost, mUsername, mPassword, port, ssl, tssl);
                if (result != MessagingException.NO_ERROR) {
                    if (result == MessagingException.AUTHENTICATION_FAILED) {
                        throw new AuthenticationFailedException("Authentication failed.");
                    } else {
                        throw new MessagingException(result);
                    }
                }
            } catch (RemoteException e) {
                throw new MessagingException("Call to validate generated an exception", e);
            }
        }
    }
    @Override
    public Bundle autoDiscover(Context context, String username, String password)
            throws MessagingException {
        try {
            return ExchangeUtils.getExchangeEmailService(context, null)
                .autoDiscover(username, password);
        } catch (RemoteException e) {
            return null;
        }
    }
}
