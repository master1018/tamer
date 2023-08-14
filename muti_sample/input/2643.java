public final class ShortSeqHolder implements org.omg.CORBA.portable.Streamable
{
    public short value[] = null;
    public ShortSeqHolder ()
    {
    }
    public ShortSeqHolder (short[] initialValue)
    {
        value = initialValue;
    }
    public void _read (org.omg.CORBA.portable.InputStream i)
    {
        value = org.omg.CORBA.ShortSeqHelper.read (i);
    }
    public void _write (org.omg.CORBA.portable.OutputStream o)
    {
        org.omg.CORBA.ShortSeqHelper.write (o, value);
    }
    public org.omg.CORBA.TypeCode _type ()
    {
        return org.omg.CORBA.ShortSeqHelper.type ();
    }
}
