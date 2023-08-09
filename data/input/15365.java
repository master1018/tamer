public final class FixedHolder implements Streamable {
    public java.math.BigDecimal value;
    public FixedHolder() {
    }
    public FixedHolder(java.math.BigDecimal initial) {
        value = initial;
    }
    public void _read(InputStream input) {
        value = input.read_fixed();
    }
    public void _write(OutputStream output) {
        output.write_fixed(value);
    }
    public org.omg.CORBA.TypeCode _type() {
        return ORB.init().get_primitive_tc(TCKind.tk_fixed);
    }
}
