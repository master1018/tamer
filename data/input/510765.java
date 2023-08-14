public class Binder implements IBinder {
    private static final boolean FIND_POTENTIAL_LEAKS = false;
    private static final String TAG = "Binder";
    private int mObject;
    private IInterface mOwner;
    private String mDescriptor;
    public static final native int getCallingPid();
    public static final native int getCallingUid();
    public static final native long clearCallingIdentity();
    public static final native void restoreCallingIdentity(long token);
    public static final native void flushPendingCommands();
    public static final native void joinThreadPool();
    public Binder() {
        init();
        if (FIND_POTENTIAL_LEAKS) {
            final Class<? extends Binder> klass = getClass();
            if ((klass.isAnonymousClass() || klass.isMemberClass() || klass.isLocalClass()) &&
                    (klass.getModifiers() & Modifier.STATIC) == 0) {
                Log.w(TAG, "The following Binder class should be static or leaks might occur: " +
                    klass.getCanonicalName());
            }
        }
    }
    public void attachInterface(IInterface owner, String descriptor) {
        mOwner = owner;
        mDescriptor = descriptor;
    }
    public String getInterfaceDescriptor() {
        return mDescriptor;
    }
    public boolean pingBinder() {
        return true;
    }
    public boolean isBinderAlive() {
        return true;
    }
    public IInterface queryLocalInterface(String descriptor) {
        if (mDescriptor.equals(descriptor)) {
            return mOwner;
        }
        return null;
    }
    protected boolean onTransact(int code, Parcel data, Parcel reply,
            int flags) throws RemoteException {
        if (code == INTERFACE_TRANSACTION) {
            reply.writeString(getInterfaceDescriptor());
            return true;
        } else if (code == DUMP_TRANSACTION) {
            ParcelFileDescriptor fd = data.readFileDescriptor();
            String[] args = data.readStringArray();
            if (fd != null) {
                try {
                    dump(fd.getFileDescriptor(), args);
                } finally {
                    try {
                        fd.close();
                    } catch (IOException e) {
                    }
                }
            }
            return true;
        }
        return false;
    }
    public void dump(FileDescriptor fd, String[] args) {
        FileOutputStream fout = new FileOutputStream(fd);
        PrintWriter pw = new PrintWriter(fout);
        try {
            dump(fd, pw, args);
        } finally {
            pw.flush();
        }
    }
    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
    }
    public final boolean transact(int code, Parcel data, Parcel reply,
            int flags) throws RemoteException {
        if (Config.LOGV) Log.v("Binder", "Transact: " + code + " to " + this);
        if (data != null) {
            data.setDataPosition(0);
        }
        boolean r = onTransact(code, data, reply, flags);
        if (reply != null) {
            reply.setDataPosition(0);
        }
        return r;
    }
    public void linkToDeath(DeathRecipient recipient, int flags) {
    }
    public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
        return true;
    }
    protected void finalize() throws Throwable {
        try {
            destroy();
        } finally {
            super.finalize();
        }
    }
    private native final void init();
    private native final void destroy();
    private boolean execTransact(int code, int dataObj, int replyObj,
            int flags) {
        Parcel data = Parcel.obtain(dataObj);
        Parcel reply = Parcel.obtain(replyObj);
        boolean res;
        try {
            res = onTransact(code, data, reply, flags);
        } catch (RemoteException e) {
            reply.writeException(e);
            res = true;
        } catch (RuntimeException e) {
            reply.writeException(e);
            res = true;
        }
        reply.recycle();
        data.recycle();
        return res;
    }
}
final class BinderProxy implements IBinder {
    public native boolean pingBinder();
    public native boolean isBinderAlive();
    public IInterface queryLocalInterface(String descriptor) {
        return null;
    }
    public native String getInterfaceDescriptor() throws RemoteException;
    public native boolean transact(int code, Parcel data, Parcel reply,
            int flags) throws RemoteException;
    public native void linkToDeath(DeathRecipient recipient, int flags)
            throws RemoteException;
    public native boolean unlinkToDeath(DeathRecipient recipient, int flags);
    public void dump(FileDescriptor fd, String[] args) throws RemoteException {
        Parcel data = Parcel.obtain();
        data.writeFileDescriptor(fd);
        data.writeStringArray(args);
        try {
            transact(DUMP_TRANSACTION, data, null, 0);
        } finally {
            data.recycle();
        }
    }
    BinderProxy() {
        mSelf = new WeakReference(this);
    }
    @Override
    protected void finalize() throws Throwable {
        try {
            destroy();
        } finally {
            super.finalize();
        }
    }
    private native final void destroy();
    private static final void sendDeathNotice(DeathRecipient recipient) {
        if (Config.LOGV) Log.v("JavaBinder", "sendDeathNotice to " + recipient);
        try {
            recipient.binderDied();
        }
        catch (RuntimeException exc) {
            Log.w("BinderNative", "Uncaught exception from death notification",
                    exc);
        }
    }
    final private WeakReference mSelf;
    private int mObject;
}
