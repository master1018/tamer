public class OperationMode implements org.omg.CORBA.portable.IDLEntity
{
    private        int __value;
    private static int __size = 2;
    private static com.sun.org.omg.CORBA.OperationMode[] __array = new com.sun.org.omg.CORBA.OperationMode [__size];
    public static final int _OP_NORMAL = 0;
    public static final com.sun.org.omg.CORBA.OperationMode OP_NORMAL = new com.sun.org.omg.CORBA.OperationMode(_OP_NORMAL);
    public static final int _OP_ONEWAY = 1;
    public static final com.sun.org.omg.CORBA.OperationMode OP_ONEWAY = new com.sun.org.omg.CORBA.OperationMode(_OP_ONEWAY);
    public int value ()
    {
        return __value;
    }
    public static com.sun.org.omg.CORBA.OperationMode from_int (int value)
    {
        if (value >= 0 && value < __size)
            return __array[value];
        else
            throw new org.omg.CORBA.BAD_PARAM ();
    }
    protected OperationMode (int value)
    {
        __value = value;
        __array[__value] = this;
    }
} 
