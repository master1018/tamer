public class MonkeySourceRandom implements MonkeyEventSource {
    private static final int[] NAV_KEYS = {
        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT,
    };
    private static final int[] MAJOR_NAV_KEYS = {
        KeyEvent.KEYCODE_MENU, 
        KeyEvent.KEYCODE_DPAD_CENTER,
    };
    private static final int[] SYS_KEYS = {
        KeyEvent.KEYCODE_HOME, KeyEvent.KEYCODE_BACK,
        KeyEvent.KEYCODE_CALL, KeyEvent.KEYCODE_ENDCALL,
        KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_MUTE,
    };
    private static final String[] KEY_NAMES = {
        "KEYCODE_UNKNOWN",
        "KEYCODE_SOFT_LEFT",
        "KEYCODE_SOFT_RIGHT",
        "KEYCODE_HOME",
        "KEYCODE_BACK",
        "KEYCODE_CALL",
        "KEYCODE_ENDCALL",
        "KEYCODE_0",
        "KEYCODE_1",
        "KEYCODE_2",
        "KEYCODE_3",
        "KEYCODE_4",
        "KEYCODE_5",
        "KEYCODE_6",
        "KEYCODE_7",
        "KEYCODE_8",
        "KEYCODE_9",
        "KEYCODE_STAR",
        "KEYCODE_POUND",
        "KEYCODE_DPAD_UP",
        "KEYCODE_DPAD_DOWN",
        "KEYCODE_DPAD_LEFT",
        "KEYCODE_DPAD_RIGHT",
        "KEYCODE_DPAD_CENTER",
        "KEYCODE_VOLUME_UP",
        "KEYCODE_VOLUME_DOWN",
        "KEYCODE_POWER",
        "KEYCODE_CAMERA",
        "KEYCODE_CLEAR",
        "KEYCODE_A",
        "KEYCODE_B",
        "KEYCODE_C",
        "KEYCODE_D",
        "KEYCODE_E",
        "KEYCODE_F",
        "KEYCODE_G",
        "KEYCODE_H",
        "KEYCODE_I",
        "KEYCODE_J",
        "KEYCODE_K",
        "KEYCODE_L",
        "KEYCODE_M",
        "KEYCODE_N",
        "KEYCODE_O",
        "KEYCODE_P",
        "KEYCODE_Q",
        "KEYCODE_R",
        "KEYCODE_S",
        "KEYCODE_T",
        "KEYCODE_U",
        "KEYCODE_V",
        "KEYCODE_W",
        "KEYCODE_X",
        "KEYCODE_Y",
        "KEYCODE_Z",
        "KEYCODE_COMMA",
        "KEYCODE_PERIOD",
        "KEYCODE_ALT_LEFT",
        "KEYCODE_ALT_RIGHT",
        "KEYCODE_SHIFT_LEFT",
        "KEYCODE_SHIFT_RIGHT",
        "KEYCODE_TAB",
        "KEYCODE_SPACE",
        "KEYCODE_SYM",
        "KEYCODE_EXPLORER",
        "KEYCODE_ENVELOPE",
        "KEYCODE_ENTER",
        "KEYCODE_DEL",
        "KEYCODE_GRAVE",
        "KEYCODE_MINUS",
        "KEYCODE_EQUALS",
        "KEYCODE_LEFT_BRACKET",
        "KEYCODE_RIGHT_BRACKET",
        "KEYCODE_BACKSLASH",
        "KEYCODE_SEMICOLON",
        "KEYCODE_APOSTROPHE",
        "KEYCODE_SLASH",
        "KEYCODE_AT",
        "KEYCODE_NUM",
        "KEYCODE_HEADSETHOOK",
        "KEYCODE_FOCUS",
        "KEYCODE_PLUS",
        "KEYCODE_MENU",
        "KEYCODE_NOTIFICATION",
        "KEYCODE_SEARCH",
        "KEYCODE_PLAYPAUSE",
        "KEYCODE_STOP",
        "KEYCODE_NEXTSONG",
        "KEYCODE_PREVIOUSSONG",
        "KEYCODE_REWIND",
        "KEYCODE_FORWARD",
        "KEYCODE_MUTE",
        "TAG_LAST_KEYCODE"      
    };
    public static final int FACTOR_TOUCH        = 0;
    public static final int FACTOR_MOTION       = 1;
    public static final int FACTOR_TRACKBALL    = 2;
    public static final int FACTOR_NAV          = 3;
    public static final int FACTOR_MAJORNAV     = 4;
    public static final int FACTOR_SYSOPS       = 5;
    public static final int FACTOR_APPSWITCH    = 6;
    public static final int FACTOR_FLIP         = 7;
    public static final int FACTOR_ANYTHING     = 8;
    public static final int FACTORZ_COUNT       = 9;    
    private float[] mFactors = new float[FACTORZ_COUNT];
    private ArrayList<ComponentName> mMainApps;
    private int mEventCount = 0;  
    private MonkeyEventQueue mQ;
    private Random mRandom;
    private int mVerbose = 0;
    private long mThrottle = 0;
    private boolean mKeyboardOpen = false;
    public static String getLastKeyName() {
        return KEY_NAMES[KeyEvent.getMaxKeyCode() + 1];
    }
    public static String getKeyName(int keycode) {
        return KEY_NAMES[keycode];
    }
    public static int getKeyCode(String keyName) {
        for (int x = 0; x < KEY_NAMES.length; x++) {
            if (KEY_NAMES[x].equals(keyName)) {
                return x;
            }
        }
        return -1;
    }
    public MonkeySourceRandom(Random random, ArrayList<ComponentName> MainApps,
            long throttle, boolean randomizeThrottle) {
        mFactors[FACTOR_TOUCH] = 15.0f;
        mFactors[FACTOR_MOTION] = 10.0f;
        mFactors[FACTOR_TRACKBALL] = 15.0f;
        mFactors[FACTOR_NAV] = 25.0f;
        mFactors[FACTOR_MAJORNAV] = 15.0f;
        mFactors[FACTOR_SYSOPS] = 2.0f;
        mFactors[FACTOR_APPSWITCH] = 2.0f;
        mFactors[FACTOR_FLIP] = 1.0f;
        mFactors[FACTOR_ANYTHING] = 15.0f;
        mRandom = random;
        mMainApps = MainApps;
        mQ = new MonkeyEventQueue(random, throttle, randomizeThrottle);
    }
    private boolean adjustEventFactors() {
        float userSum = 0.0f;
        float defaultSum = 0.0f;
        int defaultCount = 0;
        for (int i = 0; i < FACTORZ_COUNT; ++i) {
            if (mFactors[i] <= 0.0f) {   
                userSum -= mFactors[i];
            } else {
                defaultSum += mFactors[i];
                ++defaultCount;
            }
        }
        if (userSum > 100.0f) {
            System.err.println("** Event weights > 100%");
            return false;
        }
        if (defaultCount == 0 && (userSum < 99.9f || userSum > 100.1f)) {
            System.err.println("** Event weights != 100%");
            return false;
        }
        float defaultsTarget = (100.0f - userSum);
        float defaultsAdjustment = defaultsTarget / defaultSum;
        for (int i = 0; i < FACTORZ_COUNT; ++i) {
            if (mFactors[i] <= 0.0f) {   
                mFactors[i] = -mFactors[i];
            } else {
                mFactors[i] *= defaultsAdjustment;
            }
        }
        if (mVerbose > 0) {
            System.out.println("
            for (int i = 0; i < FACTORZ_COUNT; ++i) {
                System.out.println("
            }
        }
        float sum = 0.0f;
        for (int i = 0; i < FACTORZ_COUNT; ++i) {
            sum += mFactors[i] / 100.0f;
            mFactors[i] = sum;
        }
        return true;
    }
    public void setFactors(float factors[]) {
        int c = FACTORZ_COUNT;
        if (factors.length < c) {
            c = factors.length;
        }
        for (int i = 0; i < c; i++)
            mFactors[i] = factors[i];
    }
    public void setFactors(int index, float v) {
        mFactors[index] = v;
    }
    private void generateMotionEvent(Random random, boolean motionEvent){
        Display display = WindowManagerImpl.getDefault().getDefaultDisplay();
        float x = Math.abs(random.nextInt() % display.getWidth());
        float y = Math.abs(random.nextInt() % display.getHeight());
        long downAt = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        if (downAt == -1) {
            downAt = eventTime;
        }
        MonkeyMotionEvent e = new MonkeyMotionEvent(MonkeyEvent.EVENT_TYPE_POINTER,
                downAt, MotionEvent.ACTION_DOWN, x, y, 0);
        e.setIntermediateNote(false);
        mQ.addLast(e);
        if (motionEvent) {
            int count = random.nextInt(10);
            for (int i = 0; i < count; i++) {
                x = (x + (random.nextInt() % 10)) % display.getWidth();
                y = (y + (random.nextInt() % 10)) % display.getHeight();
                e = new MonkeyMotionEvent(MonkeyEvent.EVENT_TYPE_POINTER,
                        downAt, MotionEvent.ACTION_MOVE, x, y, 0);
                e.setIntermediateNote(true);
                mQ.addLast(e);
            }
        }
        e = new MonkeyMotionEvent(MonkeyEvent.EVENT_TYPE_POINTER,
                downAt, MotionEvent.ACTION_UP, x, y, 0);
        e.setIntermediateNote(false);
        mQ.addLast(e);
    }
    private void generateTrackballEvent(Random random) {
        Display display = WindowManagerImpl.getDefault().getDefaultDisplay();
        boolean drop = false;
        int count = random.nextInt(10);
        MonkeyMotionEvent e;
        for (int i = 0; i < 10; ++i) {
            int dX = random.nextInt(10) - 5;
            int dY = random.nextInt(10) - 5;
            e = new MonkeyMotionEvent(MonkeyEvent.EVENT_TYPE_TRACKBALL, -1,
                    MotionEvent.ACTION_MOVE, dX, dY, 0);
            e.setIntermediateNote(i > 0);
            mQ.addLast(e);
        }
        if (0 == random.nextInt(10)) {
            long downAt = SystemClock.uptimeMillis();
            e = new MonkeyMotionEvent(MonkeyEvent.EVENT_TYPE_TRACKBALL, downAt,
                    MotionEvent.ACTION_DOWN, 0, 0, 0);
            e.setIntermediateNote(true);
            mQ.addLast(e);
            e = new MonkeyMotionEvent(MonkeyEvent.EVENT_TYPE_TRACKBALL, downAt,
                    MotionEvent.ACTION_UP, 0, 0, 0);
            e.setIntermediateNote(false);
            mQ.addLast(e);
        }
    }
    private void generateEvents() {
        float cls = mRandom.nextFloat();
        int lastKey = 0;
        boolean touchEvent = cls < mFactors[FACTOR_TOUCH];
        boolean motionEvent = !touchEvent && (cls < mFactors[FACTOR_MOTION]);
        if (touchEvent || motionEvent) {
            generateMotionEvent(mRandom, motionEvent);
            return;
        }
        if (cls < mFactors[FACTOR_TRACKBALL]) {
            generateTrackballEvent(mRandom);
            return;
        }
        if (cls < mFactors[FACTOR_NAV]) {
            lastKey = NAV_KEYS[mRandom.nextInt(NAV_KEYS.length)];
        } else if (cls < mFactors[FACTOR_MAJORNAV]) {
            lastKey = MAJOR_NAV_KEYS[mRandom.nextInt(MAJOR_NAV_KEYS.length)];
        } else if (cls < mFactors[FACTOR_SYSOPS]) {
            lastKey = SYS_KEYS[mRandom.nextInt(SYS_KEYS.length)];
        } else if (cls < mFactors[FACTOR_APPSWITCH]) {
            MonkeyActivityEvent e = new MonkeyActivityEvent(mMainApps.get(
                    mRandom.nextInt(mMainApps.size())));
            mQ.addLast(e);
            return;
        } else if (cls < mFactors[FACTOR_FLIP]) {
            MonkeyFlipEvent e = new MonkeyFlipEvent(mKeyboardOpen);
            mKeyboardOpen = !mKeyboardOpen;
            mQ.addLast(e);
            return;
        } else {
            lastKey = 1 + mRandom.nextInt(KeyEvent.getMaxKeyCode() - 1);
        }
        MonkeyKeyEvent e = new MonkeyKeyEvent(KeyEvent.ACTION_DOWN, lastKey);
        mQ.addLast(e);
        e = new MonkeyKeyEvent(KeyEvent.ACTION_UP, lastKey);
        mQ.addLast(e);
    }
    public boolean validate() {
        return adjustEventFactors();
    }
    public void setVerbose(int verbose) {
        mVerbose = verbose;
    }
    public void generateActivity() {
        MonkeyActivityEvent e = new MonkeyActivityEvent(mMainApps.get(
                mRandom.nextInt(mMainApps.size())));
        mQ.addLast(e);
    }
    public MonkeyEvent getNextEvent() {
        if (mQ.isEmpty()) {
            generateEvents();
        }
        mEventCount++;
        MonkeyEvent e = mQ.getFirst();
        mQ.removeFirst();
        return e;
    }
}
