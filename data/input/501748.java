public class DESedeParameters
    extends DESParameters
{
    static public final int DES_EDE_KEY_LENGTH = 24;
    public DESedeParameters(
        byte[]  key)
    {
        super(key);
        if (isWeakKey(key, 0, 0))
        {
            throw new IllegalArgumentException("attempt to create weak DESede key");
        }
    }
    public static boolean isWeakKey(
        byte[]  key,
        int     offset,
        int     length)
    {
        for (int i = offset; i < length; i += DES_KEY_LENGTH)
        {
            if (DESParameters.isWeakKey(key, i))
            {
                return true;
            }
        }
        return false;
    }
    public static boolean isWeakKey(
        byte[]  key,
        int     offset)
    {
        return isWeakKey(key, offset, key.length - offset);
    }
}
