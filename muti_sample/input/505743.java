public class VibratorService extends IVibratorService.Stub {
    private static final String TAG = "VibratorService";
    private final LinkedList<Vibration> mVibrations;
    private Vibration mCurrentVibration;
    private class Vibration implements IBinder.DeathRecipient {
        private final IBinder mToken;
        private final long    mTimeout;
        private final long    mStartTime;
        private final long[]  mPattern;
        private final int     mRepeat;
        Vibration(IBinder token, long millis) {
            this(token, millis, null, 0);
        }
        Vibration(IBinder token, long[] pattern, int repeat) {
            this(token, 0, pattern, repeat);
        }
        private Vibration(IBinder token, long millis, long[] pattern,
                int repeat) {
            mToken = token;
            mTimeout = millis;
            mStartTime = SystemClock.uptimeMillis();
            mPattern = pattern;
            mRepeat = repeat;
        }
        public void binderDied() {
            synchronized (mVibrations) {
                mVibrations.remove(this);
                if (this == mCurrentVibration) {
                    doCancelVibrateLocked();
                    startNextVibrationLocked();
                }
            }
        }
        public boolean hasLongerTimeout(long millis) {
            if (mTimeout == 0) {
                return false;
            }
            if ((mStartTime + mTimeout)
                    < (SystemClock.uptimeMillis() + millis)) {
                return false;
            }
            return true;
        }
    }
    VibratorService(Context context) {
        vibratorOff();
        mContext = context;
        PowerManager pm = (PowerManager)context.getSystemService(
                Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.setReferenceCounted(true);
        mVibrations = new LinkedList<Vibration>();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        context.registerReceiver(mIntentReceiver, filter);
    }
    public void vibrate(long milliseconds, IBinder token) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.VIBRATE)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Requires VIBRATE permission");
        }
        if (milliseconds <= 0 || (mCurrentVibration != null
                && mCurrentVibration.hasLongerTimeout(milliseconds))) {
            return;
        }
        Vibration vib = new Vibration(token, milliseconds);
        synchronized (mVibrations) {
            removeVibrationLocked(token);
            doCancelVibrateLocked();
            mCurrentVibration = vib;
            startVibrationLocked(vib);
        }
    }
    private boolean isAll0(long[] pattern) {
        int N = pattern.length;
        for (int i = 0; i < N; i++) {
            if (pattern[i] != 0) {
                return false;
            }
        }
        return true;
    }
    public void vibratePattern(long[] pattern, int repeat, IBinder token) {
        if (mContext.checkCallingOrSelfPermission(android.Manifest.permission.VIBRATE)
                != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Requires VIBRATE permission");
        }
        long identity = Binder.clearCallingIdentity();
        try {
            if (false) {
                String s = "";
                int N = pattern.length;
                for (int i=0; i<N; i++) {
                    s += " " + pattern[i];
                }
                Slog.i(TAG, "vibrating with pattern: " + s);
            }
            if (pattern == null || pattern.length == 0
                    || isAll0(pattern)
                    || repeat >= pattern.length || token == null) {
                return;
            }
            Vibration vib = new Vibration(token, pattern, repeat);
            try {
                token.linkToDeath(vib, 0);
            } catch (RemoteException e) {
                return;
            }
            synchronized (mVibrations) {
                removeVibrationLocked(token);
                doCancelVibrateLocked();
                if (repeat >= 0) {
                    mVibrations.addFirst(vib);
                    startNextVibrationLocked();
                } else {
                    mCurrentVibration = vib;
                    startVibrationLocked(vib);
                }
            }
        }
        finally {
            Binder.restoreCallingIdentity(identity);
        }
    }
    public void cancelVibrate(IBinder token) {
        mContext.enforceCallingOrSelfPermission(
                android.Manifest.permission.VIBRATE,
                "cancelVibrate");
        long identity = Binder.clearCallingIdentity();
        try {
            synchronized (mVibrations) {
                final Vibration vib = removeVibrationLocked(token);
                if (vib == mCurrentVibration) {
                    doCancelVibrateLocked();
                    startNextVibrationLocked();
                }
            }
        }
        finally {
            Binder.restoreCallingIdentity(identity);
        }
    }
    private final Runnable mVibrationRunnable = new Runnable() {
        public void run() {
            synchronized (mVibrations) {
                doCancelVibrateLocked();
                startNextVibrationLocked();
            }
        }
    };
    private void doCancelVibrateLocked() {
        if (mThread != null) {
            synchronized (mThread) {
                mThread.mDone = true;
                mThread.notify();
            }
            mThread = null;
        }
        vibratorOff();
        mH.removeCallbacks(mVibrationRunnable);
    }
    private void startNextVibrationLocked() {
        if (mVibrations.size() <= 0) {
            return;
        }
        mCurrentVibration = mVibrations.getFirst();
        startVibrationLocked(mCurrentVibration);
    }
    private void startVibrationLocked(final Vibration vib) {
        if (vib.mTimeout != 0) {
            vibratorOn(vib.mTimeout);
            mH.postDelayed(mVibrationRunnable, vib.mTimeout);
        } else {
            mThread = new VibrateThread(vib);
            mThread.start();
        }
    }
    private Vibration removeVibrationLocked(IBinder token) {
        ListIterator<Vibration> iter = mVibrations.listIterator(0);
        while (iter.hasNext()) {
            Vibration vib = iter.next();
            if (vib.mToken == token) {
                iter.remove();
                return vib;
            }
        }
        if (mCurrentVibration != null && mCurrentVibration.mToken == token) {
            return mCurrentVibration;
        }
        return null;
    }
    private class VibrateThread extends Thread {
        final Vibration mVibration;
        boolean mDone;
        VibrateThread(Vibration vib) {
            mVibration = vib;
            mWakeLock.acquire();
        }
        private void delay(long duration) {
            if (duration > 0) {
                long bedtime = SystemClock.uptimeMillis();
                do {
                    try {
                        this.wait(duration);
                    }
                    catch (InterruptedException e) {
                    }
                    if (mDone) {
                        break;
                    }
                    duration = duration
                            - SystemClock.uptimeMillis() - bedtime;
                } while (duration > 0);
            }
        }
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_DISPLAY);
            synchronized (this) {
                int index = 0;
                long[] pattern = mVibration.mPattern;
                int len = pattern.length;
                int repeat = mVibration.mRepeat;
                long duration = 0;
                while (!mDone) {
                    if (index < len) {
                        duration += pattern[index++];
                    }
                    delay(duration);
                    if (mDone) {
                        break;
                    }
                    if (index < len) {
                        duration = pattern[index++];
                        if (duration > 0) {
                            VibratorService.this.vibratorOn(duration);
                        }
                    } else {
                        if (repeat < 0) {
                            break;
                        } else {
                            index = repeat;
                            duration = 0;
                        }
                    }
                }
                mWakeLock.release();
            }
            synchronized (mVibrations) {
                if (mThread == this) {
                    mThread = null;
                }
                if (!mDone) {
                    mVibrations.remove(mVibration);
                    startNextVibrationLocked();
                }
            }
        }
    };
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                synchronized (mVibrations) {
                    doCancelVibrateLocked();
                    mVibrations.clear();
                }
            }
        }
    };
    private Handler mH = new Handler();
    private final Context mContext;
    private final PowerManager.WakeLock mWakeLock;
    volatile VibrateThread mThread;
    native static void vibratorOn(long milliseconds);
    native static void vibratorOff();
}
