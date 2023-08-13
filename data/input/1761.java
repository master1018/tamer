public final class ShortHolder implements Streamable {
    public short value;
    public ShortHolder() {
    }
    public ShortHolder(short initial) {
        value = initial;
    }
    public void _read(InputStream input) {
        value = input.read_short();
    }
    public void _write(OutputStream output) {
        output.write_short(value);
    }
    public org.omg.CORBA.TypeCode _type() {
        return ORB.init().get_primitive_tc(TCKind.tk_short);
    }
}
