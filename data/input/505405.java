public class AccountAuthenticatorResponse implements Parcelable {
    private static final String TAG = "AccountAuthenticator";
    private IAccountAuthenticatorResponse mAccountAuthenticatorResponse;
     AccountAuthenticatorResponse(IAccountAuthenticatorResponse response) {
        mAccountAuthenticatorResponse = response;
    }
    public AccountAuthenticatorResponse(Parcel parcel) {
        mAccountAuthenticatorResponse =
                IAccountAuthenticatorResponse.Stub.asInterface(parcel.readStrongBinder());
    }
    public void onResult(Bundle result) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            result.keySet(); 
            Log.v(TAG, "AccountAuthenticatorResponse.onResult: "
                    + AccountManager.sanitizeResult(result));
        }
        try {
            mAccountAuthenticatorResponse.onResult(result);
        } catch (RemoteException e) {
        }
    }
    public void onRequestContinued() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "AccountAuthenticatorResponse.onRequestContinued");
        }
        try {
            mAccountAuthenticatorResponse.onRequestContinued();
        } catch (RemoteException e) {
        }
    }
    public void onError(int errorCode, String errorMessage) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "AccountAuthenticatorResponse.onError: " + errorCode + ", " + errorMessage);
        }
        try {
            mAccountAuthenticatorResponse.onError(errorCode, errorMessage);
        } catch (RemoteException e) {
        }
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(mAccountAuthenticatorResponse.asBinder());
    }
    public static final Creator<AccountAuthenticatorResponse> CREATOR =
            new Creator<AccountAuthenticatorResponse>() {
        public AccountAuthenticatorResponse createFromParcel(Parcel source) {
            return new AccountAuthenticatorResponse(source);
        }
        public AccountAuthenticatorResponse[] newArray(int size) {
            return new AccountAuthenticatorResponse[size];
        }
    };
}
