public class TypeCreation extends Tester {
    public static void main(String[] args) {
        (new TypeCreation()).run();
    }
    class A {
    }
    class O<T> {
        class I<S> {
        }
    }
    private Types types;
    private TypeDeclaration A;
    private TypeDeclaration O;
    private TypeDeclaration I;
    private DeclaredType AType;
    protected void init() {
        types = env.getTypeUtils();
        A = env.getTypeDeclaration("TypeCreation.A");
        O = env.getTypeDeclaration("TypeCreation.O");
        I = env.getTypeDeclaration("TypeCreation.O.I");
        AType = types.getDeclaredType(A);
    }
    @Test(result="boolean")
    PrimitiveType getPrimitiveType() {
        return types.getPrimitiveType(BOOLEAN);
    }
    @Test(result="void")
    VoidType getVoidType() {
        return types.getVoidType();
    }
    @Test(result="boolean[]")
    ArrayType getArrayType1() {
        return types.getArrayType(
                types.getPrimitiveType(BOOLEAN));
    }
    @Test(result="TypeCreation.A[]")
    ArrayType getArrayType2() {
        return types.getArrayType(AType);
    }
    @Test(result="? extends TypeCreation.A")
    WildcardType getWildcardType() {
        Collection<ReferenceType> uppers = new ArrayList<ReferenceType>();
        Collection<ReferenceType> downers = new ArrayList<ReferenceType>();
        uppers.add(AType);
        return types.getWildcardType(uppers, downers);
    }
    @Test(result="TypeCreation.O<java.lang.String>")
    DeclaredType getDeclaredType1() {
        TypeDeclaration stringDecl = env.getTypeDeclaration("java.lang.String");
        DeclaredType stringType = types.getDeclaredType(stringDecl);
        return types.getDeclaredType(O, stringType);
    }
    @Test(result="TypeCreation.O<java.lang.String>.I<java.lang.Number>")
    DeclaredType getDeclaredType2() {
        TypeDeclaration numDecl = env.getTypeDeclaration("java.lang.Number");
        DeclaredType numType = types.getDeclaredType(numDecl);
        DeclaredType OType = getDeclaredType1();
        return types.getDeclaredType(OType, I, numType);
    }
}
