public abstract class ContextList {
    public abstract int count();
    public abstract void add(String ctx);
    public abstract String item(int index) throws org.omg.CORBA.Bounds;
    public abstract void remove(int index) throws org.omg.CORBA.Bounds;
}
