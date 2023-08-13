class BoundMethodHandle extends MethodHandle {
    private final Object argument;     
    private final int    vmargslot;    
     BoundMethodHandle(DirectMethodHandle mh, Object argument) {
        super(mh.type().dropParameterTypes(0, 1));
        this.argument = checkReferenceArgument(argument, mh, 0);
        this.vmargslot = this.type().parameterSlotCount();
        initTarget(mh, 0);
    }
     BoundMethodHandle(MethodHandle mh, Object argument, int argnum) {
        this(mh.type().dropParameterTypes(argnum, argnum+1),
             mh, argument, argnum);
    }
     BoundMethodHandle(MethodType type, MethodHandle mh, Object argument, int argnum) {
        super(type);
        if (mh.type().parameterType(argnum).isPrimitive())
            this.argument = bindPrimitiveArgument(argument, mh, argnum);
        else {
            this.argument = checkReferenceArgument(argument, mh, argnum);
        }
        this.vmargslot = type.parameterSlotDepth(argnum);
        initTarget(mh, argnum);
    }
    private void initTarget(MethodHandle mh, int argnum) {
        MethodHandleNatives.init(this, mh, argnum);
    }
     BoundMethodHandle(MethodType type, Object argument, int vmargslot) {
        super(type);
        this.argument = argument;
        this.vmargslot = vmargslot;
        assert(this instanceof AdapterMethodHandle);
    }
     BoundMethodHandle(MethodHandle entryPoint) {
        super(entryPoint.type().dropParameterTypes(0, 1));
        this.argument = this; 
        this.vmargslot = this.type().parameterSlotDepth(0);
        initTarget(entryPoint, 0);
    }
    final static Object checkReferenceArgument(Object argument, MethodHandle mh, int argnum) {
        Class<?> ptype = mh.type().parameterType(argnum);
        if (ptype.isPrimitive()) {
        } else if (argument == null) {
            return null;
        } else if (VerifyType.isNullReferenceConversion(argument.getClass(), ptype)) {
            return argument;
        }
        throw badBoundArgumentException(argument, mh, argnum);
    }
    final static Object bindPrimitiveArgument(Object argument, MethodHandle mh, int argnum) {
        Class<?> ptype = mh.type().parameterType(argnum);
        Wrapper  wrap = Wrapper.forPrimitiveType(ptype);
        Object   zero  = wrap.zero();
        if (zero == null) {
        } else if (argument == null) {
            if (ptype != int.class && wrap.isSubwordOrInt())
                return Integer.valueOf(0);
            else
                return zero;
        } else if (VerifyType.isNullReferenceConversion(argument.getClass(), zero.getClass())) {
            if (ptype != int.class && wrap.isSubwordOrInt())
                return Wrapper.INT.wrap(argument);
            else
                return argument;
        }
        throw badBoundArgumentException(argument, mh, argnum);
    }
    final static RuntimeException badBoundArgumentException(Object argument, MethodHandle mh, int argnum) {
        String atype = (argument == null) ? "null" : argument.getClass().toString();
        return new ClassCastException("cannot bind "+atype+" argument to parameter #"+argnum+" of "+mh.type());
    }
    @Override
    String debugString() {
        return addTypeString(baseName(), this);
    }
    protected String baseName() {
        MethodHandle mh = this;
        while (mh instanceof BoundMethodHandle) {
            Object info = MethodHandleNatives.getTargetInfo(mh);
            if (info instanceof MethodHandle) {
                mh = (MethodHandle) info;
            } else {
                String name = null;
                if (info instanceof MemberName)
                    name = ((MemberName)info).getName();
                if (name != null)
                    return name;
                else
                    return noParens(super.toString()); 
            }
            assert(mh != this);
        }
        return noParens(mh.toString());
    }
    private static String noParens(String str) {
        int paren = str.indexOf('(');
        if (paren >= 0) str = str.substring(0, paren);
        return str;
    }
}
