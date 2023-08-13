public abstract class Attribute implements AnnotationValue {
    public Type type;
    public Attribute(Type type) {
        this.type = type;
    }
    public abstract void accept(Visitor v);
    public Object getValue() {
        throw new UnsupportedOperationException();
    }
    public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
        throw new UnsupportedOperationException();
    }
    public static class Constant extends Attribute {
        public final Object value;
        public void accept(Visitor v) { v.visitConstant(this); }
        public Constant(Type type, Object value) {
            super(type);
            this.value = value;
        }
        public String toString() {
            return Constants.format(value, type);
        }
        public Object getValue() {
            return Constants.decode(value, type);
        }
        public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
            if (value instanceof String)
                return v.visitString((String) value, p);
            if (value instanceof Integer) {
                int i = (Integer) value;
                switch (type.tag) {
                case BOOLEAN:   return v.visitBoolean(i != 0, p);
                case CHAR:      return v.visitChar((char) i, p);
                case BYTE:      return v.visitByte((byte) i, p);
                case SHORT:     return v.visitShort((short) i, p);
                case INT:       return v.visitInt(i, p);
                }
            }
            switch (type.tag) {
            case LONG:          return v.visitLong((Long) value, p);
            case FLOAT:         return v.visitFloat((Float) value, p);
            case DOUBLE:        return v.visitDouble((Double) value, p);
            }
            throw new AssertionError("Bad annotation element value: " + value);
        }
    }
    public static class Class extends Attribute {
        public final Type type;
        public void accept(Visitor v) { v.visitClass(this); }
        public Class(Types types, Type type) {
            super(makeClassType(types, type));
            this.type = type;
        }
        static Type makeClassType(Types types, Type type) {
            Type arg = type.isPrimitive()
                ? types.boxedClass(type).type
                : types.erasure(type);
            return new Type.ClassType(types.syms.classType.getEnclosingType(),
                                      List.of(arg),
                                      types.syms.classType.tsym);
        }
        public String toString() {
            return type + ".class";
        }
        public Type getValue() {
            return type;
        }
        public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
            return v.visitType(type, p);
        }
    }
    public static class Compound extends Attribute implements AnnotationMirror {
        public final List<Pair<MethodSymbol,Attribute>> values;
        public Compound(Type type,
                        List<Pair<MethodSymbol,Attribute>> values) {
            super(type);
            this.values = values;
        }
        public void accept(Visitor v) { v.visitCompound(this); }
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("@");
            buf.append(type);
            int len = values.length();
            if (len > 0) {
                buf.append('(');
                boolean first = true;
                for (Pair<MethodSymbol, Attribute> value : values) {
                    if (!first) buf.append(", ");
                    first = false;
                    Name name = value.fst.name;
                    if (len > 1 || name != name.table.names.value) {
                        buf.append(name);
                        buf.append('=');
                    }
                    buf.append(value.snd);
                }
                buf.append(')');
            }
            return buf.toString();
        }
        public Attribute member(Name member) {
            for (Pair<MethodSymbol,Attribute> pair : values)
                if (pair.fst.name == member) return pair.snd;
            return null;
        }
        public Attribute.Compound getValue() {
            return this;
        }
        public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
            return v.visitAnnotation(this, p);
        }
        public DeclaredType getAnnotationType() {
            return (DeclaredType) type;
        }
        public Map<MethodSymbol, Attribute> getElementValues() {
            Map<MethodSymbol, Attribute> valmap =
                new LinkedHashMap<MethodSymbol, Attribute>();
            for (Pair<MethodSymbol, Attribute> value : values)
                valmap.put(value.fst, value.snd);
            return valmap;
        }
    }
    public static class Array extends Attribute {
        public final Attribute[] values;
        public Array(Type type, Attribute[] values) {
            super(type);
            this.values = values;
        }
        public void accept(Visitor v) { v.visitArray(this); }
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append('{');
            boolean first = true;
            for (Attribute value : values) {
                if (!first)
                    buf.append(", ");
                first = false;
                buf.append(value);
            }
            buf.append('}');
            return buf.toString();
        }
        public List<Attribute> getValue() {
            return List.from(values);
        }
        public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
            return v.visitArray(getValue(), p);
        }
    }
    public static class Enum extends Attribute {
        public VarSymbol value;
        public Enum(Type type, VarSymbol value) {
            super(type);
            this.value = Assert.checkNonNull(value);
        }
        public void accept(Visitor v) { v.visitEnum(this); }
        public String toString() {
            return value.enclClass() + "." + value;     
        }
        public VarSymbol getValue() {
            return value;
        }
        public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
            return v.visitEnumConstant(value, p);
        }
    }
    public static class Error extends Attribute {
        public Error(Type type) {
            super(type);
        }
        public void accept(Visitor v) { v.visitError(this); }
        public String toString() {
            return "<error>";
        }
        public String getValue() {
            return toString();
        }
        public <R, P> R accept(AnnotationValueVisitor<R, P> v, P p) {
            return v.visitString(toString(), p);
        }
    }
    public static interface Visitor {
        void visitConstant(Attribute.Constant value);
        void visitClass(Attribute.Class clazz);
        void visitCompound(Attribute.Compound compound);
        void visitArray(Attribute.Array array);
        void visitEnum(Attribute.Enum e);
        void visitError(Attribute.Error e);
    }
    public static enum RetentionPolicy {
        SOURCE,
        CLASS,
        RUNTIME
    }
}
