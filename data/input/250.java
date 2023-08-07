public class JmlExample extends JmlNode {
    public JmlExample(TokenReference where, long privacy, JmlSpecVarDecl[] specVarDecls, JmlRequiresClause[] specHeader, JmlSpecBodyClause[] specBody) {
        super(where);
        this.lightWeighted = false;
        this.privacy = privacy;
        this.specVarDecls = specVarDecls;
        this.specHeader = specHeader;
        this.specBody = specBody;
    }
    public JmlExample(TokenReference where, JmlSpecVarDecl[] specVarDecls, JmlRequiresClause[] specHeader, JmlSpecBodyClause[] specBody) {
        this(where, 0, specVarDecls, specHeader, specBody);
        this.lightWeighted = true;
    }
    public boolean isLightWeighted() {
        return lightWeighted;
    }
    public long privacy() {
        return privacy;
    }
    public JmlSpecVarDecl[] specVarDecls() {
        return specVarDecls;
    }
    public JmlRequiresClause[] specHeader() {
        return specHeader;
    }
    public JmlSpecBodyClause[] specClauses() {
        return specBody;
    }
    public JmlSpecBodyClause[] specBody() {
        return specBody;
    }
    public void typecheck(CFlowControlContextType context) throws PositionedError {
        long privacy = lightWeighted ? privacy(context) : this.privacy;
        CFlowControlContextType newCtx = context;
        if (specVarDecls != null) {
            newCtx = context.createFlowControlContext(getTokenReference());
            for (int i = 0; i < specVarDecls.length; i++) {
                specVarDecls[i].typecheck(newCtx, privacy);
            }
        }
        if (specHeader != null) {
            for (int i = 0; i < specHeader.length; i++) {
                if (specHeader[i] != null) {
                    specHeader[i].typecheck(newCtx, privacy);
                }
            }
        }
        if (specBody != null) {
            for (int i = 0; i < specBody.length; i++) {
                if (specBody[i] != null) {
                    specBody[i].typecheck(newCtx, privacy);
                }
            }
        }
        if (specVarDecls != null) {
            newCtx.checkingComplete();
        }
    }
    protected long privacy(CFlowControlContextType context) {
        long modifiers = context.getCMethod().modifiers();
        long privacy = modifiers & (ACC_SPEC_PUBLIC | ACC_SPEC_PROTECTED);
        if (privacy == 0) {
            privacy = modifiers & (ACC_PUBLIC | ACC_PROTECTED | ACC_PRIVATE);
        }
        return privacy;
    }
    public void accept(MjcVisitor p) {
        if (p instanceof JmlVisitor) ((JmlVisitor) p).visitJmlExample(this); else throw new UnsupportedOperationException(JmlNode.MJCVISIT_MESSAGE);
    }
    private long privacy;
    private JmlSpecVarDecl[] specVarDecls;
    private JmlRequiresClause[] specHeader;
    private JmlSpecBodyClause[] specBody;
    private boolean lightWeighted;
}
