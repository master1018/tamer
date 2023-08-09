public class GetPrivateKey extends SecmodTest {
    public static void main(String[] args) throws Exception {
        if (initSecmod() == false) {
            return;
        }
        String configName = BASE + SEP + "nss.cfg";
        Provider p = getSunPKCS11(configName);
        System.out.println(p);
        Security.addProvider(p);
        KeyStore ks = KeyStore.getInstance("PKCS11", p);
        ks.load(null, password);
        Collection<String> aliases = new TreeSet<String>(Collections.list(ks.aliases()));
        System.out.println("entries: " + aliases.size());
        System.out.println(aliases);
        PrivateKey privateKey = (PrivateKey)ks.getKey(keyAlias, password);
        System.out.println(privateKey);
        byte[] data = new byte[1024];
        Random random = new Random();
        random.nextBytes(data);
        System.out.println("Signing...");
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        byte[] sig = signature.sign();
        X509Certificate[] chain = (X509Certificate[])ks.getCertificateChain(keyAlias);
        signature.initVerify(chain[0].getPublicKey());
        signature.update(data);
        boolean ok = signature.verify(sig);
        if (ok == false) {
            throw new Exception("Signature verification error");
        }
        System.out.println("OK");
    }
}
