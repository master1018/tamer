public final class AnyHolder implements  Streamable {
    public Any value;
    public AnyHolder() {
    }
    public AnyHolder(Any initial) {
        value = initial;
    }
    public void _read(InputStream input) {
        value = input.read_any();
    }
    public void _write(OutputStream output) {
        output.write_any(value);
    }
    public TypeCode _type() {
        return ORB.init().get_primitive_tc(TCKind.tk_any);
    }
}
