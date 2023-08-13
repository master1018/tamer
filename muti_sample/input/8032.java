public class SelfIssuedCert {
    static boolean separateServerThread = true;
    static String trusedCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIICejCCAeOgAwIBAgIBADANBgkqhkiG9w0BAQQFADAzMQswCQYDVQQGEwJVUzEQ\n" +
        "MA4GA1UEChMHRXhhbXBsZTESMBAGA1UEAxMJbG9jYWxob3N0MB4XDTA5MDUyNTAw\n" +
        "MDQ0M1oXDTMwMDUwNTAwMDQ0M1owMzELMAkGA1UEBhMCVVMxEDAOBgNVBAoTB0V4\n" +
        "YW1wbGUxEjAQBgNVBAMTCWxvY2FsaG9zdDCBnzANBgkqhkiG9w0BAQEFAAOBjQAw\n" +
        "gYkCgYEA0Wvh3FHYGQ3vvw59yTjUxT6QuY0fzwCGQTM9evXr/V9+pjWmaTkNDW+7\n" +
        "S/LErlWz64gOWTgcMZN162sVgx4ct/q27brY+SlUO5eSud1fSac6SfefhOPBa965\n" +
        "Xc4mnpDt5sgQPMDCuFK7Le6A+/S9J42BO2WYmNcmvcwWWrv+ehcCAwEAAaOBnTCB\n" +
        "mjAdBgNVHQ4EFgQUq3q5fYEibdvLpab+JY4pmifj2vYwWwYDVR0jBFQwUoAUq3q5\n" +
        "fYEibdvLpab+JY4pmifj2vahN6Q1MDMxCzAJBgNVBAYTAlVTMRAwDgYDVQQKEwdF\n" +
        "eGFtcGxlMRIwEAYDVQQDEwlsb2NhbGhvc3SCAQAwDwYDVR0TAQH/BAUwAwEB/zAL\n" +
        "BgNVHQ8EBAMCAgQwDQYJKoZIhvcNAQEEBQADgYEAHL8BSwtX6s8WPPG2FbQBX+K8\n" +
        "GquAyQNtgfJNm60B4i+fVBkJiQJtLmE0emvHx/3sIaHmB0Gd0HKnk/cIQXY304vr\n" +
        "QpqwudKcIZuzmj+pa7807joV+WzRDVIlt4HpYg7tiUvEoyw+X8jwY2lgiGR7mWu6\n" +
        "jQU8PN/06+qgtvSGFpo=\n" +
        "-----END CERTIFICATE-----";
    static String targetCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIICaTCCAdKgAwIBAgIBAjANBgkqhkiG9w0BAQQFADAzMQswCQYDVQQGEwJVUzEQ\n" +
        "MA4GA1UEChMHRXhhbXBsZTESMBAGA1UEAxMJbG9jYWxob3N0MB4XDTA5MDUyNTAw\n" +
        "MDQ0M1oXDTI5MDIwOTAwMDQ0M1owMzELMAkGA1UEBhMCVVMxEDAOBgNVBAoTB0V4\n" +
        "YW1wbGUxEjAQBgNVBAMTCWxvY2FsaG9zdDCBnzANBgkqhkiG9w0BAQEFAAOBjQAw\n" +
        "gYkCgYEAzmPahrH9LTQv3HEWsua+hIpzyU1ACooSd5BtDjc7XnVzSdGW8QD9R8EA\n" +
        "xko7TvfJo6IH6wwgHBspySwsl+6xvHhbwQjgtWlT71ksrUbqcUzmvSvcycQYA8RC\n" +
        "yk9HK5pEJQgSxldpR3Kmy0V6CHC4dCm15trnJYWisTuezY3fjXECAwEAAaOBjDCB\n" +
        "iTAdBgNVHQ4EFgQUQkiWFRkjKsfwFo7UMQfGEzNNW60wWwYDVR0jBFQwUoAUq3q5\n" +
        "fYEibdvLpab+JY4pmifj2vahN6Q1MDMxCzAJBgNVBAYTAlVTMRAwDgYDVQQKEwdF\n" +
        "eGFtcGxlMRIwEAYDVQQDEwlsb2NhbGhvc3SCAQAwCwYDVR0PBAQDAgPoMA0GCSqG\n" +
        "SIb3DQEBBAUAA4GBAIMz7c1R+6KEO7FmH4rnv9XE62xkg03ff0vKXLZMjjs0CX2z\n" +
        "ybRttuTFafHA6/JS+Wz0G83FCRVeiw2WPU6BweMwwejzzIrQ/K6mbp6w6sRFcbNa\n" +
        "eLBtzkjEtI/htOSSq3/0mbKmWn5uVJckO4QiB8kUR4F7ngM9l1uuI46ZfUsk\n" +
        "-----END CERTIFICATE-----";
    static String targetPrivateKey =
        "MIICeQIBADANBgkqhkiG9w0BAQEFAASCAmMwggJfAgEAAoGBAM5j2oax/S00L9xx\n" +
        "FrLmvoSKc8lNQAqKEneQbQ43O151c0nRlvEA/UfBAMZKO073yaOiB+sMIBwbKcks\n" +
        "LJfusbx4W8EI4LVpU+9ZLK1G6nFM5r0r3MnEGAPEQspPRyuaRCUIEsZXaUdypstF\n" +
        "eghwuHQpteba5yWForE7ns2N341xAgMBAAECgYEAgZ8k98OBhopoJMLBxso0jXmH\n" +
        "Dr59oiDlSEJku7DkkIajSZFggyxj5lTI78BfT1FASozQ/EY5RG2q6LXdq+41oU/U\n" +
        "JVEQWhdIE1mQDwE0vgaYdjzMaVIsC3cZYOCOmCYvNxCiTt7e/z8yBMmAE5udqJMB\n" +
        "pim4WXDfpy0ssK81oCECQQDwMC4xu+kn0yD/Qyi9Zn26gIRDv4bjzDQoJfSvMhrY\n" +
        "a4duxLzh9u4gCDd0+wHxpPQvNxGCk0c1JUxBJ2rb4G3HAkEA2/oVRV6+xiRXUnoo\n" +
        "bdPEO27zEJmdpE42yU/JLIy6DPu2IUhEqY45fU2ZERmwMdhpiK/vsf/CZKJ2j/ZU\n" +
        "PdMLBwJBAJIYTFDWAqjFpCGAASzLRZiGiW0H941h7Suqgp159ZhEN5mps1Yis47q\n" +
        "UIkoEHOiKSD69vychsiNykcrKbVaWosCQQC1UrYX4Vo1r5z/EkyjAwzcxL68rzM/\n" +
        "TW1hkU/NVg7CRvXBB3X5oY+H1t/WNauD2tRa5FMbESwmkbhTQIP+FikfAkEA4goD\n" +
        "HCxUn0Z1OQq9QL6y1Yoof6sHxicUwABosuCLJnDJmA5vhpemvdXQTzFII8g1hyQf\n" +
        "z1yyDoxhddcleKlJvQ==";
    static char passphrase[] = "passphrase".toCharArray();
    volatile static boolean serverReady = false;
    static boolean debug = false;
    void doServerSide() throws Exception {
        SSLContext context = getSSLContext(null, targetCertStr,
                                            targetPrivateKey);
        SSLServerSocketFactory sslssf = context.getServerSocketFactory();
        SSLServerSocket sslServerSocket =
            (SSLServerSocket)sslssf.createServerSocket(serverPort);
        serverPort = sslServerSocket.getLocalPort();
        serverReady = true;
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        sslSocket.setNeedClientAuth(false);
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
        SSLContext context = getSSLContext(trusedCertStr, null, null);
        SSLSocketFactory sslsf = context.getSocketFactory();
        SSLSocket sslSocket =
            (SSLSocket)sslsf.createSocket("localhost", serverPort);
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslOS.write(280);
        sslOS.flush();
        sslIS.read();
        sslSocket.close();
    }
    private static SSLContext getSSLContext(String trusedCertStr,
            String keyCertStr, String keySpecStr) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        Certificate trusedCert = null;
        ByteArrayInputStream is = null;
        if (trusedCertStr != null) {
            is = new ByteArrayInputStream(trusedCertStr.getBytes());
            trusedCert = cf.generateCertificate(is);
            is.close();
            ks.setCertificateEntry("RSA Export Signer", trusedCert);
        }
        if (keyCertStr != null) {
            PKCS8EncodedKeySpec priKeySpec = new PKCS8EncodedKeySpec(
                                new BASE64Decoder().decodeBuffer(keySpecStr));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPrivateKey priKey =
                    (RSAPrivateKey)kf.generatePrivate(priKeySpec);
            is = new ByteArrayInputStream(keyCertStr.getBytes());
            Certificate keyCert = cf.generateCertificate(is);
            is.close();
            Certificate[] chain = null;
            if (trusedCert != null) {
                chain = new Certificate[2];
                chain[0] = keyCert;
                chain[1] = trusedCert;
            } else {
                chain = new Certificate[1];
                chain[0] = keyCert;
            }
            ks.setKeyEntry("Whatever", priKey, passphrase, chain);
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmAlgorithm);
        tmf.init(ks);
        SSLContext ctx = SSLContext.getInstance("TLS");
        if (keyCertStr != null && !keyCertStr.isEmpty()) {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("NewSunX509");
            kmf.init(ks, passphrase);
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            ks = null;
        } else {
            ctx.init(null, tmf.getTrustManagers(), null);
        }
        return ctx;
    }
    private static String tmAlgorithm;        
    private static void parseArguments(String[] args) {
        tmAlgorithm = args[0];
    }
    volatile int serverPort = 0;
    volatile Exception serverException = null;
    volatile Exception clientException = null;
    public static void main(String args[]) throws Exception {
        if (debug)
            System.setProperty("javax.net.debug", "all");
        parseArguments(args);
        new SelfIssuedCert();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    SelfIssuedCert() throws Exception {
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
                        System.err.println("Server died...");
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
}
