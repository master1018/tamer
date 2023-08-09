public final class UnionMember implements org.omg.CORBA.portable.IDLEntity {
    public String name;
    public org.omg.CORBA.Any label;
    public org.omg.CORBA.TypeCode type;
    public org.omg.CORBA.IDLType type_def;
    public UnionMember() { }
    public UnionMember(String __name, org.omg.CORBA.Any __label, org.omg.CORBA.TypeCode __type, org.omg.CORBA.IDLType __type_def) {
        name = __name;
        label = __label;
        type = __type;
        type_def = __type_def;
    }
}
