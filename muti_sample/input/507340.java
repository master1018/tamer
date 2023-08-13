public class DERUTF8String
    extends DERObject
    implements DERString
{
    String string;
    public static DERUTF8String getInstance(Object obj)
    {
        if (obj == null || obj instanceof DERUTF8String)
        {
            return (DERUTF8String)obj;
        }
        if (obj instanceof ASN1OctetString)
        {
            return new DERUTF8String(((ASN1OctetString)obj).getOctets());
        }
        if (obj instanceof ASN1TaggedObject)
        {
            return getInstance(((ASN1TaggedObject)obj).getObject());
        }
        throw new IllegalArgumentException("illegal object in getInstance: "
                + obj.getClass().getName());
    }
    public static DERUTF8String getInstance(
        ASN1TaggedObject obj,
        boolean explicit)
    {
        return getInstance(obj.getObject());
    }
    DERUTF8String(byte[] string)
    {
        this.string = Strings.fromUTF8ByteArray(string);
    }
    public DERUTF8String(String string)
    {
        this.string = string;
    }
    public String getString()
    {
        return string;
    }
    public int hashCode()
    {
        return this.getString().hashCode();
    }
    public boolean equals(Object o)
    {
        if (!(o instanceof DERUTF8String))
        {
            return false;
        }
        DERUTF8String s = (DERUTF8String)o;
        return this.getString().equals(s.getString());
    }
    void encode(DEROutputStream out)
        throws IOException
    {
        out.writeEncoded(UTF8_STRING, Strings.toUTF8ByteArray(string));
    }
}
