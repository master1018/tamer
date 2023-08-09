class AssignSubtractExpression extends AssignOpExpression {
    public AssignSubtractExpression(long where, Expression left, Expression right) {
        super(ASGSUB, where, left, right);
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_isub + itype.getTypeCodeOffset());
    }
}
