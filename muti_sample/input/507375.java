public class LocalService extends Service {
    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply,
                int flags) throws RemoteException {
            if (code == ServiceTest.SET_REPORTER_CODE) {
                data.enforceInterface(ServiceTest.SERVICE_LOCAL);
                mReportObject = data.readStrongBinder();
                return true;
            } else {
                return super.onTransact(code, data, reply, flags);
            }
        }
    };
    private IBinder mReportObject;
    private int mStartCount = 1;
    public LocalService() {
    }
    @Override
    public void onStart(Intent intent, int startId) {
        if (intent.getExtras() != null) {
            mReportObject = intent.getExtras().getIBinder(ServiceTest.REPORT_OBJ_NAME);
            if (mReportObject != null) {
                try {
                    Parcel data = Parcel.obtain();
                    data.writeInterfaceToken(ServiceTest.SERVICE_LOCAL);
                    data.writeInt(mStartCount);
                    mStartCount++;
                    mReportObject.transact(
                            ServiceTest.STARTED_CODE, data, null, 0);
                    data.recycle();
                } catch (RemoteException e) {
                }
            }
        }
    }
    @Override
    public void onDestroy() {
        Log.i("LocalService", "onDestroy: mReportObject=" + mReportObject);
        if (mReportObject != null) {
            try {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken(ServiceTest.SERVICE_LOCAL);
                mReportObject.transact(
                        ServiceTest.DESTROYED_CODE, data, null, 0);
                data.recycle();
            } catch (RemoteException e) {
            }
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("LocalService", "onBind: " + intent);
        return mBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("LocalService", "onUnbind: " + intent);
        if (mReportObject != null) {
            try {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken(ServiceTest.SERVICE_LOCAL);
                mReportObject.transact(
                        ServiceTest.UNBIND_CODE, data, null, 0);
                data.recycle();
            } catch (RemoteException e) {
            }
        }
        return true;
    }
    @Override
    public void onRebind(Intent intent) {
        Log.i("LocalService", "onUnbind: " + intent);
        if (mReportObject != null) {
            try {
                Parcel data = Parcel.obtain();
                data.writeInterfaceToken(ServiceTest.SERVICE_LOCAL);
                mReportObject.transact(
                        ServiceTest.REBIND_CODE, data, null, 0);
                data.recycle();
            } catch (RemoteException e) {
            }
        }
    }
}
