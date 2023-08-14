public class LockPatternKeyguardViewTest extends AndroidTestCase {
    private MockUpdateMonitor mUpdateMonitor;
    private LockPatternUtils mLockPatternUtils;
    private TestableLockPatternKeyguardView mLPKV;
    private MockKeyguardCallback mKeyguardViewCallback;
    private static class MockUpdateMonitor extends KeyguardUpdateMonitor {
        public IccCard.State simState = IccCard.State.READY;
        private MockUpdateMonitor(Context context) {
            super(context);
        }
        @Override
        public IccCard.State getSimState() {
            return simState;
        }
    }
    private static class MockLockPatternUtils extends LockPatternUtils {
        boolean isLockPatternEnabled = true;
        public boolean isPermanentlyLocked = false;
        public MockLockPatternUtils() {
            super(null);
        }
        @Override
        public boolean isLockPatternEnabled() {
            return isLockPatternEnabled;
        }
        @Override
        public void setLockPatternEnabled(boolean lockPatternEnabled) {
            isLockPatternEnabled = lockPatternEnabled;
        }
        @Override
        public boolean isPermanentlyLocked() {
            return isPermanentlyLocked;
        }
        public void setPermanentlyLocked(boolean permanentlyLocked) {
            isPermanentlyLocked = permanentlyLocked;
        }
    }
    private static class MockKeyguardScreen extends View implements KeyguardScreen {
        private int mOnPauseCount = 0;
        private int mOnResumeCount = 0;
        private int mCleanupCount = 0;
        private MockKeyguardScreen(Context context) {
            super(context);
            setFocusable(true);
        }
        public boolean needsInput() {
            return false;
        }
        public void onPause() {
            mOnPauseCount++;
        }
        public void onResume() {
            mOnResumeCount++;
        }
        public void cleanUp() {
            mCleanupCount++;
        }
        public int getOnPauseCount() {
            return mOnPauseCount;
        }
        public int getOnResumeCount() {
            return mOnResumeCount;
        }
        public int getCleanupCount() {
            return mCleanupCount;
        }
    }
    private static class TestableLockPatternKeyguardView extends LockPatternKeyguardView {
        private List<MockKeyguardScreen> mInjectedLockScreens;
        private List<MockKeyguardScreen> mInjectedUnlockScreens;
        private TestableLockPatternKeyguardView(Context context, KeyguardUpdateMonitor updateMonitor,
                LockPatternUtils lockPatternUtils, KeyguardWindowController controller) {
            super(context, updateMonitor, lockPatternUtils, controller);
        }
        @Override
        View createLockScreen() {
            final MockKeyguardScreen newView = new MockKeyguardScreen(getContext());
            if (mInjectedLockScreens == null) mInjectedLockScreens = Lists.newArrayList();
            mInjectedLockScreens.add(newView);
            return newView;
        }
        @Override
        View createUnlockScreenFor(UnlockMode unlockMode) {
            final MockKeyguardScreen newView = new MockKeyguardScreen(getContext());
            if (mInjectedUnlockScreens == null)  mInjectedUnlockScreens = Lists.newArrayList();
            mInjectedUnlockScreens.add(newView);
            return newView;
        }
        public List<MockKeyguardScreen> getInjectedLockScreens() {
            return mInjectedLockScreens;
        }
        public List<MockKeyguardScreen> getInjectedUnlockScreens() {
            return mInjectedUnlockScreens;
        }
    }
    private static class MockKeyguardCallback implements KeyguardViewCallback {
        private int mPokeWakelockCount = 0;
        private int mKeyguardDoneCount = 0;
        public void pokeWakelock() {
            mPokeWakelockCount++;
        }
        public void pokeWakelock(int millis) {
            mPokeWakelockCount++;
        }
        public void keyguardDone(boolean authenticated) {
            mKeyguardDoneCount++;
        }
        public void keyguardDoneDrawing() {
        }
        public int getPokeWakelockCount() {
            return mPokeWakelockCount;
        }
        public int getKeyguardDoneCount() {
            return mKeyguardDoneCount;
        }
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mUpdateMonitor = new MockUpdateMonitor(getContext());
        mLockPatternUtils = new MockLockPatternUtils();
        mLPKV = new TestableLockPatternKeyguardView(getContext(), mUpdateMonitor,
                mLockPatternUtils, new KeyguardWindowController() {
            public void setNeedsInput(boolean needsInput) {
            }
        });
        mKeyguardViewCallback = new MockKeyguardCallback();
        mLPKV.setCallback(mKeyguardViewCallback);
    }
    public void testStateAfterCreatedWhileScreenOff() {
        assertEquals(1, mLPKV.getInjectedLockScreens().size());
        assertEquals(1, mLPKV.getInjectedUnlockScreens().size());
        MockKeyguardScreen lockScreen = mLPKV.getInjectedLockScreens().get(0);
        MockKeyguardScreen unlockScreen = mLPKV.getInjectedUnlockScreens().get(0);
        assertEquals(0, lockScreen.getOnPauseCount());
        assertEquals(0, lockScreen.getOnResumeCount());
        assertEquals(0, lockScreen.getCleanupCount());
        assertEquals(0, unlockScreen.getOnPauseCount());
        assertEquals(0, unlockScreen.getOnResumeCount());
        assertEquals(0, unlockScreen.getCleanupCount());
        assertEquals(0, mKeyguardViewCallback.getPokeWakelockCount());
        assertEquals(0, mKeyguardViewCallback.getKeyguardDoneCount());
    }
    public void testWokenByNonMenuKey() {
        mLPKV.wakeWhenReadyTq(0);
        assertEquals(1, mKeyguardViewCallback.getPokeWakelockCount());
        assertEquals(1, mLPKV.getInjectedLockScreens().size());
        assertEquals(1, mLPKV.getInjectedUnlockScreens().size());
        MockKeyguardScreen lockScreen = mLPKV.getInjectedLockScreens().get(0);
        MockKeyguardScreen unlockScreen = mLPKV.getInjectedUnlockScreens().get(0);
        assertEquals(View.VISIBLE, lockScreen.getVisibility());
        assertEquals(View.GONE, unlockScreen.getVisibility());
        assertEquals(0, lockScreen.getOnPauseCount());
        assertEquals(0, lockScreen.getOnResumeCount());
        assertEquals(0, lockScreen.getCleanupCount());
        assertEquals(0, unlockScreen.getOnPauseCount());
        assertEquals(0, unlockScreen.getOnResumeCount());
        assertEquals(0, unlockScreen.getCleanupCount());
        mLPKV.onScreenTurnedOn();
        assertEquals(0, lockScreen.getOnPauseCount());
        assertEquals(1, lockScreen.getOnResumeCount());
        assertEquals(0, lockScreen.getCleanupCount());
        assertEquals(0, unlockScreen.getOnPauseCount());
        assertEquals(0, unlockScreen.getOnResumeCount());
        assertEquals(0, unlockScreen.getCleanupCount());
    }
    public void testWokenByMenuKeyWhenPatternSet() {
        assertEquals(true, mLockPatternUtils.isLockPatternEnabled());
        mLPKV.wakeWhenReadyTq(KeyEvent.KEYCODE_MENU);
        assertEquals(1, mKeyguardViewCallback.getPokeWakelockCount());
        assertEquals(1, mLPKV.getInjectedLockScreens().size());
        assertEquals(1, mLPKV.getInjectedUnlockScreens().size());
        MockKeyguardScreen lockScreen = mLPKV.getInjectedLockScreens().get(0);
        MockKeyguardScreen unlockScreen = mLPKV.getInjectedUnlockScreens().get(0);
        assertEquals(View.GONE, lockScreen.getVisibility());
        assertEquals(View.VISIBLE, unlockScreen.getVisibility());
    }
    public void testScreenRequestsRecreation() {
        mLPKV.wakeWhenReadyTq(0);
        mLPKV.onScreenTurnedOn();
        assertEquals(1, mLPKV.getInjectedLockScreens().size());
        assertEquals(1, mLPKV.getInjectedUnlockScreens().size());
        MockKeyguardScreen lockScreen = mLPKV.getInjectedLockScreens().get(0);
        assertEquals(0, lockScreen.getOnPauseCount());
        assertEquals(1, lockScreen.getOnResumeCount());
        mLPKV.mKeyguardScreenCallback.recreateMe(new Configuration());
        assertEquals(2, mLPKV.getInjectedLockScreens().size());
        assertEquals(2, mLPKV.getInjectedUnlockScreens().size());
        assertEquals(1, mLPKV.getInjectedLockScreens().get(0).getCleanupCount());
        assertEquals(1, mLPKV.getInjectedUnlockScreens().get(0).getCleanupCount());
        assertEquals(1, mLPKV.getInjectedLockScreens().get(0).getOnPauseCount());
        assertEquals(0, mLPKV.getInjectedUnlockScreens().get(0).getOnPauseCount());
        assertEquals(1, mLPKV.getInjectedLockScreens().get(1).getOnResumeCount());
        assertEquals(0, mLPKV.getInjectedUnlockScreens().get(1).getOnResumeCount());
    }
    public void testMenuDoesntGoToUnlockScreenOnWakeWhenPukLocked() {
        mUpdateMonitor.simState = IccCard.State.PUK_REQUIRED;
        mLPKV.wakeWhenReadyTq(KeyEvent.KEYCODE_MENU);
        assertEquals(1, mLPKV.getInjectedLockScreens().size());
        assertEquals(1, mLPKV.getInjectedUnlockScreens().size());
        MockKeyguardScreen lockScreen = mLPKV.getInjectedLockScreens().get(0);
        MockKeyguardScreen unlockScreen = mLPKV.getInjectedUnlockScreens().get(0);
        assertEquals(View.VISIBLE, lockScreen.getVisibility());
        assertEquals(View.GONE, unlockScreen.getVisibility());
    }
    public void testMenuGoesToLockScreenWhenDeviceNotSecure() {
        mLockPatternUtils.setLockPatternEnabled(false);
        mLPKV.wakeWhenReadyTq(KeyEvent.KEYCODE_MENU);
        assertEquals(1, mLPKV.getInjectedLockScreens().size());
        assertEquals(1, mLPKV.getInjectedUnlockScreens().size());
        MockKeyguardScreen lockScreen = mLPKV.getInjectedLockScreens().get(0);
        MockKeyguardScreen unlockScreen = mLPKV.getInjectedUnlockScreens().get(0);
        assertEquals(View.VISIBLE, lockScreen.getVisibility());
        assertEquals(View.GONE, unlockScreen.getVisibility());
    }
}
