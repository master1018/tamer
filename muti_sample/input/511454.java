public class DERBitString
    extends DERObject
    implements DERString
{
    private static final char[]  table = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    protected byte[]      data;
    protected int         padBits;
    static protected int getPadBits(
        int bitString)
    {
        int val = 0;
        for (int i = 3; i >= 0; i--) 
        {
            if (i != 0)
            {
                if ((bitString >> (i * 8)) != 0) 
                {
                    val = (bitString >> (i * 8)) & 0xFF;
                    break;
                }
            }
            else
            {
                if (bitString != 0)
                {
                    val = bitString & 0xFF;
                    break;
                }
            }
        }
        if (val == 0)
        {
            return 7;
        }
        int bits = 1;
        while (((val <<= 1) & 0xFF) != 0)
        {
            bits++;
        }
        return 8 - bits;
    }
    static protected byte[] getBytes(int bitString)
    {
        int bytes = 4;
        for (int i = 3; i >= 1; i--)
        {
            if ((bitString & (0xFF << (i * 8))) != 0)
            {
                break;
            }
            bytes--;
        }
        byte[] result = new byte[bytes];
        for (int i = 0; i < bytes; i++)
        {
            result[i] = (byte) ((bitString >> (i * 8)) & 0xFF);
        }
        return result;
    }
    public static DERBitString getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof DERBitString)
        {
            return (DERBitString)obj;
        }
        if (obj instanceof ASN1OctetString)
        {
            byte[]  bytes = ((ASN1OctetString)obj).getOctets();
            int     padBits = bytes[0];
            byte[]  data = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, data, 0, bytes.length - 1);
            return new DERBitString(data, padBits);
        }
        if (obj instanceof ASN1TaggedObject)
        {
            return getInstance(((ASN1TaggedObject)obj).getObject());
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }
    public static DERBitString getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject());
    }
    protected DERBitString(
        byte    data,
        int     padBits)
    {
        this.data = new byte[1];
        this.data[0] = data;
        this.padBits = padBits;
    }
    public DERBitString(
        byte[]  data,
        int     padBits)
    {
        this.data = data;
        this.padBits = padBits;
    }
    public DERBitString(
        byte[]  data)
    {
        this(data, 0);
    }
    public DERBitString(
        DEREncodable  obj)
    {
        try
        {
            ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
            DEROutputStream         dOut = new DEROutputStream(bOut);
            dOut.writeObject(obj);
            dOut.close();
            this.data = bOut.toByteArray();
            this.padBits = 0;
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Error processing object : " + e.toString());
        }
    }
    public byte[] getBytes()
    {
        return data;
    }
    public int getPadBits()
    {
        return padBits;
    }
    public int intValue()
    {
        int value = 0;
        for (int i = 0; i != data.length && i != 4; i++)
        {
            value |= (data[i] & 0xff) << (8 * i);
        }
        return value;
    }
    void encode(
        DEROutputStream  out)
        throws IOException
    {
        byte[]  bytes = new byte[getBytes().length + 1];
        bytes[0] = (byte)getPadBits();
        System.arraycopy(getBytes(), 0, bytes, 1, bytes.length - 1);
        out.writeEncoded(BIT_STRING, bytes);
    }
    public int hashCode()
    {
        int     value = 0;
        for (int i = 0; i != data.length; i++)
        {
            value ^= (data[i] & 0xff) << (i % 4);
        }
        return value;
    }
    public boolean equals(
        Object  o)
    {
        if (!(o instanceof DERBitString))
        {
            return false;
        }
        DERBitString  other = (DERBitString)o;
        if (data.length != other.data.length)
        {
            return false;
        }
        for (int i = 0; i != data.length; i++)
        {
            if (data[i] != other.data[i])
            {
                return false;
            }
        }
        return (padBits == other.padBits);
    }
    public String getString()
    {
        StringBuffer          buf = new StringBuffer("#");
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ASN1OutputStream      aOut = new ASN1OutputStream(bOut);
        try
        {
            aOut.writeObject(this);
        }
        catch (IOException e)
        {
           throw new RuntimeException("internal error encoding BitString");
        }
        byte[]    string = bOut.toByteArray();
        for (int i = 0; i != string.length; i++)
        {
            buf.append(table[(string[i] >>> 4) % 0xf]);
            buf.append(table[string[i] & 0xf]);
        }
        return buf.toString();
    }
    public String toString()
    {
        return getString();
    }
}
