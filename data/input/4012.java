class AnnotationProxyMaker {
    private final AptEnv env;
    private final Attribute.Compound attrs;
    private final Class<? extends Annotation> annoType;
    private AnnotationProxyMaker(AptEnv env,
                                 Attribute.Compound attrs,
                                 Class<? extends Annotation> annoType) {
        this.env = env;
        this.attrs = attrs;
        this.annoType = annoType;
    }
    public static <A extends Annotation> A generateAnnotation(
            AptEnv env, Attribute.Compound attrs, Class<A> annoType) {
        AnnotationProxyMaker apm = new AnnotationProxyMaker(env, attrs, annoType);
        return annoType.cast(apm.generateAnnotation());
    }
    private Annotation generateAnnotation() {
        return AnnotationParser.annotationForMap(annoType,
                                                 getAllReflectedValues());
    }
    private Map<String, Object> getAllReflectedValues() {
        Map<String, Object> res = new LinkedHashMap<String, Object>();
        for (Map.Entry<MethodSymbol, Attribute> entry :
                                                  getAllValues().entrySet()) {
            MethodSymbol meth = entry.getKey();
            Object value = generateValue(meth, entry.getValue());
            if (value != null) {
                res.put(meth.name.toString(), value);
            } else {
            }
        }
        return res;
    }
    private Map<MethodSymbol, Attribute> getAllValues() {
        Map<MethodSymbol, Attribute> res =
            new LinkedHashMap<MethodSymbol, Attribute>();
        ClassSymbol sym = (ClassSymbol) attrs.type.tsym;
        for (Scope.Entry e = sym.members().elems; e != null; e = e.sibling) {
            if (e.sym.kind == Kinds.MTH) {
                MethodSymbol m = (MethodSymbol) e.sym;
                Attribute def = m.defaultValue;
                if (def != null) {
                    res.put(m, def);
                }
            }
        }
        for (Pair<MethodSymbol, Attribute> p : attrs.values) {
            res.put(p.fst, p.snd);
        }
        return res;
    }
    private Object generateValue(MethodSymbol meth, Attribute attr) {
        ValueVisitor vv = new ValueVisitor(meth);
        return vv.getValue(attr);
    }
    private class ValueVisitor implements Attribute.Visitor {
        private MethodSymbol meth;      
        private Class<?> runtimeType;   
        private Object value;           
        ValueVisitor(MethodSymbol meth) {
            this.meth = meth;
        }
        Object getValue(Attribute attr) {
            Method method;              
            try {
                method = annoType.getMethod(meth.name.toString());
            } catch (NoSuchMethodException e) {
                return null;
            }
            runtimeType = method.getReturnType();
            attr.accept(this);
            if (!(value instanceof ExceptionProxy) &&
                !AnnotationType.invocationHandlerReturnType(runtimeType)
                                                        .isInstance(value)) {
                typeMismatch(method, attr);
            }
            return value;
        }
        public void visitConstant(Attribute.Constant c) {
            value = Constants.decodeConstant(c.value, c.type);
        }
        public void visitClass(Attribute.Class c) {
            value = new MirroredTypeExceptionProxy(
                                env.typeMaker.getType(c.type));
        }
        public void visitArray(Attribute.Array a) {
            Type elemtype = env.jctypes.elemtype(a.type);
            if (elemtype.tsym == env.symtab.classType.tsym) {   
                ArrayList<TypeMirror> elems = new ArrayList<TypeMirror>();
                for (int i = 0; i < a.values.length; i++) {
                    Type elem = ((Attribute.Class) a.values[i]).type;
                    elems.add(env.typeMaker.getType(elem));
                }
                value = new MirroredTypesExceptionProxy(elems);
            } else {
                int len = a.values.length;
                Class<?> runtimeTypeSaved = runtimeType;
                runtimeType = runtimeType.getComponentType();
                try {
                    Object res = Array.newInstance(runtimeType, len);
                    for (int i = 0; i < len; i++) {
                        a.values[i].accept(this);
                        if (value == null || value instanceof ExceptionProxy) {
                            return;
                        }
                        try {
                            Array.set(res, i, value);
                        } catch (IllegalArgumentException e) {
                            value = null;       
                            return;
                        }
                    }
                    value = res;
                } finally {
                    runtimeType = runtimeTypeSaved;
                }
            }
        }
        @SuppressWarnings({"unchecked", "rawtypes"})
        public void visitEnum(Attribute.Enum e) {
            if (runtimeType.isEnum()) {
                String constName = e.value.toString();
                try {
                    value = Enum.valueOf((Class)runtimeType, constName);
                } catch (IllegalArgumentException ex) {
                    value = new EnumConstantNotPresentExceptionProxy(
                                                        (Class<Enum<?>>)runtimeType, constName);
                }
            } else {
                value = null;   
            }
        }
        public void visitCompound(Attribute.Compound c) {
            try {
                Class<? extends Annotation> nested =
                    runtimeType.asSubclass(Annotation.class);
                value = generateAnnotation(env, c, nested);
            } catch (ClassCastException ex) {
                value = null;   
            }
        }
        public void visitError(Attribute.Error e) {
            value = null;       
        }
        private void typeMismatch(Method method, final Attribute attr) {
            class AnnotationTypeMismatchExceptionProxy extends ExceptionProxy {
                private static final long serialVersionUID = 8473323277815075163L;
                transient final Method method;
                AnnotationTypeMismatchExceptionProxy(Method method) {
                    this.method = method;
                }
                public String toString() {
                    return "<error>";   
                }
                protected RuntimeException generateException() {
                    return new AnnotationTypeMismatchException(method,
                                attr.type.toString());
                }
            }
            value = new AnnotationTypeMismatchExceptionProxy(method);
        }
    }
    private static final class MirroredTypeExceptionProxy extends ExceptionProxy {
        private static final long serialVersionUID = 6662035281599933545L;
        private MirroredTypeException ex;
        MirroredTypeExceptionProxy(TypeMirror t) {
            ex = new MirroredTypeException(t);
        }
        public String toString() {
            return ex.getQualifiedName();
        }
        public int hashCode() {
            TypeMirror t = ex.getTypeMirror();
            return (t != null)
                    ? t.hashCode()
                    : ex.getQualifiedName().hashCode();
        }
        public boolean equals(Object obj) {
            TypeMirror t = ex.getTypeMirror();
            return t != null &&
                   obj instanceof MirroredTypeExceptionProxy &&
                   t.equals(
                        ((MirroredTypeExceptionProxy) obj).ex.getTypeMirror());
        }
        protected RuntimeException generateException() {
            return (RuntimeException) ex.fillInStackTrace();
        }
    }
    private static final class MirroredTypesExceptionProxy extends ExceptionProxy {
        private static final long serialVersionUID = -6670822532616693951L;
        private MirroredTypesException ex;
        MirroredTypesExceptionProxy(Collection<TypeMirror> ts) {
            ex = new MirroredTypesException(ts);
        }
        public String toString() {
            return ex.getQualifiedNames().toString();
        }
        public int hashCode() {
            Collection<TypeMirror> ts = ex.getTypeMirrors();
            return (ts != null)
                    ? ts.hashCode()
                    : ex.getQualifiedNames().hashCode();
        }
        public boolean equals(Object obj) {
            Collection<TypeMirror> ts = ex.getTypeMirrors();
            return ts != null &&
                   obj instanceof MirroredTypesExceptionProxy &&
                   ts.equals(
                      ((MirroredTypesExceptionProxy) obj).ex.getTypeMirrors());
        }
        protected RuntimeException generateException() {
            return (RuntimeException) ex.fillInStackTrace();
        }
    }
}
