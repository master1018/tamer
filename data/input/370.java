public final class PolicyListHolder implements org.omg.CORBA.portable.Streamable
{
  public org.omg.CORBA.Policy value[] = null;
  public PolicyListHolder ()
  {
  }
  public PolicyListHolder (org.omg.CORBA.Policy[] initialValue)
  {
    value = initialValue;
  }
  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.omg.CORBA.PolicyListHelper.read (i);
  }
  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.omg.CORBA.PolicyListHelper.write (o, value);
  }
  public org.omg.CORBA.TypeCode _type ()
  {
    return org.omg.CORBA.PolicyListHelper.type ();
  }
}
