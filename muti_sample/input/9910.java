public final class LongHolder implements Streamable {
    public long value;
    public LongHolder() {
    }
    public LongHolder(long initial) {
        value = initial;
    }
    public void _read(InputStream input) {
        value = input.read_longlong();
    }
    public void _write(OutputStream output) {
        output.write_longlong(value);
    }
    public org.omg.CORBA.TypeCode _type() {
        return ORB.init().get_primitive_tc(TCKind.tk_longlong);
    }
}
