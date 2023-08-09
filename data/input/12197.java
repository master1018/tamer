public class DynSignedProvFirst {
    public static void main(String[] args) throws Exception {
        Provider p = new com.sun.exp.provider.EXP();
        Security.insertProviderAt(p, 1);
        Object[] signers = p.getClass().getSigners();
        if (signers == null || signers.length <= 0) {
            throw new SecurityException("Test Failed");
        } else {
            for (int i = 0; i < signers.length; i++) {
                System.out.println("signer [" + i + "] = " + signers[i]);
            }
        }
        MessageDigest md = MessageDigest.getInstance("SHA1");
        System.out.println("test passed");
    }
}
