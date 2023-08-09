public class CertReplace {
    public static void main(String[] args) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(args[0]), "changeit".toCharArray());
        Validator v = Validator.getInstance
            (Validator.TYPE_PKIX, Validator.VAR_GENERIC, ks);
        X509Certificate[] chain = createPath(args[1]);
        System.out.println("Chain: ");
        for (X509Certificate c: v.validate(chain)) {
            System.out.println("   " + c.getSubjectX500Principal() +
                    " issued by " + c.getIssuerX500Principal());
        }
    }
    public static X509Certificate[] createPath(String chain) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        List list = new ArrayList();
        for (Certificate c: cf.generateCertificates(
                new FileInputStream(chain))) {
            list.add((X509Certificate)c);
        }
        return (X509Certificate[]) list.toArray(new X509Certificate[0]);
    }
}
