public class Sha1MessageDigest extends MessageDigest
{
    private int mNativeSha1Context;
    public Sha1MessageDigest()
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
