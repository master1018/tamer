public class X923Padding
    implements BlockCipherPadding
{
    SecureRandom    random = null;
    public void init(SecureRandom random)
        throws IllegalArgumentException
    {
        this.random = random;
    }
    public String getPaddingName()
    {
        return "X9.23";
    }
    public int addPadding(
        byte[]  in,
        int     inOff)
    {
        byte code = (byte)(in.length - inOff);
        while (inOff < in.length - 1)
        {
            if (random == null)
            {
                in[inOff] = 0;
            }
            else
            {
                in[inOff] = (byte)random.nextInt();
            }
            inOff++;
        }
        in[inOff] = code;
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
        return count;
    }
}
