public abstract class NVList {
    public abstract int count();
    public abstract NamedValue add(int flags);
    public abstract NamedValue add_item(String item_name, int flags);
    public abstract NamedValue add_value(String item_name, Any val, int flags);
    public abstract NamedValue item(int index) throws org.omg.CORBA.Bounds;
    public abstract void remove(int index) throws org.omg.CORBA.Bounds;
}
