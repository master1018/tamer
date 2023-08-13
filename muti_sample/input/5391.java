public abstract class TypeCode implements IDLEntity {
    public abstract boolean equal(TypeCode tc);
    public abstract boolean equivalent(TypeCode tc);
    public abstract TypeCode get_compact_typecode();
    public abstract TCKind kind();
    public abstract String id() throws BadKind;
    public abstract String name() throws BadKind;
    public abstract int member_count() throws BadKind;
    public abstract String member_name(int index)
        throws BadKind, org.omg.CORBA.TypeCodePackage.Bounds;
    public abstract TypeCode member_type(int index)
        throws BadKind, org.omg.CORBA.TypeCodePackage.Bounds;
    public abstract Any member_label(int index)
        throws BadKind, org.omg.CORBA.TypeCodePackage.Bounds;
    public abstract TypeCode discriminator_type()
        throws BadKind;
    public abstract int default_index() throws BadKind;
    public abstract int length() throws BadKind;
    public abstract TypeCode content_type() throws BadKind;
    public abstract short fixed_digits() throws BadKind ;
    public abstract short fixed_scale() throws BadKind ;
    abstract public short member_visibility(int index)
        throws BadKind, org.omg.CORBA.TypeCodePackage.Bounds ;
    abstract public short type_modifier() throws BadKind ;
    abstract public TypeCode concrete_base_type() throws BadKind ;
}
