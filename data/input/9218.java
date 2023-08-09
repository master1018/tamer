public final class PolicyHolder implements org.omg.CORBA.portable.Streamable
{
  public org.omg.CORBA.Policy value = null;
  public PolicyHolder ()
  {
  }
  public PolicyHolder (org.omg.CORBA.Policy initialValue)
  {
    value = initialValue;
  }
  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.omg.CORBA.PolicyHelper.read (i);
  }
  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.omg.CORBA.PolicyHelper.write (o, value);
  }
  public org.omg.CORBA.TypeCode _type ()
  {
    return org.omg.CORBA.PolicyHelper.type ();
  }
}
