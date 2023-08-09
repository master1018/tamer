public abstract class X509NameEntryConverter
{
    protected DERObject convertHexEncoded(
        String  str,
        int     off)
        throws IOException
    {
        str = Strings.toLowerCase(str);
        byte[] data = new byte[(str.length() - off) / 2];
        for (int index = 0; index != data.length; index++)
        {
            char left = str.charAt((index * 2) + off);
            char right = str.charAt((index * 2) + off + 1);
            if (left < 'a')
            {
                data[index] = (byte)((left - '0') << 4);
            }
            else
            {
                data[index] = (byte)((left - 'a' + 10) << 4);
            }
            if (right < 'a')
            {
                data[index] |= (byte)(right - '0');
            }
            else
            {
                data[index] |= (byte)(right - 'a' + 10);
            }
        }
        ASN1InputStream aIn = new ASN1InputStream(data);
        return aIn.readObject();
    }
    protected boolean canBePrintable(
        String  str)
    {
        for (int i = str.length() - 1; i >= 0; i--)
        {
            char    ch = str.charAt(i);
            if (str.charAt(i) > 0x007f)
            {
                return false;
            }
            if ('a' <= ch && ch <= 'z')
            {
                continue;
            }
            if ('A' <= ch && ch <= 'Z')
            {
                continue;
            }
            if ('0' <= ch && ch <= '9')
            {
                continue;
            }
            switch (ch)
            {
            case ' ':
            case '\'':
            case '(':
            case ')':
            case '+':
            case '-':
            case '.':
            case ':':
            case '=':
            case '?':
                continue;
            }
            return false;
        }
        return true;
    }
    public abstract DERObject getConvertedValue(DERObjectIdentifier oid, String value);
}
