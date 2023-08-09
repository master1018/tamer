public class CoreReflectionFactory implements GenericsFactory {
    private GenericDeclaration decl;
    private Scope scope;
    private CoreReflectionFactory(GenericDeclaration d, Scope s) {
        decl = d;
        scope = s;
    }
    private GenericDeclaration getDecl(){ return decl;}
    private Scope getScope(){ return scope;}
    private ClassLoader getDeclsLoader() {
        if (decl instanceof Class) {return ((Class) decl).getClassLoader();}
        if (decl instanceof Method) {
            return ((Method) decl).getDeclaringClass().getClassLoader();
        }
        assert decl instanceof Constructor : "Constructor expected";
        return ((Constructor) decl).getDeclaringClass().getClassLoader();
    }
    public static CoreReflectionFactory make(GenericDeclaration d, Scope s) {
        return new CoreReflectionFactory(d, s);
    }
    public TypeVariable<?> makeTypeVariable(String name,
                                            FieldTypeSignature[] bounds){
        return TypeVariableImpl.make(getDecl(), name, bounds, this);
    }
    public WildcardType makeWildcard(FieldTypeSignature[] ubs,
                                     FieldTypeSignature[] lbs) {
        return WildcardTypeImpl.make(ubs, lbs, this);
    }
    public ParameterizedType makeParameterizedType(Type declaration,
                                                   Type[] typeArgs,
                                                   Type owner) {
        return ParameterizedTypeImpl.make((Class<?>) declaration,
                                          typeArgs, owner);
    }
    public TypeVariable<?> findTypeVariable(String name){
        return getScope().lookup(name);
    }
    public Type makeNamedType(String name){
        try {return Class.forName(name, false, 
                                  getDeclsLoader());}
        catch (ClassNotFoundException c) {
            throw new TypeNotPresentException(name, c);
        }
    }
    public Type makeArrayType(Type componentType){
        if (componentType instanceof Class<?>)
            return Array.newInstance((Class<?>) componentType, 0).getClass();
        else
            return GenericArrayTypeImpl.make(componentType);
    }
    public Type makeByte(){return byte.class;}
    public Type makeBool(){return boolean.class;}
    public Type makeShort(){return short.class;}
    public Type makeChar(){return char.class;}
    public Type makeInt(){return int.class;}
    public Type makeLong(){return long.class;}
    public Type makeFloat(){return float.class;}
    public Type makeDouble(){return double.class;}
    public Type makeVoid(){return void.class;}
}
