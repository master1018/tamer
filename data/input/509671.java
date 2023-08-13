public class MockAccountAuthenticator extends AbstractAccountAuthenticator {
    private static MockAccountAuthenticator sMockAuthenticator = null;
    public static final String ACCOUNT_NAME = "android.content.cts.account.name";
    public static final String ACCOUNT_TYPE = "android.content.cts.account.type";
    public static final String ACCOUNT_PASSWORD = "android.content.cts.account.password";
    public static final String AUTH_TOKEN = "mockAuthToken";
    public static final String AUTH_TOKEN_LABEL = "mockAuthTokenLabel";
    public MockAccountAuthenticator(Context context) {
        super(context);
    }
    private Bundle createResultBundle() {
        Bundle result = new Bundle();
        result.putString(AccountManager.KEY_ACCOUNT_NAME, ACCOUNT_NAME);
        result.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
        result.putString(AccountManager.KEY_AUTHTOKEN, AUTH_TOKEN);
        return result;
    }
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
            String authTokenType, String[] requiredFeatures, Bundle options)
            throws NetworkErrorException {
        return createResultBundle();
    }
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return createResultBundle();
    }
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
            String authTokenType, Bundle options) throws NetworkErrorException {
        return createResultBundle();
    }
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
            Bundle options) throws NetworkErrorException {
        Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
        return result;
    }
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
            String authTokenType, Bundle options) throws NetworkErrorException {
        return createResultBundle();
    }
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return AUTH_TOKEN_LABEL;
    }
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
            String[] features) throws NetworkErrorException {
        Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
        return result;
    }
    public static synchronized MockAccountAuthenticator getMockAuthenticator(Context context) {
        if (null == sMockAuthenticator) {
            sMockAuthenticator = new MockAccountAuthenticator(context);
        }
        return sMockAuthenticator;
    }
}
