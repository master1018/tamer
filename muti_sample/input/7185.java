public class EndPosParser extends JavacParser {
    public EndPosParser(ParserFactory fac, Lexer S, boolean keepDocComments, boolean keepLineMap) {
        super(fac, S, keepDocComments, keepLineMap);
        this.S = S;
        endPositions = new HashMap<JCTree,Integer>();
    }
    private Lexer S;
    Map<JCTree, Integer> endPositions;
    @Override
    protected void storeEnd(JCTree tree, int endpos) {
        int errorEndPos = getErrorEndPos();
        endPositions.put(tree, errorEndPos > endpos ? errorEndPos : endpos);
    }
    @Override
    protected <T extends JCTree> T to(T t) {
        storeEnd(t, S.endPos());
        return t;
    }
    @Override
    protected <T extends JCTree> T toP(T t) {
        storeEnd(t, S.prevEndPos());
        return t;
    }
    @Override
    public JCCompilationUnit parseCompilationUnit() {
        JCCompilationUnit t = super.parseCompilationUnit();
        t.endPositions = endPositions;
        return t;
    }
    @Override
    JCExpression parExpression() {
        int pos = S.pos();
        JCExpression t = super.parExpression();
        return toP(F.at(pos).Parens(t));
    }
    @Override
    public int getEndPos(JCTree tree) {
        return TreeInfo.getEndPos(tree, endPositions);
    }
}
