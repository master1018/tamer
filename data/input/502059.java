public class SslErrorHandler extends Handler {
    private static final String LOGTAG = "network";
    private LinkedList<LoadListener> mLoaderQueue;
    private Bundle mSslPrefTable;
    private final SslErrorHandler mOriginHandler;
    private final LoadListener mLoadListener;
    private static final int HANDLE_RESPONSE = 100;
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case HANDLE_RESPONSE:
                LoadListener loader = (LoadListener) msg.obj;
                synchronized (SslErrorHandler.this) {
                    handleSslErrorResponse(loader, loader.sslError(),
                            msg.arg1 == 1);
                    mLoaderQueue.remove(loader);
                    fastProcessQueuedSslErrors();
                }
                break;
        }
    }
     SslErrorHandler() {
        mLoaderQueue = new LinkedList<LoadListener>();
        mSslPrefTable = new Bundle();
        mOriginHandler = null;
        mLoadListener = null;
    }
    private SslErrorHandler(SslErrorHandler origin, LoadListener listener) {
        mOriginHandler = origin;
        mLoadListener = listener;
    }
     synchronized boolean saveState(Bundle outState) {
        boolean success = (outState != null);
        if (success) {
            outState.putBundle("ssl-error-handler", mSslPrefTable);
        }
        return success;
    }
     synchronized boolean restoreState(Bundle inState) {
        boolean success = (inState != null);
        if (success) {
            success = inState.containsKey("ssl-error-handler");
            if (success) {
                mSslPrefTable = inState.getBundle("ssl-error-handler");
            }
        }
        return success;
    }
     synchronized void clear() {
        mSslPrefTable.clear();
    }
     synchronized void handleSslErrorRequest(LoadListener loader) {
        if (DebugFlags.SSL_ERROR_HANDLER) {
            Log.v(LOGTAG, "SslErrorHandler.handleSslErrorRequest(): " +
                  "url=" + loader.url());
        }
        if (!loader.cancelled()) {
            mLoaderQueue.offer(loader);
            if (loader == mLoaderQueue.peek()) {
                fastProcessQueuedSslErrors();
            }
        }
    }
     synchronized boolean checkSslPrefTable(LoadListener loader,
            SslError error) {
        final String host = loader.host();
        final int primary = error.getPrimaryError();
        if (DebugFlags.SSL_ERROR_HANDLER) {
            Assert.assertTrue(host != null && primary != 0);
        }
        if (mSslPrefTable.containsKey(host)) {
            if (primary <= mSslPrefTable.getInt(host)) {
                handleSslErrorResponse(loader, error, true);
                return true;
            }
        }
        return false;
    }
    void fastProcessQueuedSslErrors() {
        while (processNextLoader());
    }
    private synchronized boolean processNextLoader() {
        LoadListener loader = mLoaderQueue.peek();
        if (loader != null) {
            if (loader.cancelled()) {
                mLoaderQueue.remove(loader);
                return true;
            }
            SslError error = loader.sslError();
            if (DebugFlags.SSL_ERROR_HANDLER) {
                Assert.assertNotNull(error);
            }
            if (checkSslPrefTable(loader, error)) {
                mLoaderQueue.remove(loader);
                return true;
            }
            CallbackProxy proxy = loader.getFrame().getCallbackProxy();
            proxy.onReceivedSslError(new SslErrorHandler(this, loader), error);
        }
        return false;
    }
    public void proceed() {
        mOriginHandler.sendMessage(
                mOriginHandler.obtainMessage(
                        HANDLE_RESPONSE, 1, 0, mLoadListener));
    }
    public void cancel() {
        mOriginHandler.sendMessage(
                mOriginHandler.obtainMessage(
                        HANDLE_RESPONSE, 0, 0, mLoadListener));
    }
     synchronized void handleSslErrorResponse(LoadListener loader,
            SslError error, boolean proceed) {
        if (DebugFlags.SSL_ERROR_HANDLER) {
            Assert.assertNotNull(loader);
            Assert.assertNotNull(error);
        }
        if (DebugFlags.SSL_ERROR_HANDLER) {
            Log.v(LOGTAG, "SslErrorHandler.handleSslErrorResponse():"
                  + " proceed: " + proceed
                  + " url:" + loader.url());
        }
        if (!loader.cancelled()) {
            if (proceed) {
                int primary = error.getPrimaryError();
                String host = loader.host();
                if (DebugFlags.SSL_ERROR_HANDLER) {
                    Assert.assertTrue(host != null && primary != 0);
                }
                boolean hasKey = mSslPrefTable.containsKey(host);
                if (!hasKey ||
                    primary > mSslPrefTable.getInt(host)) {
                    mSslPrefTable.putInt(host, primary);
                }
            }
            loader.handleSslErrorResponse(proceed);
        }
    }
}
