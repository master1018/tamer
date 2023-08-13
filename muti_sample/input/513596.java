public class PowerTest extends TestActivity
{
    private final static String TAG = "PowerTest";
    IPowerManager mPowerManager;
    int mPokeState = 0;
    IBinder mPokeToken = new Binder();
    Handler mHandler = new Handler();
    @Override
    protected String tag() {
        return TAG;
    }
    @Override
    protected Test[] tests() {
        mPowerManager = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
        return mTests;
    }
    private Test[] mTests = new Test[] {
        new Test("Cheek events don't poke") {
            public void run() {
                mPokeState |= LocalPowerManager.POKE_LOCK_IGNORE_CHEEK_EVENTS;
                try {
                    mPowerManager.setPokeLock(mPokeState, mPokeToken, TAG);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        },
        new Test("Cheek events poke") {
            public void run() {
                mPokeState &= ~LocalPowerManager.POKE_LOCK_IGNORE_CHEEK_EVENTS;
                try {
                    mPowerManager.setPokeLock(mPokeState, mPokeToken, TAG);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        },
        new Test("Touch and Cheek events don't poke") {
            public void run() {
                mPokeState |= LocalPowerManager.POKE_LOCK_IGNORE_TOUCH_AND_CHEEK_EVENTS;
                try {
                    mPowerManager.setPokeLock(mPokeState, mPokeToken, TAG);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        },
        new Test("Touch and Cheek events poke") {
            public void run() {
                mPokeState &= ~LocalPowerManager.POKE_LOCK_IGNORE_TOUCH_AND_CHEEK_EVENTS;
                try {
                    mPowerManager.setPokeLock(mPokeState, mPokeToken, TAG);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        },
        new Test("Short timeout") {
            public void run() {
                mPokeState &= ~LocalPowerManager.POKE_LOCK_TIMEOUT_MASK;
                mPokeState |= LocalPowerManager.POKE_LOCK_SHORT_TIMEOUT;
                try {
                    mPowerManager.setPokeLock(mPokeState, mPokeToken, TAG);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        },
        new Test("Medium timeout") {
            public void run() {
                mPokeState &= ~LocalPowerManager.POKE_LOCK_TIMEOUT_MASK;
                mPokeState |= LocalPowerManager.POKE_LOCK_MEDIUM_TIMEOUT;
                try {
                    mPowerManager.setPokeLock(mPokeState, mPokeToken, TAG);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        },
        new Test("Normal timeout") {
            public void run() {
                mPokeState &= ~LocalPowerManager.POKE_LOCK_TIMEOUT_MASK;
                try {
                    mPowerManager.setPokeLock(mPokeState, mPokeToken, TAG);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        },
        new Test("Illegal timeout") {
            public void run() {
                mPokeState |= LocalPowerManager.POKE_LOCK_SHORT_TIMEOUT
                        | LocalPowerManager.POKE_LOCK_MEDIUM_TIMEOUT;
                try {
                    mPowerManager.setPokeLock(mPokeState, mPokeToken, TAG);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        },
    };
}
