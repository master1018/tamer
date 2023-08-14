 final class MemberName implements Member, Cloneable {
    private Class<?>   clazz;       
    private String     name;        
    private Object     type;        
    private int        flags;       
    private Object     vmtarget;    
    private int        vmindex;     
    { vmindex = VM_INDEX_UNINITIALIZED; }
    public Class<?> getDeclaringClass() {
        if (clazz == null && isResolved()) {
            expandFromVM();
        }
        return clazz;
    }
    public ClassLoader getClassLoader() {
        return clazz.getClassLoader();
    }
    public String getName() {
        if (name == null) {
            expandFromVM();
            if (name == null)  return null;
        }
        return name;
    }
    public MethodType getMethodType() {
        if (type == null) {
            expandFromVM();
            if (type == null)  return null;
        }
        if (!isInvocable())
            throw newIllegalArgumentException("not invocable, no method type");
        if (type instanceof MethodType) {
            return (MethodType) type;
        }
        if (type instanceof String) {
            String sig = (String) type;
            MethodType res = MethodType.fromMethodDescriptorString(sig, getClassLoader());
            this.type = res;
            return res;
        }
        if (type instanceof Object[]) {
            Object[] typeInfo = (Object[]) type;
            Class<?>[] ptypes = (Class<?>[]) typeInfo[1];
            Class<?> rtype = (Class<?>) typeInfo[0];
            MethodType res = MethodType.methodType(rtype, ptypes);
            this.type = res;
            return res;
        }
        throw new InternalError("bad method type "+type);
    }
    public MethodType getInvocationType() {
        MethodType itype = getMethodType();
        if (!isStatic())
            itype = itype.insertParameterTypes(0, clazz);
        return itype;
    }
    public Class<?>[] getParameterTypes() {
        return getMethodType().parameterArray();
    }
    public Class<?> getReturnType() {
        return getMethodType().returnType();
    }
    public Class<?> getFieldType() {
        if (type == null) {
            expandFromVM();
            if (type == null)  return null;
        }
        if (isInvocable())
            throw newIllegalArgumentException("not a field or nested class, no simple type");
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        if (type instanceof String) {
            String sig = (String) type;
            MethodType mtype = MethodType.fromMethodDescriptorString("()"+sig, getClassLoader());
            Class<?> res = mtype.returnType();
            this.type = res;
            return res;
        }
        throw new InternalError("bad field type "+type);
    }
    public Object getType() {
        return (isInvocable() ? getMethodType() : getFieldType());
    }
    public String getSignature() {
        if (type == null) {
            expandFromVM();
            if (type == null)  return null;
        }
        if (type instanceof String)
            return (String) type;
        if (isInvocable())
            return BytecodeDescriptor.unparse(getMethodType());
        else
            return BytecodeDescriptor.unparse(getFieldType());
    }
    public int getModifiers() {
        return (flags & RECOGNIZED_MODIFIERS);
    }
    private void setFlags(int flags) {
        this.flags = flags;
        assert(testAnyFlags(ALL_KINDS));
    }
    private boolean testFlags(int mask, int value) {
        return (flags & mask) == value;
    }
    private boolean testAllFlags(int mask) {
        return testFlags(mask, mask);
    }
    private boolean testAnyFlags(int mask) {
        return !testFlags(mask, 0);
    }
    public boolean isStatic() {
        return Modifier.isStatic(flags);
    }
    public boolean isPublic() {
        return Modifier.isPublic(flags);
    }
    public boolean isPrivate() {
        return Modifier.isPrivate(flags);
    }
    public boolean isProtected() {
        return Modifier.isProtected(flags);
    }
    public boolean isFinal() {
        return Modifier.isFinal(flags);
    }
    public boolean isAbstract() {
        return Modifier.isAbstract(flags);
    }
    static final int BRIDGE    = 0x00000040;
    static final int VARARGS   = 0x00000080;
    static final int SYNTHETIC = 0x00001000;
    static final int ANNOTATION= 0x00002000;
    static final int ENUM      = 0x00004000;
    public boolean isBridge() {
        return testAllFlags(IS_METHOD | BRIDGE);
    }
    public boolean isVarargs() {
        return testAllFlags(VARARGS) && isInvocable();
    }
    public boolean isSynthetic() {
        return testAllFlags(SYNTHETIC);
    }
    static final String CONSTRUCTOR_NAME = "<init>";  
    static final int RECOGNIZED_MODIFIERS = 0xFFFF;
    static final int
            IS_METHOD      = MN_IS_METHOD,      
            IS_CONSTRUCTOR = MN_IS_CONSTRUCTOR, 
            IS_FIELD       = MN_IS_FIELD,       
            IS_TYPE        = MN_IS_TYPE;        
    static final int  
            SEARCH_SUPERCLASSES = MN_SEARCH_SUPERCLASSES,
            SEARCH_INTERFACES   = MN_SEARCH_INTERFACES;
    static final int ALL_ACCESS = Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED;
    static final int ALL_KINDS = IS_METHOD | IS_CONSTRUCTOR | IS_FIELD | IS_TYPE;
    static final int IS_INVOCABLE = IS_METHOD | IS_CONSTRUCTOR;
    static final int IS_FIELD_OR_METHOD = IS_METHOD | IS_FIELD;
    static final int SEARCH_ALL_SUPERS = SEARCH_SUPERCLASSES | SEARCH_INTERFACES;
    public boolean isInvocable() {
        return testAnyFlags(IS_INVOCABLE);
    }
    public boolean isFieldOrMethod() {
        return testAnyFlags(IS_FIELD_OR_METHOD);
    }
    public boolean isMethod() {
        return testAllFlags(IS_METHOD);
    }
    public boolean isConstructor() {
        return testAllFlags(IS_CONSTRUCTOR);
    }
    public boolean isField() {
        return testAllFlags(IS_FIELD);
    }
    public boolean isType() {
        return testAllFlags(IS_TYPE);
    }
    public boolean isPackage() {
        return !testAnyFlags(ALL_ACCESS);
    }
    private void init(Class<?> defClass, String name, Object type, int flags) {
        this.clazz = defClass;
        this.name = name;
        this.type = type;
        setFlags(flags);
        assert(!isResolved());
    }
    private void expandFromVM() {
        if (!isResolved())  return;
        if (type instanceof Object[])
            type = null;  
        MethodHandleNatives.expand(this);
    }
    private static int flagsMods(int flags, int mods) {
        assert((flags & RECOGNIZED_MODIFIERS) == 0);
        assert((mods & ~RECOGNIZED_MODIFIERS) == 0);
        return flags | mods;
    }
    public MemberName(Method m) {
        Object[] typeInfo = { m.getReturnType(), m.getParameterTypes() };
        init(m.getDeclaringClass(), m.getName(), typeInfo, flagsMods(IS_METHOD, m.getModifiers()));
        MethodHandleNatives.init(this, m);
        assert(isResolved());
    }
    public MemberName(Constructor ctor) {
        Object[] typeInfo = { void.class, ctor.getParameterTypes() };
        init(ctor.getDeclaringClass(), CONSTRUCTOR_NAME, typeInfo, flagsMods(IS_CONSTRUCTOR, ctor.getModifiers()));
        MethodHandleNatives.init(this, ctor);
        assert(isResolved());
    }
    public MemberName(Field fld) {
        init(fld.getDeclaringClass(), fld.getName(), fld.getType(), flagsMods(IS_FIELD, fld.getModifiers()));
        MethodHandleNatives.init(this, fld);
        assert(isResolved());
    }
    public MemberName(Class<?> type) {
        init(type.getDeclaringClass(), type.getSimpleName(), type, flagsMods(IS_TYPE, type.getModifiers()));
        vmindex = 0;  
        assert(isResolved());
    }
    MemberName() { }
    @Override protected MemberName clone() {
        try {
            return (MemberName) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new InternalError();
        }
     }
    public MemberName(Class<?> defClass, String name, Class<?> type, int modifiers) {
        init(defClass, name, type, IS_FIELD | (modifiers & RECOGNIZED_MODIFIERS));
    }
    public MemberName(Class<?> defClass, String name, Class<?> type) {
        this(defClass, name, type, 0);
    }
    public MemberName(Class<?> defClass, String name, MethodType type, int modifiers) {
        int flagBit = (name.equals(CONSTRUCTOR_NAME) ? IS_CONSTRUCTOR : IS_METHOD);
        init(defClass, name, type, flagBit | (modifiers & RECOGNIZED_MODIFIERS));
    }
    public MemberName(Class<?> defClass, String name, MethodType type) {
        this(defClass, name, type, 0);
    }
    public boolean isResolved() {
        return (vmindex != VM_INDEX_UNINITIALIZED);
    }
    public boolean hasReceiverTypeDispatch() {
        return (isMethod() && getVMIndex() >= 0);
    }
    @Override
    public String toString() {
        if (isType())
            return type.toString();  
        StringBuilder buf = new StringBuilder();
        if (getDeclaringClass() != null) {
            buf.append(getName(clazz));
            buf.append('.');
        }
        String name = getName();
        buf.append(name == null ? "*" : name);
        Object type = getType();
        if (!isInvocable()) {
            buf.append('/');
            buf.append(type == null ? "*" : getName(type));
        } else {
            buf.append(type == null ? "(*)*" : getName(type));
        }
        return buf.toString();
    }
    private static String getName(Object obj) {
        if (obj instanceof Class<?>)
            return ((Class<?>)obj).getName();
        return String.valueOf(obj);
    }
     int getVMIndex() {
        if (!isResolved())
            throw newIllegalStateException("not resolved", this);
        return vmindex;
    }
    public IllegalAccessException makeAccessException(String message, Object from) {
        message = message + ": "+ toString();
        if (from != null)  message += ", from " + from;
        return new IllegalAccessException(message);
    }
    private String message() {
        if (isResolved())
            return "no access";
        else if (isConstructor())
            return "no such constructor";
        else if (isMethod())
            return "no such method";
        else
            return "no such field";
    }
    public ReflectiveOperationException makeAccessException() {
        String message = message() + ": "+ toString();
        if (isResolved())
            return new IllegalAccessException(message);
        else if (isConstructor())
            return new NoSuchMethodException(message);
        else if (isMethod())
            return new NoSuchMethodException(message);
        else
            return new NoSuchFieldException(message);
    }
     static Factory getFactory() {
        return Factory.INSTANCE;
    }
     static class Factory {
        private Factory() { } 
        static Factory INSTANCE = new Factory();
        private static int ALLOWED_FLAGS = SEARCH_ALL_SUPERS | ALL_KINDS;
        List<MemberName> getMembers(Class<?> defc,
                String matchName, Object matchType,
                int matchFlags, Class<?> lookupClass) {
            matchFlags &= ALLOWED_FLAGS;
            String matchSig = null;
            if (matchType != null) {
                matchSig = BytecodeDescriptor.unparse(matchType);
                if (matchSig.startsWith("("))
                    matchFlags &= ~(ALL_KINDS & ~IS_INVOCABLE);
                else
                    matchFlags &= ~(ALL_KINDS & ~IS_FIELD);
            }
            final int BUF_MAX = 0x2000;
            int len1 = matchName == null ? 10 : matchType == null ? 4 : 1;
            MemberName[] buf = newMemberBuffer(len1);
            int totalCount = 0;
            ArrayList<MemberName[]> bufs = null;
            int bufCount = 0;
            for (;;) {
                bufCount = MethodHandleNatives.getMembers(defc,
                        matchName, matchSig, matchFlags,
                        lookupClass,
                        totalCount, buf);
                if (bufCount <= buf.length) {
                    if (bufCount < 0)  bufCount = 0;
                    totalCount += bufCount;
                    break;
                }
                totalCount += buf.length;
                int excess = bufCount - buf.length;
                if (bufs == null)  bufs = new ArrayList<MemberName[]>(1);
                bufs.add(buf);
                int len2 = buf.length;
                len2 = Math.max(len2, excess);
                len2 = Math.max(len2, totalCount / 4);
                buf = newMemberBuffer(Math.min(BUF_MAX, len2));
            }
            ArrayList<MemberName> result = new ArrayList<MemberName>(totalCount);
            if (bufs != null) {
                for (MemberName[] buf0 : bufs) {
                    Collections.addAll(result, buf0);
                }
            }
            result.addAll(Arrays.asList(buf).subList(0, bufCount));
            if (matchType != null && matchType != matchSig) {
                for (Iterator<MemberName> it = result.iterator(); it.hasNext();) {
                    MemberName m = it.next();
                    if (!matchType.equals(m.getType()))
                        it.remove();
                }
            }
            return result;
        }
        boolean resolveInPlace(MemberName m, boolean searchSupers, Class<?> lookupClass) {
            if (m.name == null || m.type == null) {  
                Class<?> defc = m.getDeclaringClass();
                List<MemberName> choices = null;
                if (m.isMethod())
                    choices = getMethods(defc, searchSupers, m.name, (MethodType) m.type, lookupClass);
                else if (m.isConstructor())
                    choices = getConstructors(defc, lookupClass);
                else if (m.isField())
                    choices = getFields(defc, searchSupers, m.name, (Class<?>) m.type, lookupClass);
                if (choices == null || choices.size() != 1)
                    return false;
                if (m.name == null)  m.name = choices.get(0).name;
                if (m.type == null)  m.type = choices.get(0).type;
            }
            MethodHandleNatives.resolve(m, lookupClass);
            if (m.isResolved())  return true;
            int matchFlags = m.flags | (searchSupers ? SEARCH_ALL_SUPERS : 0);
            String matchSig = m.getSignature();
            MemberName[] buf = { m };
            int n = MethodHandleNatives.getMembers(m.getDeclaringClass(),
                    m.getName(), matchSig, matchFlags, lookupClass, 0, buf);
            if (n != 1)  return false;
            return m.isResolved();
        }
        public MemberName resolveOrNull(MemberName m, boolean searchSupers, Class<?> lookupClass) {
            MemberName result = m.clone();
            if (resolveInPlace(result, searchSupers, lookupClass))
                return result;
            return null;
        }
        public
        <NoSuchMemberException extends ReflectiveOperationException>
        MemberName resolveOrFail(MemberName m, boolean searchSupers, Class<?> lookupClass,
                                 Class<NoSuchMemberException> nsmClass)
                throws IllegalAccessException, NoSuchMemberException {
            MemberName result = resolveOrNull(m, searchSupers, lookupClass);
            if (result != null)
                return result;
            ReflectiveOperationException ex = m.makeAccessException();
            if (ex instanceof IllegalAccessException)  throw (IllegalAccessException) ex;
            throw nsmClass.cast(ex);
        }
        public List<MemberName> getMethods(Class<?> defc, boolean searchSupers,
                Class<?> lookupClass) {
            return getMethods(defc, searchSupers, null, null, lookupClass);
        }
        public List<MemberName> getMethods(Class<?> defc, boolean searchSupers,
                String name, MethodType type, Class<?> lookupClass) {
            int matchFlags = IS_METHOD | (searchSupers ? SEARCH_ALL_SUPERS : 0);
            return getMembers(defc, name, type, matchFlags, lookupClass);
        }
        public List<MemberName> getConstructors(Class<?> defc, Class<?> lookupClass) {
            return getMembers(defc, null, null, IS_CONSTRUCTOR, lookupClass);
        }
        public List<MemberName> getFields(Class<?> defc, boolean searchSupers,
                Class<?> lookupClass) {
            return getFields(defc, searchSupers, null, null, lookupClass);
        }
        public List<MemberName> getFields(Class<?> defc, boolean searchSupers,
                String name, Class<?> type, Class<?> lookupClass) {
            int matchFlags = IS_FIELD | (searchSupers ? SEARCH_ALL_SUPERS : 0);
            return getMembers(defc, name, type, matchFlags, lookupClass);
        }
        public List<MemberName> getNestedTypes(Class<?> defc, boolean searchSupers,
                Class<?> lookupClass) {
            int matchFlags = IS_TYPE | (searchSupers ? SEARCH_ALL_SUPERS : 0);
            return getMembers(defc, null, null, matchFlags, lookupClass);
        }
        private static MemberName[] newMemberBuffer(int length) {
            MemberName[] buf = new MemberName[length];
            for (int i = 0; i < length; i++)
                buf[i] = new MemberName();
            return buf;
        }
    }
}
