class JavadocMemberEnter extends MemberEnter {
    public static JavadocMemberEnter instance0(Context context) {
        MemberEnter instance = context.get(memberEnterKey);
        if (instance == null)
            instance = new JavadocMemberEnter(context);
        return (JavadocMemberEnter)instance;
    }
    public static void preRegister(Context context) {
        context.put(memberEnterKey, new Context.Factory<MemberEnter>() {
               public MemberEnter make(Context c) {
                   return new JavadocMemberEnter(c);
               }
        });
    }
    final DocEnv docenv;
    protected JavadocMemberEnter(Context context) {
        super(context);
        docenv = DocEnv.instance(context);
    }
    public void visitMethodDef(JCMethodDecl tree) {
        super.visitMethodDef(tree);
        MethodSymbol meth = tree.sym;
        if (meth == null || meth.kind != Kinds.MTH) return;
        String docComment = env.toplevel.docComments.get(tree);
        Position.LineMap lineMap = env.toplevel.lineMap;
        if (meth.isConstructor())
            docenv.makeConstructorDoc(meth, docComment, tree, lineMap);
        else if (isAnnotationTypeElement(meth))
            docenv.makeAnnotationTypeElementDoc(meth, docComment, tree, lineMap);
        else
            docenv.makeMethodDoc(meth, docComment, tree, lineMap);
    }
    public void visitVarDef(JCVariableDecl tree) {
        super.visitVarDef(tree);
        if (tree.sym != null &&
                tree.sym.kind == Kinds.VAR &&
                !isParameter(tree.sym)) {
            String docComment = env.toplevel.docComments.get(tree);
            Position.LineMap lineMap = env.toplevel.lineMap;
            docenv.makeFieldDoc(tree.sym, docComment, tree, lineMap);
        }
    }
    private static boolean isAnnotationTypeElement(MethodSymbol meth) {
        return ClassDocImpl.isAnnotationType(meth.enclClass());
    }
    private static boolean isParameter(VarSymbol var) {
        return (var.flags() & Flags.PARAMETER) != 0;
    }
}
