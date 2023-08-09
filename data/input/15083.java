public abstract class HttpsExchange extends HttpExchange {
    protected HttpsExchange () {
    }
    public abstract SSLSession getSSLSession ();
}
