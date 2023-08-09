public class CandidateView extends View {
    private static final float MIN_ITEM_WIDTH = 22;
    private static final String SUSPENSION_POINTS = "...";
    private int mContentWidth;
    private int mContentHeight;
    private boolean mShowFootnote = true;
    private BalloonHint mBalloonHint;
    private int mHintPositionToInputView[] = new int[2];
    private DecodingInfo mDecInfo;
    private CandidateViewListener mCvListener;
    private ArrowUpdater mArrowUpdater;
    private boolean mUpdateArrowStatusWhenDraw = false;
    private int mPageNo;
    private int mActiveCandInPage;
    private boolean mEnableActiveHighlight = true;
    private int mPageNoCalculated = -1;
    private Drawable mActiveCellDrawable;
    private Drawable mSeparatorDrawable;
    private int mImeCandidateColor;
    private int mRecommendedCandidateColor;
    private int mNormalCandidateColor;
    private int mActiveCandidateColor;
    private int mImeCandidateTextSize;
    private int mRecommendedCandidateTextSize;
    private int mCandidateTextSize;
    private Paint mCandidatesPaint;
    private Paint mFootnotePaint;
    private float mSuspensionPointsWidth;
    private RectF mActiveCellRect;
    private float mCandidateMargin;
    private float mCandidateMarginExtra;
    private Vector<RectF> mCandRects;
    private FontMetricsInt mFmiCandidates;
    private FontMetricsInt mFmiFootnote;
    private PressTimer mTimer = new PressTimer();
    private GestureDetector mGestureDetector;
    private int mLocationTmp[] = new int[2];
    public CandidateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources r = context.getResources();
        Configuration conf = r.getConfiguration();
        if (conf.keyboard == Configuration.KEYBOARD_NOKEYS
                || conf.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            mShowFootnote = false;
        }
        mActiveCellDrawable = r.getDrawable(R.drawable.candidate_hl_bg);
        mSeparatorDrawable = r.getDrawable(R.drawable.candidates_vertical_line);
        mCandidateMargin = r.getDimension(R.dimen.candidate_margin_left_right);
        mImeCandidateColor = r.getColor(R.color.candidate_color);
        mRecommendedCandidateColor = r.getColor(R.color.recommended_candidate_color);
        mNormalCandidateColor = mImeCandidateColor;
        mActiveCandidateColor = r.getColor(R.color.active_candidate_color);
        mCandidatesPaint = new Paint();
        mCandidatesPaint.setAntiAlias(true);
        mFootnotePaint = new Paint();
        mFootnotePaint.setAntiAlias(true);
        mFootnotePaint.setColor(r.getColor(R.color.footnote_color));
        mActiveCellRect = new RectF();
        mCandRects = new Vector<RectF>();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mOldWidth = mMeasuredWidth;
        int mOldHeight = mMeasuredHeight;
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(),
                widthMeasureSpec), getDefaultSize(getSuggestedMinimumHeight(),
                heightMeasureSpec));
        if (mOldWidth != mMeasuredWidth || mOldHeight != mMeasuredHeight) {
            onSizeChanged();
        }
    }
    public void initialize(ArrowUpdater arrowUpdater, BalloonHint balloonHint,
            GestureDetector gestureDetector, CandidateViewListener cvListener) {
        mArrowUpdater = arrowUpdater;
        mBalloonHint = balloonHint;
        mGestureDetector = gestureDetector;
        mCvListener = cvListener;
    }
    public void setDecodingInfo(DecodingInfo decInfo) {
        if (null == decInfo) return;
        mDecInfo = decInfo;
        mPageNoCalculated = -1;
        if (mDecInfo.candidatesFromApp()) {
            mNormalCandidateColor = mRecommendedCandidateColor;
            mCandidateTextSize = mRecommendedCandidateTextSize;
        } else {
            mNormalCandidateColor = mImeCandidateColor;
            mCandidateTextSize = mImeCandidateTextSize;
        }
        if (mCandidatesPaint.getTextSize() != mCandidateTextSize) {
            mCandidatesPaint.setTextSize(mCandidateTextSize);
            mFmiCandidates = mCandidatesPaint.getFontMetricsInt();
            mSuspensionPointsWidth =
                    mCandidatesPaint.measureText(SUSPENSION_POINTS);
        }
        mTimer.removeTimer();
    }
    public int getActiveCandiatePosInPage() {
        return mActiveCandInPage;
    }
    public int getActiveCandiatePosGlobal() {
        return mDecInfo.mPageStart.get(mPageNo) + mActiveCandInPage;
    }
    public void showPage(int pageNo, int activeCandInPage,
            boolean enableActiveHighlight) {
        if (null == mDecInfo) return;
        mPageNo = pageNo;
        mActiveCandInPage = activeCandInPage;
        if (mEnableActiveHighlight != enableActiveHighlight) {
            mEnableActiveHighlight = enableActiveHighlight;
        }
        if (!calculatePage(mPageNo)) {
            mUpdateArrowStatusWhenDraw = true;
        } else {
            mUpdateArrowStatusWhenDraw = false;
        }
        invalidate();
    }
    public void enableActiveHighlight(boolean enableActiveHighlight) {
        if (enableActiveHighlight == mEnableActiveHighlight) return;
        mEnableActiveHighlight = enableActiveHighlight;
        invalidate();
    }
    public boolean activeCursorForward() {
        if (!mDecInfo.pageReady(mPageNo)) return false;
        int pageSize = mDecInfo.mPageStart.get(mPageNo + 1)
                - mDecInfo.mPageStart.get(mPageNo);
        if (mActiveCandInPage + 1 < pageSize) {
            showPage(mPageNo, mActiveCandInPage + 1, true);
            return true;
        }
        return false;
    }
    public boolean activeCurseBackward() {
        if (mActiveCandInPage > 0) {
            showPage(mPageNo, mActiveCandInPage - 1, true);
            return true;
        }
        return false;
    }
    private void onSizeChanged() {
        mContentWidth = mMeasuredWidth - mPaddingLeft - mPaddingRight;
        mContentHeight = (int) ((mMeasuredHeight - mPaddingTop - mPaddingBottom) * 0.95f);
        int textSize = 1;
        mCandidatesPaint.setTextSize(textSize);
        mFmiCandidates = mCandidatesPaint.getFontMetricsInt();
        while (mFmiCandidates.bottom - mFmiCandidates.top < mContentHeight) {
            textSize++;
            mCandidatesPaint.setTextSize(textSize);
            mFmiCandidates = mCandidatesPaint.getFontMetricsInt();
        }
        mImeCandidateTextSize = textSize;
        mRecommendedCandidateTextSize = textSize * 3 / 4;
        if (null == mDecInfo) {
            mCandidateTextSize = mImeCandidateTextSize;
            mCandidatesPaint.setTextSize(mCandidateTextSize);
            mFmiCandidates = mCandidatesPaint.getFontMetricsInt();
            mSuspensionPointsWidth =
                mCandidatesPaint.measureText(SUSPENSION_POINTS);
        } else {
            setDecodingInfo(mDecInfo);
        }
        textSize = 1;
        mFootnotePaint.setTextSize(textSize);
        mFmiFootnote = mFootnotePaint.getFontMetricsInt();
        while (mFmiFootnote.bottom - mFmiFootnote.top < mContentHeight / 2) {
            textSize++;
            mFootnotePaint.setTextSize(textSize);
            mFmiFootnote = mFootnotePaint.getFontMetricsInt();
        }
        textSize--;
        mFootnotePaint.setTextSize(textSize);
        mFmiFootnote = mFootnotePaint.getFontMetricsInt();
        mPageNo = 0;
        mActiveCandInPage = 0;
    }
    private boolean calculatePage(int pageNo) {
        if (pageNo == mPageNoCalculated) return true;
        mContentWidth = mMeasuredWidth - mPaddingLeft - mPaddingRight;
        mContentHeight = (int) ((mMeasuredHeight - mPaddingTop - mPaddingBottom) * 0.95f);
        if (mContentWidth <= 0 || mContentHeight <= 0) return false;
        int candSize = mDecInfo.mCandidatesList.size();
        boolean onlyExtraMargin = false;
        int fromPage = mDecInfo.mPageStart.size() - 1;
        if (mDecInfo.mPageStart.size() > pageNo + 1) {
            onlyExtraMargin = true;
            fromPage = pageNo;
        }
        for (int p = fromPage; p <= pageNo; p++) {
            int pStart = mDecInfo.mPageStart.get(p);
            int pSize = 0;
            int charNum = 0;
            float lastItemWidth = 0;
            float xPos;
            xPos = 0;
            xPos += mSeparatorDrawable.getIntrinsicWidth();
            while (xPos < mContentWidth && pStart + pSize < candSize) {
                int itemPos = pStart + pSize;
                String itemStr = mDecInfo.mCandidatesList.get(itemPos);
                float itemWidth = mCandidatesPaint.measureText(itemStr);
                if (itemWidth < MIN_ITEM_WIDTH) itemWidth = MIN_ITEM_WIDTH;
                itemWidth += mCandidateMargin * 2;
                itemWidth += mSeparatorDrawable.getIntrinsicWidth();
                if (xPos + itemWidth < mContentWidth || 0 == pSize) {
                    xPos += itemWidth;
                    lastItemWidth = itemWidth;
                    pSize++;
                    charNum += itemStr.length();
                } else {
                    break;
                }
            }
            if (!onlyExtraMargin) {
                mDecInfo.mPageStart.add(pStart + pSize);
                mDecInfo.mCnToPage.add(mDecInfo.mCnToPage.get(p) + charNum);
            }
            float marginExtra = (mContentWidth - xPos) / pSize / 2;
            if (mContentWidth - xPos > lastItemWidth) {
                if (mCandidateMarginExtra <= marginExtra) {
                    marginExtra = mCandidateMarginExtra;
                }
            } else if (pSize == 1) {
                marginExtra = 0;
            }
            mCandidateMarginExtra = marginExtra;
        }
        mPageNoCalculated = pageNo;
        return true;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null == mDecInfo || mDecInfo.isCandidatesListEmpty()) return;
        calculatePage(mPageNo);
        int pStart = mDecInfo.mPageStart.get(mPageNo);
        int pSize = mDecInfo.mPageStart.get(mPageNo + 1) - pStart;
        float candMargin = mCandidateMargin + mCandidateMarginExtra;
        if (mActiveCandInPage > pSize - 1) {
            mActiveCandInPage = pSize - 1;
        }
        mCandRects.removeAllElements();
        float xPos = mPaddingLeft;
        int yPos = (getMeasuredHeight() -
                (mFmiCandidates.bottom - mFmiCandidates.top)) / 2
                - mFmiCandidates.top;
        xPos += drawVerticalSeparator(canvas, xPos);
        for (int i = 0; i < pSize; i++) {
            float footnoteSize = 0;
            String footnote = null;
            if (mShowFootnote) {
                footnote = Integer.toString(i + 1);
                footnoteSize = mFootnotePaint.measureText(footnote);
                assert (footnoteSize < candMargin);
            }
            String cand = mDecInfo.mCandidatesList.get(pStart + i);
            float candidateWidth = mCandidatesPaint.measureText(cand);
            float centerOffset = 0;
            if (candidateWidth < MIN_ITEM_WIDTH) {
                centerOffset = (MIN_ITEM_WIDTH - candidateWidth) / 2;
                candidateWidth = MIN_ITEM_WIDTH;
            }
            float itemTotalWidth = candidateWidth + 2 * candMargin;
            if (mActiveCandInPage == i && mEnableActiveHighlight) {
                mActiveCellRect.set(xPos, mPaddingTop + 1, xPos
                        + itemTotalWidth, getHeight() - mPaddingBottom - 1);
                mActiveCellDrawable.setBounds((int) mActiveCellRect.left,
                        (int) mActiveCellRect.top, (int) mActiveCellRect.right,
                        (int) mActiveCellRect.bottom);
                mActiveCellDrawable.draw(canvas);
            }
            if (mCandRects.size() < pSize) mCandRects.add(new RectF());
            mCandRects.elementAt(i).set(xPos - 1, yPos + mFmiCandidates.top,
                    xPos + itemTotalWidth + 1, yPos + mFmiCandidates.bottom);
            if (mShowFootnote) {
                canvas.drawText(footnote, xPos + (candMargin - footnoteSize)
                        / 2, yPos, mFootnotePaint);
            }
            xPos += candMargin;
            if (candidateWidth > mContentWidth - xPos - centerOffset) {
                cand = getLimitedCandidateForDrawing(cand,
                        mContentWidth - xPos - centerOffset);
            }
            if (mActiveCandInPage == i && mEnableActiveHighlight) {
                mCandidatesPaint.setColor(mActiveCandidateColor);
            } else {
                mCandidatesPaint.setColor(mNormalCandidateColor);
            }
            canvas.drawText(cand, xPos + centerOffset, yPos,
                    mCandidatesPaint);
            xPos += candidateWidth + candMargin;
            xPos += drawVerticalSeparator(canvas, xPos);
        }
        if (null != mArrowUpdater && mUpdateArrowStatusWhenDraw) {
            mArrowUpdater.updateArrowStatus();
            mUpdateArrowStatusWhenDraw = false;
        }
    }
    private String getLimitedCandidateForDrawing(String rawCandidate,
            float widthToDraw) {
        int subLen = rawCandidate.length();
        if (subLen <= 1) return rawCandidate;
        do {
            subLen--;
            float width = mCandidatesPaint.measureText(rawCandidate, 0, subLen);
            if (width + mSuspensionPointsWidth <= widthToDraw || 1 >= subLen) {
                return rawCandidate.substring(0, subLen) +
                        SUSPENSION_POINTS;
            }
        } while (true);
    }
    private float drawVerticalSeparator(Canvas canvas, float xPos) {
        mSeparatorDrawable.setBounds((int) xPos, mPaddingTop, (int) xPos
                + mSeparatorDrawable.getIntrinsicWidth(), getMeasuredHeight()
                - mPaddingBottom);
        mSeparatorDrawable.draw(canvas);
        return mSeparatorDrawable.getIntrinsicWidth();
    }
    private int mapToItemInPage(int x, int y) {
        if (!mDecInfo.pageReady(mPageNo) || mPageNoCalculated != mPageNo
                || mCandRects.size() == 0) {
            return -1;
        }
        int pageStart = mDecInfo.mPageStart.get(mPageNo);
        int pageSize = mDecInfo.mPageStart.get(mPageNo + 1) - pageStart;
        if (mCandRects.size() < pageSize) {
            return -1;
        }
        float nearestDis = Float.MAX_VALUE;
        int nearest = -1;
        for (int i = 0; i < pageSize; i++) {
            RectF r = mCandRects.elementAt(i);
            if (r.left < x && r.right > x && r.top < y && r.bottom > y) {
                return i;
            }
            float disx = (r.left + r.right) / 2 - x;
            float disy = (r.top + r.bottom) / 2 - y;
            float dis = disx * disx + disy * disy;
            if (dis < nearestDis) {
                nearestDis = dis;
                nearest = i;
            }
        }
        return nearest;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
    public boolean onTouchEventReal(MotionEvent event) {
        if (null == mDecInfo || !mDecInfo.pageReady(mPageNo)
                || mPageNoCalculated != mPageNo) return true;
        int x, y;
        x = (int) event.getX();
        y = (int) event.getY();
        if (mGestureDetector.onTouchEvent(event)) {
            mTimer.removeTimer();
            mBalloonHint.delayedDismiss(0);
            return true;
        }
        int clickedItemInPage = -1;
        switch (event.getAction()) {
        case MotionEvent.ACTION_UP:
            clickedItemInPage = mapToItemInPage(x, y);
            if (clickedItemInPage >= 0) {
                invalidate();
                mCvListener.onClickChoice(clickedItemInPage
                        + mDecInfo.mPageStart.get(mPageNo));
            }
            mBalloonHint.delayedDismiss(BalloonHint.TIME_DELAY_DISMISS);
            break;
        case MotionEvent.ACTION_DOWN:
            clickedItemInPage = mapToItemInPage(x, y);
            if (clickedItemInPage >= 0) {
                showBalloon(clickedItemInPage, true);
                mTimer.startTimer(BalloonHint.TIME_DELAY_SHOW, mPageNo,
                        clickedItemInPage);
            }
            break;
        case MotionEvent.ACTION_CANCEL:
            break;
        case MotionEvent.ACTION_MOVE:
            clickedItemInPage = mapToItemInPage(x, y);
            if (clickedItemInPage >= 0
                    && (clickedItemInPage != mTimer.getActiveCandOfPageToShow() || mPageNo != mTimer
                            .getPageToShow())) {
                showBalloon(clickedItemInPage, true);
                mTimer.startTimer(BalloonHint.TIME_DELAY_SHOW, mPageNo,
                        clickedItemInPage);
            }
        }
        return true;
    }
    private void showBalloon(int candPos, boolean delayedShow) {
        mBalloonHint.removeTimer();
        RectF r = mCandRects.elementAt(candPos);
        int desired_width = (int) (r.right - r.left);
        int desired_height = (int) (r.bottom - r.top);
        mBalloonHint.setBalloonConfig(mDecInfo.mCandidatesList
                .get(mDecInfo.mPageStart.get(mPageNo) + candPos), 44, true,
                mImeCandidateColor, desired_width, desired_height);
        getLocationOnScreen(mLocationTmp);
        mHintPositionToInputView[0] = mLocationTmp[0]
                + (int) (r.left - (mBalloonHint.getWidth() - desired_width) / 2);
        mHintPositionToInputView[1] = -mBalloonHint.getHeight();
        long delay = BalloonHint.TIME_DELAY_SHOW;
        if (!delayedShow) delay = 0;
        mBalloonHint.dismiss();
        if (!mBalloonHint.isShowing()) {
            mBalloonHint.delayedShow(delay, mHintPositionToInputView);
        } else {
            mBalloonHint.delayedUpdate(0, mHintPositionToInputView, -1, -1);
        }
    }
    private class PressTimer extends Handler implements Runnable {
        private boolean mTimerPending = false;
        private int mPageNoToShow;
        private int mActiveCandOfPage;
        public PressTimer() {
            super();
        }
        public void startTimer(long afterMillis, int pageNo, int activeInPage) {
            mTimer.removeTimer();
            postDelayed(this, afterMillis);
            mTimerPending = true;
            mPageNoToShow = pageNo;
            mActiveCandOfPage = activeInPage;
        }
        public int getPageToShow() {
            return mPageNoToShow;
        }
        public int getActiveCandOfPageToShow() {
            return mActiveCandOfPage;
        }
        public boolean removeTimer() {
            if (mTimerPending) {
                mTimerPending = false;
                removeCallbacks(this);
                return true;
            }
            return false;
        }
        public boolean isPending() {
            return mTimerPending;
        }
        public void run() {
            if (mPageNoToShow >= 0 && mActiveCandOfPage >= 0) {
                showPage(mPageNoToShow, mActiveCandOfPage, true);
                invalidate();
            }
            mTimerPending = false;
        }
    }
}
