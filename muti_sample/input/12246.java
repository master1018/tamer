public class AnnotationValueImpl implements AnnotationValue {
    private final DocEnv env;
    private final Attribute attr;
    AnnotationValueImpl(DocEnv env, Attribute attr) {
        this.env = env;
        this.attr = attr;
    }
    public Object value() {
        ValueVisitor vv = new ValueVisitor();
        attr.accept(vv);
        return vv.value;
    }
    private class ValueVisitor implements Attribute.Visitor {
        public Object value;
        public void visitConstant(Attribute.Constant c) {
            if (c.type.tag == TypeTags.BOOLEAN) {
                value = Boolean.valueOf(
                                ((Integer)c.value).intValue() != 0);
            } else {
                value = c.value;
            }
        }
        public void visitClass(Attribute.Class c) {
            value = TypeMaker.getType(env,
                                      env.types.erasure(c.type));
        }
        public void visitEnum(Attribute.Enum e) {
            value = env.getFieldDoc(e.value);
        }
        public void visitCompound(Attribute.Compound c) {
            value = new AnnotationDescImpl(env, c);
        }
        public void visitArray(Attribute.Array a) {
            AnnotationValue vals[] = new AnnotationValue[a.values.length];
            for (int i = 0; i < vals.length; i++) {
                vals[i] = new AnnotationValueImpl(env, a.values[i]);
            }
            value = vals;
        }
        public void visitError(Attribute.Error e) {
            value = "<error>";
        }
    }
    @Override
    public String toString() {
        ToStringVisitor tv = new ToStringVisitor();
        attr.accept(tv);
        return tv.toString();
    }
    private class ToStringVisitor implements Attribute.Visitor {
        private final StringBuilder sb = new StringBuilder();
        @Override
        public String toString() {
            return sb.toString();
        }
        public void visitConstant(Attribute.Constant c) {
            if (c.type.tag == TypeTags.BOOLEAN) {
                sb.append(((Integer)c.value).intValue() != 0);
            } else {
                sb.append(FieldDocImpl.constantValueExpression(c.value));
            }
        }
        public void visitClass(Attribute.Class c) {
            sb.append(c);
        }
        public void visitEnum(Attribute.Enum e) {
            sb.append(e);
        }
        public void visitCompound(Attribute.Compound c) {
            sb.append(new AnnotationDescImpl(env, c));
        }
        public void visitArray(Attribute.Array a) {
            if (a.values.length != 1) sb.append('{');
            boolean first = true;
            for (Attribute elem : a.values) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                elem.accept(this);
            }
            if (a.values.length != 1) sb.append('}');
        }
        public void visitError(Attribute.Error e) {
            sb.append("<error>");
        }
    }
}
