public class HttpsConfigurator {
    private SSLContext context;
    public HttpsConfigurator (SSLContext context) {
        if (context == null) {
            throw new NullPointerException ("null SSLContext");
        }
        this.context = context;
    }
    public SSLContext getSSLContext() {
        return context;
    }
    public void configure (HttpsParameters params) {
        params.setSSLParameters (getSSLContext().getDefaultSSLParameters());
    }
}
