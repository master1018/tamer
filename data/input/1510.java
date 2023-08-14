public final class DoubleHolder implements Streamable {
    public double value;
    public DoubleHolder() {
    }
    public DoubleHolder(double initial) {
        value = initial;
    }
    public void _read(InputStream input) {
        value = input.read_double();
    }
    public void _write(OutputStream output) {
        output.write_double(value);
    }
    public org.omg.CORBA.TypeCode _type() {
        return ORB.init().get_primitive_tc(TCKind.tk_double);
    }
}
