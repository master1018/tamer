public final class IntHolder implements Streamable {
    public int value;
    public IntHolder() {
    }
    public IntHolder(int initial) {
        value = initial;
    }
    public void _read(InputStream input) {
        value = input.read_long();
    }
    public void _write(OutputStream output) {
        output.write_long(value);
    }
    public org.omg.CORBA.TypeCode _type() {
        return ORB.init().get_primitive_tc(TCKind.tk_long);
    }
}
