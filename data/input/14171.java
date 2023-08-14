public class StaticSignedProvFirst {
    public static void main(String[] args) throws Exception {
        Class clazz = Class.forName("com.sun.exp.provider.EXP");
        Provider[] providers = Security.getProviders();
        System.out.println("Providers: " + Arrays.asList(providers));
        if (providers[0].getName().equals("EXP") == false) {
            throw new Exception("EXP provider not loaded!");
        }
        Object[] signers = providers[0].getClass().getSigners();
        if (signers == null || signers.length <= 0) {
            throw new SecurityException("Test Failed - provider not signed");
        } else {
            for (int i = 0; i < signers.length; i++) {
                System.out.println("signer [" + i + "] = " + signers[i]);
            }
        }
        MessageDigest md = MessageDigest.getInstance("SHA1", "EXP");
        System.out.println("test passed");
    }
}
