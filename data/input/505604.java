public abstract class MessageDigest 
{    
    public static MessageDigest getInstance(String algorithm) 
        throws NoSuchAlgorithmException
    {
        if (algorithm == null) {
            return null;
        }
        if (algorithm.equals("SHA-1")) {
            return new Sha1MessageDigest();
        }
        else if (algorithm.equals("MD5")) {
            return new Md5MessageDigest();
        }
        throw new NoSuchAlgorithmException();
    }
    public abstract void update(byte[] input);    
    public abstract byte[] digest();
    public abstract byte[] digest(byte[] input);
}
