public class IntentSender implements Parcelable {
    private final IIntentSender mTarget;
    public static class SendIntentException extends AndroidException {
        public SendIntentException() {
        }
        public SendIntentException(String name) {
            super(name);
        }
        public SendIntentException(Exception cause) {
            super(cause);
        }
    }
    public interface OnFinished {
        void onSendFinished(IntentSender IntentSender, Intent intent,
                int resultCode, String resultData, Bundle resultExtras);
    }
    private static class FinishedDispatcher extends IIntentReceiver.Stub
            implements Runnable {
        private final IntentSender mIntentSender;
        private final OnFinished mWho;
        private final Handler mHandler;
        private Intent mIntent;
        private int mResultCode;
        private String mResultData;
        private Bundle mResultExtras;
        FinishedDispatcher(IntentSender pi, OnFinished who, Handler handler) {
            mIntentSender = pi;
            mWho = who;
            mHandler = handler;
        }
        public void performReceive(Intent intent, int resultCode,
                String data, Bundle extras, boolean serialized, boolean sticky) {
            mIntent = intent;
            mResultCode = resultCode;
            mResultData = data;
            mResultExtras = extras;
            if (mHandler == null) {
                run();
            } else {
                mHandler.post(this);
            }
        }
        public void run() {
            mWho.onSendFinished(mIntentSender, mIntent, mResultCode,
                    mResultData, mResultExtras);
        }
    }
    public void sendIntent(Context context, int code, Intent intent,
            OnFinished onFinished, Handler handler) throws SendIntentException {
        try {
            String resolvedType = intent != null ?
                    intent.resolveTypeIfNeeded(context.getContentResolver())
                    : null;
            int res = mTarget.send(code, intent, resolvedType,
                    onFinished != null
                    ? new FinishedDispatcher(this, onFinished, handler)
                    : null);
            if (res < 0) {
                throw new SendIntentException();
            }
        } catch (RemoteException e) {
            throw new SendIntentException();
        }
    }
    @Override
    public boolean equals(Object otherObj) {
        if (otherObj instanceof IntentSender) {
            return mTarget.asBinder().equals(((IntentSender)otherObj)
                    .mTarget.asBinder());
        }
        return false;
    }
    @Override
    public int hashCode() {
        return mTarget.asBinder().hashCode();
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("IntentSender{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(": ");
        sb.append(mTarget != null ? mTarget.asBinder() : null);
        sb.append('}');
        return sb.toString();
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel out, int flags) {
        out.writeStrongBinder(mTarget.asBinder());
    }
    public static final Parcelable.Creator<IntentSender> CREATOR
            = new Parcelable.Creator<IntentSender>() {
        public IntentSender createFromParcel(Parcel in) {
            IBinder target = in.readStrongBinder();
            return target != null ? new IntentSender(target) : null;
        }
        public IntentSender[] newArray(int size) {
            return new IntentSender[size];
        }
    };
    public static void writeIntentSenderOrNullToParcel(IntentSender sender,
            Parcel out) {
        out.writeStrongBinder(sender != null ? sender.mTarget.asBinder()
                : null);
    }
    public static IntentSender readIntentSenderOrNullFromParcel(Parcel in) {
        IBinder b = in.readStrongBinder();
        return b != null ? new IntentSender(b) : null;
    }
    public IIntentSender getTarget() {
        return mTarget;
    }
    public IntentSender(IIntentSender target) {
        mTarget = target;
    }
    public IntentSender(IBinder target) {
        mTarget = IIntentSender.Stub.asInterface(target);
    }
}
