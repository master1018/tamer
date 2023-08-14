public abstract class Transaction extends Observable {
    private final int mServiceId;
    protected Context mContext;
    protected String mId;
    protected TransactionState mTransactionState;
    protected TransactionSettings mTransactionSettings;
    public static final int NOTIFICATION_TRANSACTION = 0;
    public static final int RETRIEVE_TRANSACTION     = 1;
    public static final int SEND_TRANSACTION         = 2;
    public static final int READREC_TRANSACTION      = 3;
    public Transaction(Context context, int serviceId,
            TransactionSettings settings) {
        mContext = context;
        mTransactionState = new TransactionState();
        mServiceId = serviceId;
        mTransactionSettings = settings;
    }
    @Override
    public TransactionState getState() {
        return mTransactionState;
    }
    public abstract void process();
    public boolean isEquivalent(Transaction transaction) {
        return getClass().equals(transaction.getClass())
                && mId.equals(transaction.mId);
    }
    public int getServiceId() {
        return mServiceId;
    }
    public TransactionSettings getConnectionSettings() {
        return mTransactionSettings;
    }
    public void setConnectionSettings(TransactionSettings settings) {
        mTransactionSettings = settings;
    }
    protected byte[] sendPdu(byte[] pdu) throws IOException {
        return sendPdu(SendingProgressTokenManager.NO_TOKEN, pdu,
                mTransactionSettings.getMmscUrl());
    }
    protected byte[] sendPdu(byte[] pdu, String mmscUrl) throws IOException {
        return sendPdu(SendingProgressTokenManager.NO_TOKEN, pdu, mmscUrl);
    }
    protected byte[] sendPdu(long token, byte[] pdu) throws IOException {
        return sendPdu(token, pdu, mTransactionSettings.getMmscUrl());
    }
    protected byte[] sendPdu(long token, byte[] pdu, String mmscUrl) throws IOException {
        ensureRouteToHost(mmscUrl, mTransactionSettings);
        return HttpUtils.httpConnection(
                mContext, token,
                mmscUrl,
                pdu, HttpUtils.HTTP_POST_METHOD,
                mTransactionSettings.isProxySet(),
                mTransactionSettings.getProxyAddress(),
                mTransactionSettings.getProxyPort());
    }
    protected byte[] getPdu(String url) throws IOException {
        ensureRouteToHost(url, mTransactionSettings);
        return HttpUtils.httpConnection(
                mContext, SendingProgressTokenManager.NO_TOKEN,
                url, null, HttpUtils.HTTP_GET_METHOD,
                mTransactionSettings.isProxySet(),
                mTransactionSettings.getProxyAddress(),
                mTransactionSettings.getProxyPort());
    }
    private void ensureRouteToHost(String url, TransactionSettings settings) throws IOException {
        ConnectivityManager connMgr =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        int inetAddr;
        if (settings.isProxySet()) {
            String proxyAddr = settings.getProxyAddress();
            inetAddr = lookupHost(proxyAddr);
            if (inetAddr == -1) {
                throw new IOException("Cannot establish route for " + url + ": Unknown host");
            } else {
                if (!connMgr.requestRouteToHost(
                        ConnectivityManager.TYPE_MOBILE_MMS, inetAddr)) {
                    throw new IOException("Cannot establish route to proxy " + inetAddr);
                }
            }
        } else {
            Uri uri = Uri.parse(url);
            inetAddr = lookupHost(uri.getHost());
            if (inetAddr == -1) {
                throw new IOException("Cannot establish route for " + url + ": Unknown host");
            } else {
                if (!connMgr.requestRouteToHost(
                        ConnectivityManager.TYPE_MOBILE_MMS, inetAddr)) {
                    throw new IOException("Cannot establish route to " + inetAddr + " for " + url);
                }
            }
        }
    }
    public static int lookupHost(String hostname) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            return -1;
        }
        byte[] addrBytes;
        int addr;
        addrBytes = inetAddress.getAddress();
        addr = ((addrBytes[3] & 0xff) << 24)
                | ((addrBytes[2] & 0xff) << 16)
                | ((addrBytes[1] & 0xff) << 8)
                |  (addrBytes[0] & 0xff);
        return addr;
    }
    @Override
    public String toString() {
        return getClass().getName() + ": serviceId=" + mServiceId;
    }
    abstract public int getType();
}
