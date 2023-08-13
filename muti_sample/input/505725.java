public class AccountManagerResponse implements Parcelable {
    private IAccountManagerResponse mResponse;
    public AccountManagerResponse(IAccountManagerResponse response) {
        mResponse = response;
    }
    public AccountManagerResponse(Parcel parcel) {
        mResponse =
                IAccountManagerResponse.Stub.asInterface(parcel.readStrongBinder());
    }
    public void onResult(Bundle result) {
        try {
            mResponse.onResult(result);
        } catch (RemoteException e) {
        }
    }
    public void onError(int errorCode, String errorMessage) {
        try {
            mResponse.onError(errorCode, errorMessage);
        } catch (RemoteException e) {
        }
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(mResponse.asBinder());
    }
    public static final Creator<AccountManagerResponse> CREATOR =
            new Creator<AccountManagerResponse>() {
        public AccountManagerResponse createFromParcel(Parcel source) {
            return new AccountManagerResponse(source);
        }
        public AccountManagerResponse[] newArray(int size) {
            return new AccountManagerResponse[size];
        }
    };
}
