public class LoopbackSSLSocket {
    public static void main(String[] args) throws Exception {
        SSLSocketFactory sf = (SSLSocketFactory)SSLSocketFactory.getDefault();
        try {
            SSLSocket s = (SSLSocket)sf.createSocket((String)null, 0);
            s.close();
        } catch (IOException ioe) {
        }
    }
}
