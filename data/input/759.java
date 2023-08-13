public final class WCharSeqHolder implements org.omg.CORBA.portable.Streamable
{
    public char value[] = null;
    public WCharSeqHolder ()
    {
    }
    public WCharSeqHolder (char[] initialValue)
    {
        value = initialValue;
    }
    public void _read (org.omg.CORBA.portable.InputStream i)
    {
        value = org.omg.CORBA.WCharSeqHelper.read (i);
    }
    public void _write (org.omg.CORBA.portable.OutputStream o)
    {
        org.omg.CORBA.WCharSeqHelper.write (o, value);
    }
    public org.omg.CORBA.TypeCode _type ()
    {
        return org.omg.CORBA.WCharSeqHelper.type ();
    }
}
