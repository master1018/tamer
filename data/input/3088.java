public class GetPolicyQualifiers {
    public static void main(String[] args) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509", "SUN");
        File f = new File(System.getProperty("test.src", "."), "speech2speech");
        X509Certificate mostTrustedCaCert = (X509Certificate)
            cf.generateCertificate(new FileInputStream(f));
        Set trustAnchors = Collections.singleton(
            new TrustAnchor(mostTrustedCaCert, null));
        f = new File(System.getProperty("test.src", "."), "speech2eve");
        X509Certificate eeCert = (X509Certificate)
            cf.generateCertificate(new FileInputStream(f));
        CertPathValidator cpv = CertPathValidator.getInstance("PKIX", "SUN");
        PKIXParameters params = new PKIXParameters(trustAnchors);
        params.setPolicyQualifiersRejected(false);
        params.setRevocationEnabled(false);
        List certList = Collections.singletonList(eeCert);
        CertPath cp = cf.generateCertPath(certList);
        PKIXCertPathValidatorResult result =
            (PKIXCertPathValidatorResult) cpv.validate(cp, params);
        PolicyNode policyTree = result.getPolicyTree();
        Iterator children = policyTree.getChildren();
        PolicyNode child = (PolicyNode) children.next();
        Set policyQualifiers = child.getPolicyQualifiers();
        Iterator i = policyQualifiers.iterator();
        while (i.hasNext()) {
            Object next = i.next();
            if (!(next instanceof PolicyQualifierInfo))
                throw new Exception("not a PolicyQualifierInfo");
        }
        params.setPolicyQualifiersRejected(true);
        try {
            result = (PKIXCertPathValidatorResult) cpv.validate(cp, params);
            throw new Exception("Validation of CertPath containing critical " +
                "qualifiers should have failed when policyQualifiersRejected " +
                "flag is true");
        } catch (CertPathValidatorException cpve) {
            if (cpve.getReason() != PKIXReason.INVALID_POLICY) {
                throw new Exception("unexpected reason: " + cpve.getReason());
            }
        }
    }
}
