public class MailTransport implements Transport {
     public static final int SOCKET_CONNECT_TIMEOUT = 10000;
     public static final int SOCKET_READ_TIMEOUT = 60000;
    private static final HostnameVerifier HOSTNAME_VERIFIER =
            HttpsURLConnection.getDefaultHostnameVerifier();
    private String mDebugLabel;
    private String mHost;
    private int mPort;
    private String[] mUserInfoParts;
    private int mConnectionSecurity;
    private boolean mTrustCertificates;
    private Socket mSocket;
    private InputStream mIn;
    private OutputStream mOut;
    public MailTransport(String debugLabel) {
        super();
        mDebugLabel = debugLabel;
    }
    public Transport newInstanceWithConfiguration() {
        MailTransport newObject = new MailTransport(mDebugLabel);
        newObject.mDebugLabel = mDebugLabel;
        newObject.mHost = mHost;
        newObject.mPort = mPort;
        if (mUserInfoParts != null) {
            newObject.mUserInfoParts = mUserInfoParts.clone();
        }
        newObject.mConnectionSecurity = mConnectionSecurity;
        newObject.mTrustCertificates = mTrustCertificates;
        return newObject;
    }
    public void setUri(URI uri, int defaultPort) {
        mHost = uri.getHost();
        mPort = defaultPort;
        if (uri.getPort() != -1) {
            mPort = uri.getPort();
        }
        if (uri.getUserInfo() != null) {
            mUserInfoParts = uri.getUserInfo().split(":", 2);
        }
    }
    public String[] getUserInfoParts() {
        return mUserInfoParts;
    }
    public String getHost() {
        return mHost;
    }
    public int getPort() {
        return mPort;
    }
    public void setSecurity(int connectionSecurity, boolean trustAllCertificates) {
        mConnectionSecurity = connectionSecurity;
        mTrustCertificates = trustAllCertificates;
    }
    public int getSecurity() {
        return mConnectionSecurity;
    }
    public boolean canTrySslSecurity() {
        return mConnectionSecurity == CONNECTION_SECURITY_SSL;
    }
    public boolean canTryTlsSecurity() {
        return mConnectionSecurity == Transport.CONNECTION_SECURITY_TLS;
    }
    public boolean canTrustAllCertificates() {
        return mTrustCertificates;
    }
    public void open() throws MessagingException, CertificateValidationException {
        if (Config.LOGD && Email.DEBUG) {
            Log.d(Email.LOG_TAG, "*** " + mDebugLabel + " open " + 
                    getHost() + ":" + String.valueOf(getPort()));
        }
        try {
            SocketAddress socketAddress = new InetSocketAddress(getHost(), getPort());
            if (canTrySslSecurity()) {
                mSocket = SSLUtils.getSSLSocketFactory(canTrustAllCertificates()).createSocket();
            } else {
                mSocket = new Socket();
            }
            mSocket.connect(socketAddress, SOCKET_CONNECT_TIMEOUT);
            if (canTrySslSecurity() && !canTrustAllCertificates()) {
                verifyHostname(mSocket, getHost());
            }
            mIn = new BufferedInputStream(mSocket.getInputStream(), 1024);
            mOut = new BufferedOutputStream(mSocket.getOutputStream(), 512);
        } catch (SSLException e) {
            if (Config.LOGD && Email.DEBUG) {
                Log.d(Email.LOG_TAG, e.toString());
            }
            throw new CertificateValidationException(e.getMessage(), e);
        } catch (IOException ioe) {
            if (Config.LOGD && Email.DEBUG) {
                Log.d(Email.LOG_TAG, ioe.toString());
            }
            throw new MessagingException(MessagingException.IOERROR, ioe.toString());
        }
    }
    public void reopenTls() throws MessagingException {
        try {
            mSocket = SSLUtils.getSSLSocketFactory(canTrustAllCertificates())
                    .createSocket(mSocket, getHost(), getPort(), true);
            mSocket.setSoTimeout(SOCKET_READ_TIMEOUT);
            mIn = new BufferedInputStream(mSocket.getInputStream(), 1024);
            mOut = new BufferedOutputStream(mSocket.getOutputStream(), 512);
        } catch (SSLException e) {
            if (Config.LOGD && Email.DEBUG) {
                Log.d(Email.LOG_TAG, e.toString());
            }
            throw new CertificateValidationException(e.getMessage(), e);
        } catch (IOException ioe) {
            if (Config.LOGD && Email.DEBUG) {
                Log.d(Email.LOG_TAG, ioe.toString());
            }
            throw new MessagingException(MessagingException.IOERROR, ioe.toString());
        }
    }
    private void verifyHostname(Socket socket, String hostname) throws IOException {
        SSLSocket ssl = (SSLSocket) socket;
        ssl.startHandshake();
        SSLSession session = ssl.getSession();
        if (session == null) {
            throw new SSLException("Cannot verify SSL socket without session");
        }
        if (!HOSTNAME_VERIFIER.verify(hostname, session)) {
            throw new SSLPeerUnverifiedException(
                    "Certificate hostname not useable for server: " + hostname);
        }
    }
    public void setSoTimeout(int timeoutMilliseconds) throws SocketException {
        mSocket.setSoTimeout(timeoutMilliseconds);
    }
    public boolean isOpen() {
        return (mIn != null && mOut != null && 
                mSocket != null && mSocket.isConnected() && !mSocket.isClosed());
    }
    public void close() {
        try {
            mIn.close();
        } catch (Exception e) {
        }
        try {
            mOut.close();
        } catch (Exception e) {
        }
        try {
            mSocket.close();
        } catch (Exception e) {
        }
        mIn = null;
        mOut = null;
        mSocket = null;
    }
    public InputStream getInputStream() {
        return mIn;
    }
    public OutputStream getOutputStream() {
        return mOut;
    }
    public void writeLine(String s, String sensitiveReplacement) throws IOException {
        if (Config.LOGD && Email.DEBUG) {
            if (sensitiveReplacement != null && !Email.DEBUG_SENSITIVE) {
                Log.d(Email.LOG_TAG, ">>> " + sensitiveReplacement);
            } else {
                Log.d(Email.LOG_TAG, ">>> " + s);
            }
        }
        OutputStream out = getOutputStream();
        out.write(s.getBytes());
        out.write('\r');
        out.write('\n');
        out.flush();
    }
    public String readLine() throws IOException {
        StringBuffer sb = new StringBuffer();
        InputStream in = getInputStream();
        int d;
        while ((d = in.read()) != -1) {
            if (((char)d) == '\r') {
                continue;
            } else if (((char)d) == '\n') {
                break;
            } else {
                sb.append((char)d);
            }
        }
        if (d == -1 && Config.LOGD && Email.DEBUG) {
            Log.d(Email.LOG_TAG, "End of stream reached while trying to read line.");
        }
        String ret = sb.toString();
        if (Config.LOGD) {
            if (Email.DEBUG) {
                Log.d(Email.LOG_TAG, "<<< " + ret);
            }
        }
        return ret;
    }
}
