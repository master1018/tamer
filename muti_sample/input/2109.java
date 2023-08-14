public final class UnknownUserExceptionHolder implements org.omg.CORBA.portable.Streamable
{
  public org.omg.CORBA.UnknownUserException value = null;
  public UnknownUserExceptionHolder ()
  {
  }
  public UnknownUserExceptionHolder (org.omg.CORBA.UnknownUserException initialValue)
  {
    value = initialValue;
  }
  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = org.omg.CORBA.UnknownUserExceptionHelper.read (i);
  }
  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    org.omg.CORBA.UnknownUserExceptionHelper.write (o, value);
  }
  public org.omg.CORBA.TypeCode _type ()
  {
    return org.omg.CORBA.UnknownUserExceptionHelper.type ();
  }
}
