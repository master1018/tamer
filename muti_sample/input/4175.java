public class MethodHandles {
    private MethodHandles() { }  
    private static final MemberName.Factory IMPL_NAMES = MemberName.getFactory();
    static { MethodHandleImpl.initStatics(); }
    public static Lookup lookup() {
        return new Lookup();
    }
    public static Lookup publicLookup() {
        return Lookup.PUBLIC_LOOKUP;
    }
    public static final
    class Lookup {
        private final Class<?> lookupClass;
        private final int allowedModes;
        public static final int PUBLIC = Modifier.PUBLIC;
        public static final int PRIVATE = Modifier.PRIVATE;
        public static final int PROTECTED = Modifier.PROTECTED;
        public static final int PACKAGE = Modifier.STATIC;
        private static final int ALL_MODES = (PUBLIC | PRIVATE | PROTECTED | PACKAGE);
        private static final int TRUSTED   = -1;
        private static int fixmods(int mods) {
            mods &= (ALL_MODES - PACKAGE);
            return (mods != 0) ? mods : PACKAGE;
        }
        public Class<?> lookupClass() {
            return lookupClass;
        }
        private Class<?> lookupClassOrNull() {
            return (allowedModes == TRUSTED) ? null : lookupClass;
        }
        public int lookupModes() {
            return allowedModes & ALL_MODES;
        }
        Lookup() {
            this(getCallerClassAtEntryPoint(), ALL_MODES);
            checkUnprivilegedlookupClass(lookupClass);
        }
        Lookup(Class<?> lookupClass) {
            this(lookupClass, ALL_MODES);
        }
        private Lookup(Class<?> lookupClass, int allowedModes) {
            this.lookupClass = lookupClass;
            this.allowedModes = allowedModes;
        }
        public Lookup in(Class<?> requestedLookupClass) {
            requestedLookupClass.getClass();  
            if (allowedModes == TRUSTED)  
                return new Lookup(requestedLookupClass, ALL_MODES);
            if (requestedLookupClass == this.lookupClass)
                return this;  
            int newModes = (allowedModes & (ALL_MODES & ~PROTECTED));
            if ((newModes & PACKAGE) != 0
                && !VerifyAccess.isSamePackage(this.lookupClass, requestedLookupClass)) {
                newModes &= ~(PACKAGE|PRIVATE);
            }
            if ((newModes & PRIVATE) != 0
                && !VerifyAccess.isSamePackageMember(this.lookupClass, requestedLookupClass)) {
                newModes &= ~PRIVATE;
            }
            if (newModes == PUBLIC
                && !VerifyAccess.isClassAccessible(requestedLookupClass, this.lookupClass)) {
                newModes = 0;
            }
            checkUnprivilegedlookupClass(requestedLookupClass);
            return new Lookup(requestedLookupClass, newModes);
        }
        static { IMPL_NAMES.getClass(); }
        static final Lookup PUBLIC_LOOKUP = new Lookup(Object.class, PUBLIC);
        static final Lookup IMPL_LOOKUP = new Lookup(Object.class, TRUSTED);
        private static void checkUnprivilegedlookupClass(Class<?> lookupClass) {
            String name = lookupClass.getName();
            if (name.startsWith("java.lang.invoke."))
                throw newIllegalArgumentException("illegal lookupClass: "+lookupClass);
        }
        @Override
        public String toString() {
            String cname = lookupClass.getName();
            switch (allowedModes) {
            case 0:  
                return cname + "/noaccess";
            case PUBLIC:
                return cname + "/public";
            case PUBLIC|PACKAGE:
                return cname + "/package";
            case ALL_MODES & ~PROTECTED:
                return cname + "/private";
            case ALL_MODES:
                return cname;
            case TRUSTED:
                return "/trusted";  
            default:  
                cname = cname + "/" + Integer.toHexString(allowedModes);
                assert(false) : cname;
                return cname;
            }
        }
        private static Class<?> getCallerClassAtEntryPoint() {
            final int CALLER_DEPTH = 4;
            assert(Reflection.getCallerClass(CALLER_DEPTH-1) == MethodHandles.class);
            return Reflection.getCallerClass(CALLER_DEPTH);
        }
        public
        MethodHandle findStatic(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            MemberName method = resolveOrFail(refc, name, type, true);
            checkSecurityManager(refc, method);  
            return accessStatic(refc, method);
        }
        private
        MethodHandle accessStatic(Class<?> refc, MemberName method) throws IllegalAccessException {
            checkMethod(refc, method, true);
            return MethodHandleImpl.findMethod(method, false, lookupClassOrNull());
        }
        private
        MethodHandle resolveStatic(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            MemberName method = resolveOrFail(refc, name, type, true);
            return accessStatic(refc, method);
        }
        public MethodHandle findVirtual(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            MemberName method = resolveOrFail(refc, name, type, false);
            checkSecurityManager(refc, method);  
            return accessVirtual(refc, method);
        }
        private MethodHandle resolveVirtual(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            MemberName method = resolveOrFail(refc, name, type, false);
            return accessVirtual(refc, method);
        }
        private MethodHandle accessVirtual(Class<?> refc, MemberName method) throws IllegalAccessException {
            checkMethod(refc, method, false);
            MethodHandle mh = MethodHandleImpl.findMethod(method, true, lookupClassOrNull());
            return restrictProtectedReceiver(method, mh);
        }
        public MethodHandle findConstructor(Class<?> refc, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            String name = "<init>";
            MemberName ctor = resolveOrFail(refc, name, type, false, false, lookupClassOrNull());
            checkSecurityManager(refc, ctor);  
            return accessConstructor(refc, ctor);
        }
        private MethodHandle accessConstructor(Class<?> refc, MemberName ctor) throws IllegalAccessException {
            assert(ctor.isConstructor());
            checkAccess(refc, ctor);
            MethodHandle rawMH = MethodHandleImpl.findMethod(ctor, false, lookupClassOrNull());
            MethodHandle allocMH = MethodHandleImpl.makeAllocator(rawMH);
            return fixVarargs(allocMH, rawMH);
        }
        private MethodHandle resolveConstructor(Class<?> refc, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            String name = "<init>";
            MemberName ctor = resolveOrFail(refc, name, type, false, false, lookupClassOrNull());
            return accessConstructor(refc, ctor);
        }
        private static MethodHandle fixVarargs(MethodHandle mh, MethodHandle matchMH) {
            boolean va1 = mh.isVarargsCollector();
            boolean va2 = matchMH.isVarargsCollector();
            if (va1 == va2) {
                return mh;
            } else if (va2) {
                MethodType type = mh.type();
                int arity = type.parameterCount();
                return mh.asVarargsCollector(type.parameterType(arity-1));
            } else {
                return mh.asFixedArity();
            }
        }
        public MethodHandle findSpecial(Class<?> refc, String name, MethodType type,
                                        Class<?> specialCaller) throws NoSuchMethodException, IllegalAccessException {
            checkSpecialCaller(specialCaller);
            MemberName method = resolveOrFail(refc, name, type, false, false, specialCaller);
            checkSecurityManager(refc, method);  
            return accessSpecial(refc, method, specialCaller);
        }
        private MethodHandle accessSpecial(Class<?> refc, MemberName method,
                                           Class<?> specialCaller) throws NoSuchMethodException, IllegalAccessException {
            checkMethod(refc, method, false);
            MethodHandle mh = MethodHandleImpl.findMethod(method, false, specialCaller);
            return restrictReceiver(method, mh, specialCaller);
        }
        private MethodHandle resolveSpecial(Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            Class<?> specialCaller = lookupClass();
            checkSpecialCaller(specialCaller);
            MemberName method = resolveOrFail(refc, name, type, false, false, specialCaller);
            return accessSpecial(refc, method, specialCaller);
        }
        public MethodHandle findGetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            MemberName field = resolveOrFail(refc, name, type, false);
            checkSecurityManager(refc, field);  
            return makeAccessor(refc, field, false, false, 0);
        }
        private MethodHandle resolveGetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            MemberName field = resolveOrFail(refc, name, type, false);
            return makeAccessor(refc, field, false, false, 0);
        }
        public MethodHandle findSetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            MemberName field = resolveOrFail(refc, name, type, false);
            checkSecurityManager(refc, field);  
            return makeAccessor(refc, field, false, true, 0);
        }
        private MethodHandle resolveSetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            MemberName field = resolveOrFail(refc, name, type, false);
            return makeAccessor(refc, field, false, true, 0);
        }
        public MethodHandle findStaticGetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            MemberName field = resolveOrFail(refc, name, type, true);
            checkSecurityManager(refc, field);  
            return makeAccessor(refc, field, false, false, 1);
        }
        private MethodHandle resolveStaticGetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            MemberName field = resolveOrFail(refc, name, type, true);
            return makeAccessor(refc, field, false, false, 1);
        }
        public MethodHandle findStaticSetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            MemberName field = resolveOrFail(refc, name, type, true);
            checkSecurityManager(refc, field);  
            return makeAccessor(refc, field, false, true, 1);
        }
        private MethodHandle resolveStaticSetter(Class<?> refc, String name, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
            MemberName field = resolveOrFail(refc, name, type, true);
            return makeAccessor(refc, field, false, true, 1);
        }
        public MethodHandle bind(Object receiver, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
            Class<? extends Object> refc = receiver.getClass(); 
            MemberName method = resolveOrFail(refc, name, type, false);
            checkSecurityManager(refc, method);  
            checkMethod(refc, method, false);
            MethodHandle dmh = MethodHandleImpl.findMethod(method, true, lookupClassOrNull());
            MethodHandle bmh = MethodHandleImpl.bindReceiver(dmh, receiver);
            if (bmh == null)
                throw method.makeAccessException("no access", this);
            return fixVarargs(bmh, dmh);
        }
        public MethodHandle unreflect(Method m) throws IllegalAccessException {
            MemberName method = new MemberName(m);
            assert(method.isMethod());
            if (!m.isAccessible())  checkMethod(method.getDeclaringClass(), method, method.isStatic());
            MethodHandle mh = MethodHandleImpl.findMethod(method, true, lookupClassOrNull());
            if (!m.isAccessible())  mh = restrictProtectedReceiver(method, mh);
            return mh;
        }
        public MethodHandle unreflectSpecial(Method m, Class<?> specialCaller) throws IllegalAccessException {
            checkSpecialCaller(specialCaller);
            MemberName method = new MemberName(m);
            assert(method.isMethod());
            checkMethod(m.getDeclaringClass(), method, false);
            MethodHandle mh = MethodHandleImpl.findMethod(method, false, lookupClassOrNull());
            return restrictReceiver(method, mh, specialCaller);
        }
        public MethodHandle unreflectConstructor(Constructor c) throws IllegalAccessException {
            MemberName ctor = new MemberName(c);
            assert(ctor.isConstructor());
            if (!c.isAccessible())  checkAccess(c.getDeclaringClass(), ctor);
            MethodHandle rawCtor = MethodHandleImpl.findMethod(ctor, false, lookupClassOrNull());
            MethodHandle allocator = MethodHandleImpl.makeAllocator(rawCtor);
            return fixVarargs(allocator, rawCtor);
        }
        public MethodHandle unreflectGetter(Field f) throws IllegalAccessException {
            return makeAccessor(f.getDeclaringClass(), new MemberName(f), f.isAccessible(), false, -1);
        }
        public MethodHandle unreflectSetter(Field f) throws IllegalAccessException {
            return makeAccessor(f.getDeclaringClass(), new MemberName(f), f.isAccessible(), true, -1);
        }
        MemberName resolveOrFail(Class<?> refc, String name, Class<?> type, boolean isStatic) throws NoSuchFieldException, IllegalAccessException {
            checkSymbolicClass(refc);  
            name.getClass(); type.getClass();  
            int mods = (isStatic ? Modifier.STATIC : 0);
            return IMPL_NAMES.resolveOrFail(new MemberName(refc, name, type, mods), true, lookupClassOrNull(),
                                            NoSuchFieldException.class);
        }
        MemberName resolveOrFail(Class<?> refc, String name, MethodType type, boolean isStatic) throws NoSuchMethodException, IllegalAccessException {
            checkSymbolicClass(refc);  
            name.getClass(); type.getClass();  
            int mods = (isStatic ? Modifier.STATIC : 0);
            return IMPL_NAMES.resolveOrFail(new MemberName(refc, name, type, mods), true, lookupClassOrNull(),
                                            NoSuchMethodException.class);
        }
        MemberName resolveOrFail(Class<?> refc, String name, MethodType type, boolean isStatic,
                                 boolean searchSupers, Class<?> specialCaller) throws NoSuchMethodException, IllegalAccessException {
            checkSymbolicClass(refc);  
            name.getClass(); type.getClass();  
            int mods = (isStatic ? Modifier.STATIC : 0);
            return IMPL_NAMES.resolveOrFail(new MemberName(refc, name, type, mods), searchSupers, specialCaller,
                                            NoSuchMethodException.class);
        }
        void checkSymbolicClass(Class<?> refc) throws IllegalAccessException {
            Class<?> caller = lookupClassOrNull();
            if (caller != null && !VerifyAccess.isClassAccessible(refc, caller))
                throw new MemberName(refc).makeAccessException("symbolic reference class is not public", this);
        }
        void checkSecurityManager(Class<?> refc, MemberName m) {
            SecurityManager smgr = System.getSecurityManager();
            if (smgr == null)  return;
            if (allowedModes == TRUSTED)  return;
            smgr.checkMemberAccess(refc, Member.PUBLIC);
            if (!VerifyAccess.classLoaderIsAncestor(lookupClass, refc))
                smgr.checkPackageAccess(VerifyAccess.getPackageName(refc));
            if (m.isPublic()) return;
            Class<?> defc = m.getDeclaringClass();
            smgr.checkMemberAccess(defc, Member.DECLARED);  
            if (defc != refc)
                smgr.checkPackageAccess(VerifyAccess.getPackageName(defc));
        }
        void checkMethod(Class<?> refc, MemberName m, boolean wantStatic) throws IllegalAccessException {
            String message;
            if (m.isConstructor())
                message = "expected a method, not a constructor";
            else if (!m.isMethod())
                message = "expected a method";
            else if (wantStatic != m.isStatic())
                message = wantStatic ? "expected a static method" : "expected a non-static method";
            else
                { checkAccess(refc, m); return; }
            throw m.makeAccessException(message, this);
        }
        void checkAccess(Class<?> refc, MemberName m) throws IllegalAccessException {
            int allowedModes = this.allowedModes;
            if (allowedModes == TRUSTED)  return;
            int mods = m.getModifiers();
            if (Modifier.isPublic(mods) && Modifier.isPublic(refc.getModifiers()) && allowedModes != 0)
                return;  
            int requestedModes = fixmods(mods);  
            if ((requestedModes & allowedModes) != 0
                && VerifyAccess.isMemberAccessible(refc, m.getDeclaringClass(),
                                                   mods, lookupClass()))
                return;
            if (((requestedModes & ~allowedModes) & PROTECTED) != 0
                && VerifyAccess.isSamePackage(m.getDeclaringClass(), lookupClass()))
                return;
            throw m.makeAccessException(accessFailedMessage(refc, m), this);
        }
        String accessFailedMessage(Class<?> refc, MemberName m) {
            Class<?> defc = m.getDeclaringClass();
            int mods = m.getModifiers();
            boolean classOK = (Modifier.isPublic(defc.getModifiers()) &&
                               (defc == refc ||
                                Modifier.isPublic(refc.getModifiers())));
            if (!classOK && (allowedModes & PACKAGE) != 0) {
                classOK = (VerifyAccess.isClassAccessible(defc, lookupClass()) &&
                           (defc == refc ||
                            VerifyAccess.isClassAccessible(refc, lookupClass())));
            }
            if (!classOK)
                return "class is not public";
            if (Modifier.isPublic(mods))
                return "access to public member failed";  
            if (Modifier.isPrivate(mods))
                return "member is private";
            if (Modifier.isProtected(mods))
                return "member is protected";
            return "member is private to package";
        }
        private static final boolean ALLOW_NESTMATE_ACCESS = false;
        void checkSpecialCaller(Class<?> specialCaller) throws IllegalAccessException {
            if (allowedModes == TRUSTED)  return;
            if ((allowedModes & PRIVATE) == 0
                || (specialCaller != lookupClass()
                    && !(ALLOW_NESTMATE_ACCESS &&
                         VerifyAccess.isSamePackageMember(specialCaller, lookupClass()))))
                throw new MemberName(specialCaller).
                    makeAccessException("no private access for invokespecial", this);
        }
        MethodHandle restrictProtectedReceiver(MemberName method, MethodHandle mh) throws IllegalAccessException {
            if (!method.isProtected() || method.isStatic()
                || allowedModes == TRUSTED
                || method.getDeclaringClass() == lookupClass()
                || VerifyAccess.isSamePackage(method.getDeclaringClass(), lookupClass())
                || (ALLOW_NESTMATE_ACCESS &&
                    VerifyAccess.isSamePackageMember(method.getDeclaringClass(), lookupClass())))
                return mh;
            else
                return restrictReceiver(method, mh, lookupClass());
        }
        MethodHandle restrictReceiver(MemberName method, MethodHandle mh, Class<?> caller) throws IllegalAccessException {
            assert(!method.isStatic());
            Class<?> defc = method.getDeclaringClass();  
            if (defc.isInterface() || !defc.isAssignableFrom(caller)) {
                throw method.makeAccessException("caller class must be a subclass below the method", caller);
            }
            MethodType rawType = mh.type();
            if (rawType.parameterType(0) == caller)  return mh;
            MethodType narrowType = rawType.changeParameterType(0, caller);
            MethodHandle narrowMH = MethodHandleImpl.convertArguments(mh, narrowType, rawType, 0);
            return fixVarargs(narrowMH, mh);
        }
        MethodHandle makeAccessor(Class<?> refc, MemberName field,
                                  boolean trusted, boolean isSetter,
                                  int checkStatic) throws IllegalAccessException {
            assert(field.isField());
            if (checkStatic >= 0 && (checkStatic != 0) != field.isStatic())
                throw field.makeAccessException((checkStatic != 0)
                                                ? "expected a static field"
                                                : "expected a non-static field", this);
            if (trusted)
                return MethodHandleImpl.accessField(field, isSetter, lookupClassOrNull());
            checkAccess(refc, field);
            MethodHandle mh = MethodHandleImpl.accessField(field, isSetter, lookupClassOrNull());
            return restrictProtectedReceiver(field, mh);
        }
        MethodHandle linkMethodHandleConstant(int refKind, Class<?> defc, String name, Object type) throws ReflectiveOperationException {
            switch (refKind) {
            case REF_getField:          return resolveGetter(       defc, name, (Class<?>)   type );
            case REF_getStatic:         return resolveStaticGetter( defc, name, (Class<?>)   type );
            case REF_putField:          return resolveSetter(       defc, name, (Class<?>)   type );
            case REF_putStatic:         return resolveStaticSetter( defc, name, (Class<?>)   type );
            case REF_invokeVirtual:     return resolveVirtual(      defc, name, (MethodType) type );
            case REF_invokeStatic:      return resolveStatic(       defc, name, (MethodType) type );
            case REF_invokeSpecial:     return resolveSpecial(      defc, name, (MethodType) type );
            case REF_newInvokeSpecial:  return resolveConstructor(  defc,       (MethodType) type );
            case REF_invokeInterface:   return resolveVirtual(      defc, name, (MethodType) type );
            }
            throw new ReflectiveOperationException("bad MethodHandle constant #"+refKind+" "+name+" : "+type);
        }
    }
    public static
    MethodHandle arrayElementGetter(Class<?> arrayClass) throws IllegalArgumentException {
        return MethodHandleImpl.accessArrayElement(arrayClass, false);
    }
    public static
    MethodHandle arrayElementSetter(Class<?> arrayClass) throws IllegalArgumentException {
        return MethodHandleImpl.accessArrayElement(arrayClass, true);
    }
    static public
    MethodHandle spreadInvoker(MethodType type, int leadingArgCount) {
        if (leadingArgCount < 0 || leadingArgCount > type.parameterCount())
            throw new IllegalArgumentException("bad argument count "+leadingArgCount);
        return type.invokers().spreadInvoker(leadingArgCount);
    }
    static public
    MethodHandle exactInvoker(MethodType type) {
        return type.invokers().exactInvoker();
    }
    static public
    MethodHandle invoker(MethodType type) {
        return type.invokers().generalInvoker();
    }
    static
    <T0, T1> T1 checkValue(Class<T0> t0, Class<T1> t1, Object value)
       throws ClassCastException
    {
        if (t0 == t1) {
            if (t0.isPrimitive())
                return Wrapper.asPrimitiveType(t1).cast(value);
            else
                return Wrapper.OBJECT.convert(value, t1);
        }
        boolean prim0 = t0.isPrimitive(), prim1 = t1.isPrimitive();
        if (!prim0) {
            Wrapper.OBJECT.convert(value, t0);
            if (!prim1) {
                return Wrapper.OBJECT.convert(value, t1);
            }
            Wrapper w1 = Wrapper.forPrimitiveType(t1);
            return w1.convert(value, t1);
        }
        Wrapper.asWrapperType(t0).cast(value);
        Wrapper w1 = Wrapper.forPrimitiveType(t1);
        return w1.convert(value, t1);
    }
    static
    Object checkValue(Class<?> T1, Object value)
       throws ClassCastException
    {
        Class<?> T0;
        if (value == null)
            T0 = Object.class;
        else
            T0 = value.getClass();
        return checkValue(T0, T1, value);
    }
    public static
    MethodHandle explicitCastArguments(MethodHandle target, MethodType newType) {
        return MethodHandleImpl.convertArguments(target, newType, 2);
    }
    public static
    MethodHandle permuteArguments(MethodHandle target, MethodType newType, int... reorder) {
        MethodType oldType = target.type();
        checkReorder(reorder, newType, oldType);
        return MethodHandleImpl.permuteArguments(target,
                                                 newType, oldType,
                                                 reorder);
    }
    private static void checkReorder(int[] reorder, MethodType newType, MethodType oldType) {
        if (newType.returnType() != oldType.returnType())
            throw newIllegalArgumentException("return types do not match",
                    oldType, newType);
        if (reorder.length == oldType.parameterCount()) {
            int limit = newType.parameterCount();
            boolean bad = false;
            for (int j = 0; j < reorder.length; j++) {
                int i = reorder[j];
                if (i < 0 || i >= limit) {
                    bad = true; break;
                }
                Class<?> src = newType.parameterType(i);
                Class<?> dst = oldType.parameterType(j);
                if (src != dst)
                    throw newIllegalArgumentException("parameter types do not match after reorder",
                            oldType, newType);
            }
            if (!bad)  return;
        }
        throw newIllegalArgumentException("bad reorder array: "+Arrays.toString(reorder));
    }
    public static
    MethodHandle constant(Class<?> type, Object value) {
        if (type.isPrimitive()) {
            if (type == void.class)
                throw newIllegalArgumentException("void type");
            Wrapper w = Wrapper.forPrimitiveType(type);
            return insertArguments(identity(type), 0, w.convert(value, type));
        } else {
            return identity(type).bindTo(type.cast(value));
        }
    }
    public static
    MethodHandle identity(Class<?> type) {
        if (type == void.class)
            throw newIllegalArgumentException("void type");
        else if (type == Object.class)
            return ValueConversions.identity();
        else if (type.isPrimitive())
            return ValueConversions.identity(Wrapper.forPrimitiveType(type));
        else
            return AdapterMethodHandle.makeRetypeRaw(
                    MethodType.methodType(type, type), ValueConversions.identity());
    }
    public static
    MethodHandle insertArguments(MethodHandle target, int pos, Object... values) {
        int insCount = values.length;
        MethodType oldType = target.type();
        int outargs = oldType.parameterCount();
        int inargs  = outargs - insCount;
        if (inargs < 0)
            throw newIllegalArgumentException("too many values to insert");
        if (pos < 0 || pos > inargs)
            throw newIllegalArgumentException("no argument type to append");
        MethodHandle result = target;
        for (int i = 0; i < insCount; i++) {
            Object value = values[i];
            Class<?> valueType = oldType.parameterType(pos+i);
            value = checkValue(valueType, value);
            if (pos == 0 && !valueType.isPrimitive()) {
                MethodHandle bmh = MethodHandleImpl.bindReceiver(result, value);
                if (bmh != null) {
                    result = bmh;
                    continue;
                }
            }
            result = MethodHandleImpl.bindArgument(result, pos, value);
        }
        return result;
    }
    public static
    MethodHandle dropArguments(MethodHandle target, int pos, List<Class<?>> valueTypes) {
        MethodType oldType = target.type();  
        if (valueTypes.size() == 0)  return target;
        int outargs = oldType.parameterCount();
        int inargs  = outargs + valueTypes.size();
        if (pos < 0 || pos >= inargs)
            throw newIllegalArgumentException("no argument type to remove");
        ArrayList<Class<?>> ptypes =
                new ArrayList<Class<?>>(oldType.parameterList());
        ptypes.addAll(pos, valueTypes);
        MethodType newType = MethodType.methodType(oldType.returnType(), ptypes);
        return MethodHandleImpl.dropArguments(target, newType, pos);
    }
    public static
    MethodHandle dropArguments(MethodHandle target, int pos, Class<?>... valueTypes) {
        return dropArguments(target, pos, Arrays.asList(valueTypes));
    }
    public static
    MethodHandle filterArguments(MethodHandle target, int pos, MethodHandle... filters) {
        MethodType targetType = target.type();
        MethodHandle adapter = target;
        MethodType adapterType = null;
        assert((adapterType = targetType) != null);
        int maxPos = targetType.parameterCount();
        if (pos + filters.length > maxPos)
            throw newIllegalArgumentException("too many filters");
        int curPos = pos-1;  
        for (MethodHandle filter : filters) {
            curPos += 1;
            if (filter == null)  continue;  
            adapter = filterArgument(adapter, curPos, filter);
            assert((adapterType = adapterType.changeParameterType(curPos, filter.type().parameterType(0))) != null);
        }
        assert(adapterType.equals(adapter.type()));
        return adapter;
    }
     static
    MethodHandle filterArgument(MethodHandle target, int pos, MethodHandle filter) {
        MethodType targetType = target.type();
        MethodType filterType = filter.type();
        if (filterType.parameterCount() != 1
            || filterType.returnType() != targetType.parameterType(pos))
            throw newIllegalArgumentException("target and filter types do not match", targetType, filterType);
        return MethodHandleImpl.filterArgument(target, pos, filter);
    }
    public static
    MethodHandle filterReturnValue(MethodHandle target, MethodHandle filter) {
        MethodType targetType = target.type();
        MethodType filterType = filter.type();
        Class<?> rtype = targetType.returnType();
        int filterValues = filterType.parameterCount();
        if (filterValues == 0
                ? (rtype != void.class)
                : (rtype != filterType.parameterType(0)))
            throw newIllegalArgumentException("target and filter types do not match", target, filter);
        MethodType newType = targetType.changeReturnType(filterType.returnType());
        MethodHandle result = null;
        if (AdapterMethodHandle.canCollectArguments(filterType, targetType, 0, false)) {
            result = AdapterMethodHandle.makeCollectArguments(filter, target, 0, false);
            if (result != null)  return result;
        }
        assert(MethodHandleNatives.workaroundWithoutRicochetFrames());  
        MethodHandle returner = dropArguments(filter, filterValues, targetType.parameterList());
        result = foldArguments(returner, target);
        assert(result.type().equals(newType));
        return result;
    }
    public static
    MethodHandle foldArguments(MethodHandle target, MethodHandle combiner) {
        int pos = 0;
        MethodType targetType = target.type();
        MethodType combinerType = combiner.type();
        int foldPos = pos;
        int foldArgs = combinerType.parameterCount();
        int foldVals = combinerType.returnType() == void.class ? 0 : 1;
        int afterInsertPos = foldPos + foldVals;
        boolean ok = (targetType.parameterCount() >= afterInsertPos + foldArgs);
        if (ok && !(combinerType.parameterList()
                    .equals(targetType.parameterList().subList(afterInsertPos,
                                                               afterInsertPos + foldArgs))))
            ok = false;
        if (ok && foldVals != 0 && !combinerType.returnType().equals(targetType.parameterType(0)))
            ok = false;
        if (!ok)
            throw misMatchedTypes("target and combiner types", targetType, combinerType);
        MethodType newType = targetType.dropParameterTypes(foldPos, afterInsertPos);
        MethodHandle res = MethodHandleImpl.foldArguments(target, newType, foldPos, combiner);
        if (res == null)  throw newIllegalArgumentException("cannot fold from "+newType+" to " +targetType);
        return res;
    }
    public static
    MethodHandle guardWithTest(MethodHandle test,
                               MethodHandle target,
                               MethodHandle fallback) {
        MethodType gtype = test.type();
        MethodType ttype = target.type();
        MethodType ftype = fallback.type();
        if (!ttype.equals(ftype))
            throw misMatchedTypes("target and fallback types", ttype, ftype);
        if (gtype.returnType() != boolean.class)
            throw newIllegalArgumentException("guard type is not a predicate "+gtype);
        List<Class<?>> targs = ttype.parameterList();
        List<Class<?>> gargs = gtype.parameterList();
        if (!targs.equals(gargs)) {
            int gpc = gargs.size(), tpc = targs.size();
            if (gpc >= tpc || !targs.subList(0, gpc).equals(gargs))
                throw misMatchedTypes("target and test types", ttype, gtype);
            test = dropArguments(test, gpc, targs.subList(gpc, tpc));
            gtype = test.type();
        }
        return MethodHandleImpl.makeGuardWithTest(test, target, fallback);
    }
    static RuntimeException misMatchedTypes(String what, MethodType t1, MethodType t2) {
        return newIllegalArgumentException(what + " must match: " + t1 + " != " + t2);
    }
    public static
    MethodHandle catchException(MethodHandle target,
                                Class<? extends Throwable> exType,
                                MethodHandle handler) {
        MethodType ttype = target.type();
        MethodType htype = handler.type();
        if (htype.parameterCount() < 1 ||
            !htype.parameterType(0).isAssignableFrom(exType))
            throw newIllegalArgumentException("handler does not accept exception type "+exType);
        if (htype.returnType() != ttype.returnType())
            throw misMatchedTypes("target and handler return types", ttype, htype);
        List<Class<?>> targs = ttype.parameterList();
        List<Class<?>> hargs = htype.parameterList();
        hargs = hargs.subList(1, hargs.size());  
        if (!targs.equals(hargs)) {
            int hpc = hargs.size(), tpc = targs.size();
            if (hpc >= tpc || !targs.subList(0, hpc).equals(hargs))
                throw misMatchedTypes("target and handler types", ttype, htype);
            handler = dropArguments(handler, 1+hpc, targs.subList(hpc, tpc));
            htype = handler.type();
        }
        return MethodHandleImpl.makeGuardWithCatch(target, exType, handler);
    }
    public static
    MethodHandle throwException(Class<?> returnType, Class<? extends Throwable> exType) {
        return MethodHandleImpl.throwException(MethodType.methodType(returnType, exType));
    }
}
