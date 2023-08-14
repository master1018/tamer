public class MyMessageDigest1 extends MessageDigest {
    public boolean runEngineReset = false;
    public boolean runEngineDigest = false;
    public boolean runEngineUpdate1 = false;
    public boolean runEngineUpdate2 = false;    
    public MyMessageDigest1() {
        super(null);
    }
    public MyMessageDigest1(String algorithm) {
        super(algorithm);
    }
    public void engineReset() {
        runEngineReset = true;
    }
    public byte[] engineDigest() {
        runEngineDigest = true;
        return new byte[0];
    }
    public void engineUpdate(byte arg0) {
        runEngineUpdate1 = true;
    }
    public void engineUpdate(byte[] arg0, int arg1, int arg2) {
        runEngineUpdate2 = true;
    }
}
