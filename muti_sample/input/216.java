class AssignBitXorExpression extends AssignOpExpression {
    public AssignBitXorExpression(long where, Expression left, Expression right) {
        super(ASGBITXOR, where, left, right);
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_ixor + itype.getTypeCodeOffset());
    }
}
