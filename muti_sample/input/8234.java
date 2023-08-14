public final class ValueBaseHolder implements Streamable {
    public java.io.Serializable value;
    public ValueBaseHolder() {
    }
    public ValueBaseHolder(java.io.Serializable initial) {
        value = initial;
    }
    public void _read(InputStream input) {
        value = ((org.omg.CORBA_2_3.portable.InputStream)input).read_value();
    }
    public void _write(OutputStream output) {
        ((org.omg.CORBA_2_3.portable.OutputStream)output).write_value(value);
    }
    public org.omg.CORBA.TypeCode _type() {
        return ORB.init().get_primitive_tc(TCKind.tk_value);
    }
}
