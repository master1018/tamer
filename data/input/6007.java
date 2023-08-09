public class FailoverToCRL {
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
    static String targetCertStr =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIICizCCAfSgAwIBAgIBGTANBgkqhkiG9w0BAQQFADBJMQswCQYDVQQGEwJVUzET\n" +
        "MBEGA1UECBMKU29tZS1TdGF0ZTESMBAGA1UEBxMJU29tZS1DaXR5MREwDwYDVQQK\n" +
        "EwhTb21lLU9yZzAeFw0wOTAzMTYxNDU1MzVaFw0yODEyMDExNDU1MzVaMHIxCzAJ\n" +
        "BgNVBAYTAlVTMRMwEQYDVQQIEwpTb21lLVN0YXRlMRIwEAYDVQQHEwlTb21lLUNp\n" +
        "dHkxETAPBgNVBAoTCFNvbWUtT3JnMRMwEQYDVQQLEwpTU0wtQ2xpZW50MRIwEAYD\n" +
        "VQQDEwlsb2NhbGhvc3QwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBALvwQDas\n" +
        "JlRO9KNaAC9pIW+5ejqT7KL24Y7HY9gvEjCZLrDyj/gnLSR4KIT3Ab+NRHndO9JV\n" +
        "8848slshfe/9M0qxo
        "ktP5KOB4z14fSKtcGd3hZ0O6dY31gqxDkkQbAgMBAAGjWjBYMAkGA1UdEwQCMAAw\n" +
        "CwYDVR0PBAQDAgXgMB0GA1UdDgQWBBTNu8iFqpG9/R2+zWd8/7PpTKgi5jAfBgNV\n" +
        "HSMEGDAWgBT6uVG/TOfZhpgz+efLHvEzSfeoFDANBgkqhkiG9w0BAQQFAAOBgQBv\n" +
        "p7JjCDOrMBNun46xs4Gz7Y4ygM5VHaFP0oO7369twvRSu0pCuIdZd5OIMPFeRqQw\n" +
        "PA68ZdhYVR0pG5W7isV+jB+Dfge/IOgOA85sZ/6FlP3PBRW+YMQKKdRr5So3ook9\n" +
        "PimQ7rbxRAofPECv20IUKFBbOUkU+gFcn+WbTKYxBw==\n" +
        "-----END CERTIFICATE-----";
    static String crlStr =
        "-----BEGIN X509 CRL-----\n" +
        "MIIBRTCBrwIBATANBgkqhkiG9w0BAQQFADBJMQswCQYDVQQGEwJVUzETMBEGA1UE\n" +
        "CBMKU29tZS1TdGF0ZTESMBAGA1UEBxMJU29tZS1DaXR5MREwDwYDVQQKEwhTb21l\n" +
        "LU9yZxcNMDkwMzE2MTYyNzE0WhcNMjgwNTE1MTYyNzE0WjAiMCACARkXDTA5MDMx\n" +
        "NjE2MjIwOFowDDAKBgNVHRUEAwoBBKAOMAwwCgYDVR0UBAMCAQIwDQYJKoZIhvcN\n" +
        "AQEEBQADgYEAMixJI9vBwYpOGosn46+T/MTEtlm2S5pIVT/xPDrHkCPfw8l4Zrgp\n" +
        "dGPuUkglWdrGdxY9MNRUj2YFNfdZi6zZ7JF6XbkDHYOAKYgPDJRjS/0VcBntn5RJ\n" +
        "sQfZsBqc9fFSP8gknRRn3LT41kr9xNRxTT1t3YYjv7J3zkMYyInqeUA=\n" +
        "-----END X509 CRL-----";
    private static CertPath generateCertificatePath()
            throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream is =
                new ByteArrayInputStream(targetCertStr.getBytes());
        Certificate targetCert = cf.generateCertificate(is);
        List<Certificate> list = Arrays.asList(new Certificate[] {targetCert});
        return cf.generateCertPath(list);
    }
    private static Set<TrustAnchor> generateTrustAnchors()
            throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream is =
                    new ByteArrayInputStream(trusedCertStr.getBytes());
        Certificate trusedCert = cf.generateCertificate(is);
        TrustAnchor anchor = new TrustAnchor((X509Certificate)trusedCert, null);
        return Collections.singleton(anchor);
    }
    private static CertStore generateCertificateStore() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream is =
                    new ByteArrayInputStream(crlStr.getBytes());
        Collection<? extends CRL> crls = cf.generateCRLs(is);
        return CertStore.getInstance("Collection",
                            new CollectionCertStoreParameters(crls));
    }
    public static void main(String args[]) throws Exception {
        CertPath path = generateCertificatePath();
        Set<TrustAnchor> anchors = generateTrustAnchors();
        CertStore crls = generateCertificateStore();
        PKIXParameters params = new PKIXParameters(anchors);
        params.addCertStore(crls);
        params.setRevocationEnabled(true);
        Security.setProperty("ocsp.enable", "true");
        System.setProperty("com.sun.security.enableCRLDP", "true");
        if (Security.getProperty("ocsp.responderURL") != null) {
            throw new
                Exception("The ocsp.responderURL property must not be set");
        }
        CertPathValidator validator = CertPathValidator.getInstance("PKIX");
        try {
            validator.validate(path, params);
        } catch (CertPathValidatorException cpve) {
            if (cpve.getReason() != BasicReason.REVOKED) {
                throw new Exception(
                    "unexpect exception, should be a REVOKED CPVE", cpve);
            }
        }
    }
}
