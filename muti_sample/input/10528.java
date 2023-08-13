public final class TypeCodeHolder implements Streamable {
    public TypeCode value;
    public TypeCodeHolder() {
    }
    public TypeCodeHolder(TypeCode initial) {
        value = initial;
    }
    public void _read(InputStream input) {
        value = input.read_TypeCode();
    }
    public void _write(OutputStream output) {
        output.write_TypeCode(value);
    }
    public org.omg.CORBA.TypeCode _type() {
        return ORB.init().get_primitive_tc(TCKind.tk_TypeCode);
    }
}
