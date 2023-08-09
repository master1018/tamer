public class ReadCertificates extends PKCS11Test {
    private static CertificateFactory factory;
    private static SecureRandom random;
    private static Collection<X509Certificate> readCertificates(File file) throws Exception {
        System.out.println("Loading " + file.getName() + "...");
        InputStream in = new FileInputStream(file);
        Collection<X509Certificate> certs = (Collection<X509Certificate>)factory.generateCertificates(in);
        in.close();
        return certs;
    }
    public static void main(String[] args) throws Exception {
        main(new ReadCertificates());
    }
    public void main(Provider p) throws Exception {
        if (p.getService("Signature", "SHA1withECDSA") == null) {
            System.out.println("Provider does not support ECDSA, skipping...");
            return;
        }
        Security.insertProviderAt(p, 1);
        random = new SecureRandom();
        factory = CertificateFactory.getInstance("X.509");
        try {
            factory.generateCertificate(null);
        } catch (CertificateException e) {
        }
        Map<X500Principal,X509Certificate> certs = new LinkedHashMap<X500Principal,X509Certificate>();
        File dir = new File(BASE, "certs");
        File closedDir = new File(CLOSED_BASE, "certs");
        File[] files = concat(dir.listFiles(), closedDir.listFiles());
        Arrays.sort(files);
        for (File file : files) {
            if (file.isFile() == false) {
                continue;
            }
            Collection<X509Certificate> certList = readCertificates(file);
            for (X509Certificate cert : certList) {
                X509Certificate old = certs.put(cert.getSubjectX500Principal(), cert);
                if (old != null) {
                    System.out.println("Duplicate subject:");
                    System.out.println("Old Certificate: " + old);
                    System.out.println("New Certificate: " + cert);
                    throw new Exception(file.getPath());
                }
            }
        }
        System.out.println("OK: " + certs.size() + " certificates.");
        for (X509Certificate cert : certs.values()) {
            X509Certificate issuer = certs.get(cert.getIssuerX500Principal());
            System.out.println("Verifying " + cert.getSubjectX500Principal() + "...");
            PublicKey key = issuer.getPublicKey();
            try {
                cert.verify(key, p.getName());
            } catch (NoSuchAlgorithmException e) {
                System.out.println("Warning: " + e.getMessage() +
                ". Trying another provider...");
                cert.verify(key);
            }
        }
        System.out.println("Checking incorrect signatures...");
        List<X509Certificate> certList = new ArrayList<X509Certificate>(certs.values());
        for (int i = 0; i < 20; i++) {
            X509Certificate cert, signer;
            do {
                cert = getRandomCert(certList);
                signer = getRandomCert(certList);
            } while (cert.getIssuerX500Principal().equals(signer.getSubjectX500Principal()));
            try {
                cert.verify(signer.getPublicKey());
                throw new Exception("Verified invalid signature");
            } catch (SignatureException e) {
                System.out.println("OK: " + e);
            } catch (InvalidKeyException e) {
                System.out.println("OK: " + e);
            }
        }
        Security.removeProvider(p.getName());
        System.out.println("OK");
    }
    private static X509Certificate getRandomCert(List<X509Certificate> certs) {
        int n = random.nextInt(certs.size());
        return certs.get(n);
    }
}
