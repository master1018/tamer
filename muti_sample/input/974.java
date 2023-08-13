public final class BuildEEBasicConstraints {
    public static void main(String[] args) throws Exception {
        X509Certificate rootCert = CertUtils.getCertFromFile("anchor.cer");
        TrustAnchor anchor = new TrustAnchor
            (rootCert.getSubjectX500Principal(), rootCert.getPublicKey(), null);
        X509CertSelector sel = new X509CertSelector();
        sel.setBasicConstraints(-2);
        PKIXBuilderParameters params = new PKIXBuilderParameters
            (Collections.singleton(anchor), sel);
        params.setRevocationEnabled(false);
        X509Certificate eeCert = CertUtils.getCertFromFile("ee.cer");
        X509Certificate caCert = CertUtils.getCertFromFile("ca.cer");
        ArrayList<X509Certificate> certs = new ArrayList<X509Certificate>();
        certs.add(caCert);
        certs.add(eeCert);
        CollectionCertStoreParameters ccsp =
            new CollectionCertStoreParameters(certs);
        CertStore cs = CertStore.getInstance("Collection", ccsp);
        params.addCertStore(cs);
        PKIXCertPathBuilderResult res = CertUtils.build(params);
        CertPath cp = res.getCertPath();
        List<? extends Certificate> certList = cp.getCertificates();
        X509Certificate cert = (X509Certificate) certList.get(0);
        if (cert.getBasicConstraints() != -1) {
            throw new Exception("Target certificate is not an EE certificate");
        }
    }
}
