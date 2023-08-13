public class ISO7816d4Padding
    implements BlockCipherPadding
{
    public void init(SecureRandom random)
        throws IllegalArgumentException
    {
    }
    public String getPaddingName()
    {
        return "ISO7816-4";
    }
    public int addPadding(
        byte[]  in,
        int     inOff)
    {
        int added = (in.length - inOff);
        in [inOff]= (byte) 0x80;
        inOff ++;
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
        int count = in.length - 1;
        while (count > 0 && in[count] == 0)
        {
            count--;
        }
        if (in[count] != (byte)0x80)
        {
            throw new InvalidCipherTextException("pad block corrupted");
        }
        return in.length - count;
    }
}
