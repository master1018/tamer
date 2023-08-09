public class TreeScanner<R,P> implements TreeVisitor<R,P> {
    public R scan(Tree node, P p) {
        return (node == null) ? null : node.accept(this, p);
    }
    private R scanAndReduce(Tree node, P p, R r) {
        return reduce(scan(node, p), r);
    }
    public R scan(Iterable<? extends Tree> nodes, P p) {
        R r = null;
        if (nodes != null) {
            boolean first = true;
            for (Tree node : nodes) {
                r = (first ? scan(node, p) : scanAndReduce(node, p, r));
                first = false;
            }
        }
        return r;
    }
    private R scanAndReduce(Iterable<? extends Tree> nodes, P p, R r) {
        return reduce(scan(nodes, p), r);
    }
    public R reduce(R r1, R r2) {
        return r1;
    }
    public R visitCompilationUnit(CompilationUnitTree node, P p) {
        R r = scan(node.getPackageAnnotations(), p);
        r = scanAndReduce(node.getPackageName(), p, r);
        r = scanAndReduce(node.getImports(), p, r);
        r = scanAndReduce(node.getTypeDecls(), p, r);
        return r;
    }
    public R visitImport(ImportTree node, P p) {
        return scan(node.getQualifiedIdentifier(), p);
    }
    public R visitClass(ClassTree node, P p) {
        R r = scan(node.getModifiers(), p);
        r = scanAndReduce(node.getTypeParameters(), p, r);
        r = scanAndReduce(node.getExtendsClause(), p, r);
        r = scanAndReduce(node.getImplementsClause(), p, r);
        r = scanAndReduce(node.getMembers(), p, r);
        return r;
    }
    public R visitMethod(MethodTree node, P p) {
        R r = scan(node.getModifiers(), p);
        r = scanAndReduce(node.getReturnType(), p, r);
        r = scanAndReduce(node.getTypeParameters(), p, r);
        r = scanAndReduce(node.getParameters(), p, r);
        r = scanAndReduce(node.getThrows(), p, r);
        r = scanAndReduce(node.getBody(), p, r);
        r = scanAndReduce(node.getDefaultValue(), p, r);
        return r;
    }
    public R visitVariable(VariableTree node, P p) {
        R r = scan(node.getModifiers(), p);
        r = scanAndReduce(node.getType(), p, r);
        r = scanAndReduce(node.getInitializer(), p, r);
        return r;
    }
    public R visitEmptyStatement(EmptyStatementTree node, P p) {
        return null;
    }
    public R visitBlock(BlockTree node, P p) {
        return scan(node.getStatements(), p);
    }
    public R visitDoWhileLoop(DoWhileLoopTree node, P p) {
        R r = scan(node.getStatement(), p);
        r = scanAndReduce(node.getCondition(), p, r);
        return r;
    }
    public R visitWhileLoop(WhileLoopTree node, P p) {
        R r = scan(node.getCondition(), p);
        r = scanAndReduce(node.getStatement(), p, r);
        return r;
    }
    public R visitForLoop(ForLoopTree node, P p) {
        R r = scan(node.getInitializer(), p);
        r = scanAndReduce(node.getCondition(), p, r);
        r = scanAndReduce(node.getUpdate(), p, r);
        r = scanAndReduce(node.getStatement(), p, r);
        return r;
    }
    public R visitEnhancedForLoop(EnhancedForLoopTree node, P p) {
        R r = scan(node.getVariable(), p);
        r = scanAndReduce(node.getExpression(), p, r);
        r = scanAndReduce(node.getStatement(), p, r);
        return r;
    }
    public R visitLabeledStatement(LabeledStatementTree node, P p) {
        return scan(node.getStatement(), p);
    }
    public R visitSwitch(SwitchTree node, P p) {
        R r = scan(node.getExpression(), p);
        r = scanAndReduce(node.getCases(), p, r);
        return r;
    }
    public R visitCase(CaseTree node, P p) {
        R r = scan(node.getExpression(), p);
        r = scanAndReduce(node.getStatements(), p, r);
        return r;
    }
    public R visitSynchronized(SynchronizedTree node, P p) {
        R r = scan(node.getExpression(), p);
        r = scanAndReduce(node.getBlock(), p, r);
        return r;
    }
    public R visitTry(TryTree node, P p) {
        R r = scan(node.getResources(), p);
        r = scanAndReduce(node.getBlock(), p, r);
        r = scanAndReduce(node.getCatches(), p, r);
        r = scanAndReduce(node.getFinallyBlock(), p, r);
        return r;
    }
    public R visitCatch(CatchTree node, P p) {
        R r = scan(node.getParameter(), p);
        r = scanAndReduce(node.getBlock(), p, r);
        return r;
    }
    public R visitConditionalExpression(ConditionalExpressionTree node, P p) {
        R r = scan(node.getCondition(), p);
        r = scanAndReduce(node.getTrueExpression(), p, r);
        r = scanAndReduce(node.getFalseExpression(), p, r);
        return r;
    }
    public R visitIf(IfTree node, P p) {
        R r = scan(node.getCondition(), p);
        r = scanAndReduce(node.getThenStatement(), p, r);
        r = scanAndReduce(node.getElseStatement(), p, r);
        return r;
    }
    public R visitExpressionStatement(ExpressionStatementTree node, P p) {
        return scan(node.getExpression(), p);
    }
    public R visitBreak(BreakTree node, P p) {
        return null;
    }
    public R visitContinue(ContinueTree node, P p) {
        return null;
    }
    public R visitReturn(ReturnTree node, P p) {
        return scan(node.getExpression(), p);
    }
    public R visitThrow(ThrowTree node, P p) {
        return scan(node.getExpression(), p);
    }
    public R visitAssert(AssertTree node, P p) {
        R r = scan(node.getCondition(), p);
        r = scanAndReduce(node.getDetail(), p, r);
        return r;
    }
    public R visitMethodInvocation(MethodInvocationTree node, P p) {
        R r = scan(node.getTypeArguments(), p);
        r = scanAndReduce(node.getMethodSelect(), p, r);
        r = scanAndReduce(node.getArguments(), p, r);
        return r;
    }
    public R visitNewClass(NewClassTree node, P p) {
        R r = scan(node.getEnclosingExpression(), p);
        r = scanAndReduce(node.getIdentifier(), p, r);
        r = scanAndReduce(node.getTypeArguments(), p, r);
        r = scanAndReduce(node.getArguments(), p, r);
        r = scanAndReduce(node.getClassBody(), p, r);
        return r;
    }
    public R visitNewArray(NewArrayTree node, P p) {
        R r = scan(node.getType(), p);
        r = scanAndReduce(node.getDimensions(), p, r);
        r = scanAndReduce(node.getInitializers(), p, r);
        return r;
    }
    public R visitParenthesized(ParenthesizedTree node, P p) {
        return scan(node.getExpression(), p);
    }
    public R visitAssignment(AssignmentTree node, P p) {
        R r = scan(node.getVariable(), p);
        r = scanAndReduce(node.getExpression(), p, r);
        return r;
    }
    public R visitCompoundAssignment(CompoundAssignmentTree node, P p) {
        R r = scan(node.getVariable(), p);
        r = scanAndReduce(node.getExpression(), p, r);
        return r;
    }
    public R visitUnary(UnaryTree node, P p) {
        return scan(node.getExpression(), p);
    }
    public R visitBinary(BinaryTree node, P p) {
        R r = scan(node.getLeftOperand(), p);
        r = scanAndReduce(node.getRightOperand(), p, r);
        return r;
    }
    public R visitTypeCast(TypeCastTree node, P p) {
        R r = scan(node.getType(), p);
        r = scanAndReduce(node.getExpression(), p, r);
        return r;
    }
    public R visitInstanceOf(InstanceOfTree node, P p) {
        R r = scan(node.getExpression(), p);
        r = scanAndReduce(node.getType(), p, r);
        return r;
    }
    public R visitArrayAccess(ArrayAccessTree node, P p) {
        R r = scan(node.getExpression(), p);
        r = scanAndReduce(node.getIndex(), p, r);
        return r;
    }
    public R visitMemberSelect(MemberSelectTree node, P p) {
        return scan(node.getExpression(), p);
    }
    public R visitIdentifier(IdentifierTree node, P p) {
        return null;
    }
    public R visitLiteral(LiteralTree node, P p) {
        return null;
    }
    public R visitPrimitiveType(PrimitiveTypeTree node, P p) {
        return null;
    }
    public R visitArrayType(ArrayTypeTree node, P p) {
        return scan(node.getType(), p);
    }
    public R visitParameterizedType(ParameterizedTypeTree node, P p) {
        R r = scan(node.getType(), p);
        r = scanAndReduce(node.getTypeArguments(), p, r);
        return r;
    }
    public R visitUnionType(UnionTypeTree node, P p) {
        return scan(node.getTypeAlternatives(), p);
    }
    public R visitTypeParameter(TypeParameterTree node, P p) {
        R r = scan(node.getBounds(), p);
        return r;
    }
    public R visitWildcard(WildcardTree node, P p) {
        return scan(node.getBound(), p);
    }
    public R visitModifiers(ModifiersTree node, P p) {
        return scan(node.getAnnotations(), p);
    }
    public R visitAnnotation(AnnotationTree node, P p) {
        R r = scan(node.getAnnotationType(), p);
        r = scanAndReduce(node.getArguments(), p, r);
        return r;
    }
    public R visitOther(Tree node, P p) {
        return null;
    }
    public R visitErroneous(ErroneousTree node, P p) {
        return null;
    }
}
