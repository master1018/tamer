public final class StringHolder implements Streamable {
    public String value;
    public StringHolder() {
    }
    public StringHolder(String initial) {
        value = initial;
    }
    public void _read(InputStream input) {
        value = input.read_string();
    }
    public void _write(OutputStream output) {
        output.write_string(value);
    }
    public org.omg.CORBA.TypeCode _type() {
        return ORB.init().get_primitive_tc(TCKind.tk_string);
    }
}
