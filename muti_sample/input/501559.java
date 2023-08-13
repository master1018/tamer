abstract class Ticker {
    private static final int TICKER_SEGMENT_DELAY = 3000;
    private final class Segment {
        NotificationData notificationData;
        Drawable icon;
        CharSequence text;
        int current;
        int next;
        boolean first;
        StaticLayout getLayout(CharSequence substr) {
            int w = mTextSwitcher.getWidth() - mTextSwitcher.getPaddingLeft()
                    - mTextSwitcher.getPaddingRight();
            return new StaticLayout(substr, mPaint, w, Alignment.ALIGN_NORMAL, 1, 0, true);
        }
        CharSequence rtrim(CharSequence substr, int start, int end) {
            while (end > start && !TextUtils.isGraphic(substr.charAt(end-1))) {
                end--;
            }
            if (end > start) {
                return substr.subSequence(start, end);
            }
            return null;
        }
        CharSequence getText() {
            if (this.current > this.text.length()) {
                return null;
            }
            CharSequence substr = this.text.subSequence(this.current, this.text.length());
            StaticLayout l = getLayout(substr);
            int lineCount = l.getLineCount();
            if (lineCount > 0) {
                int start = l.getLineStart(0);
                int end = l.getLineEnd(0);
                this.next = this.current + end;
                return rtrim(substr, start, end);
            } else {
                throw new RuntimeException("lineCount=" + lineCount + " current=" + current +
                        " text=" + text);
            }
        }
        CharSequence advance() {
            this.first = false;
            int index = this.next;
            final int len = this.text.length();
            while (index < len && !TextUtils.isGraphic(this.text.charAt(index))) {
                index++;
            }
            if (index >= len) {
                return null;
            }
            CharSequence substr = this.text.subSequence(index, this.text.length());
            StaticLayout l = getLayout(substr);
            final int lineCount = l.getLineCount();
            int i;
            for (i=0; i<lineCount; i++) {
                int start = l.getLineStart(i);
                int end = l.getLineEnd(i);
                if (i == lineCount-1) {
                    this.next = len;
                } else {
                    this.next = index + l.getLineStart(i+1);
                }
                CharSequence result = rtrim(substr, start, end);
                if (result != null) {
                    this.current = index + start;
                    return result;
                }
            }
            this.current = len;
            return null;
        }
        Segment(NotificationData n, Drawable icon, CharSequence text) {
            this.notificationData = n;
            this.icon = icon;
            this.text = text;
            int index = 0;
            final int len = text.length();
            while (index < len && !TextUtils.isGraphic(text.charAt(index))) {
                index++;
            }
            this.current = index;
            this.next = index;
            this.first = true;
        }
    };
    private Handler mHandler = new Handler();
    private ArrayList<Segment> mSegments = new ArrayList();
    private TextPaint mPaint;
    private View mTickerView;
    private ImageSwitcher mIconSwitcher;
    private TextSwitcher mTextSwitcher;
    Ticker(Context context, StatusBarView sb) {
        mTickerView = sb.findViewById(R.id.ticker);
        mIconSwitcher = (ImageSwitcher)sb.findViewById(R.id.tickerIcon);
        mIconSwitcher.setInAnimation(
                    AnimationUtils.loadAnimation(context, com.android.internal.R.anim.push_up_in));
        mIconSwitcher.setOutAnimation(
                    AnimationUtils.loadAnimation(context, com.android.internal.R.anim.push_up_out));
        mTextSwitcher = (TextSwitcher)sb.findViewById(R.id.tickerText);
        mTextSwitcher.setInAnimation(
                    AnimationUtils.loadAnimation(context, com.android.internal.R.anim.push_up_in));
        mTextSwitcher.setOutAnimation(
                    AnimationUtils.loadAnimation(context, com.android.internal.R.anim.push_up_out));
        TextView text = (TextView)mTextSwitcher.getChildAt(0);
        mPaint = text.getPaint();
    }
    void addEntry(NotificationData n, Drawable icon, CharSequence text) {
        int initialCount = mSegments.size();
        Segment newSegment = new Segment(n, icon, text);
        for (int i=1; i<initialCount; i++) {
            Segment seg = mSegments.get(i);
            if (n.id == seg.notificationData.id && n.pkg.equals(seg.notificationData.pkg)) {
                mSegments.set(i, newSegment);
                return ;
            }
        }
        mSegments.add(newSegment);
        if (initialCount == 0 && mSegments.size() > 0) {
            Segment seg = mSegments.get(0);
            seg.first = false;
            mIconSwitcher.setAnimateFirstView(false);
            mIconSwitcher.reset();
            mIconSwitcher.setImageDrawable(seg.icon);
            mTextSwitcher.setAnimateFirstView(false);
            mTextSwitcher.reset();
            mTextSwitcher.setText(seg.getText());
            tickerStarting();
            scheduleAdvance();
        }
    }
    void halt() {
        mHandler.removeCallbacks(mAdvanceTicker);
        mSegments.clear();
        tickerHalting();
    }
    void reflowText() {
        if (mSegments.size() > 0) {
            Segment seg = mSegments.get(0);
            CharSequence text = seg.getText();
            mTextSwitcher.setCurrentText(text);
        }
    }
    private Runnable mAdvanceTicker = new Runnable() {
        public void run() {
            while (mSegments.size() > 0) {
                Segment seg = mSegments.get(0);
                if (seg.first) {
                    mIconSwitcher.setImageDrawable(seg.icon);
                }
                CharSequence text = seg.advance();
                if (text == null) {
                    mSegments.remove(0);
                    continue;
                }
                mTextSwitcher.setText(text);
                scheduleAdvance();
                break;
            }
            if (mSegments.size() == 0) {
                tickerDone();
            }
        }
    };
    private void scheduleAdvance() {
        mHandler.postDelayed(mAdvanceTicker, TICKER_SEGMENT_DELAY);
    }
    abstract void tickerStarting();
    abstract void tickerDone();
    abstract void tickerHalting();
}
