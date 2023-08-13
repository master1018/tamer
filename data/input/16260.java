public class StrongOrUnlimited {
    public static void main(String[] args) throws Exception {
        boolean isUnlimited = true;
        Cipher c = Cipher.getInstance("AES", "SunJCE");
        try {
            c.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(new byte[16], "AES"));
        } catch (InvalidKeyException ike) {
            throw new Exception("128 bit AES not available.");
        }
        try {
            c.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(new byte[32], "AES"));
            System.out.println("Unlimited Crypto *IS* Installed");
        } catch (InvalidKeyException ike) {
            System.out.println("Unlimited Crypto *IS NOT* Installed");
        }
    }
}
