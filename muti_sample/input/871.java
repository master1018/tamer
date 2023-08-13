public final class NameValuePair implements org.omg.CORBA.portable.IDLEntity {
    public String id;
    public org.omg.CORBA.Any value;
    public NameValuePair() { }
    public NameValuePair(String __id, org.omg.CORBA.Any __value) {
        id = __id;
        value = __value;
    }
}
