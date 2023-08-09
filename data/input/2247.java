public abstract class StartTlsResponse implements ExtendedResponse {
    public static final String OID = "1.3.6.1.4.1.1466.20037";
    protected StartTlsResponse() {
    }
    public String getID() {
        return OID;
    }
    public byte[] getEncodedValue() {
        return null;
    }
    public abstract void setEnabledCipherSuites(String[] suites);
    public abstract void setHostnameVerifier(HostnameVerifier verifier);
    public abstract SSLSession negotiate() throws IOException;
    public abstract SSLSession negotiate(SSLSocketFactory factory)
        throws IOException;
    public abstract void close() throws IOException;
    private static final long serialVersionUID = 8372842182579276418L;
}
