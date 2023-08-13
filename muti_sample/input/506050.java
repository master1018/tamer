public final class Constructor<T> extends AccessibleObject implements GenericDeclaration,
        Member {
    Class<T> declaringClass;
    Class<?>[] parameterTypes;
    Class<?>[] exceptionTypes;
    ListOfTypes genericExceptionTypes;
    ListOfTypes genericParameterTypes;
    TypeVariable<Constructor<T>>[] formalTypeParameters;
    private volatile boolean genericTypesAreInitialized = false;
    private synchronized void initGenericTypes() {
        if (!genericTypesAreInitialized) {
            String signatureAttribute = getSignatureAttribute();
            GenericSignatureParser parser = new GenericSignatureParser(
                    VMStack.getCallingClassLoader2());
            parser.parseForConstructor(this, signatureAttribute);
            formalTypeParameters = parser.formalTypeParameters;
            genericParameterTypes = parser.parameterTypes;
            genericExceptionTypes = parser.exceptionTypes;
            genericTypesAreInitialized = true;
        }
    }
    int slot;
    private Constructor(){
    }
    private Constructor (Class<T> declaringClass, Class<?>[] ptypes, Class<?>[] extypes, int slot){
        this.declaringClass = declaringClass;
        this.parameterTypes = ptypes;
        this.exceptionTypes = extypes;          
        this.slot = slot;
    }
    @Override  String getSignatureAttribute() {
        Object[] annotation = getSignatureAnnotation(declaringClass, slot);
        if (annotation == null) {
            return null;
        }
        return StringUtils.combineStrings(annotation);
    }
    native private Object[] getSignatureAnnotation(Class declaringClass,
            int slot);
    public TypeVariable<Constructor<T>>[] getTypeParameters() {
        initGenericTypes();
        return formalTypeParameters.clone();
    }
    public String toGenericString() {
        StringBuilder sb = new StringBuilder(80);
        initGenericTypes();
        int modifier = getModifiers();
        if (modifier != 0) {
            sb.append(Modifier.toString(modifier & ~Modifier.VARARGS)).append(' ');
        }
        if (formalTypeParameters != null && formalTypeParameters.length > 0) {
            sb.append('<');
            for (int i = 0; i < formalTypeParameters.length; i++) {
                appendGenericType(sb, formalTypeParameters[i]);
                if (i < formalTypeParameters.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("> ");
        }
        appendArrayType(sb, getDeclaringClass());
        sb.append('(');
        appendArrayGenericType(sb, 
                Types.getClonedTypeArray(genericParameterTypes));
        sb.append(')');
        Type[] genericExceptionTypeArray = 
                Types.getClonedTypeArray(genericExceptionTypes);
        if (genericExceptionTypeArray.length > 0) {
            sb.append(" throws ");
            appendArrayGenericType(sb, genericExceptionTypeArray);
        }
        return sb.toString();
    }
    public Type[] getGenericParameterTypes() {
        initGenericTypes();
        return Types.getClonedTypeArray(genericParameterTypes);
    }
    public Type[] getGenericExceptionTypes() {
        initGenericTypes();
        return Types.getClonedTypeArray(genericExceptionTypes);
    }
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return getDeclaredAnnotations(declaringClass, slot);
    }
    native private Annotation[] getDeclaredAnnotations(Class declaringClass,
        int slot);
    public Annotation[][] getParameterAnnotations() {
        Annotation[][] parameterAnnotations
                = getParameterAnnotations(declaringClass, slot);
        if (parameterAnnotations.length == 0) {
            return Method.noAnnotations(parameterTypes.length);
        }
        return parameterAnnotations;
    }
    native private Annotation[][] getParameterAnnotations(Class declaringClass,
        int slot);
    public boolean isVarArgs() {
        int mods = getConstructorModifiers(declaringClass, slot);
        return (mods & Modifier.VARARGS) != 0;
    }
    public boolean isSynthetic() {
        int mods = getConstructorModifiers(declaringClass, slot);
        return (mods & Modifier.SYNTHETIC) != 0;
    }
    @Override
    public boolean equals(Object object) {
        return object instanceof Constructor && toString().equals(object.toString());
    }
    public Class<T> getDeclaringClass() {
        return declaringClass;
    }
    public Class<?>[] getExceptionTypes() {
        if (exceptionTypes == null)
            return new Class[0];
        return exceptionTypes.clone();
    }
    public int getModifiers() {
        return getConstructorModifiers(declaringClass, slot);
    }
    private native int getConstructorModifiers(Class<T> declaringClass, int slot);
    public String getName() {
        return declaringClass.getName();
    }
    public Class<?>[] getParameterTypes() {
        return parameterTypes.clone();
    }
    @SuppressWarnings("unused")
    private String getSignature() {
        StringBuilder result = new StringBuilder();
        result.append('(');
        for(int i = 0; i < parameterTypes.length; i++) {            
            result.append(getSignature(parameterTypes[i]));
        }
        result.append(")V");
        return result.toString();
    }
    @Override
    public int hashCode() {
        return declaringClass.getName().hashCode();
    }
    public T newInstance(Object... args) throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        return constructNative (args, declaringClass, parameterTypes, slot, flag);
    }
    private native T constructNative(Object[] args, Class<T> declaringClass,
            Class<?>[] parameterTypes, int slot,
            boolean noAccessCheck) throws InstantiationException, IllegalAccessException,
            InvocationTargetException;
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(Modifier.toString(getModifiers()));
        if (result.length() != 0)
            result.append(' ');
        result.append(declaringClass.getName());
        result.append("(");
        result.append(toString(parameterTypes));
        result.append(")");
        if (exceptionTypes != null && exceptionTypes.length != 0) {
            result.append(" throws ");
            result.append(toString(exceptionTypes));
        }
        return result.toString();
    }
}
