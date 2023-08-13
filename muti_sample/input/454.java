public abstract class ExceptionList {
    public abstract int count();
    public abstract void add(TypeCode exc);
    public abstract TypeCode item(int index)
        throws org.omg.CORBA.Bounds;
    public abstract void remove(int index)
        throws org.omg.CORBA.Bounds;
}
