public final class CharSeqHolder implements org.omg.CORBA.portable.Streamable
{
    public char value[] = null;
    public CharSeqHolder ()
    {
    }
    public CharSeqHolder (char[] initialValue)
    {
        value = initialValue;
    }
    public void _read (org.omg.CORBA.portable.InputStream i)
    {
        value = org.omg.CORBA.CharSeqHelper.read (i);
    }
    public void _write (org.omg.CORBA.portable.OutputStream o)
    {
        org.omg.CORBA.CharSeqHelper.write (o, value);
    }
    public org.omg.CORBA.TypeCode _type ()
    {
        return org.omg.CORBA.CharSeqHelper.type ();
    }
}
