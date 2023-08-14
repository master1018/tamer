public class ZeroBytePadding
    implements BlockCipherPadding
{
    public void init(SecureRandom random)
        throws IllegalArgumentException
    {
    }
    public String getPaddingName()
    {
        return "ZeroByte";
    }
    public int addPadding(
        byte[]  in,
        int     inOff)
    {
        int added = (in.length - inOff);
        while (inOff < in.length)
        {
            in[inOff] = (byte) 0;
            inOff++;
        }
        return added;
    }
    public int padCount(byte[] in)
        throws InvalidCipherTextException
    {
        int count = in.length;
        while (count > 0)
        {
            if (in[count - 1] != 0)
            {
                break;
            }
            count--;
        }
        return in.length - count;
    }
}
