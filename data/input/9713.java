public class RSAExport {
    static boolean separateServerThread = true;
    static String trusedCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIICpjCCAg+gAwIBAgIBADANBgkqhkiG9w0BAQQFADBHMQswCQYDVQQGEwJVUzET\n" +
        "MBEGA1UECBMKU29tZS1TdGF0ZTERMA8GA1UEChMIU29tZSBPcmcxEDAOBgNVBAMT\n" +
        "B1NvbWVvbmUwHhcNMDEwMzMwMTE0NDQ3WhcNMjgwNDI3MTE0NDQ3WjBHMQswCQYD\n" +
        "VQQGEwJVUzETMBEGA1UECBMKU29tZS1TdGF0ZTERMA8GA1UEChMIU29tZSBPcmcx\n" +
        "EDAOBgNVBAMTB1NvbWVvbmUwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAMGY\n" +
        "5HqHUw+Uh9za81k5PjaV6HdY/0aKgRtexUz6tpEZML5b70yqhDCkmtRor+/6tCx2\n" +
        "iykzRs84dHx51QemQzmEUjlPihzzcxkSQM/uoXdDAQK+jTIRKHD0z6tDdeT7dPGM\n" +
        "LkMkuoU/ZjoF6vfOW5fiNKPwh/T40VkSWmi3eGSpAgMBAAGjgaEwgZ4wHQYDVR0O\n" +
        "BBYEFLUyQ9cAJJK66ZXl+aNkbITuMy4VMG8GA1UdIwRoMGaAFLUyQ9cAJJK66ZXl\n" +
        "+aNkbITuMy4VoUukSTBHMQswCQYDVQQGEwJVUzETMBEGA1UECBMKU29tZS1TdGF0\n" +
        "ZTERMA8GA1UEChMIU29tZSBPcmcxEDAOBgNVBAMTB1NvbWVvbmWCAQAwDAYDVR0T\n" +
        "BAUwAwEB/zANBgkqhkiG9w0BAQQFAAOBgQBhf3PX0xWxtaUwZlWCO7GfPwCKgBWr\n" +
        "CXqlqjtWHCshaaU7wUsDOwxFDWwKjFrMerQLsLuBlhdXEbNfSPjychkQtfezQHcS\n" +
        "q0Atq7+KVSmRbDw6oKVRs5v1BBzLCupy+o16fNz3/hwreAWwQnSMtAh/osNS9w1b\n" +
        "QeVWU+JV47H+vg==\n" +
        "-----END CERTIFICATE-----";
    static String serverCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIICIDCCAYmgAwIBAgIBCzANBgkqhkiG9w0BAQUFADBHMQswCQYDVQQGEwJVUzET\n" +
        "MBEGA1UECBMKU29tZS1TdGF0ZTERMA8GA1UEChMIU29tZSBPcmcxEDAOBgNVBAMT\n" +
        "B1NvbWVvbmUwHhcNMDgwNDE4MTUwNzMwWhcNMjgwMTA0MTUwNzMwWjBNMQswCQYD\n" +
        "VQQGEwJVUzETMBEGA1UECBMKU29tZS1TdGF0ZTERMA8GA1UEChMIU29tZSBPcmcx\n" +
        "FjAUBgNVBAMTDVNvbWVvbmVFeHBvcnQwXDANBgkqhkiG9w0BAQEFAANLADBIAkEA\n" +
        "uV8aD/lnB4lxbnOot2dDa7KKtyoMOUQ0qe/0UAE6b+PTCeU8epcIKoUEugzu0byi\n" +
        "FyLVI/Lp8snl2ot/kU4fawIDAQABo1owWDAJBgNVHRMEAjAAMAsGA1UdDwQEAwIF\n" +
        "4DAdBgNVHQ4EFgQU8TCYvnyq+bGROGCuE19nnAoynjEwHwYDVR0jBBgwFoAUtTJD\n" +
        "1wAkkrrpleX5o2RshO4zLhUwDQYJKoZIhvcNAQEFBQADgYEAFU+fP9FSTQNVZOhv\n" +
        "eJ+zq6wI/biwzTgPbAq3yu2gb5kT85z4nzqBhPd2LWWFXhUW/D8QyNZ54X30y0Ug\n" +
        "3NfUAvOANW7CgUbHBmm77KQiF4nWdh338qqq9HzLGrPqcxX0dmiq2RBVPy9wb2Ea\n" +
        "FTZiU2v+9pkoLoSDnCOfPCg/4Q4=\n" +
        "-----END CERTIFICATE-----";
    static byte privateExponent[] = {
        (byte)0x4c, (byte)0xed, (byte)0x3f, (byte)0x86,
        (byte)0x93, (byte)0x8c, (byte)0x83, (byte)0x1f,
        (byte)0x31, (byte)0x98, (byte)0x91, (byte)0x9c,
        (byte)0xd9, (byte)0x87, (byte)0x9b, (byte)0xfe,
        (byte)0x0c, (byte)0x98, (byte)0xee, (byte)0x4c,
        (byte)0x1f, (byte)0xc8, (byte)0x80, (byte)0x1a,
        (byte)0x8e, (byte)0xcf, (byte)0x4a, (byte)0x87,
        (byte)0x0d, (byte)0x0b, (byte)0x70, (byte)0x34,
        (byte)0xd8, (byte)0x4e, (byte)0x8d, (byte)0x84,
        (byte)0x00, (byte)0x8f, (byte)0xaf, (byte)0x32,
        (byte)0x60, (byte)0xe9, (byte)0x53, (byte)0xe7,
        (byte)0x6f, (byte)0x98, (byte)0xe0, (byte)0x8b,
        (byte)0x52, (byte)0xaa, (byte)0xbf, (byte)0x67,
        (byte)0x6b, (byte)0x62, (byte)0x28, (byte)0x98,
        (byte)0x46, (byte)0xca, (byte)0xb9, (byte)0x9c,
        (byte)0x06, (byte)0x5c, (byte)0x6b, (byte)0x91
    };
    static byte modulus[] = {
        (byte)0x00,
        (byte)0xb9, (byte)0x5f, (byte)0x1a, (byte)0x0f,
        (byte)0xf9, (byte)0x67, (byte)0x07, (byte)0x89,
        (byte)0x71, (byte)0x6e, (byte)0x73, (byte)0xa8,
        (byte)0xb7, (byte)0x67, (byte)0x43, (byte)0x6b,
        (byte)0xb2, (byte)0x8a, (byte)0xb7, (byte)0x2a,
        (byte)0x0c, (byte)0x39, (byte)0x44, (byte)0x34,
        (byte)0xa9, (byte)0xef, (byte)0xf4, (byte)0x50,
        (byte)0x01, (byte)0x3a, (byte)0x6f, (byte)0xe3,
        (byte)0xd3, (byte)0x09, (byte)0xe5, (byte)0x3c,
        (byte)0x7a, (byte)0x97, (byte)0x08, (byte)0x2a,
        (byte)0x85, (byte)0x04, (byte)0xba, (byte)0x0c,
        (byte)0xee, (byte)0xd1, (byte)0xbc, (byte)0xa2,
        (byte)0x17, (byte)0x22, (byte)0xd5, (byte)0x23,
        (byte)0xf2, (byte)0xe9, (byte)0xf2, (byte)0xc9,
        (byte)0xe5, (byte)0xda, (byte)0x8b, (byte)0x7f,
        (byte)0x91, (byte)0x4e, (byte)0x1f, (byte)0x6b
    };
    static char passphrase[] = "passphrase".toCharArray();
    volatile static boolean serverReady = false;
    static boolean debug = false;
    void doServerSide() throws Exception {
        SSLServerSocketFactory sslssf =
                getSSLContext(true).getServerSocketFactory();
        SSLServerSocket sslServerSocket =
                (SSLServerSocket) sslssf.createServerSocket(serverPort);
        serverPort = sslServerSocket.getLocalPort();
        serverReady = true;
        try {
            String enabledSuites[] = {
                "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA"};
            sslServerSocket.setEnabledCipherSuites(enabledSuites);
        } catch (IllegalArgumentException iae) {
        }
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslIS.read();
        sslOS.write(85);
        sslOS.flush();
        sslSocket.close();
    }
    void doClientSide() throws Exception {
        while (!serverReady) {
            Thread.sleep(50);
        }
        SSLSocketFactory sslsf =
                getSSLContext(false).getSocketFactory();
        SSLSocket sslSocket = (SSLSocket)
                sslsf.createSocket("localhost", serverPort);
        try {
            String enabledSuites[] = {
                "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA"};
            sslSocket.setEnabledCipherSuites(enabledSuites);
        } catch (IllegalArgumentException iae) {
        }
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslOS.write(280);
        sslOS.flush();
        sslIS.read();
        sslSocket.close();
    }
    volatile int serverPort = 0;
    volatile Exception serverException = null;
    volatile Exception clientException = null;
    public static void main(String[] args) throws Exception {
        if (debug)
            System.setProperty("javax.net.debug", "all");
        new RSAExport();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    RSAExport() throws Exception {
        if (separateServerThread) {
            startServer(true);
            startClient(false);
        } else {
            startClient(true);
            startServer(false);
        }
        if (separateServerThread) {
            serverThread.join();
        } else {
            clientThread.join();
        }
        if (serverException != null)
            throw serverException;
        if (clientException != null)
            throw clientException;
    }
    void startServer(boolean newThread) throws Exception {
        if (newThread) {
            serverThread = new Thread() {
                public void run() {
                    try {
                        doServerSide();
                    } catch (Exception e) {
                        System.err.println("Server died..." + e);
                        serverReady = true;
                        serverException = e;
                    }
                }
            };
            serverThread.start();
        } else {
            doServerSide();
        }
    }
    void startClient(boolean newThread) throws Exception {
        if (newThread) {
            clientThread = new Thread() {
                public void run() {
                    try {
                        doClientSide();
                    } catch (Exception e) {
                        System.err.println("Client died...");
                        clientException = e;
                    }
                }
            };
            clientThread.start();
        } else {
            doClientSide();
        }
    }
    private SSLContext getSSLContext(boolean authnRequired) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream is =
                    new ByteArrayInputStream(trusedCertStr.getBytes());
        Certificate trustedCert = cf.generateCertificate(is);
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        ks.setCertificateEntry("RSA Export Signer", trustedCert);
        if (authnRequired) {
            RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(
                                            new BigInteger(modulus),
                                            new BigInteger(privateExponent));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPrivateKey priKey =
                    (RSAPrivateKey)kf.generatePrivate(priKeySpec);
            is = new ByteArrayInputStream(serverCertStr.getBytes());
            Certificate serverCert = cf.generateCertificate(is);
            Certificate[] chain = new Certificate[2];
            chain[0] = serverCert;
            chain[1] = trustedCert;
            ks.setKeyEntry("RSA Export", priKey, passphrase, chain);
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
        tmf.init(ks);
        SSLContext ctx = SSLContext.getInstance("TLS");
        if (authnRequired) {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, passphrase);
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } else {
            ctx.init(null, tmf.getTrustManagers(), null);
        }
        return ctx;
    }
}
