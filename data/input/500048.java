public class SecretKeyFactoryThread extends TestThread {
    SecretKeyFactoryThread(String[] names) {
        super(names);
    }
    @Override
    public void test() throws Exception {
        SecretKeyFactory skf = SecretKeyFactory.getInstance(algName);
        byte[] b = new byte[24];
        KeySpec ks = (KeySpec) ((algName == "DES") ? new DESKeySpec(b) :
            (algName == "DESede") ? new DESedeKeySpec(b) :
            new PBEKeySpec("passw".toCharArray()));
        skf.generateSecret(ks);
    }
}
