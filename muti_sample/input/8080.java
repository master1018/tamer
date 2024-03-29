public final class CodeBaseHelper
{
    private static String  _id = "IDL:omg.org/SendingContext/CodeBase:1.0";
    public CodeBaseHelper()
    {
    }
    public static void insert (org.omg.CORBA.Any a, com.sun.org.omg.SendingContext.CodeBase that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }
    public static com.sun.org.omg.SendingContext.CodeBase extract (org.omg.CORBA.Any a)
    {
        return read (a.create_input_stream ());
    }
    private static org.omg.CORBA.TypeCode __typeCode = null;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (__typeCode == null)
            {
                __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (com.sun.org.omg.SendingContext.CodeBaseHelper.id (), "CodeBase");
            }
        return __typeCode;
    }
    public static String id ()
    {
        return _id;
    }
    public static com.sun.org.omg.SendingContext.CodeBase read (org.omg.CORBA.portable.InputStream istream)
    {
        return narrow (istream.read_Object (_CodeBaseStub.class));
    }
    public static void write (org.omg.CORBA.portable.OutputStream ostream, com.sun.org.omg.SendingContext.CodeBase value)
    {
        ostream.write_Object ((org.omg.CORBA.Object) value);
    }
    public static com.sun.org.omg.SendingContext.CodeBase narrow (org.omg.CORBA.Object obj)
    {
        if (obj == null)
            return null;
        else if (obj instanceof com.sun.org.omg.SendingContext.CodeBase)
            return (com.sun.org.omg.SendingContext.CodeBase)obj;
        else if (!obj._is_a (id ()))
            throw new org.omg.CORBA.BAD_PARAM ();
        else
            {
                org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
                return new com.sun.org.omg.SendingContext._CodeBaseStub (delegate);
            }
    }
}
