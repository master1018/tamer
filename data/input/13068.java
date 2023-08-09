public final class OpDescriptionSeqHelper
{
    private static String  _id = "IDL:omg.org/CORBA/OpDescriptionSeq:1.0";
    public OpDescriptionSeqHelper()
    {
    }
    public static void insert (org.omg.CORBA.Any a, com.sun.org.omg.CORBA.OperationDescription[] that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }
    public static com.sun.org.omg.CORBA.OperationDescription[] extract (org.omg.CORBA.Any a)
    {
        return read (a.create_input_stream ());
    }
    private static org.omg.CORBA.TypeCode __typeCode = null;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (__typeCode == null)
            {
                __typeCode = com.sun.org.omg.CORBA.OperationDescriptionHelper.type ();
                __typeCode = org.omg.CORBA.ORB.init ().create_sequence_tc (0, __typeCode);
                __typeCode = org.omg.CORBA.ORB.init ().create_alias_tc (com.sun.org.omg.CORBA.OpDescriptionSeqHelper.id (), "OpDescriptionSeq", __typeCode);
            }
        return __typeCode;
    }
    public static String id ()
    {
        return _id;
    }
    public static com.sun.org.omg.CORBA.OperationDescription[] read (org.omg.CORBA.portable.InputStream istream)
    {
        com.sun.org.omg.CORBA.OperationDescription value[] = null;
        int _len0 = istream.read_long ();
        value = new com.sun.org.omg.CORBA.OperationDescription[_len0];
        for (int _o1 = 0;_o1 < value.length; ++_o1)
            value[_o1] = com.sun.org.omg.CORBA.OperationDescriptionHelper.read (istream);
        return value;
    }
    public static void write (org.omg.CORBA.portable.OutputStream ostream, com.sun.org.omg.CORBA.OperationDescription[] value)
    {
        ostream.write_long (value.length);
        for (int _i0 = 0;_i0 < value.length; ++_i0)
            com.sun.org.omg.CORBA.OperationDescriptionHelper.write (ostream, value[_i0]);
    }
}
