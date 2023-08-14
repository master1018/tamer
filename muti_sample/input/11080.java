public class VerifyType {
    private VerifyType() { }  
    public static boolean isNullConversion(Class<?> src, Class<?> dst) {
        if (src == dst)            return true;
        if (dst.isInterface())     dst = Object.class;
        if (src.isInterface())     src = Object.class;
        if (src == dst)            return true;  
        if (dst == void.class)     return true;  
        if (isNullType(src))       return !dst.isPrimitive();
        if (!src.isPrimitive())    return dst.isAssignableFrom(src);
        if (!dst.isPrimitive())    return false;
        Wrapper sw = Wrapper.forPrimitiveType(src);
        if (dst == int.class)      return sw.isSubwordOrInt();
        Wrapper dw = Wrapper.forPrimitiveType(dst);
        if (!sw.isSubwordOrInt())  return false;
        if (!dw.isSubwordOrInt())  return false;
        if (!dw.isSigned() && sw.isSigned())  return false;
        return dw.bitWidth() > sw.bitWidth();
    }
    public static boolean isNullReferenceConversion(Class<?> src, Class<?> dst) {
        assert(!dst.isPrimitive());
        if (dst.isInterface())  return true;   
        if (isNullType(src))    return true;
        return dst.isAssignableFrom(src);
    }
    public static boolean isNullType(Class<?> type) {
        if (type == null)  return false;
        return type == NULL_CLASS
            || type == Empty.class
            ;
    }
    private static final Class<?> NULL_CLASS;
    static {
        Class<?> nullClass = null;
        try {
            nullClass = Class.forName("java.lang.Null");
        } catch (ClassNotFoundException ex) {
        }
        NULL_CLASS = nullClass;
    }
    public static boolean isNullConversion(MethodType call, MethodType recv) {
        if (call == recv)  return true;
        int len = call.parameterCount();
        if (len != recv.parameterCount())  return false;
        for (int i = 0; i < len; i++)
            if (!isNullConversion(call.parameterType(i), recv.parameterType(i)))
                return false;
        return isNullConversion(recv.returnType(), call.returnType());
    }
    public static int canPassUnchecked(Class<?> src, Class<?> dst) {
        if (src == dst)
            return 1;
        if (dst.isPrimitive()) {
            if (dst == void.class)
                return 1;
            if (src == void.class)
                return 0;  
            if (!src.isPrimitive())
                return 0;
            Wrapper sw = Wrapper.forPrimitiveType(src);
            Wrapper dw = Wrapper.forPrimitiveType(dst);
            if (sw.isSubwordOrInt() && dw.isSubwordOrInt()) {
                if (sw.bitWidth() >= dw.bitWidth())
                    return -1;   
                if (!dw.isSigned() && sw.isSigned())
                    return -1;   
                return 1;
            }
            if (src == float.class || dst == float.class) {
                if (src == double.class || dst == double.class)
                    return -1;   
                else
                    return 0;    
            } else {
                return 0;
            }
        } else if (src.isPrimitive()) {
            return 0;
        }
        if (isNullReferenceConversion(src, dst))
            return 1;
        return -1;
    }
    public static int canPassRaw(Class<?> src, Class<?> dst) {
        if (dst.isPrimitive()) {
            if (dst == void.class)
                return 1;
            if (src == void.class)
                return dst == int.class ? 1 : 0;
            if (isNullType(src))
                return 1;
            if (!src.isPrimitive())
                return 0;
            Wrapper sw = Wrapper.forPrimitiveType(src);
            Wrapper dw = Wrapper.forPrimitiveType(dst);
            if (sw.stackSlots() == dw.stackSlots())
                return 1;  
            if (sw.isSubwordOrInt() && dw == Wrapper.VOID)
                return 1;  
            return 0;
        } else if (src.isPrimitive()) {
            return 0;
        }
        if (isNullReferenceConversion(src, dst))
            return 1;
        return -1;
    }
    public static boolean isSpreadArgType(Class<?> spreadArg) {
        return spreadArg.isArray();
    }
    public static Class<?> spreadArgElementType(Class<?> spreadArg, int i) {
        return spreadArg.getComponentType();
    }
}
