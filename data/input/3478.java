public class JavacTypes implements javax.lang.model.util.Types {
    private Symtab syms;
    private Types types;
    public static JavacTypes instance(Context context) {
        JavacTypes instance = context.get(JavacTypes.class);
        if (instance == null)
            instance = new JavacTypes(context);
        return instance;
    }
    protected JavacTypes(Context context) {
        setContext(context);
    }
    public void setContext(Context context) {
        context.put(JavacTypes.class, this);
        syms = Symtab.instance(context);
        types = Types.instance(context);
    }
    public Element asElement(TypeMirror t) {
        switch (t.getKind()) {
            case DECLARED:
            case ERROR:
            case TYPEVAR:
                Type type = cast(Type.class, t);
                return type.asElement();
            default:
                return null;
        }
    }
    public boolean isSameType(TypeMirror t1, TypeMirror t2) {
        return types.isSameType((Type) t1, (Type) t2);
    }
    public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
        validateTypeNotIn(t1, EXEC_OR_PKG);
        validateTypeNotIn(t2, EXEC_OR_PKG);
        return types.isSubtype((Type) t1, (Type) t2);
    }
    public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
        validateTypeNotIn(t1, EXEC_OR_PKG);
        validateTypeNotIn(t2, EXEC_OR_PKG);
        return types.isAssignable((Type) t1, (Type) t2);
    }
    public boolean contains(TypeMirror t1, TypeMirror t2) {
        validateTypeNotIn(t1, EXEC_OR_PKG);
        validateTypeNotIn(t2, EXEC_OR_PKG);
        return types.containsType((Type) t1, (Type) t2);
    }
    public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
        return types.isSubSignature((Type) m1, (Type) m2);
    }
    public List<Type> directSupertypes(TypeMirror t) {
        validateTypeNotIn(t, EXEC_OR_PKG);
        Type type = (Type) t;
        Type sup = types.supertype(type);
        return (sup == Type.noType || sup == type || sup == null)
              ? types.interfaces(type)
              : types.interfaces(type).prepend(sup);
    }
    public TypeMirror erasure(TypeMirror t) {
        if (t.getKind() == TypeKind.PACKAGE)
            throw new IllegalArgumentException(t.toString());
        return types.erasure((Type) t);
    }
    public TypeElement boxedClass(PrimitiveType p) {
        return types.boxedClass((Type) p);
    }
    public PrimitiveType unboxedType(TypeMirror t) {
        if (t.getKind() != TypeKind.DECLARED)
            throw new IllegalArgumentException(t.toString());
        Type unboxed = types.unboxedType((Type) t);
        if (! unboxed.isPrimitive())    
            throw new IllegalArgumentException(t.toString());
        return unboxed;
    }
    public TypeMirror capture(TypeMirror t) {
        validateTypeNotIn(t, EXEC_OR_PKG);
        return types.capture((Type) t);
    }
    public PrimitiveType getPrimitiveType(TypeKind kind) {
        switch (kind) {
        case BOOLEAN:   return syms.booleanType;
        case BYTE:      return syms.byteType;
        case SHORT:     return syms.shortType;
        case INT:       return syms.intType;
        case LONG:      return syms.longType;
        case CHAR:      return syms.charType;
        case FLOAT:     return syms.floatType;
        case DOUBLE:    return syms.doubleType;
        default:
            throw new IllegalArgumentException("Not a primitive type: " + kind);
        }
    }
    public NullType getNullType() {
        return (NullType) syms.botType;
    }
    public NoType getNoType(TypeKind kind) {
        switch (kind) {
        case VOID:      return syms.voidType;
        case NONE:      return Type.noType;
        default:
            throw new IllegalArgumentException(kind.toString());
        }
    }
    public ArrayType getArrayType(TypeMirror componentType) {
        switch (componentType.getKind()) {
        case VOID:
        case EXECUTABLE:
        case WILDCARD:  
        case PACKAGE:
            throw new IllegalArgumentException(componentType.toString());
        }
        return new Type.ArrayType((Type) componentType, syms.arrayClass);
    }
    public WildcardType getWildcardType(TypeMirror extendsBound,
                                        TypeMirror superBound) {
        BoundKind bkind;
        Type bound;
        if (extendsBound == null && superBound == null) {
            bkind = BoundKind.UNBOUND;
            bound = syms.objectType;
        } else if (superBound == null) {
            bkind = BoundKind.EXTENDS;
            bound = (Type) extendsBound;
        } else if (extendsBound == null) {
            bkind = BoundKind.SUPER;
            bound = (Type) superBound;
        } else {
            throw new IllegalArgumentException(
                    "Extends and super bounds cannot both be provided");
        }
        switch (bound.getKind()) {
        case ARRAY:
        case DECLARED:
        case ERROR:
        case TYPEVAR:
            return new Type.WildcardType(bound, bkind, syms.boundClass);
        default:
            throw new IllegalArgumentException(bound.toString());
        }
    }
    public DeclaredType getDeclaredType(TypeElement typeElem,
                                        TypeMirror... typeArgs) {
        ClassSymbol sym = (ClassSymbol) typeElem;
        if (typeArgs.length == 0)
            return (DeclaredType) sym.erasure(types);
        if (sym.type.getEnclosingType().isParameterized())
            throw new IllegalArgumentException(sym.toString());
        return getDeclaredType0(sym.type.getEnclosingType(), sym, typeArgs);
    }
    public DeclaredType getDeclaredType(DeclaredType enclosing,
                                        TypeElement typeElem,
                                        TypeMirror... typeArgs) {
        if (enclosing == null)
            return getDeclaredType(typeElem, typeArgs);
        ClassSymbol sym = (ClassSymbol) typeElem;
        Type outer = (Type) enclosing;
        if (outer.tsym != sym.owner.enclClass())
            throw new IllegalArgumentException(enclosing.toString());
        if (!outer.isParameterized())
            return getDeclaredType(typeElem, typeArgs);
        return getDeclaredType0(outer, sym, typeArgs);
    }
        private DeclaredType getDeclaredType0(Type outer,
                                              ClassSymbol sym,
                                              TypeMirror... typeArgs) {
            if (typeArgs.length != sym.type.getTypeArguments().length())
                throw new IllegalArgumentException(
                "Incorrect number of type arguments");
            ListBuffer<Type> targs = new ListBuffer<Type>();
            for (TypeMirror t : typeArgs) {
                if (!(t instanceof ReferenceType || t instanceof WildcardType))
                    throw new IllegalArgumentException(t.toString());
                targs.append((Type) t);
            }
            return (DeclaredType) new Type.ClassType(outer, targs.toList(), sym);
        }
    public TypeMirror asMemberOf(DeclaredType containing, Element element) {
        Type site = (Type)containing;
        Symbol sym = (Symbol)element;
        if (types.asSuper(site, sym.getEnclosingElement()) == null)
            throw new IllegalArgumentException(sym + "@" + site);
        return types.memberType(site, sym);
    }
    private static final Set<TypeKind> EXEC_OR_PKG =
            EnumSet.of(TypeKind.EXECUTABLE, TypeKind.PACKAGE);
    private void validateTypeNotIn(TypeMirror t, Set<TypeKind> invalidKinds) {
        if (invalidKinds.contains(t.getKind()))
            throw new IllegalArgumentException(t.toString());
    }
    private static <T> T cast(Class<T> clazz, Object o) {
        if (! clazz.isInstance(o))
            throw new IllegalArgumentException(o.toString());
        return clazz.cast(o);
    }
}
