public class TypeVariableImpl extends AbstractTypeImpl implements TypeVariable {
    TypeVariableImpl(DocEnv env, TypeVar type) {
        super(env, type);
    }
    public com.sun.javadoc.Type[] bounds() {
        return TypeMaker.getTypes(env, getBounds((TypeVar)type, env));
    }
    public ProgramElementDoc owner() {
        Symbol osym = type.tsym.owner;
        if ((osym.kind & Kinds.TYP) != 0) {
            return env.getClassDoc((ClassSymbol)osym);
        }
        Names names = osym.name.table.names;
        if (osym.name == names.init) {
            return env.getConstructorDoc((MethodSymbol)osym);
        } else {
            return env.getMethodDoc((MethodSymbol)osym);
        }
    }
    @Override
    public ClassDoc asClassDoc() {
        return env.getClassDoc((ClassSymbol)env.types.erasure(type).tsym);
    }
    @Override
    public TypeVariable asTypeVariable() {
        return this;
    }
    @Override
    public String toString() {
        return typeVarToString(env, (TypeVar)type, true);
    }
    static String typeVarToString(DocEnv env, TypeVar v, boolean full) {
        StringBuilder s = new StringBuilder(v.toString());
        List<Type> bounds = getBounds(v, env);
        if (bounds.nonEmpty()) {
            boolean first = true;
            for (Type b : bounds) {
                s.append(first ? " extends " : " & ");
                s.append(TypeMaker.getTypeString(env, b, full));
                first = false;
            }
        }
        return s.toString();
    }
    private static List<Type> getBounds(TypeVar v, DocEnv env) {
        Name boundname = v.getUpperBound().tsym.getQualifiedName();
        if (boundname == boundname.table.names.java_lang_Object) {
            return List.nil();
        } else {
            return env.types.getBounds(v);
        }
    }
}
