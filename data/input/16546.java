public class NewSize7 {
    public static void main(String[] args) throws Exception {
        String FILE = "newsize7-ks";
        new File(FILE).delete();
        KeyTool.main(("-debug -genkeypair -keystore " + FILE +
                " -alias a -dname cn=c -storepass changeit" +
                " -keypass changeit -keyalg rsa").split(" "));
        KeyStore ks = KeyStore.getInstance("JKS");
        try (FileInputStream fin = new FileInputStream(FILE)) {
            ks.load(fin, null);
        }
        Files.delete(Paths.get(FILE));
        RSAPublicKey r = (RSAPublicKey)ks.getCertificate("a").getPublicKey();
        if (r.getModulus().bitLength() != 2048) {
            throw new Exception("Bad keysize");
        }
        X509Certificate x = (X509Certificate)ks.getCertificate("a");
        if (!x.getSigAlgName().equals("SHA256withRSA")) {
            throw new Exception("Bad sigalg");
        }
    }
}
