public class ComposingView extends View {
    public enum ComposingStatus {
        SHOW_PINYIN, SHOW_STRING_LOWERCASE, EDIT_PINYIN,
    }
    private static final int LEFT_RIGHT_MARGIN = 5;
    private Paint mPaint;
    private Drawable mHlDrawable;
    private Drawable mCursor;
    private FontMetricsInt mFmi;
    private int mStrColor;
    private int mStrColorHl;
    private int mStrColorIdle;
    private int mFontSize;
    private ComposingStatus mComposingStatus;
    PinyinIME.DecodingInfo mDecInfo;
    public ComposingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources r = context.getResources();
        mHlDrawable = r.getDrawable(R.drawable.composing_hl_bg);
        mCursor = r.getDrawable(R.drawable.composing_area_cursor);
        mStrColor = r.getColor(R.color.composing_color);
        mStrColorHl = r.getColor(R.color.composing_color_hl);
        mStrColorIdle = r.getColor(R.color.composing_color_idle);
        mFontSize = r.getDimensionPixelSize(R.dimen.composing_height);
        mPaint = new Paint();
        mPaint.setColor(mStrColor);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mFontSize);
        mFmi = mPaint.getFontMetricsInt();
    }
    public void reset() {
        mComposingStatus = ComposingStatus.SHOW_PINYIN;
    }
    public void setDecodingInfo(PinyinIME.DecodingInfo decInfo,
            PinyinIME.ImeState imeStatus) {
        mDecInfo = decInfo;
        if (PinyinIME.ImeState.STATE_INPUT == imeStatus) {
            mComposingStatus = ComposingStatus.SHOW_PINYIN;
            mDecInfo.moveCursorToEdge(false);
        } else {
            if (decInfo.getFixedLen() != 0
                    || ComposingStatus.EDIT_PINYIN == mComposingStatus) {
                mComposingStatus = ComposingStatus.EDIT_PINYIN;
            } else {
                mComposingStatus = ComposingStatus.SHOW_STRING_LOWERCASE;
            }
            mDecInfo.moveCursor(0);
        }
        measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        requestLayout();
        invalidate();
    }
    public boolean moveCursor(int keyCode) {
        if (keyCode != KeyEvent.KEYCODE_DPAD_LEFT
                && keyCode != KeyEvent.KEYCODE_DPAD_RIGHT) return false;
        if (ComposingStatus.EDIT_PINYIN == mComposingStatus) {
            int offset = 0;
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT)
                offset = -1;
            else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) offset = 1;
            mDecInfo.moveCursor(offset);
        } else if (ComposingStatus.SHOW_STRING_LOWERCASE == mComposingStatus) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                mComposingStatus = ComposingStatus.EDIT_PINYIN;
                measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                requestLayout();
            }
        }
        invalidate();
        return true;
    }
    public ComposingStatus getComposingStatus() {
        return mComposingStatus;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float width;
        int height;
        height = mFmi.bottom - mFmi.top + mPaddingTop + mPaddingBottom;
        if (null == mDecInfo) {
            width = 0;
        } else {
            width = mPaddingLeft + mPaddingRight + LEFT_RIGHT_MARGIN * 2;
            String str;
            if (ComposingStatus.SHOW_STRING_LOWERCASE == mComposingStatus) {
                str = mDecInfo.getOrigianlSplStr().toString();
            } else {
                str = mDecInfo.getComposingStrForDisplay();
            }
            width += mPaint.measureText(str, 0, str.length());
        }
        setMeasuredDimension((int) (width + 0.5f), height);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        if (ComposingStatus.EDIT_PINYIN == mComposingStatus
                || ComposingStatus.SHOW_PINYIN == mComposingStatus) {
            drawForPinyin(canvas);
            return;
        }
        float x, y;
        x = mPaddingLeft + LEFT_RIGHT_MARGIN;
        y = -mFmi.top + mPaddingTop;
        mPaint.setColor(mStrColorHl);
        mHlDrawable.setBounds(mPaddingLeft, mPaddingTop, getWidth()
                - mPaddingRight, getHeight() - mPaddingBottom);
        mHlDrawable.draw(canvas);
        String splStr = mDecInfo.getOrigianlSplStr().toString();
        canvas.drawText(splStr, 0, splStr.length(), x, y, mPaint);
    }
    private void drawCursor(Canvas canvas, float x) {
        mCursor.setBounds((int) x, mPaddingTop, (int) x
                + mCursor.getIntrinsicWidth(), getHeight() - mPaddingBottom);
        mCursor.draw(canvas);
    }
    private void drawForPinyin(Canvas canvas) {
        float x, y;
        x = mPaddingLeft + LEFT_RIGHT_MARGIN;
        y = -mFmi.top + mPaddingTop;
        mPaint.setColor(mStrColor);
        int cursorPos = mDecInfo.getCursorPosInCmpsDisplay();
        int cmpsPos = cursorPos;
        String cmpsStr = mDecInfo.getComposingStrForDisplay();
        int activeCmpsLen = mDecInfo.getActiveCmpsDisplayLen();
        if (cursorPos > activeCmpsLen) cmpsPos = activeCmpsLen;
        canvas.drawText(cmpsStr, 0, cmpsPos, x, y, mPaint);
        x += mPaint.measureText(cmpsStr, 0, cmpsPos);
        if (cursorPos <= activeCmpsLen) {
            if (ComposingStatus.EDIT_PINYIN == mComposingStatus) {
                drawCursor(canvas, x);
            }
            canvas.drawText(cmpsStr, cmpsPos, activeCmpsLen, x, y, mPaint);
        }
        x += mPaint.measureText(cmpsStr, cmpsPos, activeCmpsLen);
        if (cmpsStr.length() > activeCmpsLen) {
            mPaint.setColor(mStrColorIdle);
            int oriPos = activeCmpsLen;
            if (cursorPos > activeCmpsLen) {
                if (cursorPos > cmpsStr.length()) cursorPos = cmpsStr.length();
                canvas.drawText(cmpsStr, oriPos, cursorPos, x, y, mPaint);
                x += mPaint.measureText(cmpsStr, oriPos, cursorPos);
                if (ComposingStatus.EDIT_PINYIN == mComposingStatus) {
                    drawCursor(canvas, x);
                }
                oriPos = cursorPos;
            }
            canvas.drawText(cmpsStr, oriPos, cmpsStr.length(), x, y, mPaint);
        }
    }
}
