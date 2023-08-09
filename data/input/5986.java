class AssignBitAndExpression extends AssignOpExpression {
    public AssignBitAndExpression(long where, Expression left, Expression right) {
        super(ASGBITAND, where, left, right);
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_iand + itype.getTypeCodeOffset());
    }
}
