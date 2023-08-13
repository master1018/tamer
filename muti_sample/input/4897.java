class ContinueStatement extends Statement {
    Identifier lbl;
    public ContinueStatement(long where, Identifier lbl) {
        super(CONTINUE, where);
        this.lbl = lbl;
    }
    Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        checkLabel(env, ctx);
        reach(env, vset);
        CheckContext destctx = (CheckContext)new CheckContext(ctx, this).getContinueContext(lbl);
        if (destctx != null) {
            switch (destctx.node.op) {
              case FOR:
              case DO:
              case WHILE:
                if (destctx.frameNumber != ctx.frameNumber) {
                    env.error(where, "branch.to.uplevel", lbl);
                }
                destctx.vsContinue = destctx.vsContinue.join(vset);
                break;
              default:
                env.error(where, "invalid.continue");
            }
        } else {
            if (lbl != null) {
                env.error(where, "label.not.found", lbl);
            } else {
                env.error(where, "invalid.continue");
            }
        }
        CheckContext exitctx = ctx.getTryExitContext();
        if (exitctx != null) {
            exitctx.vsTryExit = exitctx.vsTryExit.join(vset);
        }
        return DEAD_END;
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        return 1;
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        CodeContext destctx = (CodeContext)ctx.getContinueContext(lbl);
        codeFinally(env, ctx, asm, destctx, null);
        asm.add(where, opc_goto, destctx.contLabel);
    }
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("continue");
        if (lbl != null) {
            out.print(" " + lbl);
        }
        out.print(";");
    }
}
