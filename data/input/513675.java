public class SkbContainer extends RelativeLayout implements OnTouchListener {
    private static final int Y_BIAS_CORRECTION = -10;
    private static final int MOVE_TOLERANCE = 6;
    private static boolean POPUPWINDOW_FOR_PRESSED_UI = false;
    private int mSkbLayout = 0;
    private InputMethodService mService;
    private InputModeSwitcher mInputModeSwitcher;
    private GestureDetector mGestureDetector;
    private Environment mEnvironment;
    private ViewFlipper mSkbFlipper;
    private BalloonHint mBalloonPopup;
    private BalloonHint mBalloonOnKey = null;
    private SoftKeyboardView mMajorView;
    private boolean mLastCandidatesShowing;
    private boolean mPopupSkbShow = false;
    private boolean mPopupSkbNoResponse = false;
    private PopupWindow mPopupSkb;
    private SoftKeyboardView mPopupSkbView;
    private int mPopupX;
    private int mPopupY;
    private volatile boolean mWaitForTouchUp = false;
    private volatile boolean mDiscardEvent = false;
    private int mYBiasCorrection = 0;
    private int mXLast;
    private int mYLast;
    private SoftKeyboardView mSkv;
    private int mSkvPosInContainer[] = new int[2];
    private SoftKey mSoftKeyDown = null;
    private LongPressTimer mLongPressTimer;
    private int mXyPosTmp[] = new int[2];
    public SkbContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mEnvironment = Environment.getInstance();
        mLongPressTimer = new LongPressTimer(this);
        if ("1".equals(SystemProperties.get("ro.kernel.qemu"))) {
            mYBiasCorrection = 0;
        } else {
            mYBiasCorrection = Y_BIAS_CORRECTION;
        }
        mBalloonPopup = new BalloonHint(context, this, MeasureSpec.AT_MOST);
        if (POPUPWINDOW_FOR_PRESSED_UI) {
            mBalloonOnKey = new BalloonHint(context, this, MeasureSpec.AT_MOST);
        }
        mPopupSkb = new PopupWindow(mContext);
        mPopupSkb.setBackgroundDrawable(null);
        mPopupSkb.setClippingEnabled(false);
    }
    public void setService(InputMethodService service) {
        mService = service;
    }
    public void setInputModeSwitcher(InputModeSwitcher inputModeSwitcher) {
        mInputModeSwitcher = inputModeSwitcher;
    }
    public void setGestureDetector(GestureDetector gestureDetector) {
        mGestureDetector = gestureDetector;
    }
    public boolean isCurrentSkbSticky() {
        if (null == mMajorView) return true;
        SoftKeyboard skb = mMajorView.getSoftKeyboard();
        if (null != skb) {
            return skb.getStickyFlag();
        }
        return true;
    }
    public void toggleCandidateMode(boolean candidatesShowing) {
        if (null == mMajorView || !mInputModeSwitcher.isChineseText()
                || mLastCandidatesShowing == candidatesShowing) return;
        mLastCandidatesShowing = candidatesShowing;
        SoftKeyboard skb = mMajorView.getSoftKeyboard();
        if (null == skb) return;
        int state = mInputModeSwitcher.getTooggleStateForCnCand();
        if (!candidatesShowing) {
            skb.disableToggleState(state, false);
            skb.enableToggleStates(mInputModeSwitcher.getToggleStates());
        } else {
            skb.enableToggleState(state, false);
        }
        mMajorView.invalidate();
    }
    public void updateInputMode() {
        int skbLayout = mInputModeSwitcher.getSkbLayout();
        if (mSkbLayout != skbLayout) {
            mSkbLayout = skbLayout;
            updateSkbLayout();
        }
        mLastCandidatesShowing = false;
        if (null == mMajorView) return;
        SoftKeyboard skb = mMajorView.getSoftKeyboard();
        if (null == skb) return;
        skb.enableToggleStates(mInputModeSwitcher.getToggleStates());
        invalidate();
        return;
    }
    private void updateSkbLayout() {
        int screenWidth = mEnvironment.getScreenWidth();
        int keyHeight = mEnvironment.getKeyHeight();
        int skbHeight = mEnvironment.getSkbHeight();
        Resources r = mContext.getResources();
        if (null == mSkbFlipper) {
            mSkbFlipper = (ViewFlipper) findViewById(R.id.alpha_floatable);
        }
        mMajorView = (SoftKeyboardView) mSkbFlipper.getChildAt(0);
        SoftKeyboard majorSkb = null;
        SkbPool skbPool = SkbPool.getInstance();
        switch (mSkbLayout) {
        case R.xml.skb_qwerty:
            majorSkb = skbPool.getSoftKeyboard(R.xml.skb_qwerty,
                    R.xml.skb_qwerty, screenWidth, skbHeight, mContext);
            break;
        case R.xml.skb_sym1:
            majorSkb = skbPool.getSoftKeyboard(R.xml.skb_sym1, R.xml.skb_sym1,
                    screenWidth, skbHeight, mContext);
            break;
        case R.xml.skb_sym2:
            majorSkb = skbPool.getSoftKeyboard(R.xml.skb_sym2, R.xml.skb_sym2,
                    screenWidth, skbHeight, mContext);
            break;
        case R.xml.skb_smiley:
            majorSkb = skbPool.getSoftKeyboard(R.xml.skb_smiley,
                    R.xml.skb_smiley, screenWidth, skbHeight, mContext);
            break;
        case R.xml.skb_phone:
            majorSkb = skbPool.getSoftKeyboard(R.xml.skb_phone,
                    R.xml.skb_phone, screenWidth, skbHeight, mContext);
            break;
        default:
        }
        if (null == majorSkb || !mMajorView.setSoftKeyboard(majorSkb)) {
            return;
        }
        mMajorView.setBalloonHint(mBalloonOnKey, mBalloonPopup, false);
        mMajorView.invalidate();
    }
    private void responseKeyEvent(SoftKey sKey) {
        if (null == sKey) return;
        ((PinyinIME) mService).responseSoftKeyEvent(sKey);
        return;
    }
    private SoftKeyboardView inKeyboardView(int x, int y,
            int positionInParent[]) {
        if (mPopupSkbShow) {
            if (mPopupX <= x && mPopupX + mPopupSkb.getWidth() > x
                    && mPopupY <= y && mPopupY + mPopupSkb.getHeight() > y) {
                positionInParent[0] = mPopupX;
                positionInParent[1] = mPopupY;
                mPopupSkbView.setOffsetToSkbContainer(positionInParent);
                return mPopupSkbView;
            }
            return null;
        }
        return mMajorView;
    }
    private void popupSymbols() {
        int popupResId = mSoftKeyDown.getPopupResId();
        if (popupResId > 0) {
            int skbContainerWidth = getWidth();
            int skbContainerHeight = getHeight();
            int miniSkbWidth = (int) (skbContainerWidth * 0.8);
            int miniSkbHeight = (int) (skbContainerHeight * 0.23);
            SkbPool skbPool = SkbPool.getInstance();
            SoftKeyboard skb = skbPool.getSoftKeyboard(popupResId, popupResId,
                    miniSkbWidth, miniSkbHeight, mContext);
            if (null == skb) return;
            mPopupX = (skbContainerWidth - skb.getSkbTotalWidth()) / 2;
            mPopupY = (skbContainerHeight - skb.getSkbTotalHeight()) / 2;
            if (null == mPopupSkbView) {
                mPopupSkbView = new SoftKeyboardView(mContext, null);
                mPopupSkbView.onMeasure(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
            }
            mPopupSkbView.setOnTouchListener(this);
            mPopupSkbView.setSoftKeyboard(skb);
            mPopupSkbView.setBalloonHint(mBalloonOnKey, mBalloonPopup, true);
            mPopupSkb.setContentView(mPopupSkbView);
            mPopupSkb.setWidth(skb.getSkbCoreWidth()
                    + mPopupSkbView.getPaddingLeft()
                    + mPopupSkbView.getPaddingRight());
            mPopupSkb.setHeight(skb.getSkbCoreHeight()
                    + mPopupSkbView.getPaddingTop()
                    + mPopupSkbView.getPaddingBottom());
            getLocationInWindow(mXyPosTmp);
            mPopupSkb.showAtLocation(this, Gravity.NO_GRAVITY, mPopupX, mPopupY
                    + mXyPosTmp[1]);
            mPopupSkbShow = true;
            mPopupSkbNoResponse = true;
            dimSoftKeyboard(true);
            resetKeyPress(0);
        }
    }
    private void dimSoftKeyboard(boolean dimSkb) {
        mMajorView.dimSoftKeyboard(dimSkb);
    }
    private void dismissPopupSkb() {
        mPopupSkb.dismiss();
        mPopupSkbShow = false;
        dimSoftKeyboard(false);
        resetKeyPress(0);
    }
    private void resetKeyPress(long delay) {
        mLongPressTimer.removeTimer();
        if (null != mSkv) {
            mSkv.resetKeyPress(delay);
        }
    }
    public boolean handleBack(boolean realAction) {
        if (mPopupSkbShow) {
            if (!realAction) return true;
            dismissPopupSkb();
            mDiscardEvent = true;
            return true;
        }
        return false;
    }
    public void dismissPopups() {
        handleBack(true);
        resetKeyPress(0);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Environment env = Environment.getInstance();
        int measuredWidth = env.getScreenWidth();
        int measuredHeight = getPaddingTop();
        measuredHeight += env.getSkbHeight();
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth,
                MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(measuredHeight,
                MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (mSkbFlipper.isFlipping()) {
            resetKeyPress(0);
            return true;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        y = y + mYBiasCorrection;
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (Math.abs(x - mXLast) <= MOVE_TOLERANCE
                    && Math.abs(y - mYLast) <= MOVE_TOLERANCE) {
                return true;
            }
        }
        mXLast = x;
        mYLast = y;
        if (!mPopupSkbShow) {
            if (mGestureDetector.onTouchEvent(event)) {
                resetKeyPress(0);
                mDiscardEvent = true;
                return true;
            }
        }
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            resetKeyPress(0);
            mWaitForTouchUp = true;
            mDiscardEvent = false;
            mSkv = null;
            mSoftKeyDown = null;
            mSkv = inKeyboardView(x, y, mSkvPosInContainer);
            if (null != mSkv) {
                mSoftKeyDown = mSkv.onKeyPress(x - mSkvPosInContainer[0], y
                        - mSkvPosInContainer[1], mLongPressTimer, false);
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
                break;
            }
            if (mDiscardEvent) {
                resetKeyPress(0);
                break;
            }
            if (mPopupSkbShow && mPopupSkbNoResponse) {
                break;
            }
            SoftKeyboardView skv = inKeyboardView(x, y, mSkvPosInContainer);
            if (null != skv) {
                if (skv != mSkv) {
                    mSkv = skv;
                    mSoftKeyDown = mSkv.onKeyPress(x - mSkvPosInContainer[0], y
                            - mSkvPosInContainer[1], mLongPressTimer, true);
                } else if (null != skv) {
                    if (null != mSkv) {
                        mSoftKeyDown = mSkv.onKeyMove(
                                x - mSkvPosInContainer[0], y
                                        - mSkvPosInContainer[1]);
                        if (null == mSoftKeyDown) {
                            mDiscardEvent = true;
                        }
                    }
                }
            }
            break;
        case MotionEvent.ACTION_UP:
            if (mDiscardEvent) {
                resetKeyPress(0);
                break;
            }
            mWaitForTouchUp = false;
            if (null != mSkv) {
                mSkv.onKeyRelease(x - mSkvPosInContainer[0], y
                        - mSkvPosInContainer[1]);
            }
            if (!mPopupSkbShow || !mPopupSkbNoResponse) {
                responseKeyEvent(mSoftKeyDown);
            }
            if (mSkv == mPopupSkbView && !mPopupSkbNoResponse) {
                dismissPopupSkb();
            }
            mPopupSkbNoResponse = false;
            break;
        case MotionEvent.ACTION_CANCEL:
            break;
        }
        if (null == mSkv) {
            return false;
        }
        return true;
    }
    public boolean onTouch(View v, MotionEvent event) {
        MotionEvent newEv = MotionEvent.obtain(event.getDownTime(), event
                .getEventTime(), event.getAction(), event.getX() + mPopupX,
                event.getY() + mPopupY, event.getPressure(), event.getSize(),
                event.getMetaState(), event.getXPrecision(), event
                        .getYPrecision(), event.getDeviceId(), event
                        .getEdgeFlags());
        boolean ret = onTouchEvent(newEv);
        return ret;
    }
    class LongPressTimer extends Handler implements Runnable {
        public static final int LONG_PRESS_TIMEOUT1 = 500;
        private static final int LONG_PRESS_TIMEOUT2 = 100;
        private static final int LONG_PRESS_TIMEOUT3 = 100;
        public static final int LONG_PRESS_KEYNUM1 = 1;
        public static final int LONG_PRESS_KEYNUM2 = 3;
        SkbContainer mSkbContainer;
        private int mResponseTimes = 0;
        public LongPressTimer(SkbContainer skbContainer) {
            mSkbContainer = skbContainer;
        }
        public void startTimer() {
            postAtTime(this, SystemClock.uptimeMillis() + LONG_PRESS_TIMEOUT1);
            mResponseTimes = 0;
        }
        public boolean removeTimer() {
            removeCallbacks(this);
            return true;
        }
        public void run() {
            if (mWaitForTouchUp) {
                mResponseTimes++;
                if (mSoftKeyDown.repeatable()) {
                    if (mSoftKeyDown.isUserDefKey()) {
                        if (1 == mResponseTimes) {
                            if (mInputModeSwitcher
                                    .tryHandleLongPressSwitch(mSoftKeyDown.mKeyCode)) {
                                mDiscardEvent = true;
                                resetKeyPress(0);
                            }
                        }
                    } else {
                        responseKeyEvent(mSoftKeyDown);
                        long timeout;
                        if (mResponseTimes < LONG_PRESS_KEYNUM1) {
                            timeout = LONG_PRESS_TIMEOUT1;
                        } else if (mResponseTimes < LONG_PRESS_KEYNUM2) {
                            timeout = LONG_PRESS_TIMEOUT2;
                        } else {
                            timeout = LONG_PRESS_TIMEOUT3;
                        }
                        postAtTime(this, SystemClock.uptimeMillis() + timeout);
                    }
                } else {
                    if (1 == mResponseTimes) {
                        popupSymbols();
                    }
                }
            }
        }
    }
}
