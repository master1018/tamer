class HTML5VideoViewProxy extends Handler
                          implements MediaPlayer.OnPreparedListener,
                          MediaPlayer.OnCompletionListener,
                          MediaPlayer.OnErrorListener {
    private static final String LOGTAG = "HTML5VideoViewProxy";
    private static final int PLAY                = 100;
    private static final int SEEK                = 101;
    private static final int PAUSE               = 102;
    private static final int ERROR               = 103;
    private static final int LOAD_DEFAULT_POSTER = 104;
    private static final int PREPARED          = 200;
    private static final int ENDED             = 201;
    private static final int POSTER_FETCHED    = 202;
    private static final String COOKIE = "Cookie";
    private static final int TIMEUPDATE = 300;
    int mNativePointer;
    private Handler mWebCoreHandler;
    private WebView mWebView;
    private Bitmap mPoster;
    private PosterDownloader mPosterDownloader;
    private int mSeekPosition;
    private static final class VideoPlayer {
        private static HTML5VideoViewProxy mCurrentProxy;
        private static VideoView mVideoView;
        private static View mProgressView;
        private static FrameLayout mLayout;
        private static Timer mTimer;
        private static final class TimeupdateTask extends TimerTask {
            private HTML5VideoViewProxy mProxy;
            public TimeupdateTask(HTML5VideoViewProxy proxy) {
                mProxy = proxy;
            }
            public void run() {
                mProxy.onTimeupdate();
            }
        }
        private static final int TIMEUPDATE_PERIOD = 250;  
        private static final WebChromeClient.CustomViewCallback mCallback =
            new WebChromeClient.CustomViewCallback() {
                public void onCustomViewHidden() {
                    mTimer.cancel();
                    mTimer = null;
                    if (mVideoView.isPlaying()) {
                        mVideoView.stopPlayback();
                    }
                    mCurrentProxy.dispatchOnEnded();
                    mCurrentProxy = null;
                    mLayout.removeView(mVideoView);
                    mVideoView = null;
                    if (mProgressView != null) {
                        mLayout.removeView(mProgressView);
                        mProgressView = null;
                    }
                    mLayout = null;
                }
            };
        public static void play(String url, int time, HTML5VideoViewProxy proxy,
                WebChromeClient client) {
            if (mCurrentProxy == proxy) {
                if (!mVideoView.isPlaying()) {
                    mVideoView.start();
                }
                return;
            }
            if (mCurrentProxy != null) {
                proxy.dispatchOnEnded();
                return;
            }
            mCurrentProxy = proxy;
            mLayout = new FrameLayout(proxy.getContext());
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER);
            mVideoView = new VideoView(proxy.getContext());
            mVideoView.setWillNotDraw(false);
            mVideoView.setMediaController(new MediaController(proxy.getContext()));
            String cookieValue = CookieManager.getInstance().getCookie(url);
            Map<String, String> headers = null;
            if (cookieValue != null) {
                headers = new HashMap<String, String>();
                headers.put(COOKIE, cookieValue);
            }
            mVideoView.setVideoURI(Uri.parse(url), headers);
            mVideoView.setOnCompletionListener(proxy);
            mVideoView.setOnPreparedListener(proxy);
            mVideoView.setOnErrorListener(proxy);
            mVideoView.seekTo(time);
            mLayout.addView(mVideoView, layoutParams);
            mProgressView = client.getVideoLoadingProgressView();
            if (mProgressView != null) {
                mLayout.addView(mProgressView, layoutParams);
                mProgressView.setVisibility(View.VISIBLE);
            }
            mLayout.setVisibility(View.VISIBLE);
            mTimer = new Timer();
            mVideoView.start();
            client.onShowCustomView(mLayout, mCallback);
        }
        public static boolean isPlaying(HTML5VideoViewProxy proxy) {
            return (mCurrentProxy == proxy && mVideoView != null && mVideoView.isPlaying());
        }
        public static int getCurrentPosition() {
            int currentPosMs = 0;
            if (mVideoView != null) {
                currentPosMs = mVideoView.getCurrentPosition();
            }
            return currentPosMs;
        }
        public static void seek(int time, HTML5VideoViewProxy proxy) {
            if (mCurrentProxy == proxy && time >= 0 && mVideoView != null) {
                mVideoView.seekTo(time);
            }
        }
        public static void pause(HTML5VideoViewProxy proxy) {
            if (mCurrentProxy == proxy && mVideoView != null) {
                mVideoView.pause();
                mTimer.purge();
            }
        }
        public static void onPrepared() {
            if (mProgressView == null || mLayout == null) {
                return;
            }
            mTimer.schedule(new TimeupdateTask(mCurrentProxy), TIMEUPDATE_PERIOD, TIMEUPDATE_PERIOD);
            mProgressView.setVisibility(View.GONE);
            mLayout.removeView(mProgressView);
            mProgressView = null;
        }
    }
    public void onPrepared(MediaPlayer mp) {
        VideoPlayer.onPrepared();
        Message msg = Message.obtain(mWebCoreHandler, PREPARED);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("dur", new Integer(mp.getDuration()));
        map.put("width", new Integer(mp.getVideoWidth()));
        map.put("height", new Integer(mp.getVideoHeight()));
        msg.obj = map;
        mWebCoreHandler.sendMessage(msg);
    }
    public void onCompletion(MediaPlayer mp) {
        sendMessage(obtainMessage(ENDED));
    }
    public boolean onError(MediaPlayer mp, int what, int extra) {
        sendMessage(obtainMessage(ERROR));
        return false;
    }
    public void dispatchOnEnded() {
        Message msg = Message.obtain(mWebCoreHandler, ENDED);
        mWebCoreHandler.sendMessage(msg);
    }
    public void onTimeupdate() {
        sendMessage(obtainMessage(TIMEUPDATE));
    }
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case PLAY: {
                String url = (String) msg.obj;
                WebChromeClient client = mWebView.getWebChromeClient();
                if (client != null) {
                    VideoPlayer.play(url, mSeekPosition, this, client);
                }
                break;
            }
            case SEEK: {
                Integer time = (Integer) msg.obj;
                mSeekPosition = time;
                VideoPlayer.seek(mSeekPosition, this);
                break;
            }
            case PAUSE: {
                VideoPlayer.pause(this);
                break;
            }
            case ENDED:
            case ERROR: {
                WebChromeClient client = mWebView.getWebChromeClient();
                if (client != null) {
                    client.onHideCustomView();
                }
                break;
            }
            case LOAD_DEFAULT_POSTER: {
                WebChromeClient client = mWebView.getWebChromeClient();
                if (client != null) {
                    doSetPoster(client.getDefaultVideoPoster());
                }
                break;
            }
            case TIMEUPDATE: {
                if (VideoPlayer.isPlaying(this)) {
                    sendTimeupdate();
                }
                break;
            }
        }
    }
    private static final class PosterDownloader implements EventHandler {
        private static RequestQueue mRequestQueue;
        private static int mQueueRefCount = 0;
        private String mUrl;
        private final HTML5VideoViewProxy mProxy;
        private ByteArrayOutputStream mPosterBytes;
        private RequestHandle mRequestHandle;
        private int mStatusCode;
        private Headers mHeaders;
        private Handler mHandler;
        public PosterDownloader(String url, HTML5VideoViewProxy proxy) {
            mUrl = url;
            mProxy = proxy;
            mHandler = new Handler();
        }
        public void start() {
            retainQueue();
            mRequestHandle = mRequestQueue.queueRequest(mUrl, "GET", null, this, null, 0);
        }
        public void cancelAndReleaseQueue() {
            if (mRequestHandle != null) {
                mRequestHandle.cancel();
                mRequestHandle = null;
            }
            releaseQueue();
        }
        public void status(int major_version,
                int minor_version,
                int code,
                String reason_phrase) {
            mStatusCode = code;
        }
        public void headers(Headers headers) {
            mHeaders = headers;
        }
        public void data(byte[] data, int len) {
            if (mPosterBytes == null) {
                mPosterBytes = new ByteArrayOutputStream();
            }
            mPosterBytes.write(data, 0, len);
        }
        public void endData() {
            if (mStatusCode == 200) {
                if (mPosterBytes.size() > 0) {
                    Bitmap poster = BitmapFactory.decodeByteArray(
                            mPosterBytes.toByteArray(), 0, mPosterBytes.size());
                    mProxy.doSetPoster(poster);
                }
                cleanup();
            } else if (mStatusCode >= 300 && mStatusCode < 400) {
                mUrl = mHeaders.getLocation();
                if (mUrl != null) {
                    mHandler.post(new Runnable() {
                       public void run() {
                           if (mRequestHandle != null) {
                               mRequestHandle.setupRedirect(mUrl, mStatusCode,
                                       new HashMap<String, String>());
                           }
                       }
                    });
                }
            }
        }
        public void certificate(SslCertificate certificate) {
        }
        public void error(int id, String description) {
            cleanup();
        }
        public boolean handleSslErrorRequest(SslError error) {
            return false;
        }
        private void cleanup() {
            if (mPosterBytes != null) {
                try {
                    mPosterBytes.close();
                } catch (IOException ignored) {
                } finally {
                    mPosterBytes = null;
                }
            }
        }
        private void retainQueue() {
            if (mRequestQueue == null) {
                mRequestQueue = new RequestQueue(mProxy.getContext());
            }
            mQueueRefCount++;
        }
        private void releaseQueue() {
            if (mQueueRefCount == 0) {
                return;
            }
            if (--mQueueRefCount == 0) {
                mRequestQueue.shutdown();
                mRequestQueue = null;
            }
        }
    }
    private HTML5VideoViewProxy(WebView webView, int nativePtr) {
        super(Looper.getMainLooper());
        mWebView = webView;
        mNativePointer = nativePtr;
        createWebCoreHandler();
    }
    private void createWebCoreHandler() {
        mWebCoreHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case PREPARED: {
                        Map<String, Object> map = (Map<String, Object>) msg.obj;
                        Integer duration = (Integer) map.get("dur");
                        Integer width = (Integer) map.get("width");
                        Integer height = (Integer) map.get("height");
                        nativeOnPrepared(duration.intValue(), width.intValue(),
                                height.intValue(), mNativePointer);
                        break;
                    }
                    case ENDED:
                        nativeOnEnded(mNativePointer);
                        break;
                    case POSTER_FETCHED:
                        Bitmap poster = (Bitmap) msg.obj;
                        nativeOnPosterFetched(poster, mNativePointer);
                        break;
                    case TIMEUPDATE:
                        nativeOnTimeupdate(msg.arg1, mNativePointer);
                        break;
                }
            }
        };
    }
    private void doSetPoster(Bitmap poster) {
        if (poster == null) {
            return;
        }
        mPoster = poster;
        Message msg = Message.obtain(mWebCoreHandler, POSTER_FETCHED);
        msg.obj = poster;
        mWebCoreHandler.sendMessage(msg);
    }
    private void sendTimeupdate() {
        Message msg = Message.obtain(mWebCoreHandler, TIMEUPDATE);
        msg.arg1 = VideoPlayer.getCurrentPosition();
        mWebCoreHandler.sendMessage(msg);
    }
    public Context getContext() {
        return mWebView.getContext();
    }
    public void play(String url) {
        if (url == null) {
            return;
        }
        Message message = obtainMessage(PLAY);
        message.obj = url;
        sendMessage(message);
    }
    public void seek(int time) {
        Message message = obtainMessage(SEEK);
        message.obj = new Integer(time);
        sendMessage(message);
    }
    public void pause() {
        Message message = obtainMessage(PAUSE);
        sendMessage(message);
    }
    public void teardown() {
        if (mPosterDownloader != null) {
            mPosterDownloader.cancelAndReleaseQueue();
        }
        mNativePointer = 0;
    }
    public void loadPoster(String url) {
        if (url == null) {
            Message message = obtainMessage(LOAD_DEFAULT_POSTER);
            sendMessage(message);
            return;
        }
        if (mPosterDownloader != null) {
            mPosterDownloader.cancelAndReleaseQueue();
        }
        mPosterDownloader = new PosterDownloader(url, this);
        mPosterDownloader.start();
    }
    public static HTML5VideoViewProxy getInstance(WebViewCore webViewCore, int nativePtr) {
        return new HTML5VideoViewProxy(webViewCore.getWebView(), nativePtr);
    }
    private native void nativeOnPrepared(int duration, int width, int height, int nativePointer);
    private native void nativeOnEnded(int nativePointer);
    private native void nativeOnPosterFetched(Bitmap poster, int nativePointer);
    private native void nativeOnTimeupdate(int position, int nativePointer);
}
