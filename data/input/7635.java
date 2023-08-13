public class CertStoreConstructorParam {
    public static void main(String[] args) throws Exception {
        try {
            CertStore.getInstance("Collection", null, "SUN");
        } catch (InvalidAlgorithmParameterException iape) {
            System.out.println("test passed");
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("test failed");
            throw nsae;
        }
    }
}
