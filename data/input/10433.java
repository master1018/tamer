public class PackageDeclarationImpl extends DeclarationImpl
                                    implements PackageDeclaration {
    private PackageSymbol sym;
    public PackageDeclarationImpl(AptEnv env, PackageSymbol sym) {
        super(env, sym);
        this.sym = sym;
    }
    public String toString() {
        return getQualifiedName();
    }
    public String getQualifiedName() {
        return sym.getQualifiedName().toString();
    }
    public Collection<ClassDeclaration> getClasses() {
        return identityFilter.filter(getAllTypes(),
                                     ClassDeclaration.class);
    }
    public Collection<EnumDeclaration> getEnums() {
        return identityFilter.filter(getAllTypes(),
                                     EnumDeclaration.class);
    }
    public Collection<InterfaceDeclaration> getInterfaces() {
        return identityFilter.filter(getAllTypes(),
                                     InterfaceDeclaration.class);
    }
    public Collection<AnnotationTypeDeclaration> getAnnotationTypes() {
        return identityFilter.filter(getAllTypes(),
                                     AnnotationTypeDeclaration.class);
    }
    public void accept(DeclarationVisitor v) {
        v.visitPackageDeclaration(this);
    }
    private Collection<TypeDeclaration> allTypes = null;
    private Collection<TypeDeclaration> getAllTypes() {
        if (allTypes != null) {
            return allTypes;
        }
        allTypes = new ArrayList<TypeDeclaration>();
        for (Symbol s : getMembers(false)) {
            allTypes.add(env.declMaker.getTypeDeclaration((ClassSymbol) s));
        }
        return allTypes;
    }
}
