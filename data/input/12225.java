public final class OctetSeqHolder implements org.omg.CORBA.portable.Streamable
{
    public byte value[] = null;
    public OctetSeqHolder ()
    {
    }
    public OctetSeqHolder (byte[] initialValue)
    {
        value = initialValue;
    }
    public void _read (org.omg.CORBA.portable.InputStream i)
    {
        value = org.omg.CORBA.OctetSeqHelper.read (i);
    }
    public void _write (org.omg.CORBA.portable.OutputStream o)
    {
        org.omg.CORBA.OctetSeqHelper.write (o, value);
    }
    public org.omg.CORBA.TypeCode _type ()
    {
        return org.omg.CORBA.OctetSeqHelper.type ();
    }
}
