public final class CurrentHolder implements org.omg.CORBA.portable.Streamable
{
  public org.omg.CORBA.Current value = null;
  public CurrentHolder ()
  {
  }
  public CurrentHolder (org.omg.CORBA.Current initialValue)
  {
    value = initialValue;
  }
  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.omg.CORBA.CurrentHelper.read (i);
  }
  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.omg.CORBA.CurrentHelper.write (o, value);
  }
  public org.omg.CORBA.TypeCode _type ()
  {
    return org.omg.CORBA.CurrentHelper.type ();
  }
}
