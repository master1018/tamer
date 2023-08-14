public class IsSunMSCAPIAvailable {
    public static void main(String[] args) throws Exception {
        try {
            Class.forName("sun.security.mscapi.SunMSCAPI");
        } catch (Exception e) {
            System.out.println(
                "The SunMSCAPI provider is not available on this platform");
            return;
        }
        Security.addProvider(new sun.security.mscapi.SunMSCAPI());
        Provider p = Security.getProvider("SunMSCAPI");
        System.out.println("SunMSCAPI provider classname is " +
            p.getClass().getName());
        System.out.println("SunMSCAPI provider name is " + p.getName());
        System.out.println("SunMSCAPI provider version # is " + p.getVersion());
        System.out.println("SunMSCAPI provider info is " + p.getInfo());
        SecureRandom random = SecureRandom.getInstance("Windows-PRNG", p);
        System.out.println("    Windows-PRNG is implemented by: " +
            random.getClass().getName());
        KeyStore keystore = KeyStore.getInstance("Windows-MY", p);
        System.out.println("    Windows-MY is implemented by: " +
            keystore.getClass().getName());
        keystore = KeyStore.getInstance("Windows-ROOT", p);
        System.out.println("    Windows-ROOT is implemented by: " +
            keystore.getClass().getName());
        Signature signature = Signature.getInstance("SHA1withRSA", p);
        System.out.println("    SHA1withRSA is implemented by: " +
            signature.getClass().getName());
        signature = Signature.getInstance("MD5withRSA", p);
        System.out.println("    MD5withRSA is implemented by: " +
            signature.getClass().getName());
        signature = Signature.getInstance("MD2withRSA", p);
        System.out.println("    MD2withRSA is implemented by: " +
            signature.getClass().getName());
        KeyPairGenerator keypairGenerator =
            KeyPairGenerator.getInstance("RSA", p);
        System.out.println("    RSA is implemented by: " +
            keypairGenerator.getClass().getName());
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA", p);
            System.out.println("    RSA is implemented by: " +
                cipher.getClass().getName());
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", p);
            System.out.println("    RSA/ECB/PKCS1Padding is implemented by: " +
                cipher.getClass().getName());
        } catch (GeneralSecurityException e) {
            System.out.println("Cipher not supported by provider, skipping...");
        }
    }
}
