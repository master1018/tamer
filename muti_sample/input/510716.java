public abstract class AbstractAccountAuthenticator {
    private static final String TAG = "AccountAuthenticator";
    private final Context mContext;
    public AbstractAccountAuthenticator(Context context) {
        mContext = context;
    }
    private class Transport extends IAccountAuthenticator.Stub {
        public void addAccount(IAccountAuthenticatorResponse response, String accountType,
                String authTokenType, String[] features, Bundle options)
                throws RemoteException {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "addAccount: accountType " + accountType
                        + ", authTokenType " + authTokenType
                        + ", features " + (features == null ? "[]" : Arrays.toString(features)));
            }
            checkBinderPermission();
            try {
                final Bundle result = AbstractAccountAuthenticator.this.addAccount(
                    new AccountAuthenticatorResponse(response),
                        accountType, authTokenType, features, options);
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    result.keySet(); 
                    Log.v(TAG, "addAccount: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (NetworkErrorException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "addAccount", e);
                }
                response.onError(AccountManager.ERROR_CODE_NETWORK_ERROR, e.getMessage());
            } catch (UnsupportedOperationException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "addAccount", e);
                }
                response.onError(AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION,
                        "addAccount not supported");
            }
        }
        public void confirmCredentials(IAccountAuthenticatorResponse response,
                Account account, Bundle options) throws RemoteException {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "confirmCredentials: " + account);
            }
            checkBinderPermission();
            try {
                final Bundle result = AbstractAccountAuthenticator.this.confirmCredentials(
                    new AccountAuthenticatorResponse(response), account, options);
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    result.keySet(); 
                    Log.v(TAG, "confirmCredentials: result "
                            + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (NetworkErrorException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "confirmCredentials", e);
                }
                response.onError(AccountManager.ERROR_CODE_NETWORK_ERROR, e.getMessage());
            } catch (UnsupportedOperationException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "confirmCredentials", e);
                }
                response.onError(AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION,
                        "confirmCredentials not supported");
            }
        }
        public void getAuthTokenLabel(IAccountAuthenticatorResponse response,
                String authTokenType)
                throws RemoteException {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "getAuthTokenLabel: authTokenType " + authTokenType);
            }
            checkBinderPermission();
            try {
                Bundle result = new Bundle();
                result.putString(AccountManager.KEY_AUTH_TOKEN_LABEL,
                        AbstractAccountAuthenticator.this.getAuthTokenLabel(authTokenType));
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    result.keySet(); 
                    Log.v(TAG, "getAuthTokenLabel: result "
                            + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (IllegalArgumentException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "getAuthTokenLabel", e);
                }
                response.onError(AccountManager.ERROR_CODE_BAD_ARGUMENTS,
                        "unknown authTokenType");
            } catch (UnsupportedOperationException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "getAuthTokenLabel", e);
                }
                response.onError(AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION,
                        "getAuthTokenTypeLabel not supported");
            }
        }
        public void getAuthToken(IAccountAuthenticatorResponse response,
                Account account, String authTokenType, Bundle loginOptions)
                throws RemoteException {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "getAuthToken: " + account
                        + ", authTokenType " + authTokenType);
            }
            checkBinderPermission();
            try {
                final Bundle result = AbstractAccountAuthenticator.this.getAuthToken(
                        new AccountAuthenticatorResponse(response), account,
                        authTokenType, loginOptions);
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    result.keySet(); 
                    Log.v(TAG, "getAuthToken: result " + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (UnsupportedOperationException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "getAuthToken", e);
                }
                response.onError(AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION,
                        "getAuthToken not supported");
            } catch (NetworkErrorException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "getAuthToken", e);
                }
                response.onError(AccountManager.ERROR_CODE_NETWORK_ERROR, e.getMessage());
            }
        }
        public void updateCredentials(IAccountAuthenticatorResponse response, Account account,
                String authTokenType, Bundle loginOptions) throws RemoteException {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "updateCredentials: " + account
                        + ", authTokenType " + authTokenType);
            }
            checkBinderPermission();
            try {
                final Bundle result = AbstractAccountAuthenticator.this.updateCredentials(
                    new AccountAuthenticatorResponse(response), account,
                        authTokenType, loginOptions);
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    result.keySet(); 
                    Log.v(TAG, "updateCredentials: result "
                            + AccountManager.sanitizeResult(result));
                }
                if (result != null) {
                    response.onResult(result);
                }
            } catch (NetworkErrorException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "updateCredentials", e);
                }
                response.onError(AccountManager.ERROR_CODE_NETWORK_ERROR, e.getMessage());
            } catch (UnsupportedOperationException e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "updateCredentials", e);
                }
                response.onError(AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION,
                        "updateCredentials not supported");
            }
        }
        public void editProperties(IAccountAuthenticatorResponse response,
                String accountType) throws RemoteException {
            checkBinderPermission();
            try {
                final Bundle result = AbstractAccountAuthenticator.this.editProperties(
                    new AccountAuthenticatorResponse(response), accountType);
                if (result != null) {
                    response.onResult(result);
                }
            } catch (UnsupportedOperationException e) {
                response.onError(AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION,
                        "editProperties not supported");
            }
        }
        public void hasFeatures(IAccountAuthenticatorResponse response,
                Account account, String[] features) throws RemoteException {
            checkBinderPermission();
            try {
                final Bundle result = AbstractAccountAuthenticator.this.hasFeatures(
                    new AccountAuthenticatorResponse(response), account, features);
                if (result != null) {
                    response.onResult(result);
                }
            } catch (UnsupportedOperationException e) {
                response.onError(AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION,
                        "hasFeatures not supported");
            } catch (NetworkErrorException e) {
                response.onError(AccountManager.ERROR_CODE_NETWORK_ERROR, e.getMessage());
            }
        }
        public void getAccountRemovalAllowed(IAccountAuthenticatorResponse response,
                Account account) throws RemoteException {
            checkBinderPermission();
            try {
                final Bundle result = AbstractAccountAuthenticator.this.getAccountRemovalAllowed(
                    new AccountAuthenticatorResponse(response), account);
                if (result != null) {
                    response.onResult(result);
                }
            } catch (UnsupportedOperationException e) {
                response.onError(AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION,
                        "getAccountRemovalAllowed not supported");
            } catch (NetworkErrorException e) {
                response.onError(AccountManager.ERROR_CODE_NETWORK_ERROR, e.getMessage());
            }
        }
    }
    private void checkBinderPermission() {
        final int uid = Binder.getCallingUid();
        final String perm = Manifest.permission.ACCOUNT_MANAGER;
        if (mContext.checkCallingOrSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("caller uid " + uid + " lacks " + perm);
        }
    }
    private Transport mTransport = new Transport();
    public final IBinder getIBinder() {
        return mTransport.asBinder();
    }
    public abstract Bundle editProperties(AccountAuthenticatorResponse response,
            String accountType);
    public abstract Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
            String authTokenType, String[] requiredFeatures, Bundle options)
            throws NetworkErrorException;
    public abstract Bundle confirmCredentials(AccountAuthenticatorResponse response,
            Account account, Bundle options)
            throws NetworkErrorException;
    public abstract Bundle getAuthToken(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options)
            throws NetworkErrorException;
    public abstract String getAuthTokenLabel(String authTokenType);
    public abstract Bundle updateCredentials(AccountAuthenticatorResponse response,
            Account account, String authTokenType, Bundle options) throws NetworkErrorException;
    public abstract Bundle hasFeatures(AccountAuthenticatorResponse response,
            Account account, String[] features) throws NetworkErrorException;
    public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response,
            Account account) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true);
        return result;
    }
}
