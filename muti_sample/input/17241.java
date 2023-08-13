public final class BooleanSeqHolder implements org.omg.CORBA.portable.Streamable
{
    public boolean value[] = null;
    public BooleanSeqHolder ()
    {
    }
    public BooleanSeqHolder (boolean[] initialValue)
    {
        value = initialValue;
    }
    public void _read (org.omg.CORBA.portable.InputStream i)
    {
        value = org.omg.CORBA.BooleanSeqHelper.read (i);
    }
    public void _write (org.omg.CORBA.portable.OutputStream o)
    {
        org.omg.CORBA.BooleanSeqHelper.write (o, value);
    }
    public org.omg.CORBA.TypeCode _type ()
    {
        return org.omg.CORBA.BooleanSeqHelper.type ();
    }
}
