public class DERBoolean
    extends DERObject
{
    private final byte  value;
    public static final DERBoolean FALSE = new DERBoolean(false);
    public static final DERBoolean TRUE  = new DERBoolean(true);
    public static DERBoolean getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof DERBoolean)
        {
            return (DERBoolean)obj;
        }
        if (obj instanceof ASN1OctetString)
        {
            return getInstance(((ASN1OctetString)obj).getOctets());
        }
        if (obj instanceof ASN1TaggedObject)
        {
            return getInstance(((ASN1TaggedObject)obj).getObject());
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }
    public static DERBoolean getInstance(
        boolean  value)
    {
        return (value ? TRUE : FALSE);
    }
    public static DERBoolean getInstance(
        byte[] octets)
    {
        return (octets[0] != 0) ? TRUE : FALSE;
    }
    public static DERBoolean getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject());
    }
    private DERBoolean(
        boolean     value)
    {
        this.value = (value) ? (byte)0xff : (byte)0;
    }
    public boolean isTrue()
    {
        return (value != 0);
    }
    void encode(
        DEROutputStream out)
        throws IOException
    {
        byte[]  bytes = new byte[1];
        bytes[0] = value;
        out.writeEncoded(BOOLEAN, bytes);
    }
    public boolean equals(
        Object  o)
    {
        if ((o == null) || !(o instanceof DERBoolean))
        {
            return false;
        }
        return (value == ((DERBoolean)o).value);
    }
    public int hashCode()
    {
        return value;
    }
    public String toString()
    {
      return (value != 0) ? "TRUE" : "FALSE";
    }
}
