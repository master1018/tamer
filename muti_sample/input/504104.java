public class SslCertificateTest extends TestCase {
    private static final String Issue1597Certificate =
        "-----BEGIN CERTIFICATE-----\n"+
        "MIIBnjCCAQegAwIBAgIFAKvN774wDQYJKoZIhvcNAQEFBQAwADAeFw0yNzA5MjQw\n"+
        "MDAwMDFaFw0zNzA5MjQwMDAwMDFaMAAwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJ\n"+
        "AoGBAMNAaUSKw3stg6UHx6bWHNn0T5WR39UB43EZqdhhM0hnfpzwAzNs1T3jOAzF\n"+
        "OtgcX/XVt2Exc1vnwwuiJfvtPtBtQVsNu7wfk45cTUF45axBr4v8oFq7DOHCvs2C\n"+
        "pBDnw/v9PoOihuBamOjzRPL+oVhVfzEqEOILnZD1qEeVJn4RAgMBAAGjJDAiMCAG\n"+
        "A1UdEQQZMBeGD2h0dHBzOi8vMS4xLjEuMYcEAQEBATANBgkqhkiG9w0BAQUFAAOB\n"+
        "gQA7CMJylEjCR9CjztZUMLOutLe64RNhMq9iKgbDfJwYrcgvUNOxjrCdFW66lE9N\n"+
        "TDscc4zS2kpV41vcVYiGwabCNUPi2P6zfFSpYmGqwwu1NoEayqGPdDMrgCnMXVYV\n"+
        "X7HoVif4IdGvjFQrYcyU2VWSWBq6IGMVCR6RkC2YWnnNhw==\n"+
        "-----END CERTIFICATE-----\n";
    @LargeTest
    public void testSslCertificateWithEmptyIssuer() throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate x509Certificate = (X509Certificate)
            certificateFactory.generateCertificate(new ByteArrayInputStream(Issue1597Certificate.getBytes()));
        assertEquals(x509Certificate.getIssuerDN().getName(), "");
        SslCertificate sslCertificate = new SslCertificate(x509Certificate);
        assertEquals(sslCertificate.getIssuedBy().getDName(), "");
    }
}
