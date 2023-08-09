public class WebBackForwardList implements Cloneable, Serializable {
    private int mCurrentIndex;
    private ArrayList<WebHistoryItem> mArray;
    private boolean mClearPending;
    private final CallbackProxy mCallbackProxy;
     WebBackForwardList(CallbackProxy proxy) {
        mCurrentIndex = -1;
        mArray = new ArrayList<WebHistoryItem>();
        mCallbackProxy = proxy;
    }
    public synchronized WebHistoryItem getCurrentItem() {
        return getItemAtIndex(mCurrentIndex);
    }
    public synchronized int getCurrentIndex() {
        return mCurrentIndex;
    }
    public synchronized WebHistoryItem getItemAtIndex(int index) {
        if (index < 0 || index >= getSize()) {
            return null;
        }
        return mArray.get(index);
    }
    public synchronized int getSize() {
        return mArray.size();
    }
     synchronized void setClearPending() {
        mClearPending = true;
    }
     synchronized boolean getClearPending() {
        return mClearPending;
    }
     synchronized void addHistoryItem(WebHistoryItem item) {
        ++mCurrentIndex;
        final int size = mArray.size();
        final int newPos = mCurrentIndex;
        if (newPos != size) {
            for (int i = size - 1; i >= newPos; i--) {
                final WebHistoryItem h = mArray.remove(i);
            }
        }
        mArray.add(item);
        if (mCallbackProxy != null) {
            mCallbackProxy.onNewHistoryItem(item);
        }
    }
     synchronized void close(int nativeFrame) {
        mArray.clear();
        mCurrentIndex = -1;
        nativeClose(nativeFrame);
        mClearPending = false;
    }
    private synchronized void removeHistoryItem(int index) {
        if (DebugFlags.WEB_BACK_FORWARD_LIST && (index != 0)) {
            throw new AssertionError();
        }
        final WebHistoryItem h = mArray.remove(index);
        mCurrentIndex--;
    }
    protected synchronized WebBackForwardList clone() {
        WebBackForwardList l = new WebBackForwardList(null);
        if (mClearPending) {
            l.addHistoryItem(getCurrentItem());
            return l;
        }
        l.mCurrentIndex = mCurrentIndex;
        int size = getSize();
        l.mArray = new ArrayList<WebHistoryItem>(size);
        for (int i = 0; i < size; i++) {
            l.mArray.add(mArray.get(i).clone());
        }
        return l;
    }
     synchronized void setCurrentIndex(int newIndex) {
        mCurrentIndex = newIndex;
        if (mCallbackProxy != null) {
            mCallbackProxy.onIndexChanged(getItemAtIndex(newIndex), newIndex);
        }
    }
     static native synchronized void restoreIndex(int nativeFrame,
            int index);
    private static native void nativeClose(int nativeFrame);
}
