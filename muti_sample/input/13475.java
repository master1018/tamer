public final class ValueMember implements org.omg.CORBA.portable.IDLEntity {
    public String name;
    public String id;
    public String defined_in;
    public String version;
    public org.omg.CORBA.TypeCode type;
    public org.omg.CORBA.IDLType type_def;
    public short access;
    public ValueMember() { }
    public ValueMember(String __name, String __id, String __defined_in, String __version, org.omg.CORBA.TypeCode __type, org.omg.CORBA.IDLType __type_def, short __access) {
        name = __name;
        id = __id;
        defined_in = __defined_in;
        version = __version;
        type = __type;
        type_def = __type_def;
        access = __access;
    }
}
