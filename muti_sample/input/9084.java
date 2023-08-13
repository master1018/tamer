public final class StructMemberSeqHelper
{
    private static String  _id = "IDL:omg.org/CORBA/StructMemberSeq:1.0";
    public StructMemberSeqHelper()
    {
    }
    public static void insert (org.omg.CORBA.Any a, org.omg.CORBA.StructMember[] that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }
    public static org.omg.CORBA.StructMember[] extract (org.omg.CORBA.Any a)
    {
        return read (a.create_input_stream ());
    }
    private static org.omg.CORBA.TypeCode __typeCode = null;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (__typeCode == null)
            {
                __typeCode = com.sun.org.omg.CORBA.StructMemberHelper.type ();
                __typeCode = org.omg.CORBA.ORB.init ().create_sequence_tc (0, __typeCode);
                __typeCode = org.omg.CORBA.ORB.init ().create_alias_tc (com.sun.org.omg.CORBA.StructMemberSeqHelper.id (), "StructMemberSeq", __typeCode);
            }
        return __typeCode;
    }
    public static String id ()
    {
        return _id;
    }
    public static org.omg.CORBA.StructMember[] read (org.omg.CORBA.portable.InputStream istream)
    {
        org.omg.CORBA.StructMember value[] = null;
        int _len0 = istream.read_long ();
        value = new org.omg.CORBA.StructMember[_len0];
        for (int _o1 = 0;_o1 < value.length; ++_o1)
            value[_o1] = com.sun.org.omg.CORBA.StructMemberHelper.read (istream);
        return value;
    }
    public static void write (org.omg.CORBA.portable.OutputStream ostream, org.omg.CORBA.StructMember[] value)
    {
        ostream.write_long (value.length);
        for (int _i0 = 0;_i0 < value.length; ++_i0)
            com.sun.org.omg.CORBA.StructMemberHelper.write (ostream, value[_i0]);
    }
}
