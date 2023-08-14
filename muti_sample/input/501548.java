abstract class StreamLoader implements Handler.Callback {
    private static final int MSG_STATUS = 100;  
    private static final int MSG_HEADERS = 101; 
    private static final int MSG_DATA = 102;  
    private static final int MSG_END = 103;  
    protected final Context mContext;
    protected final LoadListener mLoadListener; 
    protected InputStream mDataStream; 
    protected long mContentLength; 
    private byte [] mData; 
    private Handler mHandler;
    StreamLoader(LoadListener loadlistener) {
        mLoadListener = loadlistener;
        mContext = loadlistener.getContext();
    }
    protected abstract boolean setupStreamAndSendStatus();
    abstract protected void buildHeaders(Headers headers);
    final void load() {
        synchronized (this) {
            if (mHandler == null) {
                mHandler = new Handler(this);
            }
        }
        if (!mLoadListener.isSynchronous()) {
            mHandler.sendEmptyMessage(MSG_STATUS);
        } else {
            if (setupStreamAndSendStatus()) {
                mData = new byte[8192];
                sendHeaders();
                while (!sendData() && !mLoadListener.cancelled());
                closeStreamAndSendEndData();
                mLoadListener.loadSynchronousMessages();
            }
        }
    }
    public boolean handleMessage(Message msg) {
        if (mLoadListener.isSynchronous()) {
            throw new AssertionError();
        }
        if (mLoadListener.cancelled()) {
            closeStreamAndSendEndData();
            return true;
        }
        switch(msg.what) {
            case MSG_STATUS:
                if (setupStreamAndSendStatus()) {
                    mData = new byte[8192];
                    mHandler.sendEmptyMessage(MSG_HEADERS);
                }
                break;
            case MSG_HEADERS:
                sendHeaders();
                mHandler.sendEmptyMessage(MSG_DATA);
                break;
            case MSG_DATA:
                if (sendData()) {
                    mHandler.sendEmptyMessage(MSG_END);
                } else {
                    mHandler.sendEmptyMessage(MSG_DATA);
                }
                break;
            case MSG_END:
                closeStreamAndSendEndData();
                break;
            default:
                return false;
        }
        return true;
    }
    private void sendHeaders() {
        Headers headers = new Headers();
        if (mContentLength > 0) {
            headers.setContentLength(mContentLength);
        }
        buildHeaders(headers);
        mLoadListener.headers(headers);
    }
    private boolean sendData() {
        if (mDataStream != null) {
            try {
                int amount = mDataStream.read(mData);
                if (amount > 0) {
                    mLoadListener.data(mData, amount);
                    return false;
                }
            } catch (IOException ex) {
                mLoadListener.error(EventHandler.FILE_ERROR, ex.getMessage());
            }
        }
        return true;
    }
    private void closeStreamAndSendEndData() {
        if (mDataStream != null) {
            try {
                mDataStream.close();
            } catch (IOException ex) {
            }
        }
        mLoadListener.endData();
    }
}
