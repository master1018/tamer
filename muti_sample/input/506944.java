public class DERUnknownTag
    extends DERObject
{
    int         tag;
    byte[]      data;
    public DERUnknownTag(
        int     tag,
        byte[]  data)
    {
        this.tag = tag;
        this.data = data;
    }
    public int getTag()
    {
        return tag;
    }
    public byte[] getData()
    {
        return data;
    }
    void encode(
        DEROutputStream  out)
        throws IOException
    {
        out.writeEncoded(tag, data);
    }
    public boolean equals(
        Object o)
    {
        if ((o == null) || !(o instanceof DERUnknownTag))
        {
            return false;
        }
        DERUnknownTag other = (DERUnknownTag)o;
        if (tag != other.tag)
        {
            return false;
        }
        if (data.length != other.data.length)
        {
            return false;
        }
        for (int i = 0; i < data.length; i++) 
        {
            if(data[i] != other.data[i])
            {
                return false;
            }
        }
        return true;
    }
    public int hashCode()
    {
        byte[]  b = this.getData();
        int     value = 0;
        for (int i = 0; i != b.length; i++)
        {
            value ^= (b[i] & 0xff) << (i % 4);
        }
        return value ^ this.getTag();
    }
}
