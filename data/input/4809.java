public final class FloatSeqHolder implements org.omg.CORBA.portable.Streamable
{
    public float value[] = null;
    public FloatSeqHolder ()
    {
    }
    public FloatSeqHolder (float[] initialValue)
    {
        value = initialValue;
    }
    public void _read (org.omg.CORBA.portable.InputStream i)
    {
        value = org.omg.CORBA.FloatSeqHelper.read (i);
    }
    public void _write (org.omg.CORBA.portable.OutputStream o)
    {
        org.omg.CORBA.FloatSeqHelper.write (o, value);
    }
    public org.omg.CORBA.TypeCode _type ()
    {
        return org.omg.CORBA.FloatSeqHelper.type ();
    }
}
