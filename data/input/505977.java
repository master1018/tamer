public class SoftKeyboardView extends View {
    private SoftKeyboard mSoftKeyboard;
    private BalloonHint mBalloonPopup;
    private BalloonHint mBalloonOnKey;
    private SoundManager mSoundManager;
    private SoftKey mSoftKeyDown;
    private boolean mKeyPressed = false;
    private int mOffsetToSkbContainer[] = new int[2];
    private int mHintLocationToSkbContainer[] = new int[2];
    private int mNormalKeyTextSize;
    private int mFunctionKeyTextSize;
    private SkbContainer.LongPressTimer mLongPressTimer;
    private boolean mRepeatForLongPress = false;
    private boolean mMovingNeverHidePopupBalloon = false;
    private Vibrator mVibrator;
    protected long[] mVibratePattern = new long[] {1, 20};
    private Rect mDirtyRect = new Rect();
    private Paint mPaint;
    private FontMetricsInt mFmi;
    private boolean mDimSkb;
    public SoftKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSoundManager = SoundManager.getInstance(mContext);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mFmi = mPaint.getFontMetricsInt();
    }
    public boolean setSoftKeyboard(SoftKeyboard softSkb) {
        if (null == softSkb) {
            return false;
        }
        mSoftKeyboard = softSkb;
        Drawable bg = softSkb.getSkbBackground();
        if (null != bg) setBackgroundDrawable(bg);
        return true;
    }
    public SoftKeyboard getSoftKeyboard() {
        return mSoftKeyboard;
    }
    public void resizeKeyboard(int skbWidth, int skbHeight) {
        mSoftKeyboard.setSkbCoreSize(skbWidth, skbHeight);
    }
    public void setBalloonHint(BalloonHint balloonOnKey,
            BalloonHint balloonPopup, boolean movingNeverHidePopup) {
        mBalloonOnKey = balloonOnKey;
        mBalloonPopup = balloonPopup;
        mMovingNeverHidePopupBalloon = movingNeverHidePopup;
    }
    public void setOffsetToSkbContainer(int offsetToSkbContainer[]) {
        mOffsetToSkbContainer[0] = offsetToSkbContainer[0];
        mOffsetToSkbContainer[1] = offsetToSkbContainer[1];
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = 0;
        int measuredHeight = 0;
        if (null != mSoftKeyboard) {
            measuredWidth = mSoftKeyboard.getSkbCoreWidth();
            measuredHeight = mSoftKeyboard.getSkbCoreHeight();
            measuredWidth += mPaddingLeft + mPaddingRight;
            measuredHeight += mPaddingTop + mPaddingBottom;
        }
        setMeasuredDimension(measuredWidth, measuredHeight);
    }
    private void showBalloon(BalloonHint balloon, int balloonLocationToSkb[],
            boolean movePress) {
        long delay = BalloonHint.TIME_DELAY_SHOW;
        if (movePress) delay = 0;
        if (balloon.needForceDismiss()) {
            balloon.delayedDismiss(0);
        }
        if (!balloon.isShowing()) {
            balloon.delayedShow(delay, balloonLocationToSkb);
        } else {
            balloon.delayedUpdate(delay, balloonLocationToSkb, balloon
                    .getWidth(), balloon.getHeight());
        }
        long b = System.currentTimeMillis();
    }
    public void resetKeyPress(long balloonDelay) {
        if (!mKeyPressed) return;
        mKeyPressed = false;
        if (null != mBalloonOnKey) {
            mBalloonOnKey.delayedDismiss(balloonDelay);
        } else {
            if (null != mSoftKeyDown) {
                if (mDirtyRect.isEmpty()) {
                    mDirtyRect.set(mSoftKeyDown.mLeft, mSoftKeyDown.mTop,
                            mSoftKeyDown.mRight, mSoftKeyDown.mBottom);
                }
                invalidate(mDirtyRect);
            } else {
                invalidate();
            }
        }
        mBalloonPopup.delayedDismiss(balloonDelay);
    }
    public SoftKey onKeyPress(int x, int y,
            SkbContainer.LongPressTimer longPressTimer, boolean movePress) {
        mKeyPressed = false;
        boolean moveWithinPreviousKey = false;
        if (movePress) {
            SoftKey newKey = mSoftKeyboard.mapToKey(x, y);
            if (newKey == mSoftKeyDown) moveWithinPreviousKey = true;
            mSoftKeyDown = newKey;
        } else {
            mSoftKeyDown = mSoftKeyboard.mapToKey(x, y);
        }
        if (moveWithinPreviousKey || null == mSoftKeyDown) return mSoftKeyDown;
        mKeyPressed = true;
        if (!movePress) {
            tryPlayKeyDown();
            tryVibrate();
        }
        mLongPressTimer = longPressTimer;
        if (!movePress) {
            if (mSoftKeyDown.getPopupResId() > 0 || mSoftKeyDown.repeatable()) {
                mLongPressTimer.startTimer();
            }
        } else {
            mLongPressTimer.removeTimer();
        }
        int desired_width;
        int desired_height;
        float textSize;
        Environment env = Environment.getInstance();
        if (null != mBalloonOnKey) {
            Drawable keyHlBg = mSoftKeyDown.getKeyHlBg();
            mBalloonOnKey.setBalloonBackground(keyHlBg);
            int keyXMargin = mSoftKeyboard.getKeyXMargin();
            int keyYMargin = mSoftKeyboard.getKeyYMargin();
            desired_width = mSoftKeyDown.width() - 2 * keyXMargin;
            desired_height = mSoftKeyDown.height() - 2 * keyYMargin;
            textSize = env
                    .getKeyTextSize(SoftKeyType.KEYTYPE_ID_NORMAL_KEY != mSoftKeyDown.mKeyType.mKeyTypeId);
            Drawable icon = mSoftKeyDown.getKeyIcon();
            if (null != icon) {
                mBalloonOnKey.setBalloonConfig(icon, desired_width,
                        desired_height);
            } else {
                mBalloonOnKey.setBalloonConfig(mSoftKeyDown.getKeyLabel(),
                        textSize, true, mSoftKeyDown.getColorHl(),
                        desired_width, desired_height);
            }
            mHintLocationToSkbContainer[0] = mPaddingLeft + mSoftKeyDown.mLeft
                    - (mBalloonOnKey.getWidth() - mSoftKeyDown.width()) / 2;
            mHintLocationToSkbContainer[0] += mOffsetToSkbContainer[0];
            mHintLocationToSkbContainer[1] = mPaddingTop
                    + (mSoftKeyDown.mBottom - keyYMargin)
                    - mBalloonOnKey.getHeight();
            mHintLocationToSkbContainer[1] += mOffsetToSkbContainer[1];
            showBalloon(mBalloonOnKey, mHintLocationToSkbContainer, movePress);
        } else {
            mDirtyRect.union(mSoftKeyDown.mLeft, mSoftKeyDown.mTop,
                    mSoftKeyDown.mRight, mSoftKeyDown.mBottom);
            invalidate(mDirtyRect);
        }
        if (mSoftKeyDown.needBalloon()) {
            Drawable balloonBg = mSoftKeyboard.getBalloonBackground();
            mBalloonPopup.setBalloonBackground(balloonBg);
            desired_width = mSoftKeyDown.width() + env.getKeyBalloonWidthPlus();
            desired_height = mSoftKeyDown.height()
                    + env.getKeyBalloonHeightPlus();
            textSize = env
                    .getBalloonTextSize(SoftKeyType.KEYTYPE_ID_NORMAL_KEY != mSoftKeyDown.mKeyType.mKeyTypeId);
            Drawable iconPopup = mSoftKeyDown.getKeyIconPopup();
            if (null != iconPopup) {
                mBalloonPopup.setBalloonConfig(iconPopup, desired_width,
                        desired_height);
            } else {
                mBalloonPopup.setBalloonConfig(mSoftKeyDown.getKeyLabel(),
                        textSize, mSoftKeyDown.needBalloon(), mSoftKeyDown
                                .getColorBalloon(), desired_width,
                        desired_height);
            }
            mHintLocationToSkbContainer[0] = mPaddingLeft + mSoftKeyDown.mLeft
                    + -(mBalloonPopup.getWidth() - mSoftKeyDown.width()) / 2;
            mHintLocationToSkbContainer[0] += mOffsetToSkbContainer[0];
            mHintLocationToSkbContainer[1] = mPaddingTop + mSoftKeyDown.mTop
                    - mBalloonPopup.getHeight();
            mHintLocationToSkbContainer[1] += mOffsetToSkbContainer[1];
            showBalloon(mBalloonPopup, mHintLocationToSkbContainer, movePress);
        } else {
            mBalloonPopup.delayedDismiss(0);
        }
        if (mRepeatForLongPress) longPressTimer.startTimer();
        return mSoftKeyDown;
    }
    public SoftKey onKeyRelease(int x, int y) {
        mKeyPressed = false;
        if (null == mSoftKeyDown) return null;
        mLongPressTimer.removeTimer();
        if (null != mBalloonOnKey) {
            mBalloonOnKey.delayedDismiss(BalloonHint.TIME_DELAY_DISMISS);
        } else {
            mDirtyRect.union(mSoftKeyDown.mLeft, mSoftKeyDown.mTop,
                    mSoftKeyDown.mRight, mSoftKeyDown.mBottom);
            invalidate(mDirtyRect);
        }
        if (mSoftKeyDown.needBalloon()) {
            mBalloonPopup.delayedDismiss(BalloonHint.TIME_DELAY_DISMISS);
        }
        if (mSoftKeyDown.moveWithinKey(x - mPaddingLeft, y - mPaddingTop)) {
            return mSoftKeyDown;
        }
        return null;
    }
    public SoftKey onKeyMove(int x, int y) {
        if (null == mSoftKeyDown) return null;
        if (mSoftKeyDown.moveWithinKey(x - mPaddingLeft, y - mPaddingTop)) {
            return mSoftKeyDown;
        }
        mDirtyRect.union(mSoftKeyDown.mLeft, mSoftKeyDown.mTop,
                mSoftKeyDown.mRight, mSoftKeyDown.mBottom);
        if (mRepeatForLongPress) {
            if (mMovingNeverHidePopupBalloon) {
                return onKeyPress(x, y, mLongPressTimer, true);
            }
            if (null != mBalloonOnKey) {
                mBalloonOnKey.delayedDismiss(0);
            } else {
                invalidate(mDirtyRect);
            }
            if (mSoftKeyDown.needBalloon()) {
                mBalloonPopup.delayedDismiss(0);
            }
            if (null != mLongPressTimer) {
                mLongPressTimer.removeTimer();
            }
            return onKeyPress(x, y, mLongPressTimer, true);
        } else {
            return onKeyPress(x, y, mLongPressTimer, true);
        }
    }
    private void tryVibrate() {
        if (!Settings.getVibrate()) {
            return;
        }
        if (mVibrator == null) {
            mVibrator = new Vibrator();
        }
        mVibrator.vibrate(mVibratePattern, -1);
    }
    private void tryPlayKeyDown() {
        if (Settings.getKeySound()) {
            mSoundManager.playKeyDown();
        }
    }
    public void dimSoftKeyboard(boolean dimSkb) {
        mDimSkb = dimSkb;
        invalidate();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (null == mSoftKeyboard) return;
        canvas.translate(mPaddingLeft, mPaddingTop);
        Environment env = Environment.getInstance();
        mNormalKeyTextSize = env.getKeyTextSize(false);
        mFunctionKeyTextSize = env.getKeyTextSize(true);
        int rowNum = mSoftKeyboard.getRowNum();
        int keyXMargin = mSoftKeyboard.getKeyXMargin();
        int keyYMargin = mSoftKeyboard.getKeyYMargin();
        for (int row = 0; row < rowNum; row++) {
            KeyRow keyRow = mSoftKeyboard.getKeyRowForDisplay(row);
            if (null == keyRow) continue;
            List<SoftKey> softKeys = keyRow.mSoftKeys;
            int keyNum = softKeys.size();
            for (int i = 0; i < keyNum; i++) {
                SoftKey softKey = softKeys.get(i);
                if (SoftKeyType.KEYTYPE_ID_NORMAL_KEY == softKey.mKeyType.mKeyTypeId) {
                    mPaint.setTextSize(mNormalKeyTextSize);
                } else {
                    mPaint.setTextSize(mFunctionKeyTextSize);
                }
                drawSoftKey(canvas, softKey, keyXMargin, keyYMargin);
            }
        }
        if (mDimSkb) {
            mPaint.setColor(0xa0000000);
            canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
        }
        mDirtyRect.setEmpty();
    }
    private void drawSoftKey(Canvas canvas, SoftKey softKey, int keyXMargin,
            int keyYMargin) {
        Drawable bg;
        int textColor;
        if (mKeyPressed && softKey == mSoftKeyDown) {
            bg = softKey.getKeyHlBg();
            textColor = softKey.getColorHl();
        } else {
            bg = softKey.getKeyBg();
            textColor = softKey.getColor();
        }
        if (null != bg) {
            bg.setBounds(softKey.mLeft + keyXMargin, softKey.mTop + keyYMargin,
                    softKey.mRight - keyXMargin, softKey.mBottom - keyYMargin);
            bg.draw(canvas);
        }
        String keyLabel = softKey.getKeyLabel();
        Drawable keyIcon = softKey.getKeyIcon();
        if (null != keyIcon) {
            Drawable icon = keyIcon;
            int marginLeft = (softKey.width() - icon.getIntrinsicWidth()) / 2;
            int marginRight = softKey.width() - icon.getIntrinsicWidth()
                    - marginLeft;
            int marginTop = (softKey.height() - icon.getIntrinsicHeight()) / 2;
            int marginBottom = softKey.height() - icon.getIntrinsicHeight()
                    - marginTop;
            icon.setBounds(softKey.mLeft + marginLeft,
                    softKey.mTop + marginTop, softKey.mRight - marginRight,
                    softKey.mBottom - marginBottom);
            icon.draw(canvas);
        } else if (null != keyLabel) {
            mPaint.setColor(textColor);
            float x = softKey.mLeft
                    + (softKey.width() - mPaint.measureText(keyLabel)) / 2.0f;
            int fontHeight = mFmi.bottom - mFmi.top;
            float marginY = (softKey.height() - fontHeight) / 2.0f;
            float y = softKey.mTop + marginY - mFmi.top + mFmi.bottom / 1.5f;
            canvas.drawText(keyLabel, x, y + 1, mPaint);
        }
    }
}
