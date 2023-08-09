public final class DefinitionKindHelper
{
    private static String  _id = "IDL:omg.org/CORBA/DefinitionKind:1.0";
    public DefinitionKindHelper()
    {
    }
    public static void insert (org.omg.CORBA.Any a, org.omg.CORBA.DefinitionKind that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }
    public static org.omg.CORBA.DefinitionKind extract (org.omg.CORBA.Any a)
    {
        return read (a.create_input_stream ());
    }
    private static org.omg.CORBA.TypeCode __typeCode = null;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (__typeCode == null)
            {
                __typeCode = org.omg.CORBA.ORB.init ().create_enum_tc (com.sun.org.omg.CORBA.DefinitionKindHelper.id (), "DefinitionKind", new String[] { "dk_none", "dk_all", "dk_Attribute", "dk_Constant", "dk_Exception", "dk_Interface", "dk_Module", "dk_Operation", "dk_Typedef", "dk_Alias", "dk_Struct", "dk_Union", "dk_Enum", "dk_Primitive", "dk_String", "dk_Sequence", "dk_Array", "dk_Repository", "dk_Wstring", "dk_Fixed", "dk_Value", "dk_ValueBox", "dk_ValueMember", "dk_Native"} );
            }
        return __typeCode;
    }
    public static String id ()
    {
        return _id;
    }
    public static org.omg.CORBA.DefinitionKind read (org.omg.CORBA.portable.InputStream istream)
    {
        return org.omg.CORBA.DefinitionKind.from_int (istream.read_long ());
    }
    public static void write (org.omg.CORBA.portable.OutputStream ostream, org.omg.CORBA.DefinitionKind value)
    {
        ostream.write_long (value.value ());
    }
}
