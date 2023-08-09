public class PKCS7Padding
    implements BlockCipherPadding
{
    public void init(SecureRandom random)
        throws IllegalArgumentException
    {
    }
    public String getPaddingName()
    {
        return "PKCS7";
    }
    public int addPadding(
        byte[]  in,
        int     inOff)
    {
        byte code = (byte)(in.length - inOff);
        while (inOff < in.length)
        {
            in[inOff] = code;
            inOff++;
        }
        return code;
    }
    public int padCount(byte[] in)
        throws InvalidCipherTextException
    {
        int count = in[in.length - 1] & 0xff;
        if (count > in.length)
        {
            throw new InvalidCipherTextException("pad block corrupted");
        }
        for (int i = 1; i <= count; i++)
        {
            if (in[in.length - i] != count)
            {
                throw new InvalidCipherTextException("pad block corrupted");
            }
        }
        return count;
    }
}
