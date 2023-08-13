public class NamedValueImpl extends NamedValue
{
    private String _name;
    private Any    _value;
    private int    _flags;
    private ORB    _orb;
    public NamedValueImpl(ORB orb)
    {
        _orb = orb;
        _value = new AnyImpl(_orb);
    }
    public NamedValueImpl(ORB orb,
                          String name,
                          Any value,
                          int flags)
    {
        _orb    = orb;
        _name   = name;
        _value  = value;
        _flags      = flags;
    }
    public String name()
    {
        return _name;
    }
    public Any value()
    {
        return _value;
    }
    public int flags()
    {
        return _flags;
    }
}
