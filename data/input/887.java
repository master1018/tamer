public final class UShortSeqHolder implements org.omg.CORBA.portable.Streamable
{
    public short value[] = null;
    public UShortSeqHolder ()
    {
    }
    public UShortSeqHolder (short[] initialValue)
    {
        value = initialValue;
    }
    public void _read (org.omg.CORBA.portable.InputStream i)
    {
        value = org.omg.CORBA.UShortSeqHelper.read (i);
    }
    public void _write (org.omg.CORBA.portable.OutputStream o)
    {
        org.omg.CORBA.UShortSeqHelper.write (o, value);
    }
    public org.omg.CORBA.TypeCode _type ()
    {
        return org.omg.CORBA.UShortSeqHelper.type ();
    }
}
