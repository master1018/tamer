public abstract class RemoteCallback implements Parcelable {
    final Handler mHandler;
    final IRemoteCallback mTarget;
    class DeliverResult implements Runnable {
        final Bundle mResult;
        DeliverResult(Bundle result) {
            mResult = result;
        }
        public void run() {
            onResult(mResult);
        }
    }
    class LocalCallback extends IRemoteCallback.Stub {
        public void sendResult(Bundle bundle) {
            mHandler.post(new DeliverResult(bundle));
        }
    }
    static class RemoteCallbackProxy extends RemoteCallback {
        RemoteCallbackProxy(IRemoteCallback target) {
            super(target);
        }
        protected void onResult(Bundle bundle) {
        }
    }
    public RemoteCallback(Handler handler) {
        mHandler = handler;
        mTarget = new LocalCallback();
    }
     RemoteCallback(IRemoteCallback target) {
        mHandler = null;
        mTarget = target;
    }
    public void sendResult(Bundle bundle) throws RemoteException {
        mTarget.sendResult(bundle);
    }
    protected abstract void onResult(Bundle bundle);
    public boolean equals(Object otherObj) {
        if (otherObj == null) {
            return false;
        }
        try {
            return mTarget.asBinder().equals(((RemoteCallback)otherObj)
                    .mTarget.asBinder());
        } catch (ClassCastException e) {
        }
        return false;
    }
    public int hashCode() {
        return mTarget.asBinder().hashCode();
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(mTarget.asBinder());
    }
    public static final Parcelable.Creator<RemoteCallback> CREATOR
            = new Parcelable.Creator<RemoteCallback>() {
        public RemoteCallback createFromParcel(Parcel in) {
            IBinder target = in.readStrongBinder();
            return target != null ? new RemoteCallbackProxy(
                    IRemoteCallback.Stub.asInterface(target)) : null;
        }
        public RemoteCallback[] newArray(int size) {
            return new RemoteCallback[size];
        }
    };
}
