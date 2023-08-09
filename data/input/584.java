class DeclarationScanner implements DeclarationVisitor {
    protected DeclarationVisitor pre;
    protected DeclarationVisitor post;
    DeclarationScanner(DeclarationVisitor pre, DeclarationVisitor post) {
        this.pre = pre;
        this.post = post;
    }
    public void visitDeclaration(Declaration d) {
        d.accept(pre);
        d.accept(post);
    }
    public void visitPackageDeclaration(PackageDeclaration d) {
        d.accept(pre);
        for(ClassDeclaration classDecl: d.getClasses()) {
            classDecl.accept(this);
        }
        for(InterfaceDeclaration interfaceDecl: d.getInterfaces()) {
            interfaceDecl.accept(this);
        }
        d.accept(post);
    }
    public void visitMemberDeclaration(MemberDeclaration d) {
        visitDeclaration(d);
    }
    public void visitTypeDeclaration(TypeDeclaration d) {
        d.accept(pre);
        for(TypeParameterDeclaration tpDecl: d.getFormalTypeParameters()) {
            tpDecl.accept(this);
        }
        for(FieldDeclaration fieldDecl: d.getFields()) {
            fieldDecl.accept(this);
        }
        for(MethodDeclaration methodDecl: d.getMethods()) {
            methodDecl.accept(this);
        }
        for(TypeDeclaration typeDecl: d.getNestedTypes()) {
            typeDecl.accept(this);
        }
        d.accept(post);
    }
    public void visitClassDeclaration(ClassDeclaration d) {
        d.accept(pre);
        for(TypeParameterDeclaration tpDecl: d.getFormalTypeParameters()) {
            tpDecl.accept(this);
        }
        for(FieldDeclaration fieldDecl: d.getFields()) {
            fieldDecl.accept(this);
        }
        for(MethodDeclaration methodDecl: d.getMethods()) {
            methodDecl.accept(this);
        }
        for(TypeDeclaration typeDecl: d.getNestedTypes()) {
            typeDecl.accept(this);
        }
        for(ConstructorDeclaration ctorDecl: d.getConstructors()) {
            ctorDecl.accept(this);
        }
        d.accept(post);
    }
    public void visitEnumDeclaration(EnumDeclaration d) {
        visitClassDeclaration(d);
    }
    public void visitInterfaceDeclaration(InterfaceDeclaration d) {
        visitTypeDeclaration(d);
    }
    public void visitAnnotationTypeDeclaration(AnnotationTypeDeclaration d) {
        visitInterfaceDeclaration(d);
    }
    public void visitFieldDeclaration(FieldDeclaration d) {
        visitMemberDeclaration(d);
    }
    public void visitEnumConstantDeclaration(EnumConstantDeclaration d) {
        visitFieldDeclaration(d);
    }
    public void visitExecutableDeclaration(ExecutableDeclaration d) {
        d.accept(pre);
        for(TypeParameterDeclaration tpDecl: d.getFormalTypeParameters()) {
            tpDecl.accept(this);
        }
        for(ParameterDeclaration pDecl: d.getParameters()) {
            pDecl.accept(this);
        }
        d.accept(post);
    }
    public void visitConstructorDeclaration(ConstructorDeclaration d) {
        visitExecutableDeclaration(d);
    }
    public void visitMethodDeclaration(MethodDeclaration d) {
        visitExecutableDeclaration(d);
    }
    public void visitAnnotationTypeElementDeclaration(
            AnnotationTypeElementDeclaration d) {
        visitMethodDeclaration(d);
    }
    public void visitParameterDeclaration(ParameterDeclaration d) {
        visitDeclaration(d);
    }
    public void visitTypeParameterDeclaration(TypeParameterDeclaration d) {
        visitDeclaration(d);
    }
}
