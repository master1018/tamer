public class Turkish {
    public static void main(String[] args) throws Exception {
        Locale.setDefault(new Locale("tr", "TR"));
        System.out.println(Cipher.getInstance("RSA/ECB/PKCS1Padding"));
        System.out.println(Cipher.getInstance("RSA/ECB/PKCS1PADDING"));
        System.out.println(Cipher.getInstance("rsa/ecb/pkcs1padding"));
        System.out.println(Cipher.getInstance("Blowfish"));
        System.out.println(Cipher.getInstance("blowfish"));
        System.out.println(Cipher.getInstance("BLOWFISH"));
        System.out.println("OK");
    }
}
