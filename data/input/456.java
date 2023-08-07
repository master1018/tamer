public class ImportAST extends BaseAST {
    private QualifiedNameAST mQualifiedName;
    private String mAlias;
    public ImportAST(QualifiedNameAST aQualifiedName, String anAlias) {
        mQualifiedName = aQualifiedName;
        mAlias = anAlias;
    }
    public QualifiedNameAST getQualifiedNameAST() {
        return mQualifiedName;
    }
    public String getAlias() {
        return mAlias;
    }
    protected Class getType0() throws QueryException {
        return null;
    }
    protected UnaryFunctor resolve0() throws QueryException {
        EvaluatorContext.getContext().addImport(mQualifiedName.getQualifiedName(), mAlias);
        return null;
    }
}
