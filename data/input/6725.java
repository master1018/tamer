    class Constructor<T> extends AccessibleObject implements
                                                    GenericDeclaration,
                                                    Member {
    private Class<T>            clazz;
    private int                 slot;
    private Class<?>[]          parameterTypes;
    private Class<?>[]          exceptionTypes;
    private int                 modifiers;
    private transient String    signature;
    private transient ConstructorRepository genericInfo;
    private byte[]              annotations;
    private byte[]              parameterAnnotations;
    private GenericsFactory getFactory() {
        return CoreReflectionFactory.make(this, ConstructorScope.make(this));
    }
    private ConstructorRepository getGenericInfo() {
        if (genericInfo == null) {
            genericInfo =
                ConstructorRepository.make(getSignature(),
                                           getFactory());
        }
        return genericInfo; 
    }
    private volatile ConstructorAccessor constructorAccessor;
    private Constructor<T>      root;
    Constructor(Class<T> declaringClass,
                Class<?>[] parameterTypes,
                Class<?>[] checkedExceptions,
                int modifiers,
                int slot,
                String signature,
                byte[] annotations,
                byte[] parameterAnnotations)
    {
        this.clazz = declaringClass;
        this.parameterTypes = parameterTypes;
        this.exceptionTypes = checkedExceptions;
        this.modifiers = modifiers;
        this.slot = slot;
        this.signature = signature;
        this.annotations = annotations;
        this.parameterAnnotations = parameterAnnotations;
    }
    Constructor<T> copy() {
        Constructor<T> res = new Constructor<>(clazz,
                                                parameterTypes,
                                                exceptionTypes, modifiers, slot,
                                                signature,
                                                annotations,
                                                parameterAnnotations);
        res.root = this;
        res.constructorAccessor = constructorAccessor;
        return res;
    }
    public Class<T> getDeclaringClass() {
        return clazz;
    }
    public String getName() {
        return getDeclaringClass().getName();
    }
    public int getModifiers() {
        return modifiers;
    }
    public TypeVariable<Constructor<T>>[] getTypeParameters() {
      if (getSignature() != null) {
        return (TypeVariable<Constructor<T>>[])getGenericInfo().getTypeParameters();
      } else
          return (TypeVariable<Constructor<T>>[])new TypeVariable[0];
    }
    public Class<?>[] getParameterTypes() {
        return (Class<?>[]) parameterTypes.clone();
    }
    public Type[] getGenericParameterTypes() {
        if (getSignature() != null)
            return getGenericInfo().getParameterTypes();
        else
            return getParameterTypes();
    }
    public Class<?>[] getExceptionTypes() {
        return (Class<?>[])exceptionTypes.clone();
    }
      public Type[] getGenericExceptionTypes() {
          Type[] result;
          if (getSignature() != null &&
              ( (result = getGenericInfo().getExceptionTypes()).length > 0  ))
              return result;
          else
              return getExceptionTypes();
      }
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Constructor) {
            Constructor<?> other = (Constructor<?>)obj;
            if (getDeclaringClass() == other.getDeclaringClass()) {
                Class<?>[] params1 = parameterTypes;
                Class<?>[] params2 = other.parameterTypes;
                if (params1.length == params2.length) {
                    for (int i = 0; i < params1.length; i++) {
                        if (params1[i] != params2[i])
                            return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }
    public int hashCode() {
        return getDeclaringClass().getName().hashCode();
    }
    public String toString() {
        try {
            StringBuffer sb = new StringBuffer();
            int mod = getModifiers() & Modifier.constructorModifiers();
            if (mod != 0) {
                sb.append(Modifier.toString(mod) + " ");
            }
            sb.append(Field.getTypeName(getDeclaringClass()));
            sb.append("(");
            Class<?>[] params = parameterTypes; 
            for (int j = 0; j < params.length; j++) {
                sb.append(Field.getTypeName(params[j]));
                if (j < (params.length - 1))
                    sb.append(",");
            }
            sb.append(")");
            Class<?>[] exceptions = exceptionTypes; 
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append(exceptions[k].getName());
                    if (k < (exceptions.length - 1))
                        sb.append(",");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }
    public String toGenericString() {
        try {
            StringBuilder sb = new StringBuilder();
            int mod = getModifiers() & Modifier.constructorModifiers();
            if (mod != 0) {
                sb.append(Modifier.toString(mod) + " ");
            }
            TypeVariable<?>[] typeparms = getTypeParameters();
            if (typeparms.length > 0) {
                boolean first = true;
                sb.append("<");
                for(TypeVariable<?> typeparm: typeparms) {
                    if (!first)
                        sb.append(",");
                    sb.append(typeparm.toString());
                    first = false;
                }
                sb.append("> ");
            }
            sb.append(Field.getTypeName(getDeclaringClass()));
            sb.append("(");
            Type[] params = getGenericParameterTypes();
            for (int j = 0; j < params.length; j++) {
                String param = (params[j] instanceof Class<?>)?
                    Field.getTypeName((Class<?>)params[j]):
                    (params[j].toString());
                if (isVarArgs() && (j == params.length - 1)) 
                    param = param.replaceFirst("\\[\\]$", "...");
                sb.append(param);
                if (j < (params.length - 1))
                    sb.append(",");
            }
            sb.append(")");
            Type[] exceptions = getGenericExceptionTypes();
            if (exceptions.length > 0) {
                sb.append(" throws ");
                for (int k = 0; k < exceptions.length; k++) {
                    sb.append((exceptions[k] instanceof Class)?
                              ((Class<?>)exceptions[k]).getName():
                              exceptions[k].toString());
                    if (k < (exceptions.length - 1))
                        sb.append(",");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }
    public T newInstance(Object ... initargs)
        throws InstantiationException, IllegalAccessException,
               IllegalArgumentException, InvocationTargetException
    {
        if (!override) {
            if (!Reflection.quickCheckMemberAccess(clazz, modifiers)) {
                Class<?> caller = Reflection.getCallerClass(2);
                checkAccess(caller, clazz, null, modifiers);
            }
        }
        if ((clazz.getModifiers() & Modifier.ENUM) != 0)
            throw new IllegalArgumentException("Cannot reflectively create enum objects");
        ConstructorAccessor ca = constructorAccessor;   
        if (ca == null) {
            ca = acquireConstructorAccessor();
        }
        return (T) ca.newInstance(initargs);
    }
    public boolean isVarArgs() {
        return (getModifiers() & Modifier.VARARGS) != 0;
    }
    public boolean isSynthetic() {
        return Modifier.isSynthetic(getModifiers());
    }
    private ConstructorAccessor acquireConstructorAccessor() {
        ConstructorAccessor tmp = null;
        if (root != null) tmp = root.getConstructorAccessor();
        if (tmp != null) {
            constructorAccessor = tmp;
        } else {
            tmp = reflectionFactory.newConstructorAccessor(this);
            setConstructorAccessor(tmp);
        }
        return tmp;
    }
    ConstructorAccessor getConstructorAccessor() {
        return constructorAccessor;
    }
    void setConstructorAccessor(ConstructorAccessor accessor) {
        constructorAccessor = accessor;
        if (root != null) {
            root.setConstructorAccessor(accessor);
        }
    }
    int getSlot() {
        return slot;
    }
   String getSignature() {
            return signature;
   }
    byte[] getRawAnnotations() {
        return annotations;
    }
    byte[] getRawParameterAnnotations() {
        return parameterAnnotations;
    }
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        if (annotationClass == null)
            throw new NullPointerException();
        return (T) declaredAnnotations().get(annotationClass);
    }
    public Annotation[] getDeclaredAnnotations()  {
        return AnnotationParser.toArray(declaredAnnotations());
    }
    private transient Map<Class<? extends Annotation>, Annotation> declaredAnnotations;
    private synchronized  Map<Class<? extends Annotation>, Annotation> declaredAnnotations() {
        if (declaredAnnotations == null) {
            declaredAnnotations = AnnotationParser.parseAnnotations(
                annotations, sun.misc.SharedSecrets.getJavaLangAccess().
                getConstantPool(getDeclaringClass()),
                getDeclaringClass());
        }
        return declaredAnnotations;
    }
    public Annotation[][] getParameterAnnotations() {
        int numParameters = parameterTypes.length;
        if (parameterAnnotations == null)
            return new Annotation[numParameters][0];
        Annotation[][] result = AnnotationParser.parseParameterAnnotations(
            parameterAnnotations,
            sun.misc.SharedSecrets.getJavaLangAccess().
                getConstantPool(getDeclaringClass()),
            getDeclaringClass());
        if (result.length != numParameters) {
            Class<?> declaringClass = getDeclaringClass();
            if (declaringClass.isEnum() ||
                declaringClass.isAnonymousClass() ||
                declaringClass.isLocalClass() )
                ; 
            else {
                if (!declaringClass.isMemberClass() || 
                    (declaringClass.isMemberClass() &&
                     ((declaringClass.getModifiers() & Modifier.STATIC) == 0)  &&
                     result.length + 1 != numParameters) ) {
                    throw new AnnotationFormatError(
                              "Parameter annotations don't match number of parameters");
                }
            }
        }
        return result;
    }
}
