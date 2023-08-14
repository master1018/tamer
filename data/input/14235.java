public class DeclarationVisitors {
    private DeclarationVisitors(){} 
    public static final DeclarationVisitor NO_OP = new SimpleDeclarationVisitor();
    public static DeclarationVisitor getDeclarationScanner(DeclarationVisitor pre,
                                                           DeclarationVisitor post) {
        return new DeclarationScanner(pre, post);
    }
    public static DeclarationVisitor getSourceOrderDeclarationScanner(DeclarationVisitor pre,
                                                                      DeclarationVisitor post) {
        return new SourceOrderDeclScanner(pre, post);
    }
}
