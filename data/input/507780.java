public final class BatteryStatsService extends IBatteryStats.Stub {
    static IBatteryStats sService;
    final BatteryStatsImpl mStats;
    Context mContext;
    BatteryStatsService(String filename) {
        mStats = new BatteryStatsImpl(filename);
    }
    public void publish(Context context) {
        mContext = context;
        ServiceManager.addService("batteryinfo", asBinder());
        mStats.setNumSpeedSteps(new PowerProfile(mContext).getNumSpeedSteps());
        mStats.setRadioScanningTimeout(mContext.getResources().getInteger(
                com.android.internal.R.integer.config_radioScanningTimeout)
                * 1000L);
    }
    public void shutdown() {
        Slog.w("BatteryStats", "Writing battery stats before shutdown...");
        synchronized (mStats) {
            mStats.writeLocked();
        }
    }
    public static IBatteryStats getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService("batteryinfo");
        sService = asInterface(b);
        return sService;
    }
    public BatteryStatsImpl getActiveStatistics() {
        return mStats;
    }
    public byte[] getStatistics() {
        mContext.enforceCallingPermission(
                android.Manifest.permission.BATTERY_STATS, null);
        Parcel out = Parcel.obtain();
        mStats.writeToParcel(out, 0);
        byte[] data = out.marshall();
        out.recycle();
        return data;
    }
    public void noteStartWakelock(int uid, String name, int type) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.getUidStatsLocked(uid).noteStartWakeLocked(name, type);
        }
    }
    public void noteStopWakelock(int uid, String name, int type) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.getUidStatsLocked(uid).noteStopWakeLocked(name, type);
        }
    }
    public void noteStartSensor(int uid, int sensor) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.getUidStatsLocked(uid).noteStartSensor(sensor);
        }
    }
    public void noteStopSensor(int uid, int sensor) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.getUidStatsLocked(uid).noteStopSensor(sensor);
        }
    }
    public void noteStartGps(int uid) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteStartGps(uid);
        }
    }
    public void noteStopGps(int uid) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteStopGps(uid);
        }
    }
    public void noteScreenOn() {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteScreenOnLocked();
        }
    }
    public void noteScreenBrightness(int brightness) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteScreenBrightnessLocked(brightness);
        }
    }
    public void noteScreenOff() {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteScreenOffLocked();
        }
    }
    public void noteInputEvent() {
        enforceCallingPermission();
        mStats.noteInputEventAtomic();
    }
    public void noteUserActivity(int uid, int event) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteUserActivityLocked(uid, event);
        }
    }
    public void notePhoneOn() {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.notePhoneOnLocked();
        }
    }
    public void notePhoneOff() {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.notePhoneOffLocked();
        }
    }
    public void notePhoneSignalStrength(SignalStrength signalStrength) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.notePhoneSignalStrengthLocked(signalStrength);
        }
    }
    public void notePhoneDataConnectionState(int dataType, boolean hasData) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.notePhoneDataConnectionStateLocked(dataType, hasData);
        }
    }
    public void notePhoneState(int state) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.notePhoneStateLocked(state);
        }
    }
    public void noteWifiOn(int uid) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteWifiOnLocked(uid);
        }
    }
    public void noteWifiOff(int uid) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteWifiOffLocked(uid);
        }
    }
    public void noteStartAudio(int uid) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteAudioOnLocked(uid);
        }
    }
    public void noteStopAudio(int uid) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteAudioOffLocked(uid);
        }
    }
    public void noteStartVideo(int uid) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteVideoOnLocked(uid);
        }
    }
    public void noteStopVideo(int uid) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteVideoOffLocked(uid);
        }
    }
    public void noteWifiRunning() {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteWifiRunningLocked();
        }
    }
    public void noteWifiStopped() {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteWifiStoppedLocked();
        }
    }
    public void noteBluetoothOn() {
        enforceCallingPermission();
        BluetoothHeadset headset = new BluetoothHeadset(mContext, null);
        synchronized (mStats) {
            mStats.noteBluetoothOnLocked();
            mStats.setBtHeadset(headset);
        }
    }
    public void noteBluetoothOff() {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteBluetoothOffLocked();
        }
    }
    public void noteFullWifiLockAcquired(int uid) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteFullWifiLockAcquiredLocked(uid);
        }
    }
    public void noteFullWifiLockReleased(int uid) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteFullWifiLockReleasedLocked(uid);
        }
    }
    public void noteScanWifiLockAcquired(int uid) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteScanWifiLockAcquiredLocked(uid);
        }
    }
    public void noteScanWifiLockReleased(int uid) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteScanWifiLockReleasedLocked(uid);
        }
    }
    public void noteWifiMulticastEnabled(int uid) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteWifiMulticastEnabledLocked(uid);
        }
    }
    public void noteWifiMulticastDisabled(int uid) {
        enforceCallingPermission();
        synchronized (mStats) {
            mStats.noteWifiMulticastDisabledLocked(uid);
        }
    }
    public boolean isOnBattery() {
        return mStats.isOnBattery();
    }
    public void setOnBattery(boolean onBattery, int level) {
        enforceCallingPermission();
        mStats.setOnBattery(onBattery, level);
    }
    public void recordCurrentLevel(int level) {
        enforceCallingPermission();
        mStats.recordCurrentLevel(level);
    }
    public long getAwakeTimeBattery() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.BATTERY_STATS, null);
        return mStats.getAwakeTimeBattery();
    }
    public long getAwakeTimePlugged() {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.BATTERY_STATS, null);
        return mStats.getAwakeTimePlugged();
    }
    public void enforceCallingPermission() {
        if (Binder.getCallingPid() == Process.myPid()) {
            return;
        }
        mContext.enforcePermission(android.Manifest.permission.UPDATE_DEVICE_STATS,
                Binder.getCallingPid(), Binder.getCallingUid(), null);
    }
    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (mStats) {
            boolean isCheckin = false;
            if (args != null) {
                for (String arg : args) {
                    if ("--checkin".equals(arg)) {
                        isCheckin = true;
                        break;
                    }
                }
            }
            if (isCheckin) mStats.dumpCheckinLocked(pw, args);
            else mStats.dumpLocked(pw);
        }
    }
}
