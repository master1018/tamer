public class ExceptionListImpl extends ExceptionList {
    private final int    INITIAL_CAPACITY       = 2;
    private final int    CAPACITY_INCREMENT     = 2;
    private Vector _exceptions;
    public ExceptionListImpl() {
        _exceptions = new Vector(INITIAL_CAPACITY, CAPACITY_INCREMENT);
    }
    public int count()
    {
        return _exceptions.size();
    }
    public void add(TypeCode tc)
    {
        _exceptions.addElement(tc);
    }
    public TypeCode item(int index)
        throws Bounds
    {
        try {
            return (TypeCode) _exceptions.elementAt(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new Bounds();
        }
    }
    public void remove(int index)
        throws Bounds
    {
        try {
            _exceptions.removeElementAt(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new Bounds();
        }
    }
}
