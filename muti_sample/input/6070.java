class AssignBitOrExpression extends AssignOpExpression {
    public AssignBitOrExpression(long where, Expression left, Expression right) {
        super(ASGBITOR, where, left, right);
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_ior + itype.getTypeCodeOffset());
    }
}
