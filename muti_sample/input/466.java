public final class LongSeqHolder implements org.omg.CORBA.portable.Streamable
{
    public int value[] = null;
    public LongSeqHolder ()
    {
    }
    public LongSeqHolder (int[] initialValue)
    {
        value = initialValue;
    }
    public void _read (org.omg.CORBA.portable.InputStream i)
    {
        value = org.omg.CORBA.LongSeqHelper.read (i);
    }
    public void _write (org.omg.CORBA.portable.OutputStream o)
    {
        org.omg.CORBA.LongSeqHelper.write (o, value);
    }
    public org.omg.CORBA.TypeCode _type ()
    {
        return org.omg.CORBA.LongSeqHelper.type ();
    }
}
