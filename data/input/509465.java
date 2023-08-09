public class DERPrintableString
    extends DERObject
    implements DERString
{
    private final String string;
    public static DERPrintableString getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof DERPrintableString)
        {
            return (DERPrintableString)obj;
        }
        if (obj instanceof ASN1OctetString)
        {
            return new DERPrintableString(((ASN1OctetString)obj).getOctets());
        }
        if (obj instanceof ASN1TaggedObject)
        {
            return getInstance(((ASN1TaggedObject)obj).getObject());
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }
    public static DERPrintableString getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject());
    }
    public DERPrintableString(
        byte[]   string)
    {
        char[]  cs = new char[string.length];
        for (int i = 0; i != cs.length; i++)
        {
            cs[i] = (char)(string[i] & 0xff);
        }
        this.string = new String(cs).intern();
    }
    public DERPrintableString(
        String   string)
    {
        this.string = string.intern();
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
        out.writeEncoded(PRINTABLE_STRING, this.getOctets());
    }
    public int hashCode()
    {
        return this.getString().hashCode();
    }
    public boolean equals(
        Object  o)
    {
        if (!(o instanceof DERPrintableString))
        {
            return false;
        }
        DERPrintableString  s = (DERPrintableString)o;
        return this.getString().equals(s.getString());
    }
    public String toString()
    {
      return string;
    }
}
