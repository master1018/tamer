class AssignDivideExpression extends AssignOpExpression {
    public AssignDivideExpression(long where, Expression left, Expression right) {
        super(ASGDIV, where, left, right);
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_idiv + itype.getTypeCodeOffset());
    }
}
