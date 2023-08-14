public class MockTransport implements Transport {
    private static boolean DEBUG_LOG_STREAMS = true;
    private static String LOG_TAG = "MockTransport";
    private boolean mSslAllowed = false;
    private boolean mTlsAllowed = false;
    private boolean mTlsReopened = false;
    private boolean mOpen;
    private boolean mInputOpen;
    private int mConnectionSecurity;
    private boolean mTrustCertificates;
    private String mHost;
    private ArrayList<String> mQueuedInput = new ArrayList<String>();
    private static class Transaction {
        public static final int ACTION_INJECT_TEXT = 0;
        public static final int ACTION_SERVER_CLOSE = 1;
        public static final int ACTION_CLIENT_CLOSE = 2;
        int mAction;
        String mPattern;
        String[] mResponses;
        Transaction(String pattern, String[] responses) {
            mAction = ACTION_INJECT_TEXT;
            mPattern = pattern;
            mResponses = responses;
        }
        Transaction(int otherType) {
            mAction = otherType;
            mPattern = null;
            mResponses = null;
        }
        @Override
        public String toString() {
            switch (mAction) {
                case ACTION_INJECT_TEXT:
                    return mPattern + ": " + Arrays.toString(mResponses);
                case ACTION_SERVER_CLOSE:
                    return "Close the server connection";
                case ACTION_CLIENT_CLOSE:
                    return "Expect the client to close";
                default:
                    return "(Hmm.  Unknown action.)";
            }
        }
    }
    private ArrayList<Transaction> mPairs = new ArrayList<Transaction>();
    public void expect(String pattern) {
        expect(pattern, (String[])null);
    }
    public void expect(String pattern, String response) {
        expect(pattern, (response == null) ? null : new String[] { response });
    }
    public void expect(String pattern, String[] responses) {
        Transaction pair = new Transaction(pattern, responses);
        mPairs.add(pair);
    }
    public void expectLiterally(String literal, String[] responses) {
        expect("^" + Pattern.quote(literal) + "$", responses);
    }
    public void expectClose() {
        mPairs.add(new Transaction(Transaction.ACTION_CLIENT_CLOSE));
    }
    private void sendResponse(String[] responses) {
        for (String s : responses) {
            mQueuedInput.add(s);
        }
    }
    public boolean canTrySslSecurity() {
        return (mConnectionSecurity == CONNECTION_SECURITY_SSL);
    }
    public boolean canTryTlsSecurity() {
        return (mConnectionSecurity == Transport.CONNECTION_SECURITY_TLS);
    }
    public boolean canTrustAllCertificates() {
        return mTrustCertificates;
    }
    public void closeInputStream() {
        mInputOpen = false;
    }
    public void close() {
        mOpen = false;
        mInputOpen = false;
        if (mPairs.size() > 0) {
            Transaction expect = mPairs.remove(0);
            if (expect.mAction == Transaction.ACTION_CLIENT_CLOSE) {
                return;
            }
        }
        mQueuedInput.clear();
        mPairs.clear();
    }
    public void setMockHost(String host) {
        mHost = host;
    }
    public String getHost() {
        return mHost;
    }
    public InputStream getInputStream() {
        SmtpSenderUnitTests.assertTrue(mOpen);
        return new MockInputStream();
    }
    public Transport newInstanceWithConfiguration() {
         return this;
    }
    public OutputStream getOutputStream() {
        Assert.assertTrue(mOpen);
        return new MockOutputStream();
    }
    public int getPort() {
        SmtpSenderUnitTests.fail("getPort() not implemented");
        return 0;
    }
    public int getSecurity() {
        return mConnectionSecurity;
    }
    public String[] getUserInfoParts() {
        SmtpSenderUnitTests.fail("getUserInfoParts() not implemented");
        return null;
    }
    public boolean isOpen() {
        return mOpen;
    }
    public void open()  {
        mOpen = true;
        mInputOpen = true;
    }
    public String readLine() throws IOException {
        SmtpSenderUnitTests.assertTrue(mOpen);
        if (!mInputOpen) {
            throw new IOException("Reading from MockTransport with closed input");
        }
        if (0 == mQueuedInput.size()) {
            Transaction pair = mPairs.get(0);
            if (pair != null && pair.mPattern == null) {
                mPairs.remove(0);
                sendResponse(pair.mResponses);
            }
        }
        SmtpSenderUnitTests.assertTrue("Underflow reading from MockTransport", 0 != mQueuedInput.size());
        String line = mQueuedInput.remove(0);
        if (DEBUG_LOG_STREAMS) {
            Log.d(LOG_TAG, "<<< " + line);
        }
        return line;
    }
    public void setTlsAllowed(boolean tlsAllowed) {
        mTlsAllowed = tlsAllowed;
    }
    public boolean getTlsAllowed() {
        return mTlsAllowed;
    }
    public void setSslAllowed(boolean sslAllowed) {
        mSslAllowed = sslAllowed;
    }
    public boolean getSslAllowed() {
        return mSslAllowed;
    }
    public void reopenTls() {
        SmtpSenderUnitTests.assertTrue(mOpen);
        SmtpSenderUnitTests.assertTrue(mTlsAllowed);
        mTlsReopened = true;
    }
    public boolean getTlsReopened() {
        return mTlsReopened;
    }
    public void setSecurity(int connectionSecurity, boolean trustAllCertificates) {
        mConnectionSecurity = connectionSecurity;
        mTrustCertificates = trustAllCertificates;
    }
    public void setSoTimeout(int timeoutMilliseconds)  {
    }
    public void setUri(URI uri, int defaultPort) {
        SmtpSenderUnitTests.assertTrue("Don't call setUri on a mock transport", false);
    }
    public void writeLine(String s, String sensitiveReplacement)  {
        if (DEBUG_LOG_STREAMS) {
            Log.d(LOG_TAG, ">>> " + s);
        }
        SmtpSenderUnitTests.assertTrue(mOpen);
        SmtpSenderUnitTests.assertTrue("Overflow writing to MockTransport: Getting " + s,
                0 != mPairs.size());
        Transaction pair = mPairs.remove(0);
        SmtpSenderUnitTests.assertTrue("Unexpected string written to MockTransport: Actual=" + s
                + "  Expected=" + pair.mPattern,
                pair.mPattern != null && s.matches(pair.mPattern));
        if (pair.mResponses != null) {
            sendResponse(pair.mResponses);
        }
    }
    private class MockInputStream extends InputStream {
        byte[] mNextLine = null;
        int mNextIndex = 0;
        @Override
        public int read() throws IOException {
            if (!mInputOpen) {
                throw new IOException();
            }
            if (mNextLine != null && mNextIndex < mNextLine.length) {
                return mNextLine[mNextIndex++];
            }
            String next = readLine();
            if (next == null) {
                throw new IOException("Reading from MockTransport with closed input");
            }
            mNextLine = (next + "\r\n").getBytes();
            mNextIndex = 0;
            if (mNextLine != null && mNextIndex < mNextLine.length) {
                return mNextLine[mNextIndex++];
            }
            throw new IOException();                
        }
    }
    private class MockOutputStream extends OutputStream {
        StringBuilder sb = new StringBuilder();
        @Override
        public void write(int oneByte) {
            if (oneByte == '\r') {
                writeLine(sb.toString(), null);
                sb = new StringBuilder();
            } else if (oneByte == '\n') {
            } else {
                sb.append((char)oneByte);
            }
        }
    }
}
