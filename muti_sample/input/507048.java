public class MessageListItem extends RelativeLayout {
    public long mMessageId;
    public long mMailboxId;
    public long mAccountId;
    public boolean mRead;
    public boolean mFavorite;
    public boolean mSelected;
    private boolean mAllowBatch;
    private MessageList.MessageListAdapter mAdapter;
    private boolean mDownEvent;
    private boolean mCachedViewPositions;
    private int mCheckRight;
    private int mStarLeft;
    private final static float CHECKMARK_PAD = 10.0F;
    private final static float STAR_PAD = 10.0F;
    public MessageListItem(Context context) {
        super(context);
    }
    public MessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MessageListItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public void bindViewInit(MessageList.MessageListAdapter adapter, boolean allowBatch) {
        mAdapter = adapter;
        mAllowBatch = allowBatch;
        mCachedViewPositions = false;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = false;
        int touchX = (int) event.getX();
        if (!mCachedViewPositions) {
            float paddingScale = getContext().getResources().getDisplayMetrics().density;
            int checkPadding = (int) ((CHECKMARK_PAD * paddingScale) + 0.5);
            int starPadding = (int) ((STAR_PAD * paddingScale) + 0.5);
            mCheckRight = findViewById(R.id.selected).getRight() + checkPadding;
            mStarLeft = findViewById(R.id.favorite).getLeft() - starPadding;
            mCachedViewPositions = true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownEvent = true;
                if ((mAllowBatch && touchX < mCheckRight) || touchX > mStarLeft) {
                    handled = true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mDownEvent = false;
                break;
            case MotionEvent.ACTION_UP:
                if (mDownEvent) {
                    if (mAllowBatch && touchX < mCheckRight) {
                        mSelected = !mSelected;
                        mAdapter.updateSelected(this, mSelected);
                        handled = true;
                    } else if (touchX > mStarLeft) {
                        mFavorite = !mFavorite;
                        mAdapter.updateFavorite(this, mFavorite);
                        handled = true;
                    }
                }
                break;
        }
        if (handled) {
            postInvalidate();
        } else {
            handled = super.onTouchEvent(event);
        }
        return handled;
    }
}
