public final class LongLongSeqHolder implements org.omg.CORBA.portable.Streamable
{
    public long value[] = null;
    public LongLongSeqHolder ()
    {
    }
    public LongLongSeqHolder (long[] initialValue)
    {
        value = initialValue;
    }
    public void _read (org.omg.CORBA.portable.InputStream i)
    {
        value = org.omg.CORBA.LongLongSeqHelper.read (i);
    }
    public void _write (org.omg.CORBA.portable.OutputStream o)
    {
        org.omg.CORBA.LongLongSeqHelper.write (o, value);
    }
    public org.omg.CORBA.TypeCode _type ()
    {
        return org.omg.CORBA.LongLongSeqHelper.type ();
    }
}
