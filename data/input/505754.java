public final class Prototype implements Comparable<Prototype> {
    private static final HashMap<String, Prototype> internTable =
        new HashMap<String, Prototype>(500);
    private final String descriptor;
    private final Type returnType;
    private final StdTypeList parameterTypes;
    private StdTypeList parameterFrameTypes;
    public static Prototype intern(String descriptor) {
        if (descriptor == null) {
            throw new NullPointerException("descriptor == null");
        }
        Prototype result = internTable.get(descriptor);
        if (result != null) {
            return result;
        }
        Type[] params = makeParameterArray(descriptor);
        int paramCount = 0;
        int at = 1;
        for (;;) {
            int startAt = at;
            char c = descriptor.charAt(at);
            if (c == ')') {
                at++;
                break;
            }
            while (c == '[') {
                at++;
                c = descriptor.charAt(at);
            }
            if (c == 'L') {
                int endAt = descriptor.indexOf(';', at);
                if (endAt == -1) {
                    throw new IllegalArgumentException("bad descriptor");
                }
                at = endAt + 1;
            } else {
                at++;
            }
            params[paramCount] =
                Type.intern(descriptor.substring(startAt, at));
            paramCount++;
        }
        Type returnType = Type.internReturnType(descriptor.substring(at));
        StdTypeList parameterTypes = new StdTypeList(paramCount);
        for (int i = 0; i < paramCount; i++) {
            parameterTypes.set(i, params[i]);
        }
        result = new Prototype(descriptor, returnType, parameterTypes);
        return putIntern(result);
    }
    private static Type[] makeParameterArray(String descriptor) {
        int length = descriptor.length();
        if (descriptor.charAt(0) != '(') {
            throw new IllegalArgumentException("bad descriptor");
        }
        int closeAt = 0;
        int maxParams = 0;
        for (int i = 1; i < length; i++) {
            char c = descriptor.charAt(i);
            if (c == ')') {
                closeAt = i;
                break;
            }
            if ((c >= 'A') && (c <= 'Z')) {
                maxParams++;
            }
        }
        if ((closeAt == 0) || (closeAt == (length - 1))) {
            throw new IllegalArgumentException("bad descriptor");
        }
        if (descriptor.indexOf(')', closeAt + 1) != -1) {
            throw new IllegalArgumentException("bad descriptor");
        }
        return new Type[maxParams];
    }
    public static Prototype intern(String descriptor, Type definer,
            boolean isStatic, boolean isInit) {
        Prototype base = intern(descriptor);
        if (isStatic) {
            return base;
        }
        if (isInit) {
            definer = definer.asUninitialized(Integer.MAX_VALUE);
        }
        return base.withFirstParameter(definer);
    }
    public static Prototype internInts(Type returnType, int count) {
        StringBuffer sb = new StringBuffer(100);
        sb.append('(');
        for (int i = 0; i < count; i++) {
            sb.append('I');
        }
        sb.append(')');
        sb.append(returnType.getDescriptor());
        return intern(sb.toString());
    }
    private Prototype(String descriptor, Type returnType,
            StdTypeList parameterTypes) {
        if (descriptor == null) {
            throw new NullPointerException("descriptor == null");
        }
        if (returnType == null) {
            throw new NullPointerException("returnType == null");
        }
        if (parameterTypes == null) {
            throw new NullPointerException("parameterTypes == null");
        }
        this.descriptor = descriptor;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.parameterFrameTypes = null;
    }
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Prototype)) {
            return false;
        }
        return descriptor.equals(((Prototype) other).descriptor);
    }
    @Override
    public int hashCode() {
        return descriptor.hashCode();
    }
    public int compareTo(Prototype other) {
        if (this == other) {
            return 0;
        }
        int result = returnType.compareTo(other.returnType);
        if (result != 0) {
            return result;
        }
        int thisSize = parameterTypes.size();
        int otherSize = other.parameterTypes.size();
        int size = Math.min(thisSize, otherSize);
        for (int i = 0; i < size; i++) {
            Type thisType = parameterTypes.get(i);
            Type otherType = other.parameterTypes.get(i);
            result = thisType.compareTo(otherType);
            if (result != 0) {
                return result;
            }
        }
        if (thisSize < otherSize) {
            return -1;
        } else if (thisSize > otherSize) {
            return 1;
        } else {
            return 0;
        }
    }
    @Override
    public String toString() {
        return descriptor;
    }
    public String getDescriptor() {
        return descriptor;
    }
    public Type getReturnType() {
        return returnType;
    }
    public StdTypeList getParameterTypes() {
        return parameterTypes;
    }
    public StdTypeList getParameterFrameTypes() {
        if (parameterFrameTypes == null) {
            int sz = parameterTypes.size();
            StdTypeList list = new StdTypeList(sz);
            boolean any = false;
            for (int i = 0; i < sz; i++) {
                Type one = parameterTypes.get(i);
                if (one.isIntlike()) {
                    any = true;
                    one = Type.INT;
                }
                list.set(i, one);
            }
            parameterFrameTypes = any ? list : parameterTypes;
        }
        return parameterFrameTypes;
    }
    public Prototype withFirstParameter(Type param) {
        String newDesc = "(" + param.getDescriptor() + descriptor.substring(1);
        StdTypeList newParams = parameterTypes.withFirst(param);
        newParams.setImmutable();
        Prototype result =
            new Prototype(newDesc, returnType, newParams);
        return putIntern(result);
    }
    private static Prototype putIntern(Prototype desc) {
        synchronized (internTable) {
            String descriptor = desc.getDescriptor();
            Prototype already = internTable.get(descriptor);
            if (already != null) {
                return already;
            }
            internTable.put(descriptor, desc);
            return desc;
        }
    }
}
