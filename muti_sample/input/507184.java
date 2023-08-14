public class DERUTCTime
    extends DERObject
{
    String      time;
    public static DERUTCTime getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof DERUTCTime)
        {
            return (DERUTCTime)obj;
        }
        if (obj instanceof ASN1OctetString)
        {
            return new DERUTCTime(((ASN1OctetString)obj).getOctets());
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }
    public static DERUTCTime getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(obj.getObject());
    }
    public DERUTCTime(
        String  time)
    {
        this.time = time;
    }
    public DERUTCTime(
        Date time)
    {
        SimpleDateFormat dateF = new SimpleDateFormat("yyMMddHHmmss'Z'");
        dateF.setTimeZone(new SimpleTimeZone(0,"Z"));
        this.time = dateF.format(time);
    }
    DERUTCTime(
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
        if (time.length() == 11)
        {
            return time.substring(0, 10) + "00GMT+00:00";
        }
        else if (time.length() == 13)
        {
            return time.substring(0, 12) + "GMT+00:00";
        }
        else if (time.length() == 17)
        {
            return time.substring(0, 12) + "GMT" + time.substring(12, 15) + ":" + time.substring(15, 17);
        }
        return time;
    }
    public String getAdjustedTime()
    {
        String   d = this.getTime();
        if (d.charAt(0) < '5')
        {
            return "20" + d;
        }
        else
        {
            return "19" + d;
        }
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
        out.writeEncoded(UTC_TIME, this.getOctets());
    }
    public boolean equals(
        Object  o)
    {
        if ((o == null) || !(o instanceof DERUTCTime))
        {
            return false;
        }
        return time.equals(((DERUTCTime)o).time);
    }
    public int hashCode()
    {
        return time.hashCode();
    }
    public String toString() 
    {
      return time;
    }
}
