public final class StructMember implements org.omg.CORBA.portable.IDLEntity {
    public String name;
    public org.omg.CORBA.TypeCode type;
    public org.omg.CORBA.IDLType type_def;
    public StructMember() { }
    public StructMember(String __name, org.omg.CORBA.TypeCode __type, org.omg.CORBA.IDLType __type_def) {
        name = __name;
        type = __type;
        type_def = __type_def;
    }
}
