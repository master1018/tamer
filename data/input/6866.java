public class ParameterMode implements org.omg.CORBA.portable.IDLEntity
{
    private        int __value;
    private static int __size = 3;
    private static com.sun.org.omg.CORBA.ParameterMode[] __array = new com.sun.org.omg.CORBA.ParameterMode [__size];
    public static final int _PARAM_IN = 0;
    public static final com.sun.org.omg.CORBA.ParameterMode PARAM_IN = new com.sun.org.omg.CORBA.ParameterMode(_PARAM_IN);
    public static final int _PARAM_OUT = 1;
    public static final com.sun.org.omg.CORBA.ParameterMode PARAM_OUT = new com.sun.org.omg.CORBA.ParameterMode(_PARAM_OUT);
    public static final int _PARAM_INOUT = 2;
    public static final com.sun.org.omg.CORBA.ParameterMode PARAM_INOUT = new com.sun.org.omg.CORBA.ParameterMode(_PARAM_INOUT);
    public int value ()
    {
        return __value;
    }
    public static com.sun.org.omg.CORBA.ParameterMode from_int (int value)
    {
        if (value >= 0 && value < __size)
            return __array[value];
        else
            throw new org.omg.CORBA.BAD_PARAM ();
    }
    protected ParameterMode (int value)
    {
        __value = value;
        __array[__value] = this;
    }
} 
