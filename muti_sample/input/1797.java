public class ImportKeyStore {
    public static void main(String[] args) throws Exception {
        String nssCfg = "--name=NSS\nnssSecmodDirectory=.\n ";
        Provider p = new sun.security.pkcs11.SunPKCS11(nssCfg);
        KeyStore ks = KeyStore.getInstance("PKCS11", p);
        ks.load(null, "test12".toCharArray());
        System.out.println("Aliases: " + Collections.list(ks.aliases()));
        System.out.println();
        char[] srcpw = "passphrase".toCharArray();
        importKeyStore("keystore", srcpw, ks);
        System.out.println("OK.");
    }
    private static void importKeyStore(String filename, char[] passwd, KeyStore dstks) throws Exception {
        System.out.println("Importing JKS KeyStore " + filename);
        InputStream in = new FileInputStream(filename);
        KeyStore srcks = KeyStore.getInstance("JKS");
        srcks.load(in, passwd);
        in.close();
        List<String> aliases = Collections.list(srcks.aliases());
        for (String alias : aliases) {
            System.out.println("Alias: " + alias);
            if (srcks.isCertificateEntry(alias)) {
                X509Certificate cert = (X509Certificate)srcks.getCertificate(alias);
                System.out.println("  Certificate: " + cert.getSubjectX500Principal());
                dstks.setCertificateEntry(alias + "-cert", cert);
            } else if (srcks.isKeyEntry(alias)) {
                PrivateKeyEntry entry = (PrivateKeyEntry)srcks.getEntry(alias, new PasswordProtection(passwd));
                System.out.println("  Key: " + entry.getPrivateKey().toString().split("\n")[0]);
                dstks.setEntry(alias, entry, null);
            } else {
                System.out.println("  Unknown entry: " + alias);
            }
        }
        System.out.println();
    }
}
