public class SSLTest extends TestCase {
    public void testCertificate() throws Exception {
        Socket ssl = SSLCertificateSocketFactory.getDefault().createSocket("www.fortify.net",443);
        assertNotNull(ssl);
        OutputStream out = ssl.getOutputStream();
        assertNotNull(out);
        InputStream in = ssl.getInputStream();
        assertNotNull(in);
        String get = "GET /sslcheck.html HTTP/1.1\r\nHost: 68.178.217.222\r\n\r\n";
        out.write(get.getBytes());
        byte[] b = new byte[1024];
        int ret = in.read(b);
    }
}
