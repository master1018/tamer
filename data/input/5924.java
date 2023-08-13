class FieldUpdater implements Constants {
    private long where;
    private MemberDefinition field;
    private Expression base;
    private MemberDefinition getter;
    private MemberDefinition setter;
    private int depth;
    public FieldUpdater(long where, MemberDefinition field,
                        Expression base, MemberDefinition getter, MemberDefinition setter) {
        this.where = where;
        this.field = field;
        this.base = base;
        this.getter = getter;
        this.setter = setter;
    }
    public FieldUpdater inline(Environment env, Context ctx) {
        if (base != null) {
            if (field.isStatic()) {
                base = base.inline(env, ctx);
            } else {
                base = base.inlineValue(env, ctx);
            }
        }
        return this;
    }
    public FieldUpdater copyInline(Context ctx) {
        return new FieldUpdater(where, field, base.copyInline(ctx), getter, setter);
    }
    public int costInline(int thresh, Environment env, Context ctx, boolean needGet) {
        int cost = needGet ? 7 : 3;  
        if (!field.isStatic() && base != null) {
            cost += base.costInline(thresh, env, ctx);
        }
        return cost;
    }
    private void codeDup(Assembler asm, int items, int depth) {
        switch (items) {
          case 0:
            return;
          case 1:
            switch (depth) {
              case 0:
                asm.add(where, opc_dup);
                return;
              case 1:
                asm.add(where, opc_dup_x1);
                return;
              case 2:
                asm.add(where, opc_dup_x2);
                return;
            }
            break;
          case 2:
            switch (depth) {
              case 0:
                asm.add(where, opc_dup2);
                return;
              case 1:
                asm.add(where, opc_dup2_x1);
                return;
              case 2:
                asm.add(where, opc_dup2_x2);
                return;
            }
            break;
        }
        throw new CompilerError("can't dup: " + items + ", " + depth);
    }
    public void startUpdate(Environment env, Context ctx, Assembler asm, boolean valNeeded) {
        if (!(getter.isStatic() && setter.isStatic())) {
            throw new CompilerError("startUpdate isStatic");
        }
        if (!field.isStatic()) {
            base.codeValue(env, ctx, asm);
            depth = 1;
        } else {
            if (base != null) {
                base.code(env, ctx, asm);
            }
            depth = 0;
        }
        codeDup(asm, depth, 0);
        asm.add(where, opc_invokestatic, getter);
        if (valNeeded) {
            codeDup(asm, field.getType().stackSize(), depth);
        }
    }
    public void finishUpdate(Environment env, Context ctx, Assembler asm, boolean valNeeded) {
        if (valNeeded) {
            codeDup(asm, field.getType().stackSize(), depth);
        }
        asm.add(where, opc_invokestatic, setter);
    }
    public void startAssign(Environment env, Context ctx, Assembler asm) {
        if (!setter.isStatic()) {
            throw new CompilerError("startAssign isStatic");
        }
        if (!field.isStatic()) {
            base.codeValue(env, ctx, asm);
            depth = 1;
        } else {
            if (base != null) {
                base.code(env, ctx, asm);
            }
            depth = 0;
        }
    }
    public void finishAssign(Environment env, Context ctx, Assembler asm, boolean valNeeded) {
        if (valNeeded) {
            codeDup(asm, field.getType().stackSize(), depth);
        }
        asm.add(where, opc_invokestatic, setter);
    }
}
