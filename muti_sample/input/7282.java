class AssignAddExpression extends AssignOpExpression {
    public AssignAddExpression(long where, Expression left, Expression right) {
        super(ASGADD, where, left, right);
    }
    public int costInline(int thresh, Environment env, Context ctx) {
        return type.isType(TC_CLASS) ? 25 : super.costInline(thresh, env, ctx);
    }
    void code(Environment env, Context ctx, Assembler asm, boolean valNeeded) {
        if (itype.isType(TC_CLASS)) {
            try {
                Type argTypes[] = {Type.tString};
                ClassDeclaration c =
                    env.getClassDeclaration(idJavaLangStringBuffer);
                if (updater == null) {
                    asm.add(where, opc_new, c);
                    asm.add(where, opc_dup);
                    int depth = left.codeLValue(env, ctx, asm);
                    codeDup(env, ctx, asm, depth, 2); 
                    left.codeLoad(env, ctx, asm);
                    left.ensureString(env, ctx, asm);  
                    ClassDefinition sourceClass = ctx.field.getClassDefinition();
                    MemberDefinition f = c.getClassDefinition(env)
                        .matchMethod(env, sourceClass,
                                     idInit, argTypes);
                    asm.add(where, opc_invokespecial, f);
                    right.codeAppend(env, ctx, asm, c, false);
                    f = c.getClassDefinition(env)
                        .matchMethod(env, sourceClass, idToString);
                    asm.add(where, opc_invokevirtual, f);
                    if (valNeeded) {
                        codeDup(env, ctx, asm, Type.tString.stackSize(), depth);
                    }
                    left.codeStore(env, ctx, asm);
                } else {
                    updater.startUpdate(env, ctx, asm, false);
                    left.ensureString(env, ctx, asm);  
                    asm.add(where, opc_new, c);
                    asm.add(where, opc_dup_x1);
                    asm.add(where, opc_swap);
                    ClassDefinition sourceClass = ctx.field.getClassDefinition();
                    MemberDefinition f = c.getClassDefinition(env)
                        .matchMethod(env, sourceClass,
                                     idInit, argTypes);
                    asm.add(where, opc_invokespecial, f);
                    right.codeAppend(env, ctx, asm, c, false);
                    f = c.getClassDefinition(env)
                        .matchMethod(env, sourceClass, idToString);
                    asm.add(where, opc_invokevirtual, f);
                    updater.finishUpdate(env, ctx, asm, valNeeded);
                }
            } catch (ClassNotFound e) {
                throw new CompilerError(e);
            } catch (AmbiguousMember e) {
                throw new CompilerError(e);
            }
        } else {
            super.code(env, ctx, asm, valNeeded);
        }
    }
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_iadd + itype.getTypeCodeOffset());
    }
}
