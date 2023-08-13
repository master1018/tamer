public class ConnectionFactory {
    private static ConnectionFactory sInstance;
    private ConnectionFactory() {
    }
    public synchronized static ConnectionFactory getInstance() {
        if (sInstance == null) {
            sInstance = new ConnectionFactory();
        }
        return sInstance;
    }
    public ImConnection createConnection(ConnectionConfig config) throws ImException {
        if ("IMPS".equals(config.getProtocolName())) {
            return new ImpsConnection((ImpsConnectionConfig) config);
        } else {
            throw new ImException("Unsupported protocol");
        }
    }
}
