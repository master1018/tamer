public final class ObjectHolder implements Streamable {
    public Object value;
    public ObjectHolder() {
    }
    public ObjectHolder(Object initial) {
        value = initial;
    }
    public void _read(InputStream input) {
        value = input.read_Object();
    }
    public void _write(OutputStream output) {
        output.write_Object(value);
    }
    public org.omg.CORBA.TypeCode _type() {
        return org.omg.CORBA.ORB.init().get_primitive_tc(TCKind.tk_objref);
    }
}
