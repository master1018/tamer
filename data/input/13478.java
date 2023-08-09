public class PKIXExtendedTM {
    static boolean separateServerThread = true;
    static String trusedCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIICrDCCAhWgAwIBAgIBADANBgkqhkiG9w0BAQQFADBJMQswCQYDVQQGEwJVUzET\n" +
        "MBEGA1UECBMKU29tZS1TdGF0ZTESMBAGA1UEBxMJU29tZS1DaXR5MREwDwYDVQQK\n" +
        "EwhTb21lLU9yZzAeFw0wODEyMDgwMjQzMzZaFw0yODA4MjUwMjQzMzZaMEkxCzAJ\n" +
        "BgNVBAYTAlVTMRMwEQYDVQQIEwpTb21lLVN0YXRlMRIwEAYDVQQHEwlTb21lLUNp\n" +
        "dHkxETAPBgNVBAoTCFNvbWUtT3JnMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKB\n" +
        "gQDLxDggB76Ip5OwoUNRLdeOha9U3a2ieyNbz5kTU5lFfe5tui2/461uPZ8a+QOX\n" +
        "4BdVrhEmV94BKY4FPyH35zboLjfXSKxT1mAOx1Bt9sWF94umxZE1cjyU7vEX8HHj\n" +
        "7BvOyk5AQrBt7moO1uWtPA/JuoJPePiJl4kqlRJM2Akq6QIDAQABo4GjMIGgMB0G\n" +
        "A1UdDgQWBBT6uVG/TOfZhpgz+efLHvEzSfeoFDBxBgNVHSMEajBogBT6uVG/TOfZ\n" +
        "hpgz+efLHvEzSfeoFKFNpEswSTELMAkGA1UEBhMCVVMxEzARBgNVBAgTClNvbWUt\n" +
        "U3RhdGUxEjAQBgNVBAcTCVNvbWUtQ2l0eTERMA8GA1UEChMIU29tZS1PcmeCAQAw\n" +
        "DAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQQFAAOBgQBcIm534U123Hz+rtyYO5uA\n" +
        "ofd81G6FnTfEAV8Kw9fGyyEbQZclBv34A9JsFKeMvU4OFIaixD7nLZ/NZ+IWbhmZ\n" +
        "LovmJXyCkOufea73pNiZ+f/4/ScZaIlM/PRycQSqbFNd4j9Wott+08qxHPLpsf3P\n" +
        "6Mvf0r1PNTY2hwTJLJmKtg==\n" +
        "-----END CERTIFICATE-----";
    static String serverCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIICpDCCAg2gAwIBAgIBCDANBgkqhkiG9w0BAQQFADBJMQswCQYDVQQGEwJVUzET\n" +
        "MBEGA1UECBMKU29tZS1TdGF0ZTESMBAGA1UEBxMJU29tZS1DaXR5MREwDwYDVQQK\n" +
        "EwhTb21lLU9yZzAeFw0wODEyMDgwMzQzMDRaFw0yODA4MjUwMzQzMDRaMHIxCzAJ\n" +
        "BgNVBAYTAlVTMRMwEQYDVQQIEwpTb21lLVN0YXRlMRIwEAYDVQQHEwlTb21lLUNp\n" +
        "dHkxETAPBgNVBAoTCFNvbWUtT3JnMRMwEQYDVQQLEwpTU0wtU2VydmVyMRIwEAYD\n" +
        "VQQDEwlsb2NhbGhvc3QwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAKWsWxw3\n" +
        "ot2ZiS2yebiP1Uil5xyEF41pnMasbfnyHR85GdrTch5u7ETMcKTcugAw9qBPPVR6\n" +
        "YWrMV9AKf5UoGD+a2ZTyG8gkiH7+nQ89+1dTCLMgM9Q/F0cU0c3qCNgOdU6vvszS\n" +
        "7K+peknfwtmsuCRAkKYDVirQMAVALE+r2XSJAgMBAAGjczBxMAkGA1UdEwQCMAAw\n" +
        "CwYDVR0PBAQDAgXgMB0GA1UdDgQWBBTtbtv0tVbI+xoGYT8PCLumBNgWVDAfBgNV\n" +
        "HSMEGDAWgBT6uVG/TOfZhpgz+efLHvEzSfeoFDAXBgNVHREBAf8EDTALgglsb2Nh\n" +
        "bGhvc3QwDQYJKoZIhvcNAQEEBQADgYEAoqVTciHtcvsUj+YaTct8tUh3aTCsKsac\n" +
        "PHhfQ+ObjiXSgxsKYTX7ym/wk/wvlbUcbqLKxsu7qrcJitH+H9heV1hEHEu65Uoi\n" +
        "nRugFruyOrwvAylV8Cm2af7ddilmYJ+sdJA6N2M3xJRxR0G2LFHEXDNEjYReyexn\n" +
        "JqCpf5uZGOo=\n" +
        "-----END CERTIFICATE-----";
    static String clientCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIICpDCCAg2gAwIBAgIBCTANBgkqhkiG9w0BAQQFADBJMQswCQYDVQQGEwJVUzET\n" +
        "MBEGA1UECBMKU29tZS1TdGF0ZTESMBAGA1UEBxMJU29tZS1DaXR5MREwDwYDVQQK\n" +
        "EwhTb21lLU9yZzAeFw0wODEyMDgwMzQzMjRaFw0yODA4MjUwMzQzMjRaMHIxCzAJ\n" +
        "BgNVBAYTAlVTMRMwEQYDVQQIEwpTb21lLVN0YXRlMRIwEAYDVQQHEwlTb21lLUNp\n" +
        "dHkxETAPBgNVBAoTCFNvbWUtT3JnMRMwEQYDVQQLEwpTU0wtQ2xpZW50MRIwEAYD\n" +
        "VQQDEwlsb2NhbGhvc3QwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBALvwQDas\n" +
        "JlRO9KNaAC9pIW+5ejqT7KL24Y7HY9gvEjCZLrDyj/gnLSR4KIT3Ab+NRHndO9JV\n" +
        "8848slshfe/9M0qxo
        "ktP5KOB4z14fSKtcGd3hZ0O6dY31gqxDkkQbAgMBAAGjczBxMAkGA1UdEwQCMAAw\n" +
        "CwYDVR0PBAQDAgXgMB0GA1UdDgQWBBTNu8iFqpG9/R2+zWd8/7PpTKgi5jAfBgNV\n" +
        "HSMEGDAWgBT6uVG/TOfZhpgz+efLHvEzSfeoFDAXBgNVHREBAf8EDTALgglsb2Nh\n" +
        "bGhvc3QwDQYJKoZIhvcNAQEEBQADgYEAm25gJyqW1JznQ1EyOtTGswBVwfgBOf+F\n" +
        "HJuBTcflYQLbTD/AETPQJGvZU9tdhuLtbG3OPhR7vSY8zeAbfM3dbH7QFr3r47Gj\n" +
        "XEH7qM/MX+Z3ifVaC4MeJmrYQkYFSuKeyyKpdRVX4w4nnFHF6OsNASsYrMW6LpxN\n" +
        "cl/epUcHL7E=\n" +
        "-----END CERTIFICATE-----";
    static byte serverPrivateExponent[] = {
        (byte)0x6e, (byte)0xa7, (byte)0x1b, (byte)0x83,
        (byte)0x51, (byte)0x35, (byte)0x9a, (byte)0x44,
        (byte)0x7d, (byte)0xf6, (byte)0xe3, (byte)0x89,
        (byte)0xa0, (byte)0xd7, (byte)0x90, (byte)0x60,
        (byte)0xa1, (byte)0x4e, (byte)0x27, (byte)0x21,
        (byte)0xa2, (byte)0x89, (byte)0x74, (byte)0xcc,
        (byte)0x9d, (byte)0x75, (byte)0x75, (byte)0x4e,
        (byte)0xc7, (byte)0x82, (byte)0xe3, (byte)0xe3,
        (byte)0xc3, (byte)0x7d, (byte)0x00, (byte)0x54,
        (byte)0xec, (byte)0x36, (byte)0xb1, (byte)0xdf,
        (byte)0x91, (byte)0x9c, (byte)0x7a, (byte)0xc0,
        (byte)0x62, (byte)0x0a, (byte)0xd6, (byte)0xa9,
        (byte)0x22, (byte)0x91, (byte)0x4a, (byte)0x29,
        (byte)0x2e, (byte)0x43, (byte)0xfa, (byte)0x8c,
        (byte)0xd8, (byte)0xe9, (byte)0xbe, (byte)0xd9,
        (byte)0x4f, (byte)0xca, (byte)0x23, (byte)0xc6,
        (byte)0xe4, (byte)0x3f, (byte)0xb8, (byte)0x72,
        (byte)0xcf, (byte)0x02, (byte)0xfc, (byte)0xf4,
        (byte)0x58, (byte)0x34, (byte)0x77, (byte)0x76,
        (byte)0xce, (byte)0x22, (byte)0x44, (byte)0x5f,
        (byte)0x2d, (byte)0xca, (byte)0xee, (byte)0xf5,
        (byte)0x43, (byte)0x56, (byte)0x47, (byte)0x71,
        (byte)0x0b, (byte)0x09, (byte)0x6b, (byte)0x5e,
        (byte)0xf2, (byte)0xc8, (byte)0xee, (byte)0xd4,
        (byte)0x6e, (byte)0x44, (byte)0x92, (byte)0x2a,
        (byte)0x7f, (byte)0xcc, (byte)0xa7, (byte)0xd4,
        (byte)0x5b, (byte)0xfb, (byte)0xf7, (byte)0x4a,
        (byte)0xa9, (byte)0xfb, (byte)0x54, (byte)0x18,
        (byte)0xd5, (byte)0xd5, (byte)0x14, (byte)0xba,
        (byte)0xa0, (byte)0x1c, (byte)0x13, (byte)0xb3,
        (byte)0x37, (byte)0x6b, (byte)0x37, (byte)0x59,
        (byte)0xed, (byte)0xdb, (byte)0x6d, (byte)0xb1
    };
    static byte serverModulus[] = {
        (byte)0x00,
        (byte)0xa5, (byte)0xac, (byte)0x5b, (byte)0x1c,
        (byte)0x37, (byte)0xa2, (byte)0xdd, (byte)0x99,
        (byte)0x89, (byte)0x2d, (byte)0xb2, (byte)0x79,
        (byte)0xb8, (byte)0x8f, (byte)0xd5, (byte)0x48,
        (byte)0xa5, (byte)0xe7, (byte)0x1c, (byte)0x84,
        (byte)0x17, (byte)0x8d, (byte)0x69, (byte)0x9c,
        (byte)0xc6, (byte)0xac, (byte)0x6d, (byte)0xf9,
        (byte)0xf2, (byte)0x1d, (byte)0x1f, (byte)0x39,
        (byte)0x19, (byte)0xda, (byte)0xd3, (byte)0x72,
        (byte)0x1e, (byte)0x6e, (byte)0xec, (byte)0x44,
        (byte)0xcc, (byte)0x70, (byte)0xa4, (byte)0xdc,
        (byte)0xba, (byte)0x00, (byte)0x30, (byte)0xf6,
        (byte)0xa0, (byte)0x4f, (byte)0x3d, (byte)0x54,
        (byte)0x7a, (byte)0x61, (byte)0x6a, (byte)0xcc,
        (byte)0x57, (byte)0xd0, (byte)0x0a, (byte)0x7f,
        (byte)0x95, (byte)0x28, (byte)0x18, (byte)0x3f,
        (byte)0x9a, (byte)0xd9, (byte)0x94, (byte)0xf2,
        (byte)0x1b, (byte)0xc8, (byte)0x24, (byte)0x88,
        (byte)0x7e, (byte)0xfe, (byte)0x9d, (byte)0x0f,
        (byte)0x3d, (byte)0xfb, (byte)0x57, (byte)0x53,
        (byte)0x08, (byte)0xb3, (byte)0x20, (byte)0x33,
        (byte)0xd4, (byte)0x3f, (byte)0x17, (byte)0x47,
        (byte)0x14, (byte)0xd1, (byte)0xcd, (byte)0xea,
        (byte)0x08, (byte)0xd8, (byte)0x0e, (byte)0x75,
        (byte)0x4e, (byte)0xaf, (byte)0xbe, (byte)0xcc,
        (byte)0xd2, (byte)0xec, (byte)0xaf, (byte)0xa9,
        (byte)0x7a, (byte)0x49, (byte)0xdf, (byte)0xc2,
        (byte)0xd9, (byte)0xac, (byte)0xb8, (byte)0x24,
        (byte)0x40, (byte)0x90, (byte)0xa6, (byte)0x03,
        (byte)0x56, (byte)0x2a, (byte)0xd0, (byte)0x30,
        (byte)0x05, (byte)0x40, (byte)0x2c, (byte)0x4f,
        (byte)0xab, (byte)0xd9, (byte)0x74, (byte)0x89
    };
    static byte clientPrivateExponent[] = {
        (byte)0x11, (byte)0xb7, (byte)0x6a, (byte)0x36,
        (byte)0x3d, (byte)0x30, (byte)0x37, (byte)0xce,
        (byte)0x61, (byte)0x9d, (byte)0x6c, (byte)0x84,
        (byte)0x8b, (byte)0xf3, (byte)0x9b, (byte)0x25,
        (byte)0x4f, (byte)0x14, (byte)0xc8, (byte)0xa4,
        (byte)0xdd, (byte)0x2f, (byte)0xd7, (byte)0x9a,
        (byte)0x17, (byte)0xbd, (byte)0x90, (byte)0x19,
        (byte)0xf7, (byte)0x05, (byte)0xfd, (byte)0xf2,
        (byte)0xd2, (byte)0xc5, (byte)0xf7, (byte)0x77,
        (byte)0xbe, (byte)0xea, (byte)0xe2, (byte)0x84,
        (byte)0x87, (byte)0x97, (byte)0x3a, (byte)0x41,
        (byte)0x96, (byte)0xb6, (byte)0x99, (byte)0xf8,
        (byte)0x94, (byte)0x8c, (byte)0x58, (byte)0x71,
        (byte)0x51, (byte)0x8c, (byte)0xf4, (byte)0x2a,
        (byte)0x20, (byte)0x9e, (byte)0x1a, (byte)0xa0,
        (byte)0x26, (byte)0x99, (byte)0x75, (byte)0xd6,
        (byte)0x31, (byte)0x53, (byte)0x43, (byte)0x39,
        (byte)0xf5, (byte)0x2a, (byte)0xa6, (byte)0x7e,
        (byte)0x34, (byte)0x42, (byte)0x51, (byte)0x2a,
        (byte)0x40, (byte)0x87, (byte)0x03, (byte)0x88,
        (byte)0x43, (byte)0x69, (byte)0xb2, (byte)0x89,
        (byte)0x6d, (byte)0x20, (byte)0xbd, (byte)0x7d,
        (byte)0x71, (byte)0xef, (byte)0x47, (byte)0x0a,
        (byte)0xdf, (byte)0x06, (byte)0xc1, (byte)0x69,
        (byte)0x66, (byte)0xa8, (byte)0x22, (byte)0x37,
        (byte)0x1a, (byte)0x77, (byte)0x1e, (byte)0xc7,
        (byte)0x94, (byte)0x4e, (byte)0x2c, (byte)0x27,
        (byte)0x69, (byte)0x45, (byte)0x5e, (byte)0xc8,
        (byte)0xf8, (byte)0x0c, (byte)0xb7, (byte)0xf8,
        (byte)0xc0, (byte)0x8f, (byte)0x99, (byte)0xc1,
        (byte)0xe5, (byte)0x28, (byte)0x9b, (byte)0xf9,
        (byte)0x4c, (byte)0x94, (byte)0xc6, (byte)0xb1
    };
    static byte clientModulus[] = {
        (byte)0x00,
        (byte)0xbb, (byte)0xf0, (byte)0x40, (byte)0x36,
        (byte)0xac, (byte)0x26, (byte)0x54, (byte)0x4e,
        (byte)0xf4, (byte)0xa3, (byte)0x5a, (byte)0x00,
        (byte)0x2f, (byte)0x69, (byte)0x21, (byte)0x6f,
        (byte)0xb9, (byte)0x7a, (byte)0x3a, (byte)0x93,
        (byte)0xec, (byte)0xa2, (byte)0xf6, (byte)0xe1,
        (byte)0x8e, (byte)0xc7, (byte)0x63, (byte)0xd8,
        (byte)0x2f, (byte)0x12, (byte)0x30, (byte)0x99,
        (byte)0x2e, (byte)0xb0, (byte)0xf2, (byte)0x8f,
        (byte)0xf8, (byte)0x27, (byte)0x2d, (byte)0x24,
        (byte)0x78, (byte)0x28, (byte)0x84, (byte)0xf7,
        (byte)0x01, (byte)0xbf, (byte)0x8d, (byte)0x44,
        (byte)0x79, (byte)0xdd, (byte)0x3b, (byte)0xd2,
        (byte)0x55, (byte)0xf3, (byte)0xce, (byte)0x3c,
        (byte)0xb2, (byte)0x5b, (byte)0x21, (byte)0x7d,
        (byte)0xef, (byte)0xfd, (byte)0x33, (byte)0x4a,
        (byte)0xb1, (byte)0xa3, (byte)0xff, (byte)0xc6,
        (byte)0xc8, (byte)0x9b, (byte)0xb9, (byte)0x0f,
        (byte)0x7c, (byte)0x41, (byte)0x35, (byte)0x97,
        (byte)0xf9, (byte)0xdb, (byte)0x3a, (byte)0x05,
        (byte)0x60, (byte)0x05, (byte)0x15, (byte)0xaf,
        (byte)0x59, (byte)0x17, (byte)0x92, (byte)0xa3,
        (byte)0x10, (byte)0xad, (byte)0x16, (byte)0x1c,
        (byte)0xe4, (byte)0x07, (byte)0x53, (byte)0xaf,
        (byte)0xa8, (byte)0x76, (byte)0xa2, (byte)0x56,
        (byte)0x2a, (byte)0x92, (byte)0xd3, (byte)0xf9,
        (byte)0x28, (byte)0xe0, (byte)0x78, (byte)0xcf,
        (byte)0x5e, (byte)0x1f, (byte)0x48, (byte)0xab,
        (byte)0x5c, (byte)0x19, (byte)0xdd, (byte)0xe1,
        (byte)0x67, (byte)0x43, (byte)0xba, (byte)0x75,
        (byte)0x8d, (byte)0xf5, (byte)0x82, (byte)0xac,
        (byte)0x43, (byte)0x92, (byte)0x44, (byte)0x1b
    };
    static char passphrase[] = "passphrase".toCharArray();
    volatile static boolean serverReady = false;
    static boolean debug = false;
    void doServerSide() throws Exception {
        SSLContext context = getSSLContext(trusedCertStr, serverCertStr,
            serverModulus, serverPrivateExponent, passphrase);
        SSLServerSocketFactory sslssf = context.getServerSocketFactory();
        SSLServerSocket sslServerSocket =
            (SSLServerSocket) sslssf.createServerSocket(serverPort);
        serverPort = sslServerSocket.getLocalPort();
        serverReady = true;
        SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
        sslSocket.setNeedClientAuth(true);
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
        SSLContext context = getSSLContext(trusedCertStr, clientCertStr,
            clientModulus, clientPrivateExponent, passphrase);
        SSLSocketFactory sslsf = context.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket)
            sslsf.createSocket("localhost", serverPort);
        SSLParameters params = sslSocket.getSSLParameters();
        params.setEndpointIdentificationAlgorithm("HTTPS");
        sslSocket.setSSLParameters(params);
        InputStream sslIS = sslSocket.getInputStream();
        OutputStream sslOS = sslSocket.getOutputStream();
        sslOS.write(280);
        sslOS.flush();
        sslIS.read();
        sslSocket.close();
    }
    private static SSLContext getSSLContext(String trusedCertStr,
            String keyCertStr, byte[] modulus,
            byte[] privateExponent, char[] passphrase) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream is =
                    new ByteArrayInputStream(trusedCertStr.getBytes());
        Certificate trusedCert = cf.generateCertificate(is);
        is.close();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        ks.setCertificateEntry("RSA Export Signer", trusedCert);
        if (keyCertStr != null) {
            RSAPrivateKeySpec priKeySpec = new RSAPrivateKeySpec(
                                            new BigInteger(modulus),
                                            new BigInteger(privateExponent));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPrivateKey priKey =
                    (RSAPrivateKey)kf.generatePrivate(priKeySpec);
            is = new ByteArrayInputStream(keyCertStr.getBytes());
            Certificate keyCert = cf.generateCertificate(is);
            is.close();
            Certificate[] chain = new Certificate[2];
            chain[0] = keyCert;
            chain[1] = trusedCert;
            ks.setKeyEntry("Whatever", priKey, passphrase, chain);
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
        tmf.init(ks);
        TrustManager tms[] = tmf.getTrustManagers();
        if (tms == null || tms.length == 0) {
            throw new Exception("unexpected trust manager implementation");
        } else {
           if (!(tms[0] instanceof X509ExtendedTrustManager)) {
               throw new Exception("unexpected trust manager implementation: "
                                + tms[0].getClass().getCanonicalName());
           }
        }
        SSLContext ctx = SSLContext.getInstance("TLS");
        if (keyCertStr != null) {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, passphrase);
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } else {
            ctx.init(null, tmf.getTrustManagers(), null);
        }
        return ctx;
    }
    volatile int serverPort = 0;
    volatile Exception serverException = null;
    volatile Exception clientException = null;
    public static void main(String args[]) throws Exception {
        if (debug)
            System.setProperty("javax.net.debug", "all");
        new PKIXExtendedTM();
    }
    Thread clientThread = null;
    Thread serverThread = null;
    PKIXExtendedTM() throws Exception {
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
