public class NoExtensions {
    public static void main(String[] args) {
        try {
            NoExtensions certs = new NoExtensions();
            certs.doBuild(getUserCertificate1());
            System.out.println("successfully built path for the first certificate");
            certs.doBuild(getUserCertificate2());
            System.out.println("successfully built path for the second certificate");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private void doBuild(X509Certificate userCert) throws Exception {
        HashSet trustAnchors = new HashSet();
        X509Certificate trustedCert = getTrustedCertificate();
        trustAnchors.add(new TrustAnchor(trustedCert, null));
        ArrayList certs = new ArrayList();
        certs.add(trustedCert);
        certs.add(userCert);
        CollectionCertStoreParameters certStoreParams = new CollectionCertStoreParameters(certs);
        CertStore certStore = CertStore.getInstance("Collection", certStoreParams);
        X509CertSelector certSelector = new X509CertSelector();
        certSelector.setCertificate(userCert);
        certSelector.setSubject(userCert.getSubjectDN().getName()); 
        CertPathBuilder certPathBuilder = CertPathBuilder.getInstance("PKIX", "SUN");
        PKIXBuilderParameters certPathBuilderParams = new PKIXBuilderParameters(trustAnchors, certSelector);
        certPathBuilderParams.addCertStore(certStore);
        certPathBuilderParams.setRevocationEnabled(false);
        CertPathBuilderResult result = certPathBuilder.build(certPathBuilderParams);
        CertPath certPath = result.getCertPath();
    }
    private static X509Certificate getTrustedCertificate() throws Exception {
        String sCert =
            "-----BEGIN CERTIFICATE-----\n"
          + "MIIBezCCASWgAwIBAgIQyWD8dLUoqpJFyDxrfRlrsTANBgkqhkiG9w0BAQQFADAW\n"
          + "MRQwEgYDVQQDEwtSb290IEFnZW5jeTAeFw0wMTEwMTkxMjU5MjZaFw0zOTEyMzEy\n"
          + "MzU5NTlaMBoxGDAWBgNVBAMTD1Jvb3RDZXJ0aWZpY2F0ZTBcMA0GCSqGSIb3DQEB\n"
          + "AQUAA0sAMEgCQQC+NFKszPjatUZKWmyWaFjir1wB93FX2u5SL+GMjgUsMs1JcTKQ\n"
          + "Kh0cnnQKknNkV4cTW4NPn31YCoB1+0KA3mknAgMBAAGjSzBJMEcGA1UdAQRAMD6A\n"
          + "EBLkCS0GHR1PAI1hIdwWZGOhGDAWMRQwEgYDVQQDEwtSb290IEFnZW5jeYIQBjds\n"
          + "AKoAZIoRz7jUqlw19DANBgkqhkiG9w0BAQQFAANBACJxAfP57yqaT9N+nRgAOugM\n"
          + "JG0aN3/peCIvL3p29epRL2xoWFvxpUUlsH2I39OZ6b8+twWCebhkv1I62segXAk=\n"
          + "-----END CERTIFICATE-----";
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream bytes = new ByteArrayInputStream(sCert.getBytes());
        return (X509Certificate)certFactory.generateCertificate(bytes);
    }
    private static X509Certificate getUserCertificate1() throws Exception {
        String sCert =
            "-----BEGIN CERTIFICATE-----\n"
          + "MIIBfzCCASmgAwIBAgIQWFSKzCWO2ptOAc2F3MKZSzANBgkqhkiG9w0BAQQFADAa\n"
          + "MRgwFgYDVQQDEw9Sb290Q2VydGlmaWNhdGUwHhcNMDExMDE5MTMwNzQxWhcNMzkx\n"
          + "MjMxMjM1OTU5WjAaMRgwFgYDVQQDEw9Vc2VyQ2VydGlmaWNhdGUwXDANBgkqhkiG\n"
          + "9w0BAQEFAANLADBIAkEA24gypa2YFGZHKznEWWbqIWNVXCM35W7RwJwhGpNsuBCj\n"
          + "NT6KEo66F+OOMgZmb0KrEZHBJASJ3n4Cqbt4aHm/2wIDAQABo0swSTBHBgNVHQEE\n"
          + "QDA+gBBch+eYzOPgVRbMq5vGpVWooRgwFjEUMBIGA1UEAxMLUm9vdCBBZ2VuY3mC\n"
          + "EMlg/HS1KKqSRcg8a30Za7EwDQYJKoZIhvcNAQEEBQADQQCYBIHBqQQJePi5Hzfo\n"
          + "CxeUaYlXmvbxVNkxM65Pplsj3h4ntfZaynmlhahH3YsnnA8wk6xPt04LjSId12RB\n"
          + "PeuO\n"
          + "-----END CERTIFICATE-----";
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream bytes = new ByteArrayInputStream(sCert.getBytes());
        return (X509Certificate)certFactory.generateCertificate(bytes);
    }
    private static X509Certificate getUserCertificate2() throws Exception {
        String sCert =
            "-----BEGIN CERTIFICATE-----\n"
          + "MIIBMjCB3aADAgECAhB6225ckZVssEukPuvk1U1PMA0GCSqGSIb3DQEBBAUAMBox\n"
          + "GDAWBgNVBAMTD1Jvb3RDZXJ0aWZpY2F0ZTAeFw0wMTEwMTkxNjA5NTZaFw0wMjEw\n"
          + "MTkyMjA5NTZaMBsxGTAXBgNVBAMTEFVzZXJDZXJ0aWZpY2F0ZTIwXDANBgkqhkiG\n"
          + "9w0BAQEFAANLADBIAkEAzicGiW9aUlUoQIZnLy1l8MMV5OvA+4VJ4T/xo/PpN8Oq\n"
          + "WgZVGKeEp6JCzMlXEJk3TGLfpXL4Ytw+Ldhv0QPhLwIDAnMpMA0GCSqGSIb3DQEB\n"
          + "BAUAA0EAQmj9SFHEx66JyAps3ew4pcSS3QvfVZ/6qsNUYCG75rFGcTUPHcXKql9y\n"
          + "qBT83iNLJ
          + "-----END CERTIFICATE-----";
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream bytes = new ByteArrayInputStream(sCert.getBytes());
        return (X509Certificate)certFactory.generateCertificate(bytes);
    }
}
