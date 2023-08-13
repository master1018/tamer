public class NVListImpl extends NVList
{
    private final int    INITIAL_CAPACITY       = 4;
    private final int    CAPACITY_INCREMENT     = 2;
    private Vector _namedValues;
    private ORB orb;
    public NVListImpl(ORB orb)
    {
        this.orb = orb;
        _namedValues = new Vector(INITIAL_CAPACITY, CAPACITY_INCREMENT);
    }
    public NVListImpl(ORB orb, int size)
    {
        this.orb = orb;
        _namedValues = new Vector(size);
    }
    public int count()
    {
        return _namedValues.size();
    }
    public NamedValue add(int flags)
    {
        NamedValue tmpVal = new NamedValueImpl(orb, "", new AnyImpl(orb), flags);
        _namedValues.addElement(tmpVal);
        return tmpVal;
    }
    public NamedValue add_item(String itemName, int flags)
    {
        NamedValue tmpVal = new NamedValueImpl(orb, itemName, new AnyImpl(orb),
                                               flags);
        _namedValues.addElement(tmpVal);
        return tmpVal;
    }
    public NamedValue add_value(String itemName, Any val, int flags)
    {
        NamedValue tmpVal = new NamedValueImpl(orb, itemName, val, flags);
        _namedValues.addElement(tmpVal);
        return tmpVal;
    }
    public NamedValue item(int index)
        throws Bounds
    {
        try {
            return (NamedValue) _namedValues.elementAt(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new Bounds();
        }
    }
    public void remove(int index)
        throws Bounds
    {
        try {
            _namedValues.removeElementAt(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new Bounds();
        }
    }
}
