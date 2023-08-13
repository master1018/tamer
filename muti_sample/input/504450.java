class SensorService extends ISensorService.Stub {
    static final String TAG = SensorService.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final boolean localLOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private static final int SENSOR_DISABLE = -1;
    private int mCurrentDelay = 0;
    final IBatteryStats mBatteryStats = BatteryStatsService.getService();
    private final class Listener implements IBinder.DeathRecipient {
        final IBinder mToken;
        final int mUid;
        int mSensors = 0;
        int mDelay = 0x7FFFFFFF;
        Listener(IBinder token, int uid) {
            mToken = token;
            mUid = uid;
        }
        void addSensor(int sensor, int delay) {
            mSensors |= (1<<sensor);
            if (delay < mDelay)
            	mDelay = delay;
        }
        void removeSensor(int sensor) {
            mSensors &= ~(1<<sensor);
        }
        boolean hasSensor(int sensor) {
            return ((mSensors & (1<<sensor)) != 0);
        }
        public void binderDied() {
            if (localLOGV) Slog.d(TAG, "sensor listener died");
            synchronized(mListeners) {
                mListeners.remove(this);
                mToken.unlinkToDeath(this, 0);
                for (int sensor=0 ; sensor<32 && mSensors!=0 ; sensor++) {
                    if (hasSensor(sensor)) {
                        removeSensor(sensor);
                        deactivateIfUnusedLocked(sensor);
                        try {
                            mBatteryStats.noteStopSensor(mUid, sensor);
                        } catch (RemoteException e) {
                        }
                    }
                }
                if (mListeners.size() == 0) {
                    _sensors_control_wake();
                    _sensors_control_close();
                } else {
                }
                mListeners.notify();
            }
        }
    }
    @SuppressWarnings("unused")
    public SensorService(Context context) {
        if (localLOGV) Slog.d(TAG, "SensorService startup");
        _sensors_control_init();
    }
    public Bundle getDataChannel() throws RemoteException {
        synchronized(mListeners) {
            return _sensors_control_open();
        }
    }
    public boolean enableSensor(IBinder binder, String name, int sensor, int enable)
            throws RemoteException {
        if (localLOGV) Slog.d(TAG, "enableSensor " + name + "(#" + sensor + ") " + enable);
        if (binder == null) {
            Slog.e(TAG, "listener is null (sensor=" + name + ", id=" + sensor + ")");
            return false;
        }
        if (enable < 0 && (enable != SENSOR_DISABLE)) {
            Slog.e(TAG, "invalid enable parameter (enable=" + enable +
                    ", sensor=" + name + ", id=" + sensor + ")");
            return false;
        }
        boolean res;
        int uid = Binder.getCallingUid();
        synchronized(mListeners) {
            res = enableSensorInternalLocked(binder, uid, name, sensor, enable);
            if (res == true) {
                long identity = Binder.clearCallingIdentity();
                if (enable == SENSOR_DISABLE) {
                    mBatteryStats.noteStopSensor(uid, sensor);
                } else {
                    mBatteryStats.noteStartSensor(uid, sensor);
                }
                Binder.restoreCallingIdentity(identity);
            }
        }
        return res;
    }
    private boolean enableSensorInternalLocked(IBinder binder, int uid,
            String name, int sensor, int enable) throws RemoteException {
        Listener l = null;
        for (Listener listener : mListeners) {
            if (binder == listener.mToken) {
                l = listener;
                break;
            }
        }
        if (enable != SENSOR_DISABLE) {
            if (_sensors_control_activate(sensor, true) == false) {
                Slog.w(TAG, "could not enable sensor " + sensor);
                return false;
            }
            if (l == null) {
                l = new Listener(binder, uid);
                binder.linkToDeath(l, 0);
                mListeners.add(l);
                mListeners.notify();
            }
            l.addSensor(sensor, enable);
        } else {
            if (l == null) {
                Slog.w(TAG, "listener with binder " + binder +
                        ", doesn't exist (sensor=" + name +
                        ", id=" + sensor + ")");
                return false;
            }
            l.removeSensor(sensor);
            deactivateIfUnusedLocked(sensor);
            if (l.mSensors == 0) {
                binder.unlinkToDeath(l, 0);
                mListeners.remove(l);
                if (mListeners.size() == 0) {
                    _sensors_control_wake();
                    _sensors_control_close();
                }
                mListeners.notify();
            }
        }
        int minDelay = 0x7FFFFFFF;
        for (Listener listener : mListeners) {
            if (listener.mDelay < minDelay)
                minDelay = listener.mDelay;
        }
        if (minDelay != 0x7FFFFFFF) {
            mCurrentDelay = minDelay;
            _sensors_control_set_delay(minDelay);
        }
        return true;
    }
    private void deactivateIfUnusedLocked(int sensor) {
        int size = mListeners.size();
        for (int i=0 ; i<size ; i++) {
            if (mListeners.get(i).hasSensor(sensor)) {
                return;
            }
        }
        if (_sensors_control_activate(sensor, false) == false) {
            Slog.w(TAG, "could not disable sensor " + sensor);
        }
    }
    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (mListeners) {
            Printer pr = new PrintWriterPrinter(pw);
            int c = 0;
            pr.println(mListeners.size() + " listener(s), delay=" + mCurrentDelay + " ms");
            for (Listener l : mListeners) {
                pr.println("listener[" + c + "] " +
                        "sensors=0x" + Integer.toString(l.mSensors, 16) +
                        ", uid=" + l.mUid +
                        ", delay=" +
                        l.mDelay + " ms");
                c++;
            }
        }
    }
    private ArrayList<Listener> mListeners = new ArrayList<Listener>();
    private static native int _sensors_control_init();
    private static native Bundle _sensors_control_open();
    private static native int _sensors_control_close();
    private static native boolean _sensors_control_activate(int sensor, boolean activate);
    private static native int _sensors_control_set_delay(int ms);
    private static native int _sensors_control_wake();
}
