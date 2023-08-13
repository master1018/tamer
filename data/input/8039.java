public final class ValueMemberSeqHelper
{
    private static String  _id = "IDL:omg.org/CORBA/ValueMemberSeq:1.0";
    public ValueMemberSeqHelper()
    {
    }
    public static void insert (org.omg.CORBA.Any a, org.omg.CORBA.ValueMember[] that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }
    public static org.omg.CORBA.ValueMember[] extract (org.omg.CORBA.Any a)
    {
        return read (a.create_input_stream ());
    }
    private static org.omg.CORBA.TypeCode __typeCode = null;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (__typeCode == null)
            {
                __typeCode = com.sun.org.omg.CORBA.ValueMemberHelper.type ();
                __typeCode = org.omg.CORBA.ORB.init ().create_sequence_tc (0, __typeCode);
                __typeCode = org.omg.CORBA.ORB.init ().create_alias_tc (com.sun.org.omg.CORBA.ValueMemberSeqHelper.id (), "ValueMemberSeq", __typeCode);
            }
        return __typeCode;
    }
    public static String id ()
    {
        return _id;
    }
    public static org.omg.CORBA.ValueMember[] read (org.omg.CORBA.portable.InputStream istream)
    {
        org.omg.CORBA.ValueMember value[] = null;
        int _len0 = istream.read_long ();
        value = new org.omg.CORBA.ValueMember[_len0];
        for (int _o1 = 0;_o1 < value.length; ++_o1)
            value[_o1] = com.sun.org.omg.CORBA.ValueMemberHelper.read (istream);
        return value;
    }
    public static void write (org.omg.CORBA.portable.OutputStream ostream, org.omg.CORBA.ValueMember[] value)
    {
        ostream.write_long (value.length);
        for (int _i0 = 0;_i0 < value.length; ++_i0)
            com.sun.org.omg.CORBA.ValueMemberHelper.write (ostream, value[_i0]);
    }
}
