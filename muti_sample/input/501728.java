public class DERGeneralizedTime
    extends DERObject
{
    String      time;
    public static DERGeneralizedTime getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof DERGeneralizedTime)
        {
            return (DERGeneralizedTime)obj;
        }
        if (obj instanceof ASN1OctetString)
        {
            return new DERGeneralizedTime(((ASN1OctetString)obj).getOctets());
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }
    public static DERGeneralizedTime getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject());
    }
    public DERGeneralizedTime(
        String  time)
    {
        this.time = time;
    }
    public DERGeneralizedTime(
        Date time)
    {
        SimpleDateFormat dateF = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
        dateF.setTimeZone(new SimpleTimeZone(0,"Z"));
        this.time = dateF.format(time);
    }
    DERGeneralizedTime(
        byte[]  bytes)
    {
        char[]  dateC = new char[bytes.length];
        for (int i = 0; i != dateC.length; i++)
        {
            dateC[i] = (char)(bytes[i] & 0xff);
        }
        this.time = new String(dateC);
    }
    public String getTime()
    {
        if (time.charAt(time.length() - 1) == 'Z')
        {
            return time.substring(0, time.length() - 1) + "GMT+00:00";
        }
        else
        {
            int signPos = time.length() - 5;
            char sign = time.charAt(signPos);
            if (sign == '-' || sign == '+')
            {
                return time.substring(0, signPos)
                    + "GMT"
                    + time.substring(signPos, signPos + 3)
                    + ":"
                    + time.substring(signPos + 3);
            }
            else
            {
                signPos = time.length() - 3;
                sign = time.charAt(signPos);
                if (sign == '-' || sign == '+')
                {
                    return time.substring(0, signPos)
                        + "GMT"
                        + time.substring(signPos)
                        + ":00";
                }
            }
        }            
        return time;
    }
    public Date getDate() 
        throws ParseException
    {
        SimpleDateFormat dateF;
        if (time.indexOf('.') == 14)
        {
            dateF = new SimpleDateFormat("yyyyMMddHHmmss.SSS'Z'");
        }
        else
        {
            dateF = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
        }
        dateF.setTimeZone(new SimpleTimeZone(0, "Z"));
        return dateF.parse(time);
    }
    private byte[] getOctets()
    {
        char[]  cs = time.toCharArray();
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
        out.writeEncoded(GENERALIZED_TIME, this.getOctets());
    }
    public boolean equals(
        Object  o)
    {
        if ((o == null) || !(o instanceof DERGeneralizedTime))
        {
            return false;
        }
        return time.equals(((DERGeneralizedTime)o).time);
    }
    public int hashCode()
    {
        return time.hashCode();
    }
}
