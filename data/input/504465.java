class Request {
    EventHandler mEventHandler;
    private Connection mConnection;
    BasicHttpRequest mHttpRequest;
    String mPath;
    HttpHost mHost;
    HttpHost mProxyHost;
    volatile boolean mCancelled = false;
    int mFailCount = 0;
    private int mReceivedBytes = 0;
    private InputStream mBodyProvider;
    private int mBodyLength;
    private final static String HOST_HEADER = "Host";
    private final static String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    private final static String CONTENT_LENGTH_HEADER = "content-length";
    private final Object mClientResource = new Object();
    private boolean mLoadingPaused = false;
    private static RequestContent requestContentProcessor =
            new RequestContent();
    Request(String method, HttpHost host, HttpHost proxyHost, String path,
            InputStream bodyProvider, int bodyLength,
            EventHandler eventHandler,
            Map<String, String> headers) {
        mEventHandler = eventHandler;
        mHost = host;
        mProxyHost = proxyHost;
        mPath = path;
        mBodyProvider = bodyProvider;
        mBodyLength = bodyLength;
        if (bodyProvider == null && !"POST".equalsIgnoreCase(method)) {
            mHttpRequest = new BasicHttpRequest(method, getUri());
        } else {
            mHttpRequest = new BasicHttpEntityEnclosingRequest(
                    method, getUri());
            if (bodyProvider != null) {
                setBodyProvider(bodyProvider, bodyLength);
            }
        }
        addHeader(HOST_HEADER, getHostPort());
        addHeader(ACCEPT_ENCODING_HEADER, "gzip");
        addHeaders(headers);
    }
    synchronized void setLoadingPaused(boolean pause) {
        mLoadingPaused = pause;
        if (!mLoadingPaused) {
            notify();
        }
    }
    void setConnection(Connection connection) {
        mConnection = connection;
    }
     EventHandler getEventHandler() {
        return mEventHandler;
    }
    void addHeader(String name, String value) {
        if (name == null) {
            String damage = "Null http header name";
            HttpLog.e(damage);
            throw new NullPointerException(damage);
        }
        if (value == null || value.length() == 0) {
            String damage = "Null or empty value for header \"" + name + "\"";
            HttpLog.e(damage);
            throw new RuntimeException(damage);
        }
        mHttpRequest.addHeader(name, value);
    }
    void addHeaders(Map<String, String> headers) {
        if (headers == null) {
            return;
        }
        Entry<String, String> entry;
        Iterator<Entry<String, String>> i = headers.entrySet().iterator();
        while (i.hasNext()) {
            entry = i.next();
            addHeader(entry.getKey(), entry.getValue());
        }
    }
    void sendRequest(AndroidHttpClientConnection httpClientConnection)
            throws HttpException, IOException {
        if (mCancelled) return; 
        if (HttpLog.LOGV) {
            HttpLog.v("Request.sendRequest() " + mHost.getSchemeName() + ":
            if (false) {
                Iterator i = mHttpRequest.headerIterator();
                while (i.hasNext()) {
                    Header header = (Header)i.next();
                    HttpLog.v(header.getName() + ": " + header.getValue());
                }
            }
        }
        requestContentProcessor.process(mHttpRequest,
                                        mConnection.getHttpContext());
        httpClientConnection.sendRequestHeader(mHttpRequest);
        if (mHttpRequest instanceof HttpEntityEnclosingRequest) {
            httpClientConnection.sendRequestEntity(
                    (HttpEntityEnclosingRequest) mHttpRequest);
        }
        if (HttpLog.LOGV) {
            HttpLog.v("Request.requestSent() " + mHost.getSchemeName() + ":
        }
    }
    void readResponse(AndroidHttpClientConnection httpClientConnection)
            throws IOException, ParseException {
        if (mCancelled) return; 
        StatusLine statusLine = null;
        boolean hasBody = false;
        httpClientConnection.flush();
        int statusCode = 0;
        Headers header = new Headers();
        do {
            statusLine = httpClientConnection.parseResponseHeader(header);
            statusCode = statusLine.getStatusCode();
        } while (statusCode < HttpStatus.SC_OK);
        if (HttpLog.LOGV) HttpLog.v(
                "Request.readResponseStatus() " +
                statusLine.toString().length() + " " + statusLine);
        ProtocolVersion v = statusLine.getProtocolVersion();
        mEventHandler.status(v.getMajor(), v.getMinor(),
                statusCode, statusLine.getReasonPhrase());
        mEventHandler.headers(header);
        HttpEntity entity = null;
        hasBody = canResponseHaveBody(mHttpRequest, statusCode);
        if (hasBody)
            entity = httpClientConnection.receiveResponseEntity(header);
        boolean supportPartialContent = "bytes".equalsIgnoreCase(header
                .getAcceptRanges());
        if (entity != null) {
            InputStream is = entity.getContent();
            Header contentEncoding = entity.getContentEncoding();
            InputStream nis = null;
            byte[] buf = null;
            int count = 0;
            try {
                if (contentEncoding != null &&
                    contentEncoding.getValue().equals("gzip")) {
                    nis = new GZIPInputStream(is);
                } else {
                    nis = is;
                }
                buf = mConnection.getBuf();
                int len = 0;
                int lowWater = buf.length / 2;
                while (len != -1) {
                    synchronized(this) {
                        while (mLoadingPaused) {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                HttpLog.e("Interrupted exception whilst "
                                    + "network thread paused at WebCore's request."
                                    + " " + e.getMessage());
                            }
                        }
                    }
                    len = nis.read(buf, count, buf.length - count);
                    if (len != -1) {
                        count += len;
                        if (supportPartialContent) mReceivedBytes += len;
                    }
                    if (len == -1 || count >= lowWater) {
                        if (HttpLog.LOGV) HttpLog.v("Request.readResponse() " + count);
                        mEventHandler.data(buf, count);
                        count = 0;
                    }
                }
            } catch (EOFException e) {
                if (count > 0) {
                    mEventHandler.data(buf, count);
                }
                if (HttpLog.LOGV) HttpLog.v( "readResponse() handling " + e);
            } catch(IOException e) {
                if (statusCode == HttpStatus.SC_OK
                        || statusCode == HttpStatus.SC_PARTIAL_CONTENT) {
                    if (supportPartialContent && count > 0) {
                        mEventHandler.data(buf, count);
                    }
                    throw e;
                }
            } finally {
                if (nis != null) {
                    nis.close();
                }
            }
        }
        mConnection.setCanPersist(entity, statusLine.getProtocolVersion(),
                header.getConnectionType());
        mEventHandler.endData();
        complete();
        if (HttpLog.LOGV) HttpLog.v("Request.readResponse(): done " +
                                    mHost.getSchemeName() + ":
    }
    synchronized void cancel() {
        if (HttpLog.LOGV) {
            HttpLog.v("Request.cancel(): " + getUri());
        }
        mLoadingPaused = false;
        notify();
        mCancelled = true;
        if (mConnection != null) {
            mConnection.cancel();
        }
    }
    String getHostPort() {
        String myScheme = mHost.getSchemeName();
        int myPort = mHost.getPort();
        if (myPort != 80 && myScheme.equals("http") ||
            myPort != 443 && myScheme.equals("https")) {
            return mHost.toHostString();
        } else {
            return mHost.getHostName();
        }
    }
    String getUri() {
        if (mProxyHost == null ||
            mHost.getSchemeName().equals("https")) {
            return mPath;
        }
        return mHost.getSchemeName() + ":
    }
    public String toString() {
        return mPath;
    }
    void reset() {
        mHttpRequest.removeHeaders(CONTENT_LENGTH_HEADER);
        if (mBodyProvider != null) {
            try {
                mBodyProvider.reset();
            } catch (IOException ex) {
                if (HttpLog.LOGV) HttpLog.v(
                        "failed to reset body provider " +
                        getUri());
            }
            setBodyProvider(mBodyProvider, mBodyLength);
        }
        if (mReceivedBytes > 0) {
            mFailCount = 0;
            HttpLog.v("*** Request.reset() to range:" + mReceivedBytes);
            mHttpRequest.setHeader("Range", "bytes=" + mReceivedBytes + "-");
        }
    }
    void waitUntilComplete() {
        synchronized (mClientResource) {
            try {
                if (HttpLog.LOGV) HttpLog.v("Request.waitUntilComplete()");
                mClientResource.wait();
                if (HttpLog.LOGV) HttpLog.v("Request.waitUntilComplete() done waiting");
            } catch (InterruptedException e) {
            }
        }
    }
    void complete() {
        synchronized (mClientResource) {
            mClientResource.notifyAll();
        }
    }
    private static boolean canResponseHaveBody(final HttpRequest request,
                                               final int status) {
        if ("HEAD".equalsIgnoreCase(request.getRequestLine().getMethod())) {
            return false;
        }
        return status >= HttpStatus.SC_OK
            && status != HttpStatus.SC_NO_CONTENT
            && status != HttpStatus.SC_NOT_MODIFIED;
    }
    private void setBodyProvider(InputStream bodyProvider, int bodyLength) {
        if (!bodyProvider.markSupported()) {
            throw new IllegalArgumentException(
                    "bodyProvider must support mark()");
        }
        bodyProvider.mark(Integer.MAX_VALUE);
        ((BasicHttpEntityEnclosingRequest)mHttpRequest).setEntity(
                new InputStreamEntity(bodyProvider, bodyLength));
    }
    public void handleSslErrorResponse(boolean proceed) {
        HttpsConnection connection = (HttpsConnection)(mConnection);
        if (connection != null) {
            connection.restartConnection(proceed);
        }
    }
    void error(int errorId, int resourceId) {
        mEventHandler.error(
                errorId,
                mConnection.mContext.getText(
                        resourceId).toString());
    }
}
