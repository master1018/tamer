public class TypesImpl implements Types {
    private final AptEnv env;
    private static final Context.Key<Types> typesKey =
            new Context.Key<Types>();
    public static Types instance(Context context) {
        Types instance = context.get(typesKey);
        if (instance == null) {
            instance = new TypesImpl(context);
        }
        return instance;
    }
    private TypesImpl(Context context) {
        context.put(typesKey, this);
        env = AptEnv.instance(context);
    }
    public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
        return env.jctypes.isSubtype(((TypeMirrorImpl) t1).type,
                                     ((TypeMirrorImpl) t2).type);
    }
    public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
        return env.jctypes.isAssignable(((TypeMirrorImpl) t1).type,
                                        ((TypeMirrorImpl) t2).type);
    }
    public TypeMirror getErasure(TypeMirror t) {
        return env.typeMaker.getType(
                env.jctypes.erasure(((TypeMirrorImpl) t).type));
    }
    public PrimitiveType getPrimitiveType(PrimitiveType.Kind kind) {
        Type prim = null;
        switch (kind) {
        case BOOLEAN:   prim = env.symtab.booleanType;  break;
        case BYTE:      prim = env.symtab.byteType;     break;
        case SHORT:     prim = env.symtab.shortType;    break;
        case INT:       prim = env.symtab.intType;      break;
        case LONG:      prim = env.symtab.longType;     break;
        case CHAR:      prim = env.symtab.charType;     break;
        case FLOAT:     prim = env.symtab.floatType;    break;
        case DOUBLE:    prim = env.symtab.doubleType;   break;
        default:        assert false;
        }
        return (PrimitiveType) env.typeMaker.getType(prim);
    }
    public VoidType getVoidType() {
        return (VoidType) env.typeMaker.getType(env.symtab.voidType);
    }
    public ArrayType getArrayType(TypeMirror componentType) {
        if (componentType instanceof VoidType) {
            throw new IllegalArgumentException("void");
        }
        return (ArrayType) env.typeMaker.getType(
                new Type.ArrayType(((TypeMirrorImpl) componentType).type,
                                   env.symtab.arrayClass));
    }
    public TypeVariable getTypeVariable(TypeParameterDeclaration tparam) {
        return (TypeVariable) env.typeMaker.getType(
                                ((DeclarationImpl) tparam).sym.type);
    }
    public WildcardType getWildcardType(Collection<ReferenceType> upperBounds,
                                        Collection<ReferenceType> lowerBounds) {
        BoundKind kind;
        Type bound;
        int uppers  = upperBounds.size();
        int downers = lowerBounds.size();
        if (uppers + downers > 1) {
            throw new IllegalArgumentException("Multiple bounds not allowed");
        } else if (uppers + downers == 0) {
            kind = BoundKind.UNBOUND;
            bound = env.symtab.objectType;
        } else if (uppers == 1) {
            assert downers == 0;
            kind = BoundKind.EXTENDS;
            bound = ((TypeMirrorImpl) upperBounds.iterator().next()).type;
        } else {
            assert uppers == 0 && downers == 1;
            kind = BoundKind.SUPER;
            bound = ((TypeMirrorImpl) lowerBounds.iterator().next()).type;
        }
        if (bound instanceof Type.WildcardType)
            throw new IllegalArgumentException(bound.toString());
        return (WildcardType) env.typeMaker.getType(
                new Type.WildcardType(bound, kind, env.symtab.boundClass));
    }
    public DeclaredType getDeclaredType(TypeDeclaration decl,
                                        TypeMirror... typeArgs) {
        ClassSymbol sym = ((TypeDeclarationImpl) decl).sym;
        if (typeArgs.length == 0)
            return (DeclaredType) env.typeMaker.getType(
                                        env.jctypes.erasure(sym.type));
        if (sym.type.getEnclosingType().isParameterized())
            throw new IllegalArgumentException(decl.toString());
        return getDeclaredType(sym.type.getEnclosingType(), sym, typeArgs);
    }
    public DeclaredType getDeclaredType(DeclaredType containing,
                                        TypeDeclaration decl,
                                        TypeMirror... typeArgs) {
        if (containing == null)
            return getDeclaredType(decl, typeArgs);
        ClassSymbol sym = ((TypeDeclarationImpl) decl).sym;
        Type outer = ((TypeMirrorImpl) containing).type;
        if (outer.tsym != sym.owner.enclClass())
            throw new IllegalArgumentException(containing.toString());
        if (!outer.isParameterized())
            return getDeclaredType(decl, typeArgs);
        return getDeclaredType(outer, sym, typeArgs);
    }
    private DeclaredType getDeclaredType(Type outer,
                                         ClassSymbol sym,
                                         TypeMirror... typeArgs) {
        if (typeArgs.length != sym.type.getTypeArguments().length())
            throw new IllegalArgumentException(
                                "Incorrect number of type arguments");
        ListBuffer<Type> targs = new ListBuffer<Type>();
        for (TypeMirror t : typeArgs) {
            if (!(t instanceof ReferenceType || t instanceof WildcardType))
                throw new IllegalArgumentException(t.toString());
            targs.append(((TypeMirrorImpl) t).type);
        }
        return (DeclaredType) env.typeMaker.getType(
                new Type.ClassType(outer, targs.toList(), sym));
    }
}
