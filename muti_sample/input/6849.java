class BreakStatement extends Statement {
    Identifier lbl;
    public BreakStatement(long where, Identifier lbl) {
        super(BREAK, where);
        this.lbl = lbl;
    }
    Vset check(Environment env, Context ctx, Vset vset, Hashtable exp) {
        reach(env, vset);
        checkLabel(env, ctx);
        CheckContext destctx = (CheckContext)new CheckContext(ctx, this).getBreakContext(lbl);
        if (destctx != null) {
            if (destctx.frameNumber != ctx.frameNumber) {
                env.error(where, "branch.to.uplevel", lbl);
            }
            destctx.vsBreak = destctx.vsBreak.join(vset);
        } else {
            if (lbl != null) {
                env.error(where, "label.not.found", lbl);
            } else {
                env.error(where, "invalid.break");
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
        CodeContext newctx = new CodeContext(ctx, this);
        CodeContext destctx = (CodeContext)newctx.getBreakContext(lbl);
        codeFinally(env, ctx, asm, destctx, null);
        asm.add(where, opc_goto, destctx.breakLabel);
        asm.add(newctx.breakLabel);
    }
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("break");
        if (lbl != null) {
            out.print(" " + lbl);
        }
        out.print(";");
    }
}
