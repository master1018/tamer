public final class SParametresRefluxINPHolder implements org.omg.CORBA.portable.Streamable {
    public org.fudaa.dodico.corba.reflux.SParametresRefluxINP value = null;
    public SParametresRefluxINPHolder() {
    }
    public SParametresRefluxINPHolder(org.fudaa.dodico.corba.reflux.SParametresRefluxINP initialValue) {
        value = initialValue;
    }
    public void _read(org.omg.CORBA.portable.InputStream i) {
        value = org.fudaa.dodico.corba.reflux.SParametresRefluxINPHelper.read(i);
    }
    public void _write(org.omg.CORBA.portable.OutputStream o) {
        org.fudaa.dodico.corba.reflux.SParametresRefluxINPHelper.write(o, value);
    }
    public org.omg.CORBA.TypeCode _type() {
        return org.fudaa.dodico.corba.reflux.SParametresRefluxINPHelper.type();
    }
}
