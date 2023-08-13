public class BERConstructedOctetString
    extends DEROctetString
{
    static private byte[] toBytes(
        Vector  octs)
    {
        ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
        for (int i = 0; i != octs.size(); i++)
        {
            try
            {
                DEROctetString  o = (DEROctetString)octs.elementAt(i);
                bOut.write(o.getOctets());
            }
            catch (ClassCastException e)
            {
                throw new IllegalArgumentException(octs.elementAt(i).getClass().getName() + " found in input should only contain DEROctetString");
            }
            catch (IOException e)
            {
                throw new IllegalArgumentException("exception converting octets " + e.toString());
            }
        }
        return bOut.toByteArray();
    }
    private Vector  octs;
    public BERConstructedOctetString(
        byte[]  string)
    {
        super(string);
    }
    public BERConstructedOctetString(
        Vector  octs)
    {
        super(toBytes(octs));
        this.octs = octs;
    }
    public BERConstructedOctetString(
        DERObject  obj)
    {
        super(obj);
    }
    public BERConstructedOctetString(
        DEREncodable  obj)
    {
        super(obj.getDERObject());
    }
    public byte[] getOctets()
    {
        return string;
    }
    public Enumeration getObjects()
    {
        if (octs == null)
        {
            return generateOcts().elements();
        }
        return octs.elements();
    }
    private Vector generateOcts()
    {
        int     start = 0;
        int     end = 0;
        Vector  vec = new Vector();
        while ((end + 1) < string.length)
        {
            if (string[end] == 0 && string[end + 1] == 0)
            {
                byte[]  nStr = new byte[end - start + 1];
                System.arraycopy(string, start, nStr, 0, nStr.length);
                vec.addElement(new DEROctetString(nStr));
                start = end + 1;
            }
            end++;
        }
        byte[]  nStr = new byte[string.length - start];
        System.arraycopy(string, start, nStr, 0, nStr.length);
        vec.addElement(new DEROctetString(nStr));
        return vec;
    }
    public void encode(
        DEROutputStream out)
        throws IOException
    {
        if (out instanceof ASN1OutputStream || out instanceof BEROutputStream)
        {
            out.write(CONSTRUCTED | OCTET_STRING);
            out.write(0x80);
            if (octs != null)
            {
                for (int i = 0; i != octs.size(); i++)
                {
                    out.writeObject(octs.elementAt(i));
                }
            }
            else
            {
                int     start = 0;
                int     end = 0;
                while ((end + 1) < string.length)
                {
                    if (string[end] == 0 && string[end + 1] == 0)
                    {
                        byte[]  nStr = new byte[end - start + 1];
                        System.arraycopy(string, start, nStr, 0, nStr.length);
                        out.writeObject(new DEROctetString(nStr));
                        start = end + 1;
                    }
                    end++;
                }
                byte[]  nStr = new byte[string.length - start];
                System.arraycopy(string, start, nStr, 0, nStr.length);
                out.writeObject(new DEROctetString(nStr));
            }
            out.write(0x00);
            out.write(0x00);
        }
        else
        {
            super.encode(out);
        }
    }
}
