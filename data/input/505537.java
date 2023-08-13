public class HttpsConnection extends Connection {
    private static SSLSocketFactory mSslSocketFactory = null;
    static {
        initializeEngine(null);
    }
    public static void initializeEngine(File sessionDir) {
        try {
            SSLClientSessionCache cache = null;
            if (sessionDir != null) {
                Log.d("HttpsConnection", "Caching SSL sessions in "
                        + sessionDir + ".");
                cache = FileClientSessionCache.usingDirectory(sessionDir);
            }
            SSLContextImpl sslContext = new SSLContextImpl();
            TrustManager[] trustManagers = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(
                        X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                        X509Certificate[] certs, String authType) {
                    }
                }
            };
            sslContext.engineInit(null, trustManagers, null, cache, null);
            synchronized (HttpsConnection.class) {
                mSslSocketFactory = sslContext.engineGetSocketFactory();
            }
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private synchronized static SSLSocketFactory getSocketFactory() {
        return mSslSocketFactory;
    }
    private Object mSuspendLock = new Object();
    private boolean mSuspended = false;
    private boolean mAborted = false;
    private HttpHost mProxyHost;
    HttpsConnection(Context context, HttpHost host, HttpHost proxy,
                    RequestFeeder requestFeeder) {
        super(context, host, requestFeeder);
        mProxyHost = proxy;
    }
     void setCertificate(SslCertificate certificate) {
        mCertificate = certificate;
    }
    @Override
    AndroidHttpClientConnection openConnection(Request req) throws IOException {
        SSLSocket sslSock = null;
        if (mProxyHost != null) {
            AndroidHttpClientConnection proxyConnection = null;
            Socket proxySock = null;
            try {
                proxySock = new Socket
                    (mProxyHost.getHostName(), mProxyHost.getPort());
                proxySock.setSoTimeout(60 * 1000);
                proxyConnection = new AndroidHttpClientConnection();
                HttpParams params = new BasicHttpParams();
                HttpConnectionParams.setSocketBufferSize(params, 8192);
                proxyConnection.bind(proxySock, params);
            } catch(IOException e) {
                if (proxyConnection != null) {
                    proxyConnection.close();
                }
                String errorMessage = e.getMessage();
                if (errorMessage == null) {
                    errorMessage =
                        "failed to establish a connection to the proxy";
                }
                throw new IOException(errorMessage);
            }
            StatusLine statusLine = null;
            int statusCode = 0;
            Headers headers = new Headers();
            try {
                BasicHttpRequest proxyReq = new BasicHttpRequest
                    ("CONNECT", mHost.toHostString());
                for (Header h : req.mHttpRequest.getAllHeaders()) {
                    String headerName = h.getName().toLowerCase();
                    if (headerName.startsWith("proxy") || headerName.equals("keep-alive")) {
                        proxyReq.addHeader(h);
                    }
                }
                proxyConnection.sendRequestHeader(proxyReq);
                proxyConnection.flush();
                do {
                    statusLine = proxyConnection.parseResponseHeader(headers);
                    statusCode = statusLine.getStatusCode();
                } while (statusCode < HttpStatus.SC_OK);
            } catch (ParseException e) {
                String errorMessage = e.getMessage();
                if (errorMessage == null) {
                    errorMessage =
                        "failed to send a CONNECT request";
                }
                throw new IOException(errorMessage);
            } catch (HttpException e) {
                String errorMessage = e.getMessage();
                if (errorMessage == null) {
                    errorMessage =
                        "failed to send a CONNECT request";
                }
                throw new IOException(errorMessage);
            } catch (IOException e) {
                String errorMessage = e.getMessage();
                if (errorMessage == null) {
                    errorMessage =
                        "failed to send a CONNECT request";
                }
                throw new IOException(errorMessage);
            }
            if (statusCode == HttpStatus.SC_OK) {
                try {
                    sslSock = (SSLSocket) getSocketFactory().createSocket(
                            proxySock, mHost.getHostName(), mHost.getPort(), true);
                } catch(IOException e) {
                    if (sslSock != null) {
                        sslSock.close();
                    }
                    String errorMessage = e.getMessage();
                    if (errorMessage == null) {
                        errorMessage =
                            "failed to create an SSL socket";
                    }
                    throw new IOException(errorMessage);
                }
            } else {
                ProtocolVersion version = statusLine.getProtocolVersion();
                req.mEventHandler.status(version.getMajor(),
                                         version.getMinor(),
                                         statusCode,
                                         statusLine.getReasonPhrase());
                req.mEventHandler.headers(headers);
                req.mEventHandler.endData();
                proxyConnection.close();
                return null;
            }
        } else {
            try {
                sslSock = (SSLSocket) getSocketFactory().createSocket();
                sslSock.setSoTimeout(SOCKET_TIMEOUT);
                sslSock.connect(new InetSocketAddress(mHost.getHostName(),
                        mHost.getPort()));
            } catch(IOException e) {
                if (sslSock != null) {
                    sslSock.close();
                }
                String errorMessage = e.getMessage();
                if (errorMessage == null) {
                    errorMessage = "failed to create an SSL socket";
                }
                throw new IOException(errorMessage);
            }
        }
        SslError error = CertificateChainValidator.getInstance().
            doHandshakeAndValidateServerCertificates(this, sslSock, mHost.getHostName());
        if (error != null) {
            synchronized (mSuspendLock) {
                mSuspended = true;
            }
            boolean canHandle = req.getEventHandler().handleSslErrorRequest(error);
            if(!canHandle) {
                throw new IOException("failed to handle "+ error);
            }
            synchronized (mSuspendLock) {
                if (mSuspended) {
                    try {
                        mSuspendLock.wait(10 * 60 * 1000);
                        if (mSuspended) {
                            mSuspended = false;
                            mAborted = true;
                            if (HttpLog.LOGV) {
                                HttpLog.v("HttpsConnection.openConnection():" +
                                          " SSL timeout expired and request was cancelled!!!");
                            }
                        }
                    } catch (InterruptedException e) {
                    }
                }
                if (mAborted) {
                    sslSock.close();
                    throw new SSLConnectionClosedByUserException("connection closed by the user");
                }
            }
        }
        AndroidHttpClientConnection conn = new AndroidHttpClientConnection();
        BasicHttpParams params = new BasicHttpParams();
        params.setIntParameter(HttpConnectionParams.SOCKET_BUFFER_SIZE, 8192);
        conn.bind(sslSock, params);
        return conn;
    }
    @Override
    void closeConnection() {
        if (mSuspended) {
            restartConnection(false);
        }
        try {
            if (mHttpClientConnection != null && mHttpClientConnection.isOpen()) {
                mHttpClientConnection.close();
            }
        } catch (IOException e) {
            if (HttpLog.LOGV)
                HttpLog.v("HttpsConnection.closeConnection():" +
                          " failed closing connection " + mHost);
            e.printStackTrace();
        }
    }
    void restartConnection(boolean proceed) {
        if (HttpLog.LOGV) {
            HttpLog.v("HttpsConnection.restartConnection():" +
                      " proceed: " + proceed);
        }
        synchronized (mSuspendLock) {
            if (mSuspended) {
                mSuspended = false;
                mAborted = !proceed;
                mSuspendLock.notify();
            }
        }
    }
    @Override
    String getScheme() {
        return "https";
    }
}
class SSLConnectionClosedByUserException extends SSLException {
    public SSLConnectionClosedByUserException(String reason) {
        super(reason);
    }
}
