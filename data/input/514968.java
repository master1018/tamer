public final class PendingIntent implements Parcelable {
    private final IIntentSender mTarget;
    public static final int FLAG_ONE_SHOT = 1<<30;
    public static final int FLAG_NO_CREATE = 1<<29;
    public static final int FLAG_CANCEL_CURRENT = 1<<28;
    public static final int FLAG_UPDATE_CURRENT = 1<<27;
    public static class CanceledException extends AndroidException {
        public CanceledException() {
        }
        public CanceledException(String name) {
            super(name);
        }
        public CanceledException(Exception cause) {
            super(cause);
        }
    }
    public interface OnFinished {
        void onSendFinished(PendingIntent pendingIntent, Intent intent,
                int resultCode, String resultData, Bundle resultExtras);
    }
    private static class FinishedDispatcher extends IIntentReceiver.Stub
            implements Runnable {
        private final PendingIntent mPendingIntent;
        private final OnFinished mWho;
        private final Handler mHandler;
        private Intent mIntent;
        private int mResultCode;
        private String mResultData;
        private Bundle mResultExtras;
        FinishedDispatcher(PendingIntent pi, OnFinished who, Handler handler) {
            mPendingIntent = pi;
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
            mWho.onSendFinished(mPendingIntent, mIntent, mResultCode,
                    mResultData, mResultExtras);
        }
    }
    public static PendingIntent getActivity(Context context, int requestCode,
            Intent intent, int flags) {
        String packageName = context.getPackageName();
        String resolvedType = intent != null ? intent.resolveTypeIfNeeded(
                context.getContentResolver()) : null;
        try {
            IIntentSender target =
                ActivityManagerNative.getDefault().getIntentSender(
                    IActivityManager.INTENT_SENDER_ACTIVITY, packageName,
                    null, null, requestCode, intent, resolvedType, flags);
            return target != null ? new PendingIntent(target) : null;
        } catch (RemoteException e) {
        }
        return null;
    }
    public static PendingIntent getBroadcast(Context context, int requestCode,
            Intent intent, int flags) {
        String packageName = context.getPackageName();
        String resolvedType = intent != null ? intent.resolveTypeIfNeeded(
                context.getContentResolver()) : null;
        try {
            IIntentSender target =
                ActivityManagerNative.getDefault().getIntentSender(
                    IActivityManager.INTENT_SENDER_BROADCAST, packageName,
                    null, null, requestCode, intent, resolvedType, flags);
            return target != null ? new PendingIntent(target) : null;
        } catch (RemoteException e) {
        }
        return null;
    }
    public static PendingIntent getService(Context context, int requestCode,
            Intent intent, int flags) {
        String packageName = context.getPackageName();
        String resolvedType = intent != null ? intent.resolveTypeIfNeeded(
                context.getContentResolver()) : null;
        try {
            IIntentSender target =
                ActivityManagerNative.getDefault().getIntentSender(
                    IActivityManager.INTENT_SENDER_SERVICE, packageName,
                    null, null, requestCode, intent, resolvedType, flags);
            return target != null ? new PendingIntent(target) : null;
        } catch (RemoteException e) {
        }
        return null;
    }
    public IntentSender getIntentSender() {
        return new IntentSender(mTarget);
    }
    public void cancel() {
        try {
            ActivityManagerNative.getDefault().cancelIntentSender(mTarget);
        } catch (RemoteException e) {
        }
    }
    public void send() throws CanceledException {
        send(null, 0, null, null, null);
    }
    public void send(int code) throws CanceledException {
        send(null, code, null, null, null);
    }
    public void send(Context context, int code, Intent intent)
            throws CanceledException {
        send(context, code, intent, null, null);
    }
    public void send(int code, OnFinished onFinished, Handler handler)
            throws CanceledException {
        send(null, code, null, onFinished, handler);
    }
    public void send(Context context, int code, Intent intent,
            OnFinished onFinished, Handler handler) throws CanceledException {
        try {
            String resolvedType = intent != null ?
                    intent.resolveTypeIfNeeded(context.getContentResolver())
                    : null;
            int res = mTarget.send(code, intent, resolvedType,
                    onFinished != null
                    ? new FinishedDispatcher(this, onFinished, handler)
                    : null);
            if (res < 0) {
                throw new CanceledException();
            }
        } catch (RemoteException e) {
            throw new CanceledException(e);
        }
    }
    public String getTargetPackage() {
        try {
            return ActivityManagerNative.getDefault()
                .getPackageForIntentSender(mTarget);
        } catch (RemoteException e) {
            return null;
        }
    }
    @Override
    public boolean equals(Object otherObj) {
        if (otherObj instanceof PendingIntent) {
            return mTarget.asBinder().equals(((PendingIntent)otherObj)
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
        sb.append("PendingIntent{");
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
    public static final Parcelable.Creator<PendingIntent> CREATOR
            = new Parcelable.Creator<PendingIntent>() {
        public PendingIntent createFromParcel(Parcel in) {
            IBinder target = in.readStrongBinder();
            return target != null ? new PendingIntent(target) : null;
        }
        public PendingIntent[] newArray(int size) {
            return new PendingIntent[size];
        }
    };
    public static void writePendingIntentOrNullToParcel(PendingIntent sender,
            Parcel out) {
        out.writeStrongBinder(sender != null ? sender.mTarget.asBinder()
                : null);
    }
    public static PendingIntent readPendingIntentOrNullFromParcel(Parcel in) {
        IBinder b = in.readStrongBinder();
        return b != null ? new PendingIntent(b) : null;
    }
     PendingIntent(IIntentSender target) {
        mTarget = target;
    }
     PendingIntent(IBinder target) {
        mTarget = IIntentSender.Stub.asInterface(target);
    }
    public IIntentSender getTarget() {
        return mTarget;
    }
}
