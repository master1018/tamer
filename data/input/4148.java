public final class IORAddressingInfo implements org.omg.CORBA.portable.IDLEntity
{
  public int selected_profile_index = (int)0;
  public org.omg.IOP.IOR ior = null;
  public IORAddressingInfo ()
  {
  } 
  public IORAddressingInfo (int _selected_profile_index, org.omg.IOP.IOR _ior)
  {
    selected_profile_index = _selected_profile_index;
    ior = _ior;
  } 
} 
