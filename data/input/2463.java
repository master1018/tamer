public class InternalBindingValue
{
    public Binding theBinding;
    public String strObjectRef;
    public org.omg.CORBA.Object theObjectRef;
    public InternalBindingValue() {}
    public InternalBindingValue(Binding b, String o) {
        theBinding = b;
        strObjectRef = o;
    }
}
