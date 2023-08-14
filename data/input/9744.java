public class ConfigQuotedString extends PKCS11Test {
    public static void main(String[] args) throws Exception {
        main(new ConfigQuotedString());
    }
    public void main(Provider p) throws Exception {
        System.out.println(p);
        System.out.println("test passed");
    }
}
