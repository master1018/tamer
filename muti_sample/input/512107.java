public class DESedeKeyGenerator
    extends DESKeyGenerator
{
    public void init(
        KeyGenerationParameters param)
    {
        super.init(param);
        if (strength == 0 || strength == (168 / 8))
        {
            strength = DESedeParameters.DES_EDE_KEY_LENGTH;
        }
        else if (strength == (112 / 8))
        {
            strength = 2 * DESedeParameters.DES_KEY_LENGTH;
        }
        else if (strength != DESedeParameters.DES_EDE_KEY_LENGTH
                && strength != (2 * DESedeParameters.DES_KEY_LENGTH))
        {
            throw new IllegalArgumentException("DESede key must be "
                + (DESedeParameters.DES_EDE_KEY_LENGTH * 8) + " or "
                + (2 * 8 * DESedeParameters.DES_KEY_LENGTH)
                + " bits long.");
        }
    }
    public byte[] generateKey()
    {
        byte[]  newKey = new byte[strength];
        do
        {
            random.nextBytes(newKey);
            DESedeParameters.setOddParity(newKey);
        }
        while (DESedeParameters.isWeakKey(newKey, 0, newKey.length));
        return newKey;
    }
}
