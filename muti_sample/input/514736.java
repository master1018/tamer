public class TransactionBundle {
    public static final String TRANSACTION_TYPE = "type";
    private static final String PUSH_DATA = "mms-push-data";
    private static final String MMSC_URL = "mmsc-url";
    private static final String PROXY_ADDRESS = "proxy-address";
    private static final String PROXY_PORT = "proxy-port";
    public static final String URI = "uri";
    private final Bundle mBundle;
    private TransactionBundle(int transactionType) {
        mBundle = new Bundle();
        mBundle.putInt(TRANSACTION_TYPE, transactionType);
    }
    public TransactionBundle(int transactionType, String uri) {
        this(transactionType);
        mBundle.putString(URI, uri);
    }
    public TransactionBundle(Bundle bundle) {
        mBundle = bundle;
    }
    public void setConnectionSettings(String mmscUrl,
            String proxyAddress,
            int proxyPort) {
        mBundle.putString(MMSC_URL, mmscUrl);
        mBundle.putString(PROXY_ADDRESS, proxyAddress);
        mBundle.putInt(PROXY_PORT, proxyPort);
    }
    public void setConnectionSettings(TransactionSettings settings) {
        setConnectionSettings(
                settings.getMmscUrl(),
                settings.getProxyAddress(),
                settings.getProxyPort());
    }
    public Bundle getBundle() {
        return mBundle;
    }
    public int getTransactionType() {
        return mBundle.getInt(TRANSACTION_TYPE);
    }
    public String getUri() {
        return mBundle.getString(URI);
    }
    public byte[] getPushData() {
        return mBundle.getByteArray(PUSH_DATA);
    }
    public String getMmscUrl() {
        return mBundle.getString(MMSC_URL);
    }
    public String getProxyAddress() {
        return mBundle.getString(PROXY_ADDRESS);
    }
    public int getProxyPort() {
        return mBundle.getInt(PROXY_PORT);
    }
}
