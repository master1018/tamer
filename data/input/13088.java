class AssignShiftLeftExpression extends AssignOpExpression {
    public AssignShiftLeftExpression(long where, Expression left, Expression right) {
        super(ASGLSHIFT, where, left, right);
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_ishl + itype.getTypeCodeOffset());
    }
}
