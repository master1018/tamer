public class X509DefaultEntryConverter
    extends X509NameEntryConverter
{
    public DERObject getConvertedValue(
        DERObjectIdentifier  oid,
        String               value)
    {
        if (value.length() != 0 && value.charAt(0) == '#')
        {
            try
            {
                return convertHexEncoded(value, 1);
            }
            catch (IOException e)
            {
                throw new RuntimeException("can't recode value for oid " + oid.getId());
            }
        }
        else if (oid.equals(X509Name.EmailAddress) || oid.equals(X509Name.DC))
        {
            return new DERIA5String(value);
        }
        else if (oid.equals(X509Name.DATE_OF_BIRTH))
        {
            return new DERGeneralizedTime(value);
        }
        return new DERUTF8String(value);
    }
}
