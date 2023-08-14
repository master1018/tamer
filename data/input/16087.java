public class AnnotationMirrorImpl implements AnnotationMirror {
    protected final AptEnv env;
    protected final Attribute.Compound anno;
    protected final Declaration decl;
    AnnotationMirrorImpl(AptEnv env, Attribute.Compound anno, Declaration decl) {
        this.env = env;
        this.anno = anno;
        this.decl = decl;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder("@");
        Constants.Formatter fmtr = Constants.getFormatter(sb);
        fmtr.append(anno.type.tsym);
        int len = anno.values.length();
        if (len > 0) {          
            sb.append('(');
            boolean first = true;
            for (Pair<MethodSymbol, Attribute> val : anno.values) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                Name name = val.fst.name;
                if (len > 1 || name != env.names.value) {
                    fmtr.append(name);
                    sb.append('=');
                }
                sb.append(new AnnotationValueImpl(env, val.snd, this));
            }
            sb.append(')');
        }
        return fmtr.toString();
    }
    public AnnotationType getAnnotationType() {
        return (AnnotationType) env.typeMaker.getType(anno.type);
    }
    public Map<AnnotationTypeElementDeclaration, AnnotationValue>
                                                        getElementValues() {
        Map<AnnotationTypeElementDeclaration, AnnotationValue> res =
            new LinkedHashMap<AnnotationTypeElementDeclaration,
                                                   AnnotationValue>(); 
        for (Pair<MethodSymbol, Attribute> val : anno.values) {
            res.put(getElement(val.fst),
                    new AnnotationValueImpl(env, val.snd, this));
        }
        return res;
    }
    public SourcePosition getPosition() {
        return (decl == null) ? null : decl.getPosition();
    }
    public Declaration getDeclaration() {
        return this.decl;
    }
    private AnnotationTypeElementDeclaration getElement(MethodSymbol m) {
        return (AnnotationTypeElementDeclaration)
                    env.declMaker.getExecutableDeclaration(m);
    }
}
