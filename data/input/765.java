public class Else extends Statement {
    private final Statement[] statements;
    public Else(Statement[] statements, int sourceStart, int sourceEnd, int beginLine, int endLine, int beginColumn, int endColumn) {
        super(sourceStart, sourceEnd, beginLine, endLine, beginColumn, endColumn);
        this.statements = statements;
    }
    public Else(Statement statement, int sourceStart, int sourceEnd, int beginLine, int endLine, int beginColumn, int endColumn) {
        super(sourceStart, sourceEnd, beginLine, endLine, beginColumn, endColumn);
        statements = new Statement[1];
        statements[0] = statement;
    }
    @Override
    public String toString(int tab) {
        StringBuilder buff = new StringBuilder(tabString(tab));
        buff.append("else \n");
        Statement statement;
        for (int i = 0; i < statements.length; i++) {
            statement = statements[i];
            buff.append(statement.toString(tab + 1)).append('\n');
        }
        return buff.toString();
    }
    @Override
    public void getOutsideVariable(List<VariableUsage> list) {
        for (int i = 0; i < statements.length; i++) {
            statements[i].getOutsideVariable(list);
        }
    }
    @Override
    public void getModifiedVariable(List<VariableUsage> list) {
        for (int i = 0; i < statements.length; i++) {
            statements[i].getModifiedVariable(list);
        }
    }
    @Override
    public void getUsedVariable(List<VariableUsage> list) {
        for (int i = 0; i < statements.length; i++) {
            statements[i].getUsedVariable(list);
        }
    }
    @Override
    public AstNode subNodeAt(int line, int column) {
        for (int i = 0; i < statements.length; i++) {
            Statement statement = statements[i];
            if (statement.isAt(line, column)) return statement.subNodeAt(line, column);
        }
        return null;
    }
    @Override
    public void analyzeCode(PHPParser parser) {
        for (int i = 0; i < statements.length; i++) {
            statements[i].analyzeCode(parser);
        }
    }
}
