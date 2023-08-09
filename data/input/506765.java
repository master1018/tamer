class HttpDataChannel extends DataChannel implements Runnable, HeartbeatService.Callback {
    private static final int MAX_RETRY_COUNT = 10;
    private static final int INIT_RETRY_DELAY_MS = 5000;
    private static final int MAX_RETRY_DELAY_MS = 300 * 1000;
    private Thread mSendThread;
    private boolean mStopped;
    private boolean mSuspended;
    private boolean mConnected;
    private boolean mStopRetry;
    private Object mRetryLock = new Object();
    private LinkedBlockingQueue<Primitive> mSendQueue;
    private LinkedBlockingQueue<Primitive> mReceiveQueue;
    private long mLastActive;
    private long mKeepAliveMillis;
    private Primitive mKeepAlivePrimitive;
    private AtomicBoolean mHasPendingPolling = new AtomicBoolean(false);
    private final AndroidHttpClient mHttpClient;
    private final Header mContentTypeHeader;
    private final Header mMsisdnHeader;
    private URI mPostUri;
    private ImpsTransactionManager mTxManager;
    public HttpDataChannel(ImpsConnection connection) throws ImException {
        super(connection);
        mTxManager = connection.getTransactionManager();
        ImpsConnectionConfig cfg = connection.getConfig();
        try {
            String host = cfg.getHost();
            if (host == null || host.length() == 0) {
                throw new ImException(ImErrorInfo.INVALID_HOST_NAME,
                       "Empty host name.");
            }
            mPostUri = new URI(cfg.getHost());
            if (mPostUri.getPath() == null || "".equals(mPostUri.getPath())) {
                mPostUri = new URI(cfg.getHost() + "/");
            }
            if (!"http".equalsIgnoreCase(mPostUri.getScheme())
                    && !"https".equalsIgnoreCase(mPostUri.getScheme())) {
                throw new ImException(ImErrorInfo.INVALID_HOST_NAME,
                        "Non HTTP/HTTPS host name.");
            }
            mHttpClient = AndroidHttpClient.newInstance("Android-Imps/0.1");
            HttpParams params = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, cfg.getReplyTimeout());
            HttpConnectionParams.setSoTimeout(params, cfg.getReplyTimeout());
        } catch (URISyntaxException e) {
            throw new ImException(ImErrorInfo.INVALID_HOST_NAME,
                    e.getLocalizedMessage());
        }
        mContentTypeHeader = new BasicHeader("Content-Type", cfg.getTransportContentType());
        String msisdn = cfg.getMsisdn();
        mMsisdnHeader = (msisdn != null) ? new BasicHeader("MSISDN", msisdn) : null;
        mParser = cfg.createPrimitiveParser();
        mSerializer = cfg.createPrimitiveSerializer();
    }
    @Override
    public void connect() throws ImException {
        if (mConnected) {
            throw new ImException("Already connected");
        }
        mStopped = false;
        mStopRetry = false;
        mSendQueue = new LinkedBlockingQueue<Primitive>();
        mReceiveQueue = new LinkedBlockingQueue<Primitive>();
        mSendThread = new Thread(this, "HttpDataChannel");
        mSendThread.setDaemon(true);
        mSendThread.start();
        mConnected = true;
    }
    @Override
    public void suspend() {
        mSuspended = true;
    }
    @Override
    public boolean resume() {
        long now = SystemClock.elapsedRealtime();
        if (now - mLastActive > mKeepAliveMillis) {
            shutdown();
            return false;
        } else {
            mSuspended = false;
            Primitive polling = new Primitive(ImpsTags.Polling_Request);
            polling.setSession(mConnection.getSession().getID());
            sendPrimitive(polling);
            startHeartbeat();
            return true;
        }
    }
    @Override
    public void shutdown() {
        HeartbeatService heartbeatService
            = SystemService.getDefault().getHeartbeatService();
        if (heartbeatService != null) {
            heartbeatService.stopHeartbeat(this);
        }
        mStopped = true;
        mSendThread.interrupt();
        mConnected = false;
    }
    @Override
    public void sendPrimitive(Primitive p) {
        if (!mConnected || mStopped) {
            ImpsLog.log("DataChannel not connected, ignore primitive " + p.getType());
            return;
        }
        if (ImpsTags.Polling_Request.equals(p.getType())) {
            if (!mHasPendingPolling.compareAndSet(false, true)) {
                ImpsLog.log("HttpDataChannel: Ignoring Polling-Request");
                return;
            }
        } else if (ImpsTags.Logout_Request.equals(p.getType())) {
            mStopRetry = true;
            synchronized (mRetryLock) {
                mRetryLock.notify();
            }
        }
        if (!mSendQueue.offer(p)) {
            mTxManager.notifyErrorResponse(p.getTransactionID(),
                    ImErrorInfo.UNKNOWN_ERROR, "sending queue full");
        }
    }
    @Override
    public Primitive receivePrimitive() throws InterruptedException {
        if (!mConnected || mStopped) {
            throw new IllegalStateException();
        }
        return mReceiveQueue.take();
    }
    @Override
    public void startKeepAlive(long interval) {
        if (!mConnected || mStopped) {
            throw new IllegalStateException();
        }
        if (interval <= 0) {
            interval = mConnection.getConfig().getDefaultKeepAliveInterval();
        }
        mKeepAliveMillis = interval * 1000;
        if (mKeepAliveMillis < 0) {
            ImpsLog.log("Negative keep alive time. Won't send keep-alive");
        }
        mKeepAlivePrimitive = new Primitive(ImpsTags.KeepAlive_Request);
        startHeartbeat();
    }
    private void startHeartbeat() {
        HeartbeatService heartbeatService
            = SystemService.getDefault().getHeartbeatService();
        if (heartbeatService != null) {
            heartbeatService.startHeartbeat(this, mKeepAliveMillis);
        }
    }
    public long sendHeartbeat() {
        if (mSuspended) {
            return 0;
        }
        long inactiveTime = SystemClock.elapsedRealtime() - mLastActive;
        if (needSendKeepAlive(inactiveTime)) {
            sendKeepAlive();
            return mKeepAliveMillis;
        } else {
            return mKeepAliveMillis - inactiveTime;
        }
    }
    private boolean needSendKeepAlive(long inactiveTime) {
        return mKeepAliveMillis - inactiveTime <= 500;
    }
    @Override
    public long getLastActiveTime() {
        return mLastActive;
    }
    @Override
    public boolean isSendingQueueEmpty() {
        if (!mConnected || mStopped) {
            throw new IllegalStateException();
        }
        return mSendQueue.isEmpty();
    }
    public void run() {
        while (!mStopped) {
            try {
                Primitive primitive = mSendQueue.take();
                if (primitive.getType().equals(ImpsTags.Polling_Request)) {
                    mHasPendingPolling.set(false);
                }
                doSendPrimitive(primitive);
            } catch (InterruptedException e) {
            }
        }
        mHttpClient.close();
    }
    private void sendKeepAlive() {
        ImpsTransactionManager tm = mConnection.getTransactionManager();
        AsyncTransaction tx = new AsyncTransaction(tm) {
            @Override
            public void onResponseError(ImpsErrorInfo error) {
            }
            @Override
            public void onResponseOk(Primitive response) {
            }
        };
        tx.sendRequest(mKeepAlivePrimitive);
    }
    private void doSendPrimitive(Primitive p) {
        String errorInfo = null;
        int retryCount = 0;
        long retryDelay = INIT_RETRY_DELAY_MS;
        while (retryCount < MAX_RETRY_COUNT) {
            try {
                trySend(p);
                return;
            } catch (IOException e) {
                errorInfo = e.getLocalizedMessage();
                String type = p.getType();
                if (ImpsTags.Login_Request.equals(type)
                        || ImpsTags.Logout_Request.equals(type)) {
                    break;
                }
                if (p.getTransactionMode() == TransactionMode.Response) {
                    return;
                }
                retryCount++;
                if (retryCount < MAX_RETRY_COUNT) {
                   mTxManager.reassignTransactionId(p);
                    Log.w(ImpsLog.TAG, "Send primitive failed, retry after " + retryDelay + "ms");
                    synchronized (mRetryLock) {
                        try {
                            mRetryLock.wait(retryDelay);
                        } catch (InterruptedException ignore) {
                        }
                        if (mStopRetry) {
                            break;
                        }
                    }
                    retryDelay = retryDelay * 2;
                    if (retryDelay > MAX_RETRY_DELAY_MS) {
                        retryDelay = MAX_RETRY_DELAY_MS;
                    }
                }
            }
        }
        Log.w(ImpsLog.TAG, "Failed to send primitive after " + MAX_RETRY_COUNT + " retries");
        mTxManager.notifyErrorResponse(p.getTransactionID(),
                ImErrorInfo.NETWORK_ERROR, errorInfo);
    }
    private void trySend(Primitive p) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            mSerializer.serialize(p, out);
        } catch (SerializerException e) {
            mTxManager.notifyErrorResponse(p.getTransactionID(),
                    ImErrorInfo.SERIALIZER_ERROR,
                    "Internal serializer error, primitive: " + p.getType());
            out.close();
            return;
        }
        HttpPost req = new HttpPost(mPostUri);
        req.addHeader(mContentTypeHeader);
        if (mMsisdnHeader != null) {
            req.addHeader(mMsisdnHeader);
        }
        ByteArrayEntity entity = new ByteArrayEntity(out.toByteArray());
        req.setEntity(entity);
        mLastActive = SystemClock.elapsedRealtime();
        if (Log.isLoggable(ImpsLog.TAG, Log.DEBUG)) {
            long sendBytes = entity.getContentLength() + 176 ;
            ImpsLog.log(mConnection.getLoginUserName() + " >> " + p.getType() + " HTTP payload approx. " + sendBytes + " bytes");
        }
        if (Log.isLoggable(ImpsLog.PACKET_TAG, Log.DEBUG)) {
            ImpsLog.dumpRawPacket(out.toByteArray());
            ImpsLog.dumpPrimitive(p);
        }
        HttpResponse res = mHttpClient.execute(req);
        StatusLine statusLine = res.getStatusLine();
        HttpEntity resEntity = res.getEntity();
        InputStream in = resEntity.getContent();
        if (Log.isLoggable(ImpsLog.PACKET_TAG, Log.DEBUG)) {
            Log.d(ImpsLog.PACKET_TAG, statusLine.toString());
            Header[] headers = res.getAllHeaders();
            for (Header h : headers) {
                Log.d(ImpsLog.PACKET_TAG, h.toString());
            }
            int len = (int) resEntity.getContentLength();
            if (len > 0) {
                byte[] content = new byte[len];
                int offset = 0;
                int bytesRead = 0;
                do {
                    bytesRead = in.read(content, offset, len);
                    offset += bytesRead;
                    len -= bytesRead;
                } while (bytesRead > 0);
                in.close();
                ImpsLog.dumpRawPacket(content);
                in = new ByteArrayInputStream(content);
            }
        }
        try {
            if (statusLine.getStatusCode() != HttpURLConnection.HTTP_OK) {
                mTxManager.notifyErrorResponse(p.getTransactionID(), statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
                return;
            }
            if (resEntity.getContentLength() == 0) {
                if ((p.getTransactionMode() != TransactionMode.Response)
                        && !p.getType().equals(ImpsTags.Polling_Request)) {
                    mTxManager.notifyErrorResponse(p.getTransactionID(),
                            ImErrorInfo.ILLEGAL_SERVER_RESPONSE,
                            "bad response from server");
                }
                return;
            }
            Primitive response = mParser.parse(in);
            if (Log.isLoggable(ImpsLog.PACKET_TAG, Log.DEBUG)) {
                ImpsLog.dumpPrimitive(response);
            }
            if (Log.isLoggable(ImpsLog.TAG, Log.DEBUG)) {
                long len = 2 + resEntity.getContentLength() + statusLine.toString().length() + 2;
                Header[] headers = res.getAllHeaders();
                for (Header header : headers) {
                    len += header.getName().length() + header.getValue().length() + 4;
                }
                ImpsLog.log(mConnection.getLoginUserName() + " << "
                        + response.getType() + " HTTP payload approx. " + len + "bytes");
            }
            if (!mReceiveQueue.offer(response)) {
                mTxManager.notifyErrorResponse(p.getTransactionID(),
                        ImErrorInfo.UNKNOWN_ERROR, "receiving queue full");
            }
        } catch (ParserException e) {
            ImpsLog.logError(e);
            mTxManager.notifyErrorResponse(p.getTransactionID(),
                    ImErrorInfo.PARSER_ERROR,
                    "Parser error, received a bad response from server");
        } finally {
            resEntity.consumeContent();
        }
    }
}
