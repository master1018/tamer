 abstract class MethodHandleImpl {
    private static final MemberName.Factory LOOKUP = MemberName.Factory.INSTANCE;
    static void initStatics() {
    }
    static
    MethodHandle findMethod(MemberName method,
                            boolean doDispatch, Class<?> lookupClass) throws IllegalAccessException {
        MethodType mtype = method.getMethodType();
        if (!method.isStatic()) {
            Class<?> recvType = method.getDeclaringClass();
            mtype = mtype.insertParameterTypes(0, recvType);
        }
        DirectMethodHandle mh = new DirectMethodHandle(mtype, method, doDispatch, lookupClass);
        if (!mh.isValid())
            throw method.makeAccessException("no direct method handle", lookupClass);
        assert(mh.type() == mtype);
        if (!method.isVarargs())
            return mh;
        int argc = mtype.parameterCount();
        if (argc != 0) {
            Class<?> arrayType = mtype.parameterType(argc-1);
            if (arrayType.isArray())
                return AdapterMethodHandle.makeVarargsCollector(mh, arrayType);
        }
        throw method.makeAccessException("cannot make variable arity", null);
    }
    static
    MethodHandle makeAllocator(MethodHandle rawConstructor) {
        MethodType rawConType = rawConstructor.type();
        Class<?> allocateClass = rawConType.parameterType(0);
        if (AdapterMethodHandle.canCollectArguments(rawConType, MethodType.methodType(allocateClass), 0, true)) {
            MethodHandle returner = MethodHandles.identity(allocateClass);
            MethodType ctype = rawConType.insertParameterTypes(0, allocateClass).changeReturnType(allocateClass);
            MethodHandle  cookedConstructor = AdapterMethodHandle.makeCollectArguments(returner, rawConstructor, 1, false);
            assert(cookedConstructor.type().equals(ctype));
            ctype = ctype.dropParameterTypes(0, 1);
            cookedConstructor = AdapterMethodHandle.makeCollectArguments(cookedConstructor, returner, 0, true);
            MethodHandle allocator = new AllocateObject(allocateClass);
            assert(allocator.type().equals(MethodType.methodType(allocateClass)));
            ctype = ctype.dropParameterTypes(0, 1);
            MethodHandle fold = foldArguments(cookedConstructor, ctype, 0, allocator);
            return fold;
        }
        assert(MethodHandleNatives.workaroundWithoutRicochetFrames());  
        MethodHandle allocator
            = AllocateObject.make(allocateClass, rawConstructor);
        assert(allocator.type()
               .equals(rawConType.dropParameterTypes(0, 1).changeReturnType(rawConType.parameterType(0))));
        return allocator;
    }
    static final class AllocateObject<C> extends BoundMethodHandle {
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        private final Class<C> allocateClass;
        private final MethodHandle rawConstructor;
        private AllocateObject(MethodHandle invoker,
                               Class<C> allocateClass, MethodHandle rawConstructor) {
            super(invoker);
            this.allocateClass = allocateClass;
            this.rawConstructor = rawConstructor;
            assert(MethodHandleNatives.workaroundWithoutRicochetFrames());  
        }
        private AllocateObject(Class<C> allocateClass) {
            super(ALLOCATE.asType(MethodType.methodType(allocateClass, AllocateObject.class)));
            this.allocateClass = allocateClass;
            this.rawConstructor = null;
        }
        static MethodHandle make(Class<?> allocateClass, MethodHandle rawConstructor) {
            assert(MethodHandleNatives.workaroundWithoutRicochetFrames());  
            MethodType rawConType = rawConstructor.type();
            assert(rawConType.parameterType(0) == allocateClass);
            MethodType newType = rawConType.dropParameterTypes(0, 1).changeReturnType(allocateClass);
            int nargs = rawConType.parameterCount() - 1;
            if (nargs < INVOKES.length) {
                MethodHandle invoke = INVOKES[nargs];
                MethodType conType = CON_TYPES[nargs];
                MethodHandle gcon = convertArguments(rawConstructor, conType, rawConType, 0);
                if (gcon == null)  return null;
                MethodHandle galloc = new AllocateObject(invoke, allocateClass, gcon);
                assert(galloc.type() == newType.generic());
                return convertArguments(galloc, newType, galloc.type(), 0);
            } else {
                MethodHandle invoke = VARARGS_INVOKE;
                MethodType conType = CON_TYPES[nargs];
                MethodHandle gcon = spreadArgumentsFromPos(rawConstructor, conType, 1);
                if (gcon == null)  return null;
                MethodHandle galloc = new AllocateObject(invoke, allocateClass, gcon);
                return collectArguments(galloc, newType, 1, null);
            }
        }
        @Override
        String debugString() {
            return addTypeString(allocateClass.getSimpleName(), this);
        }
        @SuppressWarnings("unchecked")
        private C allocate() throws InstantiationException {
            return (C) unsafe.allocateInstance(allocateClass);
        }
        private C invoke_V(Object... av) throws Throwable {
            C obj = allocate();
            rawConstructor.invokeExact((Object)obj, av);
            return obj;
        }
        private C invoke_L0() throws Throwable {
            C obj = allocate();
            rawConstructor.invokeExact((Object)obj);
            return obj;
        }
        private C invoke_L1(Object a0) throws Throwable {
            C obj = allocate();
            rawConstructor.invokeExact((Object)obj, a0);
            return obj;
        }
        private C invoke_L2(Object a0, Object a1) throws Throwable {
            C obj = allocate();
            rawConstructor.invokeExact((Object)obj, a0, a1);
            return obj;
        }
        private C invoke_L3(Object a0, Object a1, Object a2) throws Throwable {
            C obj = allocate();
            rawConstructor.invokeExact((Object)obj, a0, a1, a2);
            return obj;
        }
        private C invoke_L4(Object a0, Object a1, Object a2, Object a3) throws Throwable {
            C obj = allocate();
            rawConstructor.invokeExact((Object)obj, a0, a1, a2, a3);
            return obj;
        }
        private C invoke_L5(Object a0, Object a1, Object a2, Object a3, Object a4) throws Throwable {
            C obj = allocate();
            rawConstructor.invokeExact((Object)obj, a0, a1, a2, a3, a4);
            return obj;
        }
        private C invoke_L6(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5) throws Throwable {
            C obj = allocate();
            rawConstructor.invokeExact((Object)obj, a0, a1, a2, a3, a4, a5);
            return obj;
        }
        private C invoke_L7(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6) throws Throwable {
            C obj = allocate();
            rawConstructor.invokeExact((Object)obj, a0, a1, a2, a3, a4, a5, a6);
            return obj;
        }
        private C invoke_L8(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7) throws Throwable {
            C obj = allocate();
            rawConstructor.invokeExact((Object)obj, a0, a1, a2, a3, a4, a5, a6, a7);
            return obj;
        }
        static MethodHandle[] makeInvokes() {
            ArrayList<MethodHandle> invokes = new ArrayList<MethodHandle>();
            MethodHandles.Lookup lookup = IMPL_LOOKUP;
            for (;;) {
                int nargs = invokes.size();
                String name = "invoke_L"+nargs;
                MethodHandle invoke = null;
                try {
                    invoke = lookup.findVirtual(AllocateObject.class, name, MethodType.genericMethodType(nargs));
                } catch (ReflectiveOperationException ex) {
                }
                if (invoke == null)  break;
                invokes.add(invoke);
            }
            assert(invokes.size() == 9);  
            return invokes.toArray(new MethodHandle[0]);
        };
        static final MethodHandle[] INVOKES = makeInvokes();
        static final MethodHandle VARARGS_INVOKE;
        static final MethodHandle ALLOCATE;
        static {
            try {
                VARARGS_INVOKE = IMPL_LOOKUP.findVirtual(AllocateObject.class, "invoke_V", MethodType.genericMethodType(0, true));
                ALLOCATE = IMPL_LOOKUP.findVirtual(AllocateObject.class, "allocate", MethodType.genericMethodType(0));
            } catch (ReflectiveOperationException ex) {
                throw uncaughtException(ex);
            }
        }
        static final MethodType[] CON_TYPES = new MethodType[INVOKES.length];
        static {
            for (int i = 0; i < INVOKES.length; i++)
                CON_TYPES[i] = makeConType(INVOKES[i]);
        }
        static final MethodType VARARGS_CON_TYPE = makeConType(VARARGS_INVOKE);
        static MethodType makeConType(MethodHandle invoke) {
            MethodType invType = invoke.type();
            return invType.changeParameterType(0, Object.class).changeReturnType(void.class);
        }
    }
    static
    MethodHandle accessField(MemberName member, boolean isSetter,
                             Class<?> lookupClass) {
        MethodHandle mh = new FieldAccessor(member, isSetter);
        return mh;
    }
    static
    MethodHandle accessArrayElement(Class<?> arrayClass, boolean isSetter) {
        if (!arrayClass.isArray())
            throw newIllegalArgumentException("not an array: "+arrayClass);
        Class<?> elemClass = arrayClass.getComponentType();
        MethodHandle[] mhs = FieldAccessor.ARRAY_CACHE.get(elemClass);
        if (mhs == null) {
            if (!FieldAccessor.doCache(elemClass))
                return FieldAccessor.ahandle(arrayClass, isSetter);
            mhs = new MethodHandle[] {
                FieldAccessor.ahandle(arrayClass, false),
                FieldAccessor.ahandle(arrayClass, true)
            };
            if (mhs[0].type().parameterType(0) == Class.class) {
                mhs[0] = mhs[0].bindTo(elemClass);
                mhs[1] = mhs[1].bindTo(elemClass);
            }
            synchronized (FieldAccessor.ARRAY_CACHE) {}  
            FieldAccessor.ARRAY_CACHE.put(elemClass, mhs);
        }
        return mhs[isSetter ? 1 : 0];
    }
    static final class FieldAccessor<C,V> extends BoundMethodHandle {
        private static final Unsafe unsafe = Unsafe.getUnsafe();
        final Object base;  
        final long offset;
        final String name;
        FieldAccessor(MemberName field, boolean isSetter) {
            super(fhandle(field.getDeclaringClass(), field.getFieldType(), isSetter, field.isStatic()));
            this.offset = (long) field.getVMIndex();
            this.name = field.getName();
            this.base = staticBase(field);
        }
        @Override
        String debugString() { return addTypeString(name, this); }
        int getFieldI(C obj) { return unsafe.getInt(obj, offset); }
        void setFieldI(C obj, int x) { unsafe.putInt(obj, offset, x); }
        long getFieldJ(C obj) { return unsafe.getLong(obj, offset); }
        void setFieldJ(C obj, long x) { unsafe.putLong(obj, offset, x); }
        float getFieldF(C obj) { return unsafe.getFloat(obj, offset); }
        void setFieldF(C obj, float x) { unsafe.putFloat(obj, offset, x); }
        double getFieldD(C obj) { return unsafe.getDouble(obj, offset); }
        void setFieldD(C obj, double x) { unsafe.putDouble(obj, offset, x); }
        boolean getFieldZ(C obj) { return unsafe.getBoolean(obj, offset); }
        void setFieldZ(C obj, boolean x) { unsafe.putBoolean(obj, offset, x); }
        byte getFieldB(C obj) { return unsafe.getByte(obj, offset); }
        void setFieldB(C obj, byte x) { unsafe.putByte(obj, offset, x); }
        short getFieldS(C obj) { return unsafe.getShort(obj, offset); }
        void setFieldS(C obj, short x) { unsafe.putShort(obj, offset, x); }
        char getFieldC(C obj) { return unsafe.getChar(obj, offset); }
        void setFieldC(C obj, char x) { unsafe.putChar(obj, offset, x); }
        @SuppressWarnings("unchecked")
        V getFieldL(C obj) { return (V) unsafe.getObject(obj, offset); }
        @SuppressWarnings("unchecked")
        void setFieldL(C obj, V x) { unsafe.putObject(obj, offset, x); }
        static Object staticBase(final MemberName field) {
            if (!field.isStatic())  return null;
            return AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public Object run() {
                        try {
                            Class c = field.getDeclaringClass();
                            java.lang.reflect.Field f = c.getDeclaredField(field.getName());
                            return unsafe.staticFieldBase(f);
                        } catch (NoSuchFieldException ee) {
                            throw uncaughtException(ee);
                        }
                    }
                });
        }
        int getStaticI() { return unsafe.getInt(base, offset); }
        void setStaticI(int x) { unsafe.putInt(base, offset, x); }
        long getStaticJ() { return unsafe.getLong(base, offset); }
        void setStaticJ(long x) { unsafe.putLong(base, offset, x); }
        float getStaticF() { return unsafe.getFloat(base, offset); }
        void setStaticF(float x) { unsafe.putFloat(base, offset, x); }
        double getStaticD() { return unsafe.getDouble(base, offset); }
        void setStaticD(double x) { unsafe.putDouble(base, offset, x); }
        boolean getStaticZ() { return unsafe.getBoolean(base, offset); }
        void setStaticZ(boolean x) { unsafe.putBoolean(base, offset, x); }
        byte getStaticB() { return unsafe.getByte(base, offset); }
        void setStaticB(byte x) { unsafe.putByte(base, offset, x); }
        short getStaticS() { return unsafe.getShort(base, offset); }
        void setStaticS(short x) { unsafe.putShort(base, offset, x); }
        char getStaticC() { return unsafe.getChar(base, offset); }
        void setStaticC(char x) { unsafe.putChar(base, offset, x); }
        V getStaticL() { return (V) unsafe.getObject(base, offset); }
        void setStaticL(V x) { unsafe.putObject(base, offset, x); }
        static String fname(Class<?> vclass, boolean isSetter, boolean isStatic) {
            String stem;
            if (!isStatic)
                stem = (!isSetter ? "getField" : "setField");
            else
                stem = (!isSetter ? "getStatic" : "setStatic");
            return stem + Wrapper.basicTypeChar(vclass);
        }
        static MethodType ftype(Class<?> cclass, Class<?> vclass, boolean isSetter, boolean isStatic) {
            MethodType type;
            if (!isStatic) {
                if (!isSetter)
                    return MethodType.methodType(vclass, cclass);
                else
                    return MethodType.methodType(void.class, cclass, vclass);
            } else {
                if (!isSetter)
                    return MethodType.methodType(vclass);
                else
                    return MethodType.methodType(void.class, vclass);
            }
        }
        static MethodHandle fhandle(Class<?> cclass, Class<?> vclass, boolean isSetter, boolean isStatic) {
            String name = FieldAccessor.fname(vclass, isSetter, isStatic);
            if (cclass.isPrimitive())  throw newIllegalArgumentException("primitive "+cclass);
            Class<?> ecclass = Object.class;  
            Class<?> evclass = vclass;
            if (!evclass.isPrimitive())  evclass = Object.class;
            MethodType type = FieldAccessor.ftype(ecclass, evclass, isSetter, isStatic);
            MethodHandle mh;
            try {
                mh = IMPL_LOOKUP.findVirtual(FieldAccessor.class, name, type);
            } catch (ReflectiveOperationException ex) {
                throw uncaughtException(ex);
            }
            if (evclass != vclass || (!isStatic && ecclass != cclass)) {
                MethodType strongType = FieldAccessor.ftype(cclass, vclass, isSetter, isStatic);
                strongType = strongType.insertParameterTypes(0, FieldAccessor.class);
                mh = convertArguments(mh, strongType, 0);
            }
            return mh;
        }
        static final HashMap<Class<?>, MethodHandle[]> ARRAY_CACHE =
                new HashMap<Class<?>, MethodHandle[]>();
        static boolean doCache(Class<?> elemClass) {
            if (elemClass.isPrimitive())  return true;
            ClassLoader cl = elemClass.getClassLoader();
            return cl == null || cl == ClassLoader.getSystemClassLoader();
        }
        static int getElementI(int[] a, int i) { return a[i]; }
        static void setElementI(int[] a, int i, int x) { a[i] = x; }
        static long getElementJ(long[] a, int i) { return a[i]; }
        static void setElementJ(long[] a, int i, long x) { a[i] = x; }
        static float getElementF(float[] a, int i) { return a[i]; }
        static void setElementF(float[] a, int i, float x) { a[i] = x; }
        static double getElementD(double[] a, int i) { return a[i]; }
        static void setElementD(double[] a, int i, double x) { a[i] = x; }
        static boolean getElementZ(boolean[] a, int i) { return a[i]; }
        static void setElementZ(boolean[] a, int i, boolean x) { a[i] = x; }
        static byte getElementB(byte[] a, int i) { return a[i]; }
        static void setElementB(byte[] a, int i, byte x) { a[i] = x; }
        static short getElementS(short[] a, int i) { return a[i]; }
        static void setElementS(short[] a, int i, short x) { a[i] = x; }
        static char getElementC(char[] a, int i) { return a[i]; }
        static void setElementC(char[] a, int i, char x) { a[i] = x; }
        static Object getElementL(Object[] a, int i) { return a[i]; }
        static void setElementL(Object[] a, int i, Object x) { a[i] = x; }
        static <V> V getElementL(Class<V[]> aclass, V[] a, int i) { return aclass.cast(a)[i]; }
        static <V> void setElementL(Class<V[]> aclass, V[] a, int i, V x) { aclass.cast(a)[i] = x; }
        static String aname(Class<?> aclass, boolean isSetter) {
            Class<?> vclass = aclass.getComponentType();
            if (vclass == null)  throw new IllegalArgumentException();
            return (!isSetter ? "getElement" : "setElement") + Wrapper.basicTypeChar(vclass);
        }
        static MethodType atype(Class<?> aclass, boolean isSetter) {
            Class<?> vclass = aclass.getComponentType();
            if (!isSetter)
                return MethodType.methodType(vclass, aclass, int.class);
            else
                return MethodType.methodType(void.class, aclass, int.class, vclass);
        }
        static MethodHandle ahandle(Class<?> aclass, boolean isSetter) {
            Class<?> vclass = aclass.getComponentType();
            String name = FieldAccessor.aname(aclass, isSetter);
            Class<?> caclass = null;
            if (!vclass.isPrimitive() && vclass != Object.class) {
                caclass = aclass;
                aclass = Object[].class;
                vclass = Object.class;
            }
            MethodType type = FieldAccessor.atype(aclass, isSetter);
            if (caclass != null)
                type = type.insertParameterTypes(0, Class.class);
            MethodHandle mh;
            try {
                mh = IMPL_LOOKUP.findStatic(FieldAccessor.class, name, type);
            } catch (ReflectiveOperationException ex) {
                throw uncaughtException(ex);
            }
            if (caclass != null) {
                MethodType strongType = FieldAccessor.atype(caclass, isSetter);
                mh = mh.bindTo(caclass);
                mh = convertArguments(mh, strongType, 0);
            }
            return mh;
        }
    }
    static
    MethodHandle bindReceiver(MethodHandle target, Object receiver) {
        if (receiver == null)  return null;
        if (target instanceof AdapterMethodHandle &&
            ((AdapterMethodHandle)target).conversionOp() == MethodHandleNatives.Constants.OP_RETYPE_ONLY
            ) {
            Object info = MethodHandleNatives.getTargetInfo(target);
            if (info instanceof DirectMethodHandle) {
                DirectMethodHandle dmh = (DirectMethodHandle) info;
                if (dmh.type().parameterType(0).isAssignableFrom(receiver.getClass())) {
                    MethodHandle bmh = new BoundMethodHandle(dmh, receiver, 0);
                    MethodType newType = target.type().dropParameterTypes(0, 1);
                    return convertArguments(bmh, newType, bmh.type(), 0);
                }
            }
        }
        if (target instanceof DirectMethodHandle)
            return new BoundMethodHandle((DirectMethodHandle)target, receiver, 0);
        return null;   
    }
    static
    MethodHandle bindArgument(MethodHandle target, int argnum, Object receiver) {
        return new BoundMethodHandle(target, receiver, argnum);
    }
    static MethodHandle permuteArguments(MethodHandle target,
                                                MethodType newType,
                                                MethodType oldType,
                                                int[] permutationOrNull) {
        assert(oldType.parameterCount() == target.type().parameterCount());
        int outargs = oldType.parameterCount(), inargs = newType.parameterCount();
        if (permutationOrNull.length != outargs)
            throw newIllegalArgumentException("wrong number of arguments in permutation");
        Class<?>[] callTypeArgs = new Class<?>[outargs];
        for (int i = 0; i < outargs; i++)
            callTypeArgs[i] = newType.parameterType(permutationOrNull[i]);
        MethodType callType = MethodType.methodType(oldType.returnType(), callTypeArgs);
        target = convertArguments(target, callType, oldType, 0);
        assert(target != null);
        oldType = target.type();
        List<Integer> goal = new ArrayList<Integer>();  
        List<Integer> state = new ArrayList<Integer>(); 
        List<Integer> drops = new ArrayList<Integer>(); 
        List<Integer> dups = new ArrayList<Integer>();  
        final int TOKEN = 10; 
        for (int i = 0; i < outargs; i++) {
            state.add(permutationOrNull[i] * TOKEN);
        }
        for (int i = 0; i < inargs; i++) {
            if (state.contains(i * TOKEN)) {
                goal.add(i * TOKEN);
            } else {
                drops.add(i);
            }
        }
        while (state.size() > goal.size()) {
            for (int i2 = 0; i2 < state.size(); i2++) {
                int arg1 = state.get(i2);
                int i1 = state.indexOf(arg1);
                if (i1 != i2) {
                    int arg2 = (inargs++) * TOKEN;
                    state.set(i2, arg2);
                    dups.add(goal.indexOf(arg1));
                    goal.add(arg2);
                }
            }
        }
        assert(state.size() == goal.size());
        int size = goal.size();
        while (!state.equals(goal)) {
            int bestRotArg = -10 * TOKEN, bestRotLen = 0;
            int thisRotArg = -10 * TOKEN, thisRotLen = 0;
            for (int i = 0; i < size; i++) {
                int arg = state.get(i);
                if (arg == thisRotArg + TOKEN) {
                    thisRotArg = arg;
                    thisRotLen += 1;
                    if (bestRotLen < thisRotLen) {
                        bestRotLen = thisRotLen;
                        bestRotArg = thisRotArg;
                    }
                } else {
                    thisRotLen = 0;
                    thisRotArg = -10 * TOKEN;
                    int wantArg = goal.get(i);
                    final int MAX_ARG_ROTATION = AdapterMethodHandle.MAX_ARG_ROTATION;
                    if (arg != wantArg &&
                        arg >= wantArg - TOKEN * MAX_ARG_ROTATION &&
                        arg <= wantArg + TOKEN * MAX_ARG_ROTATION) {
                        thisRotArg = arg;
                        thisRotLen = 1;
                    }
                }
            }
            if (bestRotLen >= 2) {
                int dstEnd = state.indexOf(bestRotArg);
                int srcEnd = goal.indexOf(bestRotArg);
                int rotBy = dstEnd - srcEnd;
                int dstBeg = dstEnd - (bestRotLen - 1);
                int srcBeg = srcEnd - (bestRotLen - 1);
                assert((dstEnd | dstBeg | srcEnd | srcBeg) >= 0); 
                int rotBeg = Math.min(dstBeg, srcBeg);
                int rotEnd = Math.max(dstEnd, srcEnd);
                int score = 0;
                for (int i = rotBeg; i <= rotEnd; i++) {
                    if ((int)state.get(i) != (int)goal.get(i))
                        score += 1;
                }
                List<Integer> rotSpan = state.subList(rotBeg, rotEnd+1);
                Collections.rotate(rotSpan, -rotBy);  
                for (int i = rotBeg; i <= rotEnd; i++) {
                    if ((int)state.get(i) != (int)goal.get(i))
                        score -= 1;
                }
                if (score >= 2) {
                    List<Class<?>> ptypes = Arrays.asList(oldType.parameterArray());
                    Collections.rotate(ptypes.subList(rotBeg, rotEnd+1), -rotBy);
                    MethodType rotType = MethodType.methodType(oldType.returnType(), ptypes);
                    MethodHandle nextTarget
                            = AdapterMethodHandle.makeRotateArguments(rotType, target,
                                    rotBeg, rotSpan.size(), rotBy);
                    if (nextTarget != null) {
                        target = nextTarget;
                        oldType = rotType;
                        continue;
                    }
                }
                Collections.rotate(rotSpan, rotBy);
            }
            List<Class<?>> ptypes = Arrays.asList(oldType.parameterArray());
            for (int i = 0; i < size; i++) {
                int arg = goal.get(i);
                if (arg != state.get(i)) {
                    int j = state.indexOf(arg);
                    Collections.swap(ptypes, i, j);
                    MethodType swapType = MethodType.methodType(oldType.returnType(), ptypes);
                    target = AdapterMethodHandle.makeSwapArguments(swapType, target, i, j);
                    if (target == null)  throw newIllegalArgumentException("cannot swap");
                    assert(target.type() == swapType);
                    oldType = swapType;
                    Collections.swap(state, i, j);
                }
            }
            assert(state.equals(goal));
        }
        while (!dups.isEmpty()) {
            int grab = dups.size() - 1;
            int dupArgPos = dups.get(grab), dupArgCount = 1;
            while (grab - 1 >= 0) {
                int dup0 = dups.get(grab - 1);
                if (dup0 != dupArgPos - 1)  break;
                dupArgPos -= 1;
                dupArgCount += 1;
                grab -= 1;
            }
            dups.subList(grab, dups.size()).clear();
            List<Class<?>> ptypes = oldType.parameterList();
            ptypes = ptypes.subList(0, ptypes.size() - dupArgCount);
            MethodType dupType = MethodType.methodType(oldType.returnType(), ptypes);
            target = AdapterMethodHandle.makeDupArguments(dupType, target, dupArgPos, dupArgCount);
            if (target == null)
                throw newIllegalArgumentException("cannot dup");
            oldType = target.type();
        }
        while (!drops.isEmpty()) {
            int dropArgPos = drops.get(0), dropArgCount = 1;
            while (dropArgCount < drops.size()) {
                int drop1 = drops.get(dropArgCount);
                if (drop1 != dropArgPos + dropArgCount)  break;
                dropArgCount += 1;
            }
            drops.subList(0, dropArgCount).clear();
            List<Class<?>> dropTypes = newType.parameterList()
                    .subList(dropArgPos, dropArgPos + dropArgCount);
            MethodType dropType = oldType.insertParameterTypes(dropArgPos, dropTypes);
            target = AdapterMethodHandle.makeDropArguments(dropType, target, dropArgPos, dropArgCount);
            if (target == null)  throw newIllegalArgumentException("cannot drop");
            oldType = target.type();
        }
        target = convertArguments(target, newType, oldType, 0);
        assert(target != null);
        return target;
    }
     static
    MethodHandle convertArguments(MethodHandle target, MethodType newType, int level) {
        MethodType oldType = target.type();
        if (oldType.equals(newType))
            return target;
        assert(level > 1 || oldType.isConvertibleTo(newType));
        MethodHandle retFilter = null;
        Class<?> oldRT = oldType.returnType();
        Class<?> newRT = newType.returnType();
        if (!VerifyType.isNullConversion(oldRT, newRT)) {
            if (oldRT == void.class) {
                Wrapper wrap = newRT.isPrimitive() ? Wrapper.forPrimitiveType(newRT) : Wrapper.OBJECT;
                retFilter = ValueConversions.zeroConstantFunction(wrap);
            } else {
                retFilter = MethodHandles.identity(newRT);
                retFilter = convertArguments(retFilter, retFilter.type().changeParameterType(0, oldRT), level);
            }
            newType = newType.changeReturnType(oldRT);
        }
        MethodHandle res = null;
        Exception ex = null;
        try {
            res = convertArguments(target, newType, oldType, level);
        } catch (IllegalArgumentException ex1) {
            ex = ex1;
        }
        if (res == null) {
            WrongMethodTypeException wmt = new WrongMethodTypeException("cannot convert to "+newType+": "+target);
            wmt.initCause(ex);
            throw wmt;
        }
        if (retFilter != null)
            res = MethodHandles.filterReturnValue(res, retFilter);
        return res;
    }
    static MethodHandle convertArguments(MethodHandle target,
                                                MethodType newType,
                                                MethodType oldType,
                                                int level) {
        assert(oldType.parameterCount() == target.type().parameterCount());
        if (newType == oldType)
            return target;
        if (oldType.parameterCount() != newType.parameterCount())
            throw newIllegalArgumentException("mismatched parameter count", oldType, newType);
        MethodHandle res = AdapterMethodHandle.makePairwiseConvert(newType, target, level);
        if (res != null)
            return res;
        int argc = oldType.parameterCount();
        assert(MethodHandleNatives.workaroundWithoutRicochetFrames());  
        MethodType objType = MethodType.genericMethodType(argc);
        MethodHandle objTarget = AdapterMethodHandle.makePairwiseConvert(objType, target, level);
        if (objTarget == null)
            objTarget = FromGeneric.make(target);
        res = AdapterMethodHandle.makePairwiseConvert(newType, objTarget, level);
        if (res != null)
            return res;
        return ToGeneric.make(newType, objTarget);
    }
    static MethodHandle spreadArguments(MethodHandle target, Class<?> arrayType, int arrayLength) {
        MethodType oldType = target.type();
        int nargs = oldType.parameterCount();
        int keepPosArgs = nargs - arrayLength;
        MethodType newType = oldType
                .dropParameterTypes(keepPosArgs, nargs)
                .insertParameterTypes(keepPosArgs, arrayType);
        return spreadArguments(target, newType, keepPosArgs, arrayType, arrayLength);
    }
    static MethodHandle spreadArgumentsFromPos(MethodHandle target, MethodType newType, int spreadArgPos) {
        int arrayLength = target.type().parameterCount() - spreadArgPos;
        return spreadArguments(target, newType, spreadArgPos, Object[].class, arrayLength);
    }
    static MethodHandle spreadArguments(MethodHandle target,
                                               MethodType newType,
                                               int spreadArgPos,
                                               Class<?> arrayType,
                                               int arrayLength) {
        MethodType oldType = target.type();
        assert(arrayLength == oldType.parameterCount() - spreadArgPos);
        assert(newType.parameterType(spreadArgPos) == arrayType);
        return AdapterMethodHandle.makeSpreadArguments(newType, target, arrayType, spreadArgPos, arrayLength);
    }
    static MethodHandle collectArguments(MethodHandle target,
                                                int collectArg,
                                                MethodHandle collector) {
        MethodType type = target.type();
        Class<?> collectType = collector.type().returnType();
        assert(collectType != void.class);  
        if (collectType != type.parameterType(collectArg))
            target = target.asType(type.changeParameterType(collectArg, collectType));
        MethodType newType = type
                .dropParameterTypes(collectArg, collectArg+1)
                .insertParameterTypes(collectArg, collector.type().parameterArray());
        return collectArguments(target, newType, collectArg, collector);
    }
    static MethodHandle collectArguments(MethodHandle target,
                                                MethodType newType,
                                                int collectArg,
                                                MethodHandle collector) {
        MethodType oldType = target.type();     
        MethodType colType = collector.type();  
        assert(newType.parameterCount() == collectArg + colType.parameterCount());
        assert(oldType.parameterCount() == collectArg + 1);
        MethodHandle result = null;
        if (AdapterMethodHandle.canCollectArguments(oldType, colType, collectArg, false)) {
            result = AdapterMethodHandle.makeCollectArguments(target, collector, collectArg, false);
        }
        if (result == null) {
            assert(MethodHandleNatives.workaroundWithoutRicochetFrames());  
            MethodHandle gtarget = convertArguments(target, oldType.generic(), oldType, 0);
            MethodHandle gcollector = convertArguments(collector, colType.generic(), colType, 0);
            if (gtarget == null || gcollector == null)  return null;
            MethodHandle gresult = FilterGeneric.makeArgumentCollector(gcollector, gtarget);
            result = convertArguments(gresult, newType, gresult.type(), 0);
        }
        return result;
    }
    static MethodHandle filterArgument(MethodHandle target,
                                       int pos,
                                       MethodHandle filter) {
        MethodType ttype = target.type();
        MethodType ftype = filter.type();
        assert(ftype.parameterCount() == 1);
        MethodHandle result = null;
        if (AdapterMethodHandle.canCollectArguments(ttype, ftype, pos, false)) {
            result = AdapterMethodHandle.makeCollectArguments(target, filter, pos, false);
            if (result != null)  return result;
        }
        assert(MethodHandleNatives.workaroundWithoutRicochetFrames());  
        MethodType rtype = ttype.changeParameterType(pos, ftype.parameterType(0));
        MethodType gttype = ttype.generic();
        if (ttype != gttype) {
            target = convertArguments(target, gttype, ttype, 0);
            ttype = gttype;
        }
        MethodType gftype = ftype.generic();
        if (ftype != gftype) {
            filter = convertArguments(filter, gftype, ftype, 0);
            ftype = gftype;
        }
        if (ftype == ttype) {
            result = FilterOneArgument.make(filter, target);
        } else {
            result = FilterGeneric.makeArgumentFilter(pos, filter, target);
        }
        if (result.type() != rtype)
            result = result.asType(rtype);
        return result;
    }
    static MethodHandle foldArguments(MethodHandle target,
                                      MethodType newType,
                                      int foldPos,
                                      MethodHandle combiner) {
        MethodType oldType = target.type();
        MethodType ctype = combiner.type();
        if (AdapterMethodHandle.canCollectArguments(oldType, ctype, foldPos, true)) {
            MethodHandle res = AdapterMethodHandle.makeCollectArguments(target, combiner, foldPos, true);
            if (res != null)  return res;
        }
        assert(MethodHandleNatives.workaroundWithoutRicochetFrames());  
        if (foldPos != 0)  return null;
        MethodHandle gtarget = convertArguments(target, oldType.generic(), oldType, 0);
        MethodHandle gcombiner = convertArguments(combiner, ctype.generic(), ctype, 0);
        if (ctype.returnType() == void.class) {
            gtarget = dropArguments(gtarget, oldType.generic().insertParameterTypes(foldPos, Object.class), foldPos);
        }
        if (gtarget == null || gcombiner == null)  return null;
        MethodHandle gresult = FilterGeneric.makeArgumentFolder(gcombiner, gtarget);
        return convertArguments(gresult, newType, gresult.type(), 0);
    }
    static
    MethodHandle dropArguments(MethodHandle target,
                               MethodType newType, int argnum) {
        int drops = newType.parameterCount() - target.type().parameterCount();
        MethodHandle res = AdapterMethodHandle.makeDropArguments(newType, target, argnum, drops);
        if (res != null)
            return res;
        throw new UnsupportedOperationException("NYI");
    }
    private static class GuardWithTest extends BoundMethodHandle {
        private final MethodHandle test, target, fallback;
        private GuardWithTest(MethodHandle invoker,
                              MethodHandle test, MethodHandle target, MethodHandle fallback) {
            super(invoker);
            this.test = test;
            this.target = target;
            this.fallback = fallback;
        }
        static boolean preferRicochetFrame(MethodType type) {
            return true;  
        }
        static MethodHandle make(MethodHandle test, MethodHandle target, MethodHandle fallback) {
            MethodType type = target.type();
            int nargs = type.parameterCount();
            if (nargs < INVOKES.length) {
                if (preferRicochetFrame(type))
                    assert(MethodHandleNatives.workaroundWithoutRicochetFrames());  
                MethodHandle invoke = INVOKES[nargs];
                MethodType gtype = type.generic();
                assert(invoke.type().dropParameterTypes(0,1) == gtype);
                MethodHandle gtest = convertArguments(test, gtype.changeReturnType(boolean.class), test.type(), 2);
                MethodHandle gtarget = convertArguments(target, gtype, type, 2);
                MethodHandle gfallback = convertArguments(fallback, gtype, type, 2);
                if (gtest == null || gtarget == null || gfallback == null)  return null;
                MethodHandle gguard = new GuardWithTest(invoke, gtest, gtarget, gfallback);
                return convertArguments(gguard, type, gtype, 2);
            } else {
                assert(MethodHandleNatives.workaroundWithoutRicochetFrames());  
                MethodHandle invoke = VARARGS_INVOKE;
                MethodType gtype = MethodType.genericMethodType(1);
                assert(invoke.type().dropParameterTypes(0,1) == gtype);
                MethodHandle gtest = spreadArgumentsFromPos(test, gtype.changeReturnType(boolean.class), 0);
                MethodHandle gtarget = spreadArgumentsFromPos(target, gtype, 0);
                MethodHandle gfallback = spreadArgumentsFromPos(fallback, gtype, 0);
                MethodHandle gguard = new GuardWithTest(invoke, gtest, gtarget, gfallback);
                if (gtest == null || gtarget == null || gfallback == null)  return null;
                return collectArguments(gguard, type, 0, null);
            }
        }
        @Override
        String debugString() {
            return addTypeString(target, this);
        }
        private Object invoke_V(Object... av) throws Throwable {
            if ((boolean) test.invokeExact(av))
                return target.invokeExact(av);
            return fallback.invokeExact(av);
        }
        private Object invoke_L0() throws Throwable {
            if ((boolean) test.invokeExact())
                return target.invokeExact();
            return fallback.invokeExact();
        }
        private Object invoke_L1(Object a0) throws Throwable {
            if ((boolean) test.invokeExact(a0))
                return target.invokeExact(a0);
            return fallback.invokeExact(a0);
        }
        private Object invoke_L2(Object a0, Object a1) throws Throwable {
            if ((boolean) test.invokeExact(a0, a1))
                return target.invokeExact(a0, a1);
            return fallback.invokeExact(a0, a1);
        }
        private Object invoke_L3(Object a0, Object a1, Object a2) throws Throwable {
            if ((boolean) test.invokeExact(a0, a1, a2))
                return target.invokeExact(a0, a1, a2);
            return fallback.invokeExact(a0, a1, a2);
        }
        private Object invoke_L4(Object a0, Object a1, Object a2, Object a3) throws Throwable {
            if ((boolean) test.invokeExact(a0, a1, a2, a3))
                return target.invokeExact(a0, a1, a2, a3);
            return fallback.invokeExact(a0, a1, a2, a3);
        }
        private Object invoke_L5(Object a0, Object a1, Object a2, Object a3, Object a4) throws Throwable {
            if ((boolean) test.invokeExact(a0, a1, a2, a3, a4))
                return target.invokeExact(a0, a1, a2, a3, a4);
            return fallback.invokeExact(a0, a1, a2, a3, a4);
        }
        private Object invoke_L6(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5) throws Throwable {
            if ((boolean) test.invokeExact(a0, a1, a2, a3, a4, a5))
                return target.invokeExact(a0, a1, a2, a3, a4, a5);
            return fallback.invokeExact(a0, a1, a2, a3, a4, a5);
        }
        private Object invoke_L7(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6) throws Throwable {
            if ((boolean) test.invokeExact(a0, a1, a2, a3, a4, a5, a6))
                return target.invokeExact(a0, a1, a2, a3, a4, a5, a6);
            return fallback.invokeExact(a0, a1, a2, a3, a4, a5, a6);
        }
        private Object invoke_L8(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7) throws Throwable {
            if ((boolean) test.invokeExact(a0, a1, a2, a3, a4, a5, a6, a7))
                return target.invokeExact(a0, a1, a2, a3, a4, a5, a6, a7);
            return fallback.invokeExact(a0, a1, a2, a3, a4, a5, a6, a7);
        }
        static MethodHandle[] makeInvokes() {
            ArrayList<MethodHandle> invokes = new ArrayList<MethodHandle>();
            MethodHandles.Lookup lookup = IMPL_LOOKUP;
            for (;;) {
                int nargs = invokes.size();
                String name = "invoke_L"+nargs;
                MethodHandle invoke = null;
                try {
                    invoke = lookup.findVirtual(GuardWithTest.class, name, MethodType.genericMethodType(nargs));
                } catch (ReflectiveOperationException ex) {
                }
                if (invoke == null)  break;
                invokes.add(invoke);
            }
            assert(invokes.size() == 9);  
            return invokes.toArray(new MethodHandle[0]);
        };
        static final MethodHandle[] INVOKES = makeInvokes();
        static final MethodHandle VARARGS_INVOKE;
        static {
            try {
                VARARGS_INVOKE = IMPL_LOOKUP.findVirtual(GuardWithTest.class, "invoke_V", MethodType.genericMethodType(0, true));
            } catch (ReflectiveOperationException ex) {
                throw uncaughtException(ex);
            }
        }
    }
    static
    MethodHandle selectAlternative(boolean testResult, MethodHandle target, MethodHandle fallback) {
        return testResult ? target : fallback;
    }
    static MethodHandle SELECT_ALTERNATIVE;
    static MethodHandle selectAlternative() {
        if (SELECT_ALTERNATIVE != null)  return SELECT_ALTERNATIVE;
        try {
            SELECT_ALTERNATIVE
            = IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "selectAlternative",
                    MethodType.methodType(MethodHandle.class, boolean.class, MethodHandle.class, MethodHandle.class));
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
        return SELECT_ALTERNATIVE;
    }
    static
    MethodHandle makeGuardWithTest(MethodHandle test,
                                   MethodHandle target,
                                   MethodHandle fallback) {
        assert(test.type().returnType() == boolean.class);
        MethodType targetType = target.type();
        MethodType foldTargetType = targetType.insertParameterTypes(0, boolean.class);
        if (AdapterMethodHandle.canCollectArguments(foldTargetType, test.type(), 0, true)
            && GuardWithTest.preferRicochetFrame(targetType)) {
            assert(target.type().equals(fallback.type()));
            MethodHandle tailcall = MethodHandles.exactInvoker(target.type());
            MethodHandle select = selectAlternative();
            select = bindArgument(select, 2, fallback);
            select = bindArgument(select, 1, target);
            MethodHandle filter = filterArgument(tailcall, 0, select);
            assert(filter.type().parameterType(0) == boolean.class);
            MethodHandle fold = foldArguments(filter, filter.type().dropParameterTypes(0, 1), 0, test);
            return fold;
        }
        return GuardWithTest.make(test, target, fallback);
    }
    private static class GuardWithCatch extends BoundMethodHandle {
        private final MethodHandle target;
        private final Class<? extends Throwable> exType;
        private final MethodHandle catcher;
        GuardWithCatch(MethodHandle target, Class<? extends Throwable> exType, MethodHandle catcher) {
            this(INVOKES[target.type().parameterCount()], target, exType, catcher);
        }
        GuardWithCatch(MethodHandle invoker,
                       MethodHandle target, Class<? extends Throwable> exType, MethodHandle catcher) {
            super(invoker);
            this.target = target;
            this.exType = exType;
            this.catcher = catcher;
        }
        @Override
        String debugString() {
            return addTypeString(target, this);
        }
        private Object invoke_V(Object... av) throws Throwable {
            try {
                return target.invokeExact(av);
            } catch (Throwable t) {
                if (!exType.isInstance(t))  throw t;
                return catcher.invokeExact(t, av);
            }
        }
        private Object invoke_L0() throws Throwable {
            try {
                return target.invokeExact();
            } catch (Throwable t) {
                if (!exType.isInstance(t))  throw t;
                return catcher.invokeExact(t);
            }
        }
        private Object invoke_L1(Object a0) throws Throwable {
            try {
                return target.invokeExact(a0);
            } catch (Throwable t) {
                if (!exType.isInstance(t))  throw t;
                return catcher.invokeExact(t, a0);
            }
        }
        private Object invoke_L2(Object a0, Object a1) throws Throwable {
            try {
                return target.invokeExact(a0, a1);
            } catch (Throwable t) {
                if (!exType.isInstance(t))  throw t;
                return catcher.invokeExact(t, a0, a1);
            }
        }
        private Object invoke_L3(Object a0, Object a1, Object a2) throws Throwable {
            try {
                return target.invokeExact(a0, a1, a2);
            } catch (Throwable t) {
                if (!exType.isInstance(t))  throw t;
                return catcher.invokeExact(t, a0, a1, a2);
            }
        }
        private Object invoke_L4(Object a0, Object a1, Object a2, Object a3) throws Throwable {
            try {
                return target.invokeExact(a0, a1, a2, a3);
            } catch (Throwable t) {
                if (!exType.isInstance(t))  throw t;
                return catcher.invokeExact(t, a0, a1, a2, a3);
            }
        }
        private Object invoke_L5(Object a0, Object a1, Object a2, Object a3, Object a4) throws Throwable {
            try {
                return target.invokeExact(a0, a1, a2, a3, a4);
            } catch (Throwable t) {
                if (!exType.isInstance(t))  throw t;
                return catcher.invokeExact(t, a0, a1, a2, a3, a4);
            }
        }
        private Object invoke_L6(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5) throws Throwable {
            try {
                return target.invokeExact(a0, a1, a2, a3, a4, a5);
            } catch (Throwable t) {
                if (!exType.isInstance(t))  throw t;
                return catcher.invokeExact(t, a0, a1, a2, a3, a4, a5);
            }
        }
        private Object invoke_L7(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6) throws Throwable {
            try {
                return target.invokeExact(a0, a1, a2, a3, a4, a5, a6);
            } catch (Throwable t) {
                if (!exType.isInstance(t))  throw t;
                return catcher.invokeExact(t, a0, a1, a2, a3, a4, a5, a6);
            }
        }
        private Object invoke_L8(Object a0, Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7) throws Throwable {
            try {
                return target.invokeExact(a0, a1, a2, a3, a4, a5, a6, a7);
            } catch (Throwable t) {
                if (!exType.isInstance(t))  throw t;
                return catcher.invokeExact(t, a0, a1, a2, a3, a4, a5, a6, a7);
            }
        }
        static MethodHandle[] makeInvokes() {
            ArrayList<MethodHandle> invokes = new ArrayList<MethodHandle>();
            MethodHandles.Lookup lookup = IMPL_LOOKUP;
            for (;;) {
                int nargs = invokes.size();
                String name = "invoke_L"+nargs;
                MethodHandle invoke = null;
                try {
                    invoke = lookup.findVirtual(GuardWithCatch.class, name, MethodType.genericMethodType(nargs));
                } catch (ReflectiveOperationException ex) {
                }
                if (invoke == null)  break;
                invokes.add(invoke);
            }
            assert(invokes.size() == 9);  
            return invokes.toArray(new MethodHandle[0]);
        };
        static final MethodHandle[] INVOKES = makeInvokes();
        static final MethodHandle VARARGS_INVOKE;
        static {
            try {
                VARARGS_INVOKE = IMPL_LOOKUP.findVirtual(GuardWithCatch.class, "invoke_V", MethodType.genericMethodType(0, true));
            } catch (ReflectiveOperationException ex) {
                throw uncaughtException(ex);
            }
        }
    }
    static
    MethodHandle makeGuardWithCatch(MethodHandle target,
                                    Class<? extends Throwable> exType,
                                    MethodHandle catcher) {
        MethodType type = target.type();
        MethodType ctype = catcher.type();
        int nargs = type.parameterCount();
        if (nargs < GuardWithCatch.INVOKES.length) {
            MethodType gtype = type.generic();
            MethodType gcatchType = gtype.insertParameterTypes(0, Throwable.class);
            MethodHandle gtarget = convertArguments(target, gtype, type, 2);
            MethodHandle gcatcher = convertArguments(catcher, gcatchType, ctype, 2);
            MethodHandle gguard = new GuardWithCatch(gtarget, exType, gcatcher);
            if (gtarget == null || gcatcher == null || gguard == null)  return null;
            return convertArguments(gguard, type, gtype, 2);
        } else {
            MethodType gtype = MethodType.genericMethodType(0, true);
            MethodType gcatchType = gtype.insertParameterTypes(0, Throwable.class);
            MethodHandle gtarget = spreadArgumentsFromPos(target, gtype, 0);
            catcher = catcher.asType(ctype.changeParameterType(0, Throwable.class));
            MethodHandle gcatcher = spreadArgumentsFromPos(catcher, gcatchType, 1);
            MethodHandle gguard = new GuardWithCatch(GuardWithCatch.VARARGS_INVOKE, gtarget, exType, gcatcher);
            if (gtarget == null || gcatcher == null || gguard == null)  return null;
            return collectArguments(gguard, type, 0, ValueConversions.varargsArray(nargs)).asType(type);
        }
    }
    static
    MethodHandle throwException(MethodType type) {
        return AdapterMethodHandle.makeRetypeRaw(type, throwException());
    }
    static MethodHandle THROW_EXCEPTION;
    static MethodHandle throwException() {
        if (THROW_EXCEPTION != null)  return THROW_EXCEPTION;
        try {
            THROW_EXCEPTION
            = IMPL_LOOKUP.findStatic(MethodHandleImpl.class, "throwException",
                    MethodType.methodType(Empty.class, Throwable.class));
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
        return THROW_EXCEPTION;
    }
    static <T extends Throwable> Empty throwException(T t) throws T { throw t; }
    static void registerBootstrap(Class<?> callerClass, MethodHandle bootstrapMethod) {
        MethodHandleNatives.registerBootstrap(callerClass, bootstrapMethod);
    }
    static MethodHandle getBootstrap(Class<?> callerClass) {
        return MethodHandleNatives.getBootstrap(callerClass);
    }
}
