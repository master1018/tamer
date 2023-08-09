public class CandidatesContainer extends RelativeLayout implements
        OnTouchListener, AnimationListener, ArrowUpdater {
    private static int ARROW_ALPHA_ENABLED = 0xff;
    private static int ARROW_ALPHA_DISABLED = 0x40;
    private static int ANIMATION_TIME = 200;
    private CandidateViewListener mCvListener;
    private ImageButton mLeftArrowBtn;
    private ImageButton mRightArrowBtn;
    private DecodingInfo mDecInfo;
    private ViewFlipper mFlipper;
    private int xOffsetForFlipper;
    private Animation mInAnimPushLeft;
    private Animation mInAnimPushRight;
    private Animation mInAnimPushUp;
    private Animation mInAnimPushDown;
    private Animation mOutAnimPushLeft;
    private Animation mOutAnimPushRight;
    private Animation mOutAnimPushUp;
    private Animation mOutAnimPushDown;
    private Animation mInAnimInUse;
    private Animation mOutAnimInUse;
    private int mCurrentPage = -1;
    public CandidatesContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void initialize(CandidateViewListener cvListener,
            BalloonHint balloonHint, GestureDetector gestureDetector) {
        mCvListener = cvListener;
        mLeftArrowBtn = (ImageButton) findViewById(R.id.arrow_left_btn);
        mRightArrowBtn = (ImageButton) findViewById(R.id.arrow_right_btn);
        mLeftArrowBtn.setOnTouchListener(this);
        mRightArrowBtn.setOnTouchListener(this);
        mFlipper = (ViewFlipper) findViewById(R.id.candidate_flipper);
        mFlipper.setMeasureAllChildren(true);
        invalidate();
        requestLayout();
        for (int i = 0; i < mFlipper.getChildCount(); i++) {
            CandidateView cv = (CandidateView) mFlipper.getChildAt(i);
            cv.initialize(this, balloonHint, gestureDetector, mCvListener);
        }
    }
    public void showCandidates(PinyinIME.DecodingInfo decInfo,
            boolean enableActiveHighlight) {
        if (null == decInfo) return;
        mDecInfo = decInfo;
        mCurrentPage = 0;
        if (decInfo.isCandidatesListEmpty()) {
            showArrow(mLeftArrowBtn, false);
            showArrow(mRightArrowBtn, false);
        } else {
            showArrow(mLeftArrowBtn, true);
            showArrow(mRightArrowBtn, true);
        }
        for (int i = 0; i < mFlipper.getChildCount(); i++) {
            CandidateView cv = (CandidateView) mFlipper.getChildAt(i);
            cv.setDecodingInfo(mDecInfo);
        }
        stopAnimation();
        CandidateView cv = (CandidateView) mFlipper.getCurrentView();
        cv.showPage(mCurrentPage, 0, enableActiveHighlight);
        updateArrowStatus();
        invalidate();
    }
    public int getCurrentPage() {
        return mCurrentPage;
    }
    public void enableActiveHighlight(boolean enableActiveHighlight) {
        CandidateView cv = (CandidateView) mFlipper.getCurrentView();
        cv.enableActiveHighlight(enableActiveHighlight);
        invalidate();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Environment env = Environment.getInstance();
        int measuredWidth = env.getScreenWidth();
        int measuredHeight = getPaddingTop();
        measuredHeight += env.getHeightForCandidates();
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth,
                MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(measuredHeight,
                MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (null != mLeftArrowBtn) {
            xOffsetForFlipper = mLeftArrowBtn.getMeasuredWidth();
        }
    }
    public boolean activeCurseBackward() {
        if (mFlipper.isFlipping() || null == mDecInfo) {
            return false;
        }
        CandidateView cv = (CandidateView) mFlipper.getCurrentView();
        if (cv.activeCurseBackward()) {
            cv.invalidate();
            return true;
        } else {
            return pageBackward(true, true);
        }
    }
    public boolean activeCurseForward() {
        if (mFlipper.isFlipping() || null == mDecInfo) {
            return false;
        }
        CandidateView cv = (CandidateView) mFlipper.getCurrentView();
        if (cv.activeCursorForward()) {
            cv.invalidate();
            return true;
        } else {
            return pageForward(true, true);
        }
    }
    public boolean pageBackward(boolean animLeftRight,
            boolean enableActiveHighlight) {
        if (null == mDecInfo) return false;
        if (mFlipper.isFlipping() || 0 == mCurrentPage) return false;
        int child = mFlipper.getDisplayedChild();
        int childNext = (child + 1) % 2;
        CandidateView cv = (CandidateView) mFlipper.getChildAt(child);
        CandidateView cvNext = (CandidateView) mFlipper.getChildAt(childNext);
        mCurrentPage--;
        int activeCandInPage = cv.getActiveCandiatePosInPage();
        if (animLeftRight)
            activeCandInPage = mDecInfo.mPageStart.elementAt(mCurrentPage + 1)
                    - mDecInfo.mPageStart.elementAt(mCurrentPage) - 1;
        cvNext.showPage(mCurrentPage, activeCandInPage, enableActiveHighlight);
        loadAnimation(animLeftRight, false);
        startAnimation();
        updateArrowStatus();
        return true;
    }
    public boolean pageForward(boolean animLeftRight,
            boolean enableActiveHighlight) {
        if (null == mDecInfo) return false;
        if (mFlipper.isFlipping() || !mDecInfo.preparePage(mCurrentPage + 1)) {
            return false;
        }
        int child = mFlipper.getDisplayedChild();
        int childNext = (child + 1) % 2;
        CandidateView cv = (CandidateView) mFlipper.getChildAt(child);
        int activeCandInPage = cv.getActiveCandiatePosInPage();
        cv.enableActiveHighlight(enableActiveHighlight);
        CandidateView cvNext = (CandidateView) mFlipper.getChildAt(childNext);
        mCurrentPage++;
        if (animLeftRight) activeCandInPage = 0;
        cvNext.showPage(mCurrentPage, activeCandInPage, enableActiveHighlight);
        loadAnimation(animLeftRight, true);
        startAnimation();
        updateArrowStatus();
        return true;
    }
    public int getActiveCandiatePos() {
        if (null == mDecInfo) return -1;
        CandidateView cv = (CandidateView) mFlipper.getCurrentView();
        return cv.getActiveCandiatePosGlobal();
    }
    public void updateArrowStatus() {
        if (mCurrentPage < 0) return;
        boolean forwardEnabled = mDecInfo.pageForwardable(mCurrentPage);
        boolean backwardEnabled = mDecInfo.pageBackwardable(mCurrentPage);
        if (backwardEnabled) {
            enableArrow(mLeftArrowBtn, true);
        } else {
            enableArrow(mLeftArrowBtn, false);
        }
        if (forwardEnabled) {
            enableArrow(mRightArrowBtn, true);
        } else {
            enableArrow(mRightArrowBtn, false);
        }
    }
    private void enableArrow(ImageButton arrowBtn, boolean enabled) {
        arrowBtn.setEnabled(enabled);
        if (enabled)
            arrowBtn.setAlpha(ARROW_ALPHA_ENABLED);
        else
            arrowBtn.setAlpha(ARROW_ALPHA_DISABLED);
    }
    private void showArrow(ImageButton arrowBtn, boolean show) {
        if (show)
            arrowBtn.setVisibility(View.VISIBLE);
        else
            arrowBtn.setVisibility(View.INVISIBLE);
    }
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (v == mLeftArrowBtn) {
                mCvListener.onToRightGesture();
            } else if (v == mRightArrowBtn) {
                mCvListener.onToLeftGesture();
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            CandidateView cv = (CandidateView) mFlipper.getCurrentView();
            cv.enableActiveHighlight(true);
        }
        return false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        event.offsetLocation(-xOffsetForFlipper, 0);
        CandidateView cv = (CandidateView) mFlipper.getCurrentView();
        cv.onTouchEventReal(event);
        return true;
    }
    public void loadAnimation(boolean animLeftRight, boolean forward) {
        if (animLeftRight) {
            if (forward) {
                if (null == mInAnimPushLeft) {
                    mInAnimPushLeft = createAnimation(1.0f, 0, 0, 0, 0, 1.0f,
                            ANIMATION_TIME);
                    mOutAnimPushLeft = createAnimation(0, -1.0f, 0, 0, 1.0f, 0,
                            ANIMATION_TIME);
                }
                mInAnimInUse = mInAnimPushLeft;
                mOutAnimInUse = mOutAnimPushLeft;
            } else {
                if (null == mInAnimPushRight) {
                    mInAnimPushRight = createAnimation(-1.0f, 0, 0, 0, 0, 1.0f,
                            ANIMATION_TIME);
                    mOutAnimPushRight = createAnimation(0, 1.0f, 0, 0, 1.0f, 0,
                            ANIMATION_TIME);
                }
                mInAnimInUse = mInAnimPushRight;
                mOutAnimInUse = mOutAnimPushRight;
            }
        } else {
            if (forward) {
                if (null == mInAnimPushUp) {
                    mInAnimPushUp = createAnimation(0, 0, 1.0f, 0, 0, 1.0f,
                            ANIMATION_TIME);
                    mOutAnimPushUp = createAnimation(0, 0, 0, -1.0f, 1.0f, 0,
                            ANIMATION_TIME);
                }
                mInAnimInUse = mInAnimPushUp;
                mOutAnimInUse = mOutAnimPushUp;
            } else {
                if (null == mInAnimPushDown) {
                    mInAnimPushDown = createAnimation(0, 0, -1.0f, 0, 0, 1.0f,
                            ANIMATION_TIME);
                    mOutAnimPushDown = createAnimation(0, 0, 0, 1.0f, 1.0f, 0,
                            ANIMATION_TIME);
                }
                mInAnimInUse = mInAnimPushDown;
                mOutAnimInUse = mOutAnimPushDown;
            }
        }
        mInAnimInUse.setAnimationListener(this);
        mFlipper.setInAnimation(mInAnimInUse);
        mFlipper.setOutAnimation(mOutAnimInUse);
    }
    private Animation createAnimation(float xFrom, float xTo, float yFrom,
            float yTo, float alphaFrom, float alphaTo, long duration) {
        AnimationSet animSet = new AnimationSet(getContext(), null);
        Animation trans = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                xFrom, Animation.RELATIVE_TO_SELF, xTo,
                Animation.RELATIVE_TO_SELF, yFrom, Animation.RELATIVE_TO_SELF,
                yTo);
        Animation alpha = new AlphaAnimation(alphaFrom, alphaTo);
        animSet.addAnimation(trans);
        animSet.addAnimation(alpha);
        animSet.setDuration(duration);
        return animSet;
    }
    private void startAnimation() {
        mFlipper.showNext();
    }
    private void stopAnimation() {
        mFlipper.stopFlipping();
    }
    public void onAnimationEnd(Animation animation) {
        if (!mLeftArrowBtn.isPressed() && !mRightArrowBtn.isPressed()) {
            CandidateView cv = (CandidateView) mFlipper.getCurrentView();
            cv.enableActiveHighlight(true);
        }
    }
    public void onAnimationRepeat(Animation animation) {
    }
    public void onAnimationStart(Animation animation) {
    }
}
