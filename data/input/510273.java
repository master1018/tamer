public class HttpConnectionParamBean extends HttpAbstractParamBean {
    public HttpConnectionParamBean (final HttpParams params) {
        super(params);
    }
    public void setSoTimeout (int soTimeout) {
        HttpConnectionParams.setSoTimeout(params, soTimeout);
    }
    public void setTcpNoDelay (boolean tcpNoDelay) {
        HttpConnectionParams.setTcpNoDelay(params, tcpNoDelay);
    }
    public void setSocketBufferSize (int socketBufferSize) {
        HttpConnectionParams.setSocketBufferSize(params, socketBufferSize);
    }
    public void setLinger (int linger) {
        HttpConnectionParams.setLinger(params, linger);
    }
    public void setConnectionTimeout (int connectionTimeout) {
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
    }
    public void setStaleCheckingEnabled (boolean staleCheckingEnabled) {
        HttpConnectionParams.setStaleCheckingEnabled(params, staleCheckingEnabled);
    }
}
