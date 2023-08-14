class IncDecExpression extends UnaryExpression {
    private FieldUpdater updater = null;
    public IncDecExpression(int op, long where, Expression right) {
        super(op, where, right.type, right);
    }
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable exp) {
        vset = right.checkAssignOp(env, ctx, vset, exp, this);
        if (right.type.inMask(TM_NUMBER)) {
            type = right.type;
        } else {
            if (!right.type.isType(TC_ERROR)) {
                env.error(where, "invalid.arg.type", right.type, opNames[op]);
            }
            type = Type.tError;
        }
        updater = right.getUpdater(env, ctx);  
        return vset;
    }
    public Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        return checkValue(env, ctx, vset, exp);
    }
    public Expression inline(Environment env, Context ctx) {
        return inlineValue(env, ctx);
    }
    public Expression inlineValue(Environment env, Context ctx) {
        right = right.inlineValue(env, ctx);
        if (updater != null) {
            updater = updater.inline(env, ctx);
        }
        return this;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        if (updater == null) {
            if ((right.op == IDENT) && type.isType(TC_INT) &&
                (((IdentifierExpression)right).field.isLocal())) {
                return 3;
            }
            return right.costInline(thresh, env, ctx) + 4;
        } else {
            return updater.costInline(thresh, env, ctx, true) + 1;
        }
    }
    private void codeIncDecOp(Assembler asm, boolean inc) {
        switch (type.getTypeCode()) {
          case TC_BYTE:
            asm.add(where, opc_ldc, new Integer(1));
            asm.add(where, inc ? opc_iadd : opc_isub);
            asm.add(where, opc_i2b);
            break;
          case TC_SHORT:
            asm.add(where, opc_ldc, new Integer(1));
            asm.add(where, inc ? opc_iadd : opc_isub);
            asm.add(where, opc_i2s);
            break;
          case TC_CHAR:
            asm.add(where, opc_ldc, new Integer(1));
            asm.add(where, inc ? opc_iadd : opc_isub);
            asm.add(where, opc_i2c);
            break;
          case TC_INT:
            asm.add(where, opc_ldc, new Integer(1));
            asm.add(where, inc ? opc_iadd : opc_isub);
            break;
          case TC_LONG:
            asm.add(where, opc_ldc2_w, new Long(1));
            asm.add(where, inc ? opc_ladd : opc_lsub);
            break;
          case TC_FLOAT:
            asm.add(where, opc_ldc, new Float(1));
            asm.add(where, inc ? opc_fadd : opc_fsub);
            break;
          case TC_DOUBLE:
            asm.add(where, opc_ldc2_w, new Double(1));
            asm.add(where, inc ? opc_dadd : opc_dsub);
            break;
          default:
            throw new CompilerError("invalid type");
        }
    }
    void codeIncDec(Environment env, Context ctx, Assembler asm, boolean inc, boolean prefix, boolean valNeeded) {
        if ((right.op == IDENT) && type.isType(TC_INT) &&
            (((IdentifierExpression)right).field.isLocal()) && updater == null) {
            if (valNeeded && !prefix) {
                right.codeLoad(env, ctx, asm);
            }
            int v = ((LocalMember)((IdentifierExpression)right).field).number;
            int[] operands = { v, inc ? 1 : -1 };
            asm.add(where, opc_iinc, operands);
            if (valNeeded && prefix) {
                right.codeLoad(env, ctx, asm);
            }
            return;
        }
        if (updater == null) {
            int depth = right.codeLValue(env, ctx, asm);
            codeDup(env, ctx, asm, depth, 0);
            right.codeLoad(env, ctx, asm);
            if (valNeeded && !prefix) {
                codeDup(env, ctx, asm, type.stackSize(), depth);
            }
            codeIncDecOp(asm, inc);
            if (valNeeded && prefix) {
                codeDup(env, ctx, asm, type.stackSize(), depth);
            }
            right.codeStore(env, ctx, asm);
        } else {
            updater.startUpdate(env, ctx, asm, (valNeeded && !prefix));
            codeIncDecOp(asm, inc);
            updater.finishUpdate(env, ctx, asm, (valNeeded && prefix));
        }
    }
}
