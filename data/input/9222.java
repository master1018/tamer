abstract public class CurrentHelper
{
    private static String  _id = "IDL:omg.org/PortableServer/Current:2.3";
    public static void insert (org.omg.CORBA.Any a,
        org.omg.PortableServer.Current that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }
    public static org.omg.PortableServer.Current extract (org.omg.CORBA.Any a)
    {
        return read (a.create_input_stream ());
    }
    private static org.omg.CORBA.TypeCode __typeCode = null;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (__typeCode == null)
        {
            __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (
                org.omg.PortableServer.CurrentHelper.id (), "Current");
        }
        return __typeCode;
    }
    public static String id ()
    {
        return _id;
    }
    public static org.omg.PortableServer.Current read (
        org.omg.CORBA.portable.InputStream istream)
    {
        throw new org.omg.CORBA.MARSHAL ();
    }
    public static void write (org.omg.CORBA.portable.OutputStream ostream,
        org.omg.PortableServer.Current value)
    {
        throw new org.omg.CORBA.MARSHAL ();
    }
    public static org.omg.PortableServer.Current narrow (
        org.omg.CORBA.Object obj)
    {
        if (obj == null)
            return null;
        else if (obj instanceof org.omg.PortableServer.Current)
            return (org.omg.PortableServer.Current)obj;
        else if (!obj._is_a (id ()))
            throw new org.omg.CORBA.BAD_PARAM ();
        return null;
    }
}
