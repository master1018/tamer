class AssignMultiplyExpression extends AssignOpExpression {
    public AssignMultiplyExpression(long where, Expression left, Expression right) {
        super(ASGMUL, where, left, right);
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_imul + itype.getTypeCodeOffset());
    }
}
