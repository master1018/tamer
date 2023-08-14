public class DERNumericString
    extends DERObject
    implements DERString
{
    String  string;
    public static DERNumericString getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof DERNumericString)
        {
            return (DERNumericString)obj;
        }
        if (obj instanceof ASN1OctetString)
        {
            return new DERNumericString(((ASN1OctetString)obj).getOctets());
        }
        if (obj instanceof ASN1TaggedObject)
        {
            return getInstance(((ASN1TaggedObject)obj).getObject());
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }
    public static DERNumericString getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject());
    }
    public DERNumericString(
        byte[]   string)
    {
        char[]  cs = new char[string.length];
        for (int i = 0; i != cs.length; i++)
        {
            cs[i] = (char)(string[i] & 0xff);
        }
        this.string = new String(cs);
    }
    public DERNumericString(
        String   string)
    {
        this.string = string;
    }
    public String getString()
    {
        return string;
    }
    public byte[] getOctets()
    {
        char[]  cs = string.toCharArray();
        byte[]  bs = new byte[cs.length];
        for (int i = 0; i != cs.length; i++)
        {
            bs[i] = (byte)cs[i];
        }
        return bs; 
    }
    void encode(
        DEROutputStream  out)
        throws IOException
    {
        out.writeEncoded(NUMERIC_STRING, this.getOctets());
    }
    public int hashCode()
    {
        return this.getString().hashCode();
    }
    public boolean equals(
        Object  o)
    {
        if (!(o instanceof DERNumericString))
        {
            return false;
        }
        DERNumericString  s = (DERNumericString)o;
        return this.getString().equals(s.getString());
    }
}
