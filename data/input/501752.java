public class Md5MessageDigest extends MessageDigest
{
    private int mNativeMd5Context;
    public Md5MessageDigest()
    {
        init();
    }
    public byte[] digest(byte[] input)
    {
        update(input);
        return digest();
    }
    private native void init();
    public native void update(byte[] input);  
    public native byte[] digest();
    native public void reset();
}
