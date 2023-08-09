public class DERVisibleString
    extends DERObject
    implements DERString
{
    String  string;
    public static DERVisibleString getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof DERVisibleString)
        {
            return (DERVisibleString)obj;
        }
        if (obj instanceof ASN1OctetString)
        {
            return new DERVisibleString(((ASN1OctetString)obj).getOctets());
        }
        if (obj instanceof ASN1TaggedObject)
        {
            return getInstance(((ASN1TaggedObject)obj).getObject());
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }
    public static DERVisibleString getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject());
    }
    public DERVisibleString(
        byte[]   string)
    {
        char[]  cs = new char[string.length];
        for (int i = 0; i != cs.length; i++)
        {
            cs[i] = (char)(string[i] & 0xff);
        }
        this.string = new String(cs);
    }
    public DERVisibleString(
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
        out.writeEncoded(VISIBLE_STRING, this.getOctets());
    }
    public boolean equals(
        Object  o)
    {
        if ((o == null) || !(o instanceof DERVisibleString))
        {
            return false;
        }
        return this.getString().equals(((DERVisibleString)o).getString());
    }
    public int hashCode()
    {
        return this.getString().hashCode();
    }
}
