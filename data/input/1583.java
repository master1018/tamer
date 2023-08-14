public class AnnotationDescImpl implements AnnotationDesc {
    private final DocEnv env;
    private final Attribute.Compound annotation;
    AnnotationDescImpl(DocEnv env, Attribute.Compound annotation) {
        this.env = env;
        this.annotation = annotation;
    }
    public AnnotationTypeDoc annotationType() {
        ClassSymbol atsym = (ClassSymbol)annotation.type.tsym;
        if (annotation.type.isErroneous()) {
            env.warning(null, "javadoc.class_not_found", annotation.type.toString());
            return new AnnotationTypeDocImpl(env, atsym);
        } else {
            return (AnnotationTypeDoc)env.getClassDoc(atsym);
        }
    }
    public ElementValuePair[] elementValues() {
        List<Pair<MethodSymbol,Attribute>> vals = annotation.values;
        ElementValuePair res[] = new ElementValuePair[vals.length()];
        int i = 0;
        for (Pair<MethodSymbol,Attribute> val : vals) {
            res[i++] = new ElementValuePairImpl(env, val.fst, val.snd);
        }
        return res;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("@");
        sb.append(annotation.type.tsym);
        ElementValuePair vals[] = elementValues();
        if (vals.length > 0) {          
            sb.append('(');
            boolean first = true;
            for (ElementValuePair val : vals) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                String name = val.element().name();
                if (vals.length == 1 && name.equals("value")) {
                    sb.append(val.value());
                } else {
                    sb.append(val);
                }
            }
            sb.append(')');
        }
        return sb.toString();
    }
    public static class ElementValuePairImpl implements ElementValuePair {
        private final DocEnv env;
        private final MethodSymbol meth;
        private final Attribute value;
        ElementValuePairImpl(DocEnv env, MethodSymbol meth, Attribute value) {
            this.env = env;
            this.meth = meth;
            this.value = value;
        }
        public AnnotationTypeElementDoc element() {
            return env.getAnnotationTypeElementDoc(meth);
        }
        public AnnotationValue value() {
            return new AnnotationValueImpl(env, value);
        }
        @Override
        public String toString() {
            return meth.name + "=" + value();
        }
    }
}
