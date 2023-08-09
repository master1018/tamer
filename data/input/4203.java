public final class ULongLongSeqHolder implements org.omg.CORBA.portable.Streamable
{
    public long value[] = null;
    public ULongLongSeqHolder ()
    {
    }
    public ULongLongSeqHolder (long[] initialValue)
    {
        value = initialValue;
    }
    public void _read (org.omg.CORBA.portable.InputStream i)
    {
        value = org.omg.CORBA.ULongLongSeqHelper.read (i);
    }
    public void _write (org.omg.CORBA.portable.OutputStream o)
    {
        org.omg.CORBA.ULongLongSeqHelper.write (o, value);
    }
    public org.omg.CORBA.TypeCode _type ()
    {
        return org.omg.CORBA.ULongLongSeqHelper.type ();
    }
}
