public class MyMessageDigest2 extends MessageDigestSpi {
    public static boolean runEngineReset = false;
    public static boolean runEngineDigest = false;
    public static boolean runEngineUpdate1 = false;
    public static boolean runEngineUpdate2 = false;            
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
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
