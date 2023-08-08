public final class SResultatsEffortsTranchantsHolder implements org.omg.CORBA.portable.Streamable {
    public org.fudaa.dodico.corba.oscar.SResultatsEffortsTranchants value = null;
    public SResultatsEffortsTranchantsHolder() {
    }
    public SResultatsEffortsTranchantsHolder(org.fudaa.dodico.corba.oscar.SResultatsEffortsTranchants initialValue) {
        value = initialValue;
    }
    public void _read(org.omg.CORBA.portable.InputStream i) {
        value = org.fudaa.dodico.corba.oscar.SResultatsEffortsTranchantsHelper.read(i);
    }
    public void _write(org.omg.CORBA.portable.OutputStream o) {
        org.fudaa.dodico.corba.oscar.SResultatsEffortsTranchantsHelper.write(o, value);
    }
    public org.omg.CORBA.TypeCode _type() {
        return org.fudaa.dodico.corba.oscar.SResultatsEffortsTranchantsHelper.type();
    }
}
