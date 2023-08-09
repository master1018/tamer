public final class BigIntegers
{
    public static byte[] asUnsignedByteArray(
        BigInteger value)
    {
        byte[] bytes = value.toByteArray();
        if (bytes[0] == 0)
        {
            byte[] tmp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, tmp, 0, tmp.length);
            return tmp;
        }
        return bytes;
    }
}
