public final class TargetAddress implements org.omg.CORBA.portable.IDLEntity
{
  private byte[] ___object_key;
  private org.omg.IOP.TaggedProfile ___profile;
  private com.sun.corba.se.impl.protocol.giopmsgheaders.IORAddressingInfo ___ior;
  private short __discriminator;
  private boolean __uninitialized = true;
  public TargetAddress ()
  {
  }
  public short discriminator ()
  {
    if (__uninitialized)
      throw new org.omg.CORBA.BAD_OPERATION ();
    return __discriminator;
  }
  public byte[] object_key ()
  {
    if (__uninitialized)
      throw new org.omg.CORBA.BAD_OPERATION ();
    verifyobject_key (__discriminator);
    return ___object_key;
  }
  public void object_key (byte[] value)
  {
    __discriminator = com.sun.corba.se.impl.protocol.giopmsgheaders.KeyAddr.value;
    ___object_key = value;
    __uninitialized = false;
  }
  private void verifyobject_key (short discriminator)
  {
    if (discriminator != com.sun.corba.se.impl.protocol.giopmsgheaders.KeyAddr.value)
      throw new org.omg.CORBA.BAD_OPERATION ();
  }
  public org.omg.IOP.TaggedProfile profile ()
  {
    if (__uninitialized)
      throw new org.omg.CORBA.BAD_OPERATION ();
    verifyprofile (__discriminator);
    return ___profile;
  }
  public void profile (org.omg.IOP.TaggedProfile value)
  {
    __discriminator = com.sun.corba.se.impl.protocol.giopmsgheaders.ProfileAddr.value;
    ___profile = value;
    __uninitialized = false;
  }
  private void verifyprofile (short discriminator)
  {
    if (discriminator != com.sun.corba.se.impl.protocol.giopmsgheaders.ProfileAddr.value)
      throw new org.omg.CORBA.BAD_OPERATION ();
  }
  public com.sun.corba.se.impl.protocol.giopmsgheaders.IORAddressingInfo ior ()
  {
    if (__uninitialized)
      throw new org.omg.CORBA.BAD_OPERATION ();
    verifyior (__discriminator);
    return ___ior;
  }
  public void ior (com.sun.corba.se.impl.protocol.giopmsgheaders.IORAddressingInfo value)
  {
    __discriminator = com.sun.corba.se.impl.protocol.giopmsgheaders.ReferenceAddr.value;
    ___ior = value;
    __uninitialized = false;
  }
  private void verifyior (short discriminator)
  {
    if (discriminator != com.sun.corba.se.impl.protocol.giopmsgheaders.ReferenceAddr.value)
      throw new org.omg.CORBA.BAD_OPERATION ();
  }
  public void _default ()
  {
    __discriminator = -32768;
    __uninitialized = false;
  }
} 
