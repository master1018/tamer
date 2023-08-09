public class BalloonHint extends PopupWindow {
    public static final int TIME_DELAY_SHOW = 0;
    public static final int TIME_DELAY_DISMISS = 200;
    private Rect mPaddingRect = new Rect();
    private Context mContext;
    private View mParent;
    BalloonView mBalloonView;
    private int mMeasureSpecMode;
    private boolean mForceDismiss;
    private BalloonTimer mBalloonTimer;
    private int mParentLocationInWindow[] = new int[2];
    public BalloonHint(Context context, View parent, int measureSpecMode) {
        super(context);
        mParent = parent;
        mMeasureSpecMode = measureSpecMode;
        setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        setTouchable(false);
        setBackgroundDrawable(new ColorDrawable(0));
        mBalloonView = new BalloonView(context);
        mBalloonView.setClickable(false);
        setContentView(mBalloonView);
        mBalloonTimer = new BalloonTimer();
    }
    public Context getContext() {
        return mContext;
    }
    public Rect getPadding() {
        return mPaddingRect;
    }
    public void setBalloonBackground(Drawable drawable) {
        if (mBalloonView.getBackground() == drawable) return;
        mBalloonView.setBackgroundDrawable(drawable);
        if (null != drawable) {
            drawable.getPadding(mPaddingRect);
        } else {
            mPaddingRect.set(0, 0, 0, 0);
        }
    }
    public void setBalloonConfig(String label, float textSize,
            boolean textBold, int textColor, int width, int height) {
        mBalloonView.setTextConfig(label, textSize, textBold, textColor);
        setBalloonSize(width, height);
    }
    public void setBalloonConfig(Drawable icon, int width, int height) {
        mBalloonView.setIcon(icon);
        setBalloonSize(width, height);
    }
    public boolean needForceDismiss() {
        return mForceDismiss;
    }
    public int getPaddingLeft() {
        return mPaddingRect.left;
    }
    public int getPaddingTop() {
        return mPaddingRect.top;
    }
    public int getPaddingRight() {
        return mPaddingRect.right;
    }
    public int getPaddingBottom() {
        return mPaddingRect.bottom;
    }
    public void delayedShow(long delay, int locationInParent[]) {
        if (mBalloonTimer.isPending()) {
            mBalloonTimer.removeTimer();
        }
        if (delay <= 0) {
            mParent.getLocationInWindow(mParentLocationInWindow);
            showAtLocation(mParent, Gravity.LEFT | Gravity.TOP,
                    locationInParent[0], locationInParent[1]
                            + mParentLocationInWindow[1]);
        } else {
            mBalloonTimer.startTimer(delay, BalloonTimer.ACTION_SHOW,
                    locationInParent, -1, -1);
        }
    }
    public void delayedUpdate(long delay, int locationInParent[],
            int width, int height) {
        mBalloonView.invalidate();
        if (mBalloonTimer.isPending()) {
            mBalloonTimer.removeTimer();
        }
        if (delay <= 0) {
            mParent.getLocationInWindow(mParentLocationInWindow);
            update(locationInParent[0], locationInParent[1]
                    + mParentLocationInWindow[1], width, height);
        } else {
            mBalloonTimer.startTimer(delay, BalloonTimer.ACTION_UPDATE,
                    locationInParent, width, height);
        }
    }
    public void delayedDismiss(long delay) {
        if (mBalloonTimer.isPending()) {
            mBalloonTimer.removeTimer();
            int pendingAction = mBalloonTimer.getAction();
            if (0 != delay && BalloonTimer.ACTION_HIDE != pendingAction) {
                mBalloonTimer.run();
            }
        }
        if (delay <= 0) {
            dismiss();
        } else {
            mBalloonTimer.startTimer(delay, BalloonTimer.ACTION_HIDE, null, -1,
                    -1);
        }
    }
    public void removeTimer() {
        if (mBalloonTimer.isPending()) {
            mBalloonTimer.removeTimer();
        }
    }
    private void setBalloonSize(int width, int height) {
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec(width,
                mMeasureSpecMode);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec(height,
                mMeasureSpecMode);
        mBalloonView.measure(widthMeasureSpec, heightMeasureSpec);
        int oldWidth = getWidth();
        int oldHeight = getHeight();
        int newWidth = mBalloonView.getMeasuredWidth() + getPaddingLeft()
                + getPaddingRight();
        int newHeight = mBalloonView.getMeasuredHeight() + getPaddingTop()
                + getPaddingBottom();
        setWidth(newWidth);
        setHeight(newHeight);
        mForceDismiss = false;
        if (isShowing()) {
            mForceDismiss = oldWidth - newWidth > 1 || newWidth - oldWidth > 1;
        }
    }
    private class BalloonTimer extends Handler implements Runnable {
        public static final int ACTION_SHOW = 1;
        public static final int ACTION_HIDE = 2;
        public static final int ACTION_UPDATE = 3;
        private int mAction;
        private int mPositionInParent[] = new int[2];
        private int mWidth;
        private int mHeight;
        private boolean mTimerPending = false;
        public void startTimer(long time, int action, int positionInParent[],
                int width, int height) {
            mAction = action;
            if (ACTION_HIDE != action) {
                mPositionInParent[0] = positionInParent[0];
                mPositionInParent[1] = positionInParent[1];
            }
            mWidth = width;
            mHeight = height;
            postDelayed(this, time);
            mTimerPending = true;
        }
        public boolean isPending() {
            return mTimerPending;
        }
        public boolean removeTimer() {
            if (mTimerPending) {
                mTimerPending = false;
                removeCallbacks(this);
                return true;
            }
            return false;
        }
        public int getAction() {
            return mAction;
        }
        public void run() {
            switch (mAction) {
            case ACTION_SHOW:
                mParent.getLocationInWindow(mParentLocationInWindow);
                showAtLocation(mParent, Gravity.LEFT | Gravity.TOP,
                        mPositionInParent[0], mPositionInParent[1]
                                + mParentLocationInWindow[1]);
                break;
            case ACTION_HIDE:
                dismiss();
                break;
            case ACTION_UPDATE:
                mParent.getLocationInWindow(mParentLocationInWindow);
                update(mPositionInParent[0], mPositionInParent[1]
                        + mParentLocationInWindow[1], mWidth, mHeight);
            }
            mTimerPending = false;
        }
    }
    private class BalloonView extends View {
        private static final String SUSPENSION_POINTS = "...";
        private Drawable mIcon;
        private String mLabel;
        private int mLabeColor = 0xff000000;
        private Paint mPaintLabel;
        private FontMetricsInt mFmi;
        private float mSuspensionPointsWidth;
        public BalloonView(Context context) {
            super(context);
            mPaintLabel = new Paint();
            mPaintLabel.setColor(mLabeColor);
            mPaintLabel.setAntiAlias(true);
            mPaintLabel.setFakeBoldText(true);
            mFmi = mPaintLabel.getFontMetricsInt();
        }
        public void setIcon(Drawable icon) {
            mIcon = icon;
        }
        public void setTextConfig(String label, float fontSize,
                boolean textBold, int textColor) {
            mIcon = null;
            mLabel = label;
            mPaintLabel.setTextSize(fontSize);
            mPaintLabel.setFakeBoldText(textBold);
            mPaintLabel.setColor(textColor);
            mFmi = mPaintLabel.getFontMetricsInt();
            mSuspensionPointsWidth = mPaintLabel.measureText(SUSPENSION_POINTS);
        }
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            if (widthMode == MeasureSpec.EXACTLY) {
                setMeasuredDimension(widthSize, heightSize);
                return;
            }
            int measuredWidth = mPaddingLeft + mPaddingRight;
            int measuredHeight = mPaddingTop + mPaddingBottom;
            if (null != mIcon) {
                measuredWidth += mIcon.getIntrinsicWidth();
                measuredHeight += mIcon.getIntrinsicHeight();
            } else if (null != mLabel) {
                measuredWidth += (int) (mPaintLabel.measureText(mLabel));
                measuredHeight += mFmi.bottom - mFmi.top;
            }
            if (widthSize > measuredWidth || widthMode == MeasureSpec.AT_MOST) {
                measuredWidth = widthSize;
            }
            if (heightSize > measuredHeight
                    || heightMode == MeasureSpec.AT_MOST) {
                measuredHeight = heightSize;
            }
            int maxWidth = Environment.getInstance().getScreenWidth() -
                    mPaddingLeft - mPaddingRight;
            if (measuredWidth > maxWidth) {
                measuredWidth = maxWidth;
            }
            setMeasuredDimension(measuredWidth, measuredHeight);
        }
        @Override
        protected void onDraw(Canvas canvas) {
            int width = getWidth();
            int height = getHeight();
            if (null != mIcon) {
                int marginLeft = (width - mIcon.getIntrinsicWidth()) / 2;
                int marginRight = width - mIcon.getIntrinsicWidth()
                        - marginLeft;
                int marginTop = (height - mIcon.getIntrinsicHeight()) / 2;
                int marginBottom = height - mIcon.getIntrinsicHeight()
                        - marginTop;
                mIcon.setBounds(marginLeft, marginTop, width - marginRight,
                        height - marginBottom);
                mIcon.draw(canvas);
            } else if (null != mLabel) {
                float labelMeasuredWidth = mPaintLabel.measureText(mLabel);
                float x = mPaddingLeft;
                x += (width - labelMeasuredWidth - mPaddingLeft - mPaddingRight) / 2.0f;
                String labelToDraw = mLabel;
                if (x < mPaddingLeft) {
                    x = mPaddingLeft;
                    labelToDraw = getLimitedLabelForDrawing(mLabel,
                            width - mPaddingLeft - mPaddingRight);
                }
                int fontHeight = mFmi.bottom - mFmi.top;
                float marginY = (height - fontHeight) / 2.0f;
                float y = marginY - mFmi.top;
                canvas.drawText(labelToDraw, x, y, mPaintLabel);
            }
        }
        private String getLimitedLabelForDrawing(String rawLabel,
                float widthToDraw) {
            int subLen = rawLabel.length();
            if (subLen <= 1) return rawLabel;
            do {
                subLen--;
                float width = mPaintLabel.measureText(rawLabel, 0, subLen);
                if (width + mSuspensionPointsWidth <= widthToDraw || 1 >= subLen) {
                    return rawLabel.substring(0, subLen) +
                            SUSPENSION_POINTS;
                }
            } while (true);
        }
    }
}
