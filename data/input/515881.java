public final class LayoutResult implements ILayoutResult {
    private final ILayoutViewInfo mRootView;
    private final BufferedImage mImage;
    private final int mSuccess;
    private final String mErrorMessage;
    public LayoutResult(ILayoutViewInfo rootView, BufferedImage image) {
        mSuccess = SUCCESS;
        mErrorMessage = null;
        mRootView = rootView;
        mImage = image;
    }
    public LayoutResult(int code, String message) {
        mSuccess = code;
        mErrorMessage = message;
        mRootView = null;
        mImage = null;
    }
    public int getSuccess() {
        return mSuccess;
    }
    public String getErrorMessage() {
        return mErrorMessage;
    }
    public BufferedImage getImage() {
        return mImage;
    }
    public ILayoutViewInfo getRootView() {
        return mRootView;
    }
    public static final class LayoutViewInfo implements ILayoutViewInfo {
        private final Object mKey;
        private final String mName;
        private final int mLeft;
        private final int mRight;
        private final int mTop;
        private final int mBottom;
        private ILayoutViewInfo[] mChildren;
        public LayoutViewInfo(String name, Object key, int left, int top, int right, int bottom) {
            mName = name;
            mKey = key;
            mLeft = left;
            mRight = right;
            mTop = top;
            mBottom = bottom;
        }
        public void setChildren(ILayoutViewInfo[] children) {
            mChildren = children;
        }
        public ILayoutViewInfo[] getChildren() {
            return mChildren;
        }
        public Object getViewKey() {
            return mKey;
        }
        public String getName() {
            return mName;
        }
        public int getLeft() {
            return mLeft;
        }
        public int getTop() {
            return mTop;
        }
        public int getRight() {
            return mRight;
        }
        public int getBottom() {
            return mBottom;
        }
    }
}
