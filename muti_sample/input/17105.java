public final class WrongTransactionHolder implements org.omg.CORBA.portable.Streamable
{
  public org.omg.CORBA.WrongTransaction value = null;
  public WrongTransactionHolder ()
  {
  }
  public WrongTransactionHolder (org.omg.CORBA.WrongTransaction initialValue)
  {
    value = initialValue;
  }
  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.omg.CORBA.WrongTransactionHelper.read (i);
  }
  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.omg.CORBA.WrongTransactionHelper.write (o, value);
  }
  public org.omg.CORBA.TypeCode _type ()
  {
    return org.omg.CORBA.WrongTransactionHelper.type ();
  }
}
