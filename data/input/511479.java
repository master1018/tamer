public final class AnnotationFactory implements InvocationHandler, Serializable {
    private static final transient 
    Map<Class<? extends Annotation>, AnnotationMember[]> 
    cache = new WeakHashMap<Class<? extends Annotation>, AnnotationMember[]>();
    public static AnnotationMember[] getElementsDescription(Class<? extends Annotation> annotationType ) {
        AnnotationMember[] desc = cache.get(annotationType);
        if (desc == null) {
            if (!annotationType.isAnnotation()) {
                throw new IllegalArgumentException("Type is not annotation: " 
                        + annotationType.getName());
            }
            Method[] m = annotationType.getDeclaredMethods();
            desc = new AnnotationMember[m.length];
            int idx = 0;
            for(Method element : m) {
                String name = element.getName();
                Class<?> type = element.getReturnType();
                try {
                    desc[idx] = new AnnotationMember(name, 
                            element.getDefaultValue(), type, element);
                } catch (Throwable t) {
                    desc[idx] = new AnnotationMember(name, t, type, element);
                }
                idx++;
            }
            cache.put(annotationType, desc);
        }
        return desc;
    }
    public static Annotation createAnnotation(
            Class<? extends Annotation> annotationType, 
            AnnotationMember[] elements) 
    {
        AnnotationFactory antn = new AnnotationFactory(annotationType, elements); 
        return (Annotation)Proxy.newProxyInstance( annotationType.getClassLoader(), 
                new Class[]{annotationType}, antn);
    }
    private final Class<? extends Annotation> klazz;
    private AnnotationMember[] elements;
    private AnnotationFactory(Class<? extends Annotation> klzz, AnnotationMember[] values) {
        klazz = klzz;
        AnnotationMember[] defs = getElementsDescription(klazz);
        if (values == null) {
            elements = defs;
        } else {
            elements = new AnnotationMember[defs.length];
            next: for (int i = elements.length - 1; i >= 0; i-- ){
                for (AnnotationMember val : values){
                    if (val.name.equals(defs[i].name)) {
                        elements[i] = val.setDefinition(defs[i]);
                        continue next;
                    }
                }
                elements[i] = defs[i];
            }
        }
    }
    private void readObject(ObjectInputStream os) throws IOException, 
    ClassNotFoundException {
        os.defaultReadObject();
        AnnotationMember[] defs = getElementsDescription(klazz);
        AnnotationMember[] old = elements;
        List<AnnotationMember> merged = new ArrayList<AnnotationMember>(
                defs.length + old.length);
        nextOld: for (AnnotationMember el1 : old) {
            for (AnnotationMember el2 : defs) {
                if (el2.name.equals(el1.name)) {
                    continue nextOld;
                }
            }
            merged.add(el1); 
        }
        nextNew: for (AnnotationMember def : defs){
            for (AnnotationMember val : old){
                if (val.name.equals(def.name)) {
                    merged.add(val.setDefinition(def));
                    continue nextNew;
                }
            }
            merged.add(def); 
        }  
        elements = merged.toArray(new AnnotationMember[merged.size()]);
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!klazz.isInstance(obj)) {
            return false;
        }
        Object handler = null;
        if (Proxy.isProxyClass(obj.getClass()) 
                && (handler = Proxy.getInvocationHandler(obj)) instanceof AnnotationFactory ) {
            AnnotationFactory other = (AnnotationFactory) handler;
            if (elements.length != other.elements.length) {
                return false;
            }
            next: for (AnnotationMember el1 : elements){
                for (AnnotationMember el2 : other.elements) {
                    if (el1.equals(el2)) {
                        continue next;
                    }
                }
                return false;
            }
            return true;
        } else {
            for (final AnnotationMember el : elements) {
                if (el.tag == ERROR) {
                    return false;
                }
                try {
                    if (!el.definingMethod.isAccessible()) {
                        AccessController.doPrivileged(new PrivilegedAction<Object>(){
                            public Object run() {
                                try {
                                    el.definingMethod.setAccessible(true);
                                } catch (Exception ignore) {}
                                return null;
                            }
                        });
                    }
                    Object otherValue = el.definingMethod.invoke(obj);
                    if (otherValue != null ) {
                        if (el.tag == ARRAY) { 
                            if (!el.equalArrayValue(otherValue)) {
                                return false;
                            }
                        } else {
                            if (!el.value.equals(otherValue)) {
                                return false;
                            }
                        }
                    } else if (el.value != AnnotationMember.NO_VALUE) {
                        return false;
                    }
                } catch (Throwable e) {
                    return false;
                }
            }
            return true;
        }
    }
    public int hashCode() {
        int hash = 0;
        for (AnnotationMember element : elements) {
            hash += element.hashCode();
        }
        return hash;
    }
    public String toString() {
        String res = "@" + klazz.getName() + "(";
        for(int i = 0; i < elements.length; i++) {
            if ( i != 0 ) {
                res += ", ";    
            }
            res += elements[i].toString();;
        }
        return res + ")";
    }
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        String name = method.getName();
        Class[] params = method.getParameterTypes();
        if (params.length == 0) {
            if ("annotationType".equals(name)) {
                return klazz;
            } else if ("toString".equals(name)) {
                return toString();
            } else if ("hashCode".equals(name)) {
                return hashCode();
            }
            AnnotationMember element = null;
            for (AnnotationMember el : elements) {
                if (name.equals(el.name)) {
                    element = el;
                    break;
                }                
            }
            if (element == null || !method.equals(element.definingMethod)) {
                throw new IllegalArgumentException(method.toString());
            } else {
                Object value = element.validateValue();
                if (value == null) {
                    throw new IncompleteAnnotationException(klazz, name);
                }
                return value;
            }
        } else if (params.length == 1 && params[0] == Object.class && "equals".equals(name)){
            return Boolean.valueOf(equals(args[0]));
        }
        throw new IllegalArgumentException(
                "Invalid method for annotation type: " + method);
    }
}
