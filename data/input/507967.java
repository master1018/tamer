public class AccountFolderListItem extends LinearLayout {
    public long mAccountId;
    private AccountFolderList.AccountsAdapter mAdapter;
    private boolean mHasFolderButton;
    private boolean mDownEvent;
    private boolean mCachedViewPositions;
    private int mFolderLeft;
    private final static float FOLDER_PAD = 5.0F;
    public AccountFolderListItem(Context context) {
        super(context);
    }
    public AccountFolderListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void bindViewInit(AccountFolderList.AccountsAdapter adapter, boolean hasFolderButton) {
        mAdapter = adapter;
        mCachedViewPositions = false;
        mHasFolderButton = hasFolderButton;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mAdapter.isOnDeletingAccountView(mAccountId)) {
            return true;
        }
        if (!mHasFolderButton) {
            return super.onTouchEvent(event);
        }
        boolean handled = false;
        int touchX = (int) event.getX();
        if (!mCachedViewPositions) {
            float paddingScale = getContext().getResources().getDisplayMetrics().density;
            int folderPadding = (int) ((FOLDER_PAD * paddingScale) + 0.5);
            mFolderLeft = findViewById(R.id.folder_button).getLeft() - folderPadding;
            mCachedViewPositions = true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownEvent = true;
                if (touchX > mFolderLeft) {
                    handled = true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mDownEvent = false;
                break;
            case MotionEvent.ACTION_UP:
                if (mDownEvent) {
                    if (touchX > mFolderLeft) {
                        mAdapter.onClickFolder(this);
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
