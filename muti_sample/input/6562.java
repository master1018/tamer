public final class ULongSeqHolder implements org.omg.CORBA.portable.Streamable
{
    public int value[] = null;
    public ULongSeqHolder ()
    {
    }
    public ULongSeqHolder (int[] initialValue)
    {
        value = initialValue;
    }
    public void _read (org.omg.CORBA.portable.InputStream i)
    {
        value = org.omg.CORBA.ULongSeqHelper.read (i);
    }
    public void _write (org.omg.CORBA.portable.OutputStream o)
    {
        org.omg.CORBA.ULongSeqHelper.write (o, value);
    }
    public org.omg.CORBA.TypeCode _type ()
    {
        return org.omg.CORBA.ULongSeqHelper.type ();
    }
}
