public class HmacSaltLengths {
    private static void runTest(String alg, byte[] plaintext,
                                char[] password, Provider p)
        throws Exception {
        Mac mac = Mac.getInstance(alg, p);
        PBEKeySpec pbeKeySpec = new PBEKeySpec(password);
        SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBE", p);
        SecretKey key = keyFac.generateSecret(pbeKeySpec);
        System.out.println("testing parameters with 4-byte salt...");
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec
            (new byte[4], 1024);
        try {
            mac.init(key, pbeParamSpec);
            throw new Exception("ERROR: should throw IAPE for short salts");
        } catch (InvalidAlgorithmParameterException iape) {
        }
        System.out.println("testing parameters with 8-byte salt...");
        pbeParamSpec = new PBEParameterSpec(new byte[8], 1024);
        mac.init(key, pbeParamSpec);
        mac.doFinal(plaintext);
        System.out.println("testing parameters with 20-byte salt...");
        pbeParamSpec = new PBEParameterSpec(new byte[20], 1024);
        mac.init(key, pbeParamSpec);
        mac.doFinal(plaintext);
        System.out.println("testing parameters with 30-byte salt...");
        pbeParamSpec = new PBEParameterSpec(new byte[30], 1024);
        mac.init(key, pbeParamSpec);
        mac.doFinal(plaintext);
        System.out.println("passed: " + alg);
    }
    public static void main(String[] argv) throws Exception {
        byte[] input = new byte[1024];
        new SecureRandom().nextBytes(input);
        char[] PASSWD = { 'p','a','s','s','w','o','r','d' };
        long start = System.currentTimeMillis();
        Provider p = Security.getProvider("SunJCE");
        System.out.println("Testing provider " + p.getName() + "...");
        runTest("HmacPBESHA1", input, PASSWD, p);
        System.out.println("All tests passed");
        long stop = System.currentTimeMillis();
        System.out.println("Done (" + (stop - start) + " ms).");
    }
}
class MyPBEKey implements PBEKey {
    char[] passwd;
    byte[] salt;
    int iCount;
    MyPBEKey(char[] passwd, byte[] salt, int iCount) {
        this.passwd = passwd;
        this.salt = salt;
        this.iCount = iCount;
    }
    public char[] getPassword() { return passwd; }
    public byte[] getSalt() { return salt; }
    public int getIterationCount() { return iCount; }
    public String getAlgorithm() { return "PBE"; }
    public String getFormat() { return "RAW"; }
    public byte[] getEncoded() { return null; }
}
