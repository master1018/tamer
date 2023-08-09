public final class FloatHolder implements Streamable {
    public float value;
    public FloatHolder() {
    }
    public FloatHolder(float initial) {
        value = initial;
    }
    public void _read(InputStream input) {
        value = input.read_float();
    }
    public void _write(OutputStream output) {
        output.write_float(value);
    }
    public org.omg.CORBA.TypeCode _type() {
        return ORB.init().get_primitive_tc(TCKind.tk_float);
    }
}
