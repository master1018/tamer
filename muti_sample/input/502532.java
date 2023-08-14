final class WebViewWorker extends Handler {
    private static final String THREAD_NAME = "WebViewWorkerThread";
    private static WebViewWorker sWorkerHandler;
    private static Map<LoadListener, CacheManager.CacheResult> mCacheResultMap
            = new HashMap<LoadListener, CacheManager.CacheResult>();
    static class CacheCreateData {
        LoadListener mListener;
        String mUrl;
        String mMimeType;
        int mStatusCode;
        long mPostId;
        Headers mHeaders;
    }
    static class CacheSaveData {
        LoadListener mListener;
        String mUrl;
        long mPostId;
    }
    static class CacheEncoding {
        LoadListener mListener;
        String mEncoding;
    }
    static class CacheData {
        LoadListener mListener;
        ByteArrayBuilder.Chunk mChunk;
    }
    static synchronized WebViewWorker getHandler() {
        if (sWorkerHandler == null) {
            HandlerThread thread = new HandlerThread(THREAD_NAME,
                    android.os.Process.THREAD_PRIORITY_DEFAULT
                            + android.os.Process.THREAD_PRIORITY_LESS_FAVORABLE);
            thread.start();
            sWorkerHandler = new WebViewWorker(thread.getLooper());
        }
        return sWorkerHandler;
    }
    private WebViewWorker(Looper looper) {
        super(looper);
    }
    private static final int CACHE_TRANSACTION_TICKER_INTERVAL = 60 * 1000;
    private static boolean mCacheTickersBlocked = true;
    static final int MSG_ADD_STREAMLOADER = 101;
    static final int MSG_ADD_HTTPLOADER = 102;
    static final int MSG_CREATE_CACHE = 103;
    static final int MSG_UPDATE_CACHE_ENCODING = 104;
    static final int MSG_APPEND_CACHE = 105;
    static final int MSG_SAVE_CACHE = 106;
    static final int MSG_REMOVE_CACHE = 107;
    static final int MSG_TRIM_CACHE = 108;
    static final int MSG_CLEAR_CACHE = 109;
    static final int MSG_CACHE_TRANSACTION_TICKER = 110;
    static final int MSG_PAUSE_CACHE_TRANSACTION = 111;
    static final int MSG_RESUME_CACHE_TRANSACTION = 112;
    @Override
    public void handleMessage(Message msg) {
        switch(msg.what) {
            case MSG_ADD_STREAMLOADER: {
                StreamLoader loader = (StreamLoader) msg.obj;
                loader.load();
                break;
            }
            case MSG_ADD_HTTPLOADER: {
                FrameLoader loader = (FrameLoader) msg.obj;
                loader.handleHTTPLoad();
                break;
            }
            case MSG_CREATE_CACHE: {
                CacheCreateData data = (CacheCreateData) msg.obj;
                CacheManager.CacheResult cache = CacheManager.createCacheFile(
                        data.mUrl, data.mStatusCode, data.mHeaders,
                        data.mMimeType, data.mPostId, false);
                if (cache != null) {
                    mCacheResultMap.put(data.mListener, cache);
                } else {
                    mCacheResultMap.remove(data.mListener);
                }
                break;
            }
            case MSG_UPDATE_CACHE_ENCODING: {
                CacheEncoding data = (CacheEncoding) msg.obj;
                CacheManager.CacheResult cache = mCacheResultMap
                        .get(data.mListener);
                if (cache != null) {
                    cache.encoding = data.mEncoding;
                }
                break;
            }
            case MSG_APPEND_CACHE: {
                CacheData data = (CacheData) msg.obj;
                CacheManager.CacheResult cache = mCacheResultMap
                        .get(data.mListener);
                if (cache != null) {
                    cache.contentLength += data.mChunk.mLength;
                    if (cache.contentLength > CacheManager.CACHE_MAX_SIZE) {
                        CacheManager.cleanupCacheFile(cache);
                        mCacheResultMap.remove(data.mListener);
                    } else {
                        try {
                            cache.outStream.write(data.mChunk.mArray, 0,
                                    data.mChunk.mLength);
                        } catch (IOException e) {
                            CacheManager.cleanupCacheFile(cache);
                            mCacheResultMap.remove(data.mListener);
                        }
                    }
                }
                data.mChunk.release();
                break;
            }
            case MSG_SAVE_CACHE: {
                CacheSaveData data = (CacheSaveData) msg.obj;
                CacheManager.CacheResult cache = mCacheResultMap
                        .get(data.mListener);
                if (cache != null) {
                    CacheManager.saveCacheFile(data.mUrl, data.mPostId, cache);
                    mCacheResultMap.remove(data.mListener);
                }
                break;
            }
            case MSG_REMOVE_CACHE: {
                LoadListener listener = (LoadListener) msg.obj;
                CacheManager.CacheResult cache = mCacheResultMap.get(listener);
                if (cache != null) {
                    CacheManager.cleanupCacheFile(cache);
                    mCacheResultMap.remove(listener);
                }
                break;
            }
            case MSG_TRIM_CACHE: {
                CacheManager.trimCacheIfNeeded();
                break;
            }
            case MSG_CLEAR_CACHE: {
                CacheManager.clearCache();
                break;
            }
            case MSG_CACHE_TRANSACTION_TICKER: {
                if (!mCacheTickersBlocked) {
                    CacheManager.endTransaction();
                    CacheManager.startTransaction();
                    sendEmptyMessageDelayed(MSG_CACHE_TRANSACTION_TICKER,
                            CACHE_TRANSACTION_TICKER_INTERVAL);
                }
                break;
            }
            case MSG_PAUSE_CACHE_TRANSACTION: {
                if (CacheManager.disableTransaction()) {
                    mCacheTickersBlocked = true;
                    removeMessages(MSG_CACHE_TRANSACTION_TICKER);
                }
                break;
            }
            case MSG_RESUME_CACHE_TRANSACTION: {
                if (CacheManager.enableTransaction()) {
                    mCacheTickersBlocked = false;
                    sendEmptyMessageDelayed(MSG_CACHE_TRANSACTION_TICKER,
                            CACHE_TRANSACTION_TICKER_INTERVAL);
                }
                break;
            }
        }
    }
}
