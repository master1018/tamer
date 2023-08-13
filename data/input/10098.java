public abstract class MethodHandle {
    private byte       vmentry;    
     Object vmtarget;   
    static final int  INT_FIELD = 0;
    static final long LONG_FIELD = 0;
    static { MethodHandleImpl.initStatics(); }
    @java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @interface PolymorphicSignature { }
    private MethodType type;
    public MethodType type() {
        return type;
    }
     MethodHandle(MethodType type) {
        type.getClass();  
        this.type = type;
    }
    public final native @PolymorphicSignature Object invokeExact(Object... args) throws Throwable;
    public final native @PolymorphicSignature Object invoke(Object... args) throws Throwable;
    public Object invokeWithArguments(Object... arguments) throws Throwable {
        int argc = arguments == null ? 0 : arguments.length;
        MethodType type = type();
        if (type.parameterCount() != argc || isVarargsCollector()) {
            return asType(MethodType.genericMethodType(argc)).invokeWithArguments(arguments);
        }
        MethodHandle invoker = type.invokers().varargsInvoker();
        return invoker.invokeExact(this, arguments);
    }
    public Object invokeWithArguments(java.util.List<?> arguments) throws Throwable {
        return invokeWithArguments(arguments.toArray());
    }
    public MethodHandle asType(MethodType newType) {
        if (!type.isConvertibleTo(newType)) {
            throw new WrongMethodTypeException("cannot convert "+this+" to "+newType);
        }
        return MethodHandleImpl.convertArguments(this, newType, 1);
    }
    public MethodHandle asSpreader(Class<?> arrayType, int arrayLength) {
        asSpreaderChecks(arrayType, arrayLength);
        return MethodHandleImpl.spreadArguments(this, arrayType, arrayLength);
    }
    private void asSpreaderChecks(Class<?> arrayType, int arrayLength) {
        spreadArrayChecks(arrayType, arrayLength);
        int nargs = type().parameterCount();
        if (nargs < arrayLength || arrayLength < 0)
            throw newIllegalArgumentException("bad spread array length");
        if (arrayType != Object[].class && arrayLength != 0) {
            boolean sawProblem = false;
            Class<?> arrayElement = arrayType.getComponentType();
            for (int i = nargs - arrayLength; i < nargs; i++) {
                if (!MethodType.canConvert(arrayElement, type().parameterType(i))) {
                    sawProblem = true;
                    break;
                }
            }
            if (sawProblem) {
                ArrayList<Class<?>> ptypes = new ArrayList<Class<?>>(type().parameterList());
                for (int i = nargs - arrayLength; i < nargs; i++) {
                    ptypes.set(i, arrayElement);
                }
                this.asType(MethodType.methodType(type().returnType(), ptypes));
            }
        }
    }
    private void spreadArrayChecks(Class<?> arrayType, int arrayLength) {
        Class<?> arrayElement = arrayType.getComponentType();
        if (arrayElement == null)
            throw newIllegalArgumentException("not an array type", arrayType);
        if ((arrayLength & 0x7F) != arrayLength) {
            if ((arrayLength & 0xFF) != arrayLength)
                throw newIllegalArgumentException("array length is not legal", arrayLength);
            assert(arrayLength >= 128);
            if (arrayElement == long.class ||
                arrayElement == double.class)
                throw newIllegalArgumentException("array length is not legal for long[] or double[]", arrayLength);
        }
    }
    public MethodHandle asCollector(Class<?> arrayType, int arrayLength) {
        asCollectorChecks(arrayType, arrayLength);
        MethodHandle collector = ValueConversions.varargsArray(arrayType, arrayLength);
        return MethodHandleImpl.collectArguments(this, type.parameterCount()-1, collector);
    }
    private boolean asCollectorChecks(Class<?> arrayType, int arrayLength) {
        spreadArrayChecks(arrayType, arrayLength);
        int nargs = type().parameterCount();
        if (nargs != 0) {
            Class<?> lastParam = type().parameterType(nargs-1);
            if (lastParam == arrayType)  return true;
            if (lastParam.isAssignableFrom(arrayType))  return false;
        }
        throw newIllegalArgumentException("array type not assignable to trailing argument", this, arrayType);
    }
    public MethodHandle asVarargsCollector(Class<?> arrayType) {
        Class<?> arrayElement = arrayType.getComponentType();
        boolean lastMatch = asCollectorChecks(arrayType, 0);
        if (isVarargsCollector() && lastMatch)
            return this;
        return AdapterMethodHandle.makeVarargsCollector(this, arrayType);
    }
    public boolean isVarargsCollector() {
        return false;
    }
    public MethodHandle asFixedArity() {
        assert(!isVarargsCollector());
        return this;
    }
    public MethodHandle bindTo(Object x) {
        Class<?> ptype;
        if (type().parameterCount() == 0 ||
            (ptype = type().parameterType(0)).isPrimitive())
            throw newIllegalArgumentException("no leading reference parameter", x);
        x = MethodHandles.checkValue(ptype, x);
        MethodHandle bmh = MethodHandleImpl.bindReceiver(this, x);
        if (bmh != null)  return bmh;
        return MethodHandleImpl.bindArgument(this, 0, x);
    }
    @Override
    public String toString() {
        if (DEBUG_METHOD_HANDLE_NAMES)  return debugString();
        return "MethodHandle"+type;
    }
    String debugString() {
        return getNameString(this);
    }
}
