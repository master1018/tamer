public final class AnySeqHolder implements org.omg.CORBA.portable.Streamable
{
    public org.omg.CORBA.Any value[] = null;
    public AnySeqHolder ()
    {
    }
    public AnySeqHolder (org.omg.CORBA.Any[] initialValue)
    {
        value = initialValue;
    }
    public void _read (org.omg.CORBA.portable.InputStream i)
    {
        value = org.omg.CORBA.AnySeqHelper.read (i);
    }
    public void _write (org.omg.CORBA.portable.OutputStream o)
    {
        org.omg.CORBA.AnySeqHelper.write (o, value);
    }
    public org.omg.CORBA.TypeCode _type ()
    {
        return org.omg.CORBA.AnySeqHelper.type ();
    }
}
