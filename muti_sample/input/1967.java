class MethodType implements java.io.Serializable {
    private static final long serialVersionUID = 292L;  
    private final Class<?>   rtype;
    private final Class<?>[] ptypes;
    private MethodTypeForm form; 
    private MethodType wrapAlt;  
    private Invokers invokers;   
    private MethodType(Class<?> rtype, Class<?>[] ptypes) {
        checkRtype(rtype);
        checkPtypes(ptypes);
        this.rtype = rtype;
        this.ptypes = ptypes;
    }
     MethodTypeForm form() { return form; }
     Class<?> rtype() { return rtype; }
     Class<?>[] ptypes() { return ptypes; }
    private static void checkRtype(Class<?> rtype) {
        rtype.equals(rtype);  
    }
    private static int checkPtype(Class<?> ptype) {
        ptype.getClass();  
        if (ptype == void.class)
            throw newIllegalArgumentException("parameter type cannot be void");
        if (ptype == double.class || ptype == long.class)  return 1;
        return 0;
    }
    private static int checkPtypes(Class<?>[] ptypes) {
        int slots = 0;
        for (Class<?> ptype : ptypes) {
            slots += checkPtype(ptype);
        }
        checkSlotCount(ptypes.length + slots);
        return slots;
    }
    private static void checkSlotCount(int count) {
        if ((count & 0xFF) != count)
            throw newIllegalArgumentException("bad parameter count "+count);
    }
    private static IndexOutOfBoundsException newIndexOutOfBoundsException(Object num) {
        if (num instanceof Integer)  num = "bad index: "+num;
        return new IndexOutOfBoundsException(num.toString());
    }
    static final HashMap<MethodType,MethodType> internTable
            = new HashMap<MethodType, MethodType>();
    static final Class<?>[] NO_PTYPES = {};
    public static
    MethodType methodType(Class<?> rtype, Class<?>[] ptypes) {
        return makeImpl(rtype, ptypes, false);
    }
    public static
    MethodType methodType(Class<?> rtype, List<Class<?>> ptypes) {
        boolean notrust = false;  
        return makeImpl(rtype, listToArray(ptypes), notrust);
    }
    private static Class<?>[] listToArray(List<Class<?>> ptypes) {
        checkSlotCount(ptypes.size());
        return ptypes.toArray(NO_PTYPES);
    }
    public static
    MethodType methodType(Class<?> rtype, Class<?> ptype0, Class<?>... ptypes) {
        Class<?>[] ptypes1 = new Class<?>[1+ptypes.length];
        ptypes1[0] = ptype0;
        System.arraycopy(ptypes, 0, ptypes1, 1, ptypes.length);
        return makeImpl(rtype, ptypes1, true);
    }
    public static
    MethodType methodType(Class<?> rtype) {
        return makeImpl(rtype, NO_PTYPES, true);
    }
    public static
    MethodType methodType(Class<?> rtype, Class<?> ptype0) {
        return makeImpl(rtype, new Class<?>[]{ ptype0 }, true);
    }
    public static
    MethodType methodType(Class<?> rtype, MethodType ptypes) {
        return makeImpl(rtype, ptypes.ptypes, true);
    }
     static
    MethodType makeImpl(Class<?> rtype, Class<?>[] ptypes, boolean trusted) {
        if (ptypes.length == 0) {
            ptypes = NO_PTYPES; trusted = true;
        }
        MethodType mt1 = new MethodType(rtype, ptypes);
        MethodType mt0;
        synchronized (internTable) {
            mt0 = internTable.get(mt1);
            if (mt0 != null)
                return mt0;
        }
        if (!trusted)
            mt1 = new MethodType(rtype, ptypes.clone());
        MethodTypeForm form = MethodTypeForm.findForm(mt1);
        mt1.form = form;
        if (form.erasedType == mt1) {
            MethodHandleNatives.init(mt1);
        }
        synchronized (internTable) {
            mt0 = internTable.get(mt1);
            if (mt0 != null)
                return mt0;
            internTable.put(mt1, mt1);
        }
        return mt1;
    }
    private static final MethodType[] objectOnlyTypes = new MethodType[20];
    public static
    MethodType genericMethodType(int objectArgCount, boolean finalArray) {
        MethodType mt;
        checkSlotCount(objectArgCount);
        int ivarargs = (!finalArray ? 0 : 1);
        int ootIndex = objectArgCount*2 + ivarargs;
        if (ootIndex < objectOnlyTypes.length) {
            mt = objectOnlyTypes[ootIndex];
            if (mt != null)  return mt;
        }
        Class<?>[] ptypes = new Class<?>[objectArgCount + ivarargs];
        Arrays.fill(ptypes, Object.class);
        if (ivarargs != 0)  ptypes[objectArgCount] = Object[].class;
        mt = makeImpl(Object.class, ptypes, true);
        if (ootIndex < objectOnlyTypes.length) {
            objectOnlyTypes[ootIndex] = mt;     
        }
        return mt;
    }
    public static
    MethodType genericMethodType(int objectArgCount) {
        return genericMethodType(objectArgCount, false);
    }
    public MethodType changeParameterType(int num, Class<?> nptype) {
        if (parameterType(num) == nptype)  return this;
        checkPtype(nptype);
        Class<?>[] nptypes = ptypes.clone();
        nptypes[num] = nptype;
        return makeImpl(rtype, nptypes, true);
    }
    public MethodType insertParameterTypes(int num, Class<?>... ptypesToInsert) {
        int len = ptypes.length;
        if (num < 0 || num > len)
            throw newIndexOutOfBoundsException(num);
        int ins = checkPtypes(ptypesToInsert);
        checkSlotCount(parameterSlotCount() + ptypesToInsert.length + ins);
        int ilen = ptypesToInsert.length;
        if (ilen == 0)  return this;
        Class<?>[] nptypes = Arrays.copyOfRange(ptypes, 0, len+ilen);
        System.arraycopy(nptypes, num, nptypes, num+ilen, len-num);
        System.arraycopy(ptypesToInsert, 0, nptypes, num, ilen);
        return makeImpl(rtype, nptypes, true);
    }
    public MethodType appendParameterTypes(Class<?>... ptypesToInsert) {
        return insertParameterTypes(parameterCount(), ptypesToInsert);
    }
    public MethodType insertParameterTypes(int num, List<Class<?>> ptypesToInsert) {
        return insertParameterTypes(num, listToArray(ptypesToInsert));
    }
    public MethodType appendParameterTypes(List<Class<?>> ptypesToInsert) {
        return insertParameterTypes(parameterCount(), ptypesToInsert);
    }
    public MethodType dropParameterTypes(int start, int end) {
        int len = ptypes.length;
        if (!(0 <= start && start <= end && end <= len))
            throw newIndexOutOfBoundsException("start="+start+" end="+end);
        if (start == end)  return this;
        Class<?>[] nptypes;
        if (start == 0) {
            if (end == len) {
                nptypes = NO_PTYPES;
            } else {
                nptypes = Arrays.copyOfRange(ptypes, end, len);
            }
        } else {
            if (end == len) {
                nptypes = Arrays.copyOfRange(ptypes, 0, start);
            } else {
                int tail = len - end;
                nptypes = Arrays.copyOfRange(ptypes, 0, start + tail);
                System.arraycopy(ptypes, end, nptypes, start, tail);
            }
        }
        return makeImpl(rtype, nptypes, true);
    }
    public MethodType changeReturnType(Class<?> nrtype) {
        if (returnType() == nrtype)  return this;
        return makeImpl(nrtype, ptypes, true);
    }
    public boolean hasPrimitives() {
        return form.hasPrimitives();
    }
    public boolean hasWrappers() {
        return unwrap() != this;
    }
    public MethodType erase() {
        return form.erasedType();
    }
    public MethodType generic() {
        return genericMethodType(parameterCount());
    }
    public MethodType wrap() {
        return hasPrimitives() ? wrapWithPrims(this) : this;
    }
    public MethodType unwrap() {
        MethodType noprims = !hasPrimitives() ? this : wrapWithPrims(this);
        return unwrapWithNoPrims(noprims);
    }
    private static MethodType wrapWithPrims(MethodType pt) {
        assert(pt.hasPrimitives());
        MethodType wt = pt.wrapAlt;
        if (wt == null) {
            wt = MethodTypeForm.canonicalize(pt, MethodTypeForm.WRAP, MethodTypeForm.WRAP);
            assert(wt != null);
            pt.wrapAlt = wt;
        }
        return wt;
    }
    private static MethodType unwrapWithNoPrims(MethodType wt) {
        assert(!wt.hasPrimitives());
        MethodType uwt = wt.wrapAlt;
        if (uwt == null) {
            uwt = MethodTypeForm.canonicalize(wt, MethodTypeForm.UNWRAP, MethodTypeForm.UNWRAP);
            if (uwt == null)
                uwt = wt;    
            wt.wrapAlt = uwt;
        }
        return uwt;
    }
    public Class<?> parameterType(int num) {
        return ptypes[num];
    }
    public int parameterCount() {
        return ptypes.length;
    }
    public Class<?> returnType() {
        return rtype;
    }
    public List<Class<?>> parameterList() {
        return Collections.unmodifiableList(Arrays.asList(ptypes));
    }
    public Class<?>[] parameterArray() {
        return ptypes.clone();
    }
    @Override
    public boolean equals(Object x) {
        return this == x || x instanceof MethodType && equals((MethodType)x);
    }
    private boolean equals(MethodType that) {
        return this.rtype == that.rtype
            && Arrays.equals(this.ptypes, that.ptypes);
    }
    @Override
    public int hashCode() {
      int hashCode = 31 + rtype.hashCode();
      for (Class<?> ptype : ptypes)
          hashCode = 31*hashCode + ptype.hashCode();
      return hashCode;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < ptypes.length; i++) {
            if (i > 0)  sb.append(",");
            sb.append(ptypes[i].getSimpleName());
        }
        sb.append(")");
        sb.append(rtype.getSimpleName());
        return sb.toString();
    }
    boolean isConvertibleTo(MethodType newType) {
        if (!canConvert(returnType(), newType.returnType()))
            return false;
        int argc = parameterCount();
        if (argc != newType.parameterCount())
            return false;
        for (int i = 0; i < argc; i++) {
            if (!canConvert(newType.parameterType(i), parameterType(i)))
                return false;
        }
        return true;
    }
    static boolean canConvert(Class<?> src, Class<?> dst) {
        if (src == dst || dst == Object.class)  return true;
        if (src.isPrimitive()) {
            if (src == void.class)  return true;  
            Wrapper sw = Wrapper.forPrimitiveType(src);
            if (dst.isPrimitive()) {
                return Wrapper.forPrimitiveType(dst).isConvertibleFrom(sw);
            } else {
                return dst.isAssignableFrom(sw.wrapperType());
            }
        } else if (dst.isPrimitive()) {
            if (dst == void.class)  return true;
            Wrapper dw = Wrapper.forPrimitiveType(dst);
            if (src.isAssignableFrom(dw.wrapperType())) {
                return true;
            }
            if (Wrapper.isWrapperType(src) &&
                dw.isConvertibleFrom(Wrapper.forWrapperType(src))) {
                return true;
            }
            return false;
        } else {
            return true;
        }
    }
     int parameterSlotCount() {
        return form.parameterSlotCount();
    }
     Invokers invokers() {
        Invokers inv = invokers;
        if (inv != null)  return inv;
        invokers = inv = new Invokers(this);
        return inv;
    }
     int parameterSlotDepth(int num) {
        if (num < 0 || num > ptypes.length)
            parameterType(num);  
        return form.parameterToArgSlot(num-1);
    }
     int returnSlotCount() {
        return form.returnSlotCount();
    }
    public static MethodType fromMethodDescriptorString(String descriptor, ClassLoader loader)
        throws IllegalArgumentException, TypeNotPresentException
    {
        if (!descriptor.startsWith("(") ||  
            descriptor.indexOf(')') < 0 ||
            descriptor.indexOf('.') >= 0)
            throw new IllegalArgumentException("not a method descriptor: "+descriptor);
        List<Class<?>> types = BytecodeDescriptor.parseMethod(descriptor, loader);
        Class<?> rtype = types.remove(types.size() - 1);
        checkSlotCount(types.size());
        Class<?>[] ptypes = listToArray(types);
        return makeImpl(rtype, ptypes, true);
    }
    public String toMethodDescriptorString() {
        return BytecodeDescriptor.unparse(this);
    }
    private static final java.io.ObjectStreamField[] serialPersistentFields = { };
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        s.defaultWriteObject();  
        s.writeObject(returnType());
        s.writeObject(parameterArray());
    }
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();  
        Class<?>   returnType     = (Class<?>)   s.readObject();
        Class<?>[] parameterArray = (Class<?>[]) s.readObject();
        checkRtype(returnType);
        checkPtypes(parameterArray);
        parameterArray = parameterArray.clone();  
        MethodType_init(returnType, parameterArray);
    }
    private MethodType() {
        this.rtype = null;
        this.ptypes = null;
    }
    private void MethodType_init(Class<?> rtype, Class<?>[] ptypes) {
        checkRtype(rtype);
        checkPtypes(ptypes);
        unsafe.putObject(this, rtypeOffset, rtype);
        unsafe.putObject(this, ptypesOffset, ptypes);
    }
    private static final sun.misc.Unsafe unsafe = sun.misc.Unsafe.getUnsafe();
    private static final long rtypeOffset, ptypesOffset;
    static {
        try {
            rtypeOffset = unsafe.objectFieldOffset
                (MethodType.class.getDeclaredField("rtype"));
            ptypesOffset = unsafe.objectFieldOffset
                (MethodType.class.getDeclaredField("ptypes"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }
    private Object readResolve() {
        return methodType(rtype, ptypes);
    }
}
