public final class ParameterDescription implements org.omg.CORBA.portable.IDLEntity
{
    public String name = null;
    public org.omg.CORBA.TypeCode type = null;
    public org.omg.CORBA.IDLType type_def = null;
    public com.sun.org.omg.CORBA.ParameterMode mode = null;
    public ParameterDescription ()
    {
    } 
    public ParameterDescription (String _name, org.omg.CORBA.TypeCode _type, org.omg.CORBA.IDLType _type_def, com.sun.org.omg.CORBA.ParameterMode _mode)
    {
        name = _name;
        type = _type;
        type_def = _type_def;
        mode = _mode;
    } 
} 
