public class RequestRectangleVisibleWithInternalScroll extends Activity {
    private int scrollYofBlob = 52;
    private TextView mTextBlob;
    private Button mScrollToBlob;
    public int getScrollYofBlob() {
        return scrollYofBlob;
    }
    public TextView getTextBlob() {
        return mTextBlob;
    }
    public Button getScrollToBlob() {
        return mScrollToBlob;
    }
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.scroll_to_rect_with_internal_scroll);
        mTextBlob = (TextView) findViewById(R.id.blob);
        mTextBlob.scrollBy(0, scrollYofBlob);
        mScrollToBlob = (Button) findViewById(R.id.scrollToBlob);
        mScrollToBlob.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Rect rect = new Rect();
                rect.set(0, 0, 0, mTextBlob.getHeight());
                rect.offset(0, mTextBlob.getScrollY());
                mTextBlob.requestRectangleOnScreen(rect);
            }
        });
    }
}
