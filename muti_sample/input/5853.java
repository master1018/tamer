abstract public class SetOverrideTypeHelper
{
  private static String  _id = "IDL:omg.org/CORBA/SetOverrideType:1.0";
  public static void insert (org.omg.CORBA.Any a, org.omg.CORBA.SetOverrideType that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }
  public static org.omg.CORBA.SetOverrideType extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }
  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_enum_tc (org.omg.CORBA.SetOverrideTypeHelper.id (), "SetOverrideType", new String[] { "SET_OVERRIDE", "ADD_OVERRIDE"} );
    }
    return __typeCode;
  }
  public static String id ()
  {
    return _id;
  }
  public static org.omg.CORBA.SetOverrideType read (org.omg.CORBA.portable.InputStream istream)
  {
    return org.omg.CORBA.SetOverrideType.from_int (istream.read_long ());
  }
  public static void write (org.omg.CORBA.portable.OutputStream ostream, org.omg.CORBA.SetOverrideType value)
  {
    ostream.write_long (value.value ());
  }
}
