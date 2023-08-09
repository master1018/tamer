public class Flow extends TreeScanner {
    protected static final Context.Key<Flow> flowKey =
        new Context.Key<Flow>();
    private final Names names;
    private final Log log;
    private final Symtab syms;
    private final Types types;
    private final Check chk;
    private       TreeMaker make;
    private final Resolve rs;
    private Env<AttrContext> attrEnv;
    private       Lint lint;
    private final boolean allowImprovedRethrowAnalysis;
    private final boolean allowImprovedCatchAnalysis;
    public static Flow instance(Context context) {
        Flow instance = context.get(flowKey);
        if (instance == null)
            instance = new Flow(context);
        return instance;
    }
    protected Flow(Context context) {
        context.put(flowKey, this);
        names = Names.instance(context);
        log = Log.instance(context);
        syms = Symtab.instance(context);
        types = Types.instance(context);
        chk = Check.instance(context);
        lint = Lint.instance(context);
        rs = Resolve.instance(context);
        Source source = Source.instance(context);
        allowImprovedRethrowAnalysis = source.allowImprovedRethrowAnalysis();
        allowImprovedCatchAnalysis = source.allowImprovedCatchAnalysis();
    }
    private boolean alive;
    Bits inits;
    Bits uninits;
    HashMap<Symbol, List<Type>> preciseRethrowTypes;
    Bits uninitsTry;
    Bits initsWhenTrue;
    Bits initsWhenFalse;
    Bits uninitsWhenTrue;
    Bits uninitsWhenFalse;
    VarSymbol[] vars;
    JCClassDecl classDef;
    int firstadr;
    int nextadr;
    List<Type> thrown;
    List<Type> caught;
    Scope unrefdResources;
    boolean loopPassTwo = false;
    static class PendingExit {
        JCTree tree;
        Bits inits;
        Bits uninits;
        Type thrown;
        PendingExit(JCTree tree, Bits inits, Bits uninits) {
            this.tree = tree;
            this.inits = inits.dup();
            this.uninits = uninits.dup();
        }
        PendingExit(JCTree tree, Type thrown) {
            this.tree = tree;
            this.thrown = thrown;
        }
    }
    ListBuffer<PendingExit> pendingExits;
    void errorUncaught() {
        for (PendingExit exit = pendingExits.next();
             exit != null;
             exit = pendingExits.next()) {
            if (classDef != null &&
                classDef.pos == exit.tree.pos) {
                log.error(exit.tree.pos(),
                        "unreported.exception.default.constructor",
                        exit.thrown);
            } else if (exit.tree.getTag() == JCTree.VARDEF &&
                    ((JCVariableDecl)exit.tree).sym.isResourceVariable()) {
                log.error(exit.tree.pos(),
                        "unreported.exception.implicit.close",
                        exit.thrown,
                        ((JCVariableDecl)exit.tree).sym.name);
            } else {
                log.error(exit.tree.pos(),
                        "unreported.exception.need.to.catch.or.throw",
                        exit.thrown);
            }
        }
    }
    void markThrown(JCTree tree, Type exc) {
        if (!chk.isUnchecked(tree.pos(), exc)) {
            if (!chk.isHandled(exc, caught))
                pendingExits.append(new PendingExit(tree, exc));
                thrown = chk.incl(exc, thrown);
        }
    }
    boolean trackable(VarSymbol sym) {
        return
            (sym.owner.kind == MTH ||
             ((sym.flags() & (FINAL | HASINIT | PARAMETER)) == FINAL &&
              classDef.sym.isEnclosedBy((ClassSymbol)sym.owner)));
    }
    void newVar(VarSymbol sym) {
        if (nextadr == vars.length) {
            VarSymbol[] newvars = new VarSymbol[nextadr * 2];
            System.arraycopy(vars, 0, newvars, 0, nextadr);
            vars = newvars;
        }
        sym.adr = nextadr;
        vars[nextadr] = sym;
        inits.excl(nextadr);
        uninits.incl(nextadr);
        nextadr++;
    }
    void letInit(DiagnosticPosition pos, VarSymbol sym) {
        if (sym.adr >= firstadr && trackable(sym)) {
            if ((sym.flags() & FINAL) != 0) {
                if ((sym.flags() & PARAMETER) != 0) {
                    if ((sym.flags() & UNION) != 0) { 
                        log.error(pos, "multicatch.parameter.may.not.be.assigned",
                                  sym);
                    }
                    else {
                        log.error(pos, "final.parameter.may.not.be.assigned",
                              sym);
                    }
                } else if (!uninits.isMember(sym.adr)) {
                    log.error(pos,
                              loopPassTwo
                              ? "var.might.be.assigned.in.loop"
                              : "var.might.already.be.assigned",
                              sym);
                } else if (!inits.isMember(sym.adr)) {
                    uninits.excl(sym.adr);
                    uninitsTry.excl(sym.adr);
                } else {
                    uninits.excl(sym.adr);
                }
            }
            inits.incl(sym.adr);
        } else if ((sym.flags() & FINAL) != 0) {
            log.error(pos, "var.might.already.be.assigned", sym);
        }
    }
    void letInit(JCTree tree) {
        tree = TreeInfo.skipParens(tree);
        if (tree.getTag() == JCTree.IDENT || tree.getTag() == JCTree.SELECT) {
            Symbol sym = TreeInfo.symbol(tree);
            if (sym.kind == VAR) {
                letInit(tree.pos(), (VarSymbol)sym);
            }
        }
    }
    void checkInit(DiagnosticPosition pos, VarSymbol sym) {
        if ((sym.adr >= firstadr || sym.owner.kind != TYP) &&
            trackable(sym) &&
            !inits.isMember(sym.adr)) {
            log.error(pos, "var.might.not.have.been.initialized",
                      sym);
            inits.incl(sym.adr);
        }
    }
    void recordExit(JCTree tree) {
        pendingExits.append(new PendingExit(tree, inits, uninits));
        markDead();
    }
    boolean resolveBreaks(JCTree tree,
                          ListBuffer<PendingExit> oldPendingExits) {
        boolean result = false;
        List<PendingExit> exits = pendingExits.toList();
        pendingExits = oldPendingExits;
        for (; exits.nonEmpty(); exits = exits.tail) {
            PendingExit exit = exits.head;
            if (exit.tree.getTag() == JCTree.BREAK &&
                ((JCBreak) exit.tree).target == tree) {
                inits.andSet(exit.inits);
                uninits.andSet(exit.uninits);
                result = true;
            } else {
                pendingExits.append(exit);
            }
        }
        return result;
    }
    boolean resolveContinues(JCTree tree) {
        boolean result = false;
        List<PendingExit> exits = pendingExits.toList();
        pendingExits = new ListBuffer<PendingExit>();
        for (; exits.nonEmpty(); exits = exits.tail) {
            PendingExit exit = exits.head;
            if (exit.tree.getTag() == JCTree.CONTINUE &&
                ((JCContinue) exit.tree).target == tree) {
                inits.andSet(exit.inits);
                uninits.andSet(exit.uninits);
                result = true;
            } else {
                pendingExits.append(exit);
            }
        }
        return result;
    }
    void markDead() {
        inits.inclRange(firstadr, nextadr);
        uninits.inclRange(firstadr, nextadr);
        alive = false;
    }
    void split(boolean setToNull) {
        initsWhenFalse = inits.dup();
        uninitsWhenFalse = uninits.dup();
        initsWhenTrue = inits;
        uninitsWhenTrue = uninits;
        if (setToNull)
            inits = uninits = null;
    }
    void merge() {
        inits = initsWhenFalse.andSet(initsWhenTrue);
        uninits = uninitsWhenFalse.andSet(uninitsWhenTrue);
    }
    void scanDef(JCTree tree) {
        scanStat(tree);
        if (tree != null && tree.getTag() == JCTree.BLOCK && !alive) {
            log.error(tree.pos(),
                      "initializer.must.be.able.to.complete.normally");
        }
    }
    void scanStat(JCTree tree) {
        if (!alive && tree != null) {
            log.error(tree.pos(), "unreachable.stmt");
            if (tree.getTag() != JCTree.SKIP) alive = true;
        }
        scan(tree);
    }
    void scanStats(List<? extends JCStatement> trees) {
        if (trees != null)
            for (List<? extends JCStatement> l = trees; l.nonEmpty(); l = l.tail)
                scanStat(l.head);
    }
    void scanExpr(JCTree tree) {
        if (tree != null) {
            scan(tree);
            if (inits == null) merge();
        }
    }
    void scanExprs(List<? extends JCExpression> trees) {
        if (trees != null)
            for (List<? extends JCExpression> l = trees; l.nonEmpty(); l = l.tail)
                scanExpr(l.head);
    }
    void scanCond(JCTree tree) {
        if (tree.type.isFalse()) {
            if (inits == null) merge();
            initsWhenTrue = inits.dup();
            initsWhenTrue.inclRange(firstadr, nextadr);
            uninitsWhenTrue = uninits.dup();
            uninitsWhenTrue.inclRange(firstadr, nextadr);
            initsWhenFalse = inits;
            uninitsWhenFalse = uninits;
        } else if (tree.type.isTrue()) {
            if (inits == null) merge();
            initsWhenFalse = inits.dup();
            initsWhenFalse.inclRange(firstadr, nextadr);
            uninitsWhenFalse = uninits.dup();
            uninitsWhenFalse.inclRange(firstadr, nextadr);
            initsWhenTrue = inits;
            uninitsWhenTrue = uninits;
        } else {
            scan(tree);
            if (inits != null)
                split(tree.type != syms.unknownType);
        }
        if (tree.type != syms.unknownType)
            inits = uninits = null;
    }
    public void visitClassDef(JCClassDecl tree) {
        if (tree.sym == null) return;
        JCClassDecl classDefPrev = classDef;
        List<Type> thrownPrev = thrown;
        List<Type> caughtPrev = caught;
        boolean alivePrev = alive;
        int firstadrPrev = firstadr;
        int nextadrPrev = nextadr;
        ListBuffer<PendingExit> pendingExitsPrev = pendingExits;
        Lint lintPrev = lint;
        pendingExits = new ListBuffer<PendingExit>();
        if (tree.name != names.empty) {
            caught = List.nil();
            firstadr = nextadr;
        }
        classDef = tree;
        thrown = List.nil();
        lint = lint.augment(tree.sym.attributes_field);
        try {
            for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                if (l.head.getTag() == JCTree.VARDEF) {
                    JCVariableDecl def = (JCVariableDecl)l.head;
                    if ((def.mods.flags & STATIC) != 0) {
                        VarSymbol sym = def.sym;
                        if (trackable(sym))
                            newVar(sym);
                    }
                }
            }
            for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                if (l.head.getTag() != JCTree.METHODDEF &&
                    (TreeInfo.flags(l.head) & STATIC) != 0) {
                    scanDef(l.head);
                    errorUncaught();
                }
            }
            if (tree.name != names.empty) {
                boolean firstConstructor = true;
                for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                    if (TreeInfo.isInitialConstructor(l.head)) {
                        List<Type> mthrown =
                            ((JCMethodDecl) l.head).sym.type.getThrownTypes();
                        if (firstConstructor) {
                            caught = mthrown;
                            firstConstructor = false;
                        } else {
                            caught = chk.intersect(mthrown, caught);
                        }
                    }
                }
            }
            for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                if (l.head.getTag() == JCTree.VARDEF) {
                    JCVariableDecl def = (JCVariableDecl)l.head;
                    if ((def.mods.flags & STATIC) == 0) {
                        VarSymbol sym = def.sym;
                        if (trackable(sym))
                            newVar(sym);
                    }
                }
            }
            for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                if (l.head.getTag() != JCTree.METHODDEF &&
                    (TreeInfo.flags(l.head) & STATIC) == 0) {
                    scanDef(l.head);
                    errorUncaught();
                }
            }
            if (tree.name == names.empty) {
                for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                    if (TreeInfo.isInitialConstructor(l.head)) {
                        JCMethodDecl mdef = (JCMethodDecl)l.head;
                        mdef.thrown = make.Types(thrown);
                        mdef.sym.type = types.createMethodTypeWithThrown(mdef.sym.type, thrown);
                    }
                }
                thrownPrev = chk.union(thrown, thrownPrev);
            }
            for (List<JCTree> l = tree.defs; l.nonEmpty(); l = l.tail) {
                if (l.head.getTag() == JCTree.METHODDEF) {
                    scan(l.head);
                    errorUncaught();
                }
            }
            thrown = thrownPrev;
        } finally {
            pendingExits = pendingExitsPrev;
            alive = alivePrev;
            nextadr = nextadrPrev;
            firstadr = firstadrPrev;
            caught = caughtPrev;
            classDef = classDefPrev;
            lint = lintPrev;
        }
    }
    public void visitMethodDef(JCMethodDecl tree) {
        if (tree.body == null) return;
        List<Type> caughtPrev = caught;
        List<Type> mthrown = tree.sym.type.getThrownTypes();
        Bits initsPrev = inits.dup();
        Bits uninitsPrev = uninits.dup();
        int nextadrPrev = nextadr;
        int firstadrPrev = firstadr;
        Lint lintPrev = lint;
        lint = lint.augment(tree.sym.attributes_field);
        Assert.check(pendingExits.isEmpty());
        try {
            boolean isInitialConstructor =
                TreeInfo.isInitialConstructor(tree);
            if (!isInitialConstructor)
                firstadr = nextadr;
            for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail) {
                JCVariableDecl def = l.head;
                scan(def);
                inits.incl(def.sym.adr);
                uninits.excl(def.sym.adr);
            }
            if (isInitialConstructor)
                caught = chk.union(caught, mthrown);
            else if ((tree.sym.flags() & (BLOCK | STATIC)) != BLOCK)
                caught = mthrown;
            alive = true;
            scanStat(tree.body);
            if (alive && tree.sym.type.getReturnType().tag != VOID)
                log.error(TreeInfo.diagEndPos(tree.body), "missing.ret.stmt");
            if (isInitialConstructor) {
                for (int i = firstadr; i < nextadr; i++)
                    if (vars[i].owner == classDef.sym)
                        checkInit(TreeInfo.diagEndPos(tree.body), vars[i]);
            }
            List<PendingExit> exits = pendingExits.toList();
            pendingExits = new ListBuffer<PendingExit>();
            while (exits.nonEmpty()) {
                PendingExit exit = exits.head;
                exits = exits.tail;
                if (exit.thrown == null) {
                    Assert.check(exit.tree.getTag() == JCTree.RETURN);
                    if (isInitialConstructor) {
                        inits = exit.inits;
                        for (int i = firstadr; i < nextadr; i++)
                            checkInit(exit.tree.pos(), vars[i]);
                    }
                } else {
                    pendingExits.append(exit);
                }
            }
        } finally {
            inits = initsPrev;
            uninits = uninitsPrev;
            nextadr = nextadrPrev;
            firstadr = firstadrPrev;
            caught = caughtPrev;
            lint = lintPrev;
        }
    }
    public void visitVarDef(JCVariableDecl tree) {
        boolean track = trackable(tree.sym);
        if (track && tree.sym.owner.kind == MTH) newVar(tree.sym);
        if (tree.init != null) {
            Lint lintPrev = lint;
            lint = lint.augment(tree.sym.attributes_field);
            try{
                scanExpr(tree.init);
                if (track) letInit(tree.pos(), tree.sym);
            } finally {
                lint = lintPrev;
            }
        }
    }
    public void visitBlock(JCBlock tree) {
        int nextadrPrev = nextadr;
        scanStats(tree.stats);
        nextadr = nextadrPrev;
    }
    public void visitDoLoop(JCDoWhileLoop tree) {
        ListBuffer<PendingExit> prevPendingExits = pendingExits;
        boolean prevLoopPassTwo = loopPassTwo;
        pendingExits = new ListBuffer<PendingExit>();
        int prevErrors = log.nerrors;
        do {
            Bits uninitsEntry = uninits.dup();
            uninitsEntry.excludeFrom(nextadr);
            scanStat(tree.body);
            alive |= resolveContinues(tree);
            scanCond(tree.cond);
            if (log.nerrors !=  prevErrors ||
                loopPassTwo ||
                uninitsEntry.dup().diffSet(uninitsWhenTrue).nextBit(firstadr)==-1)
                break;
            inits = initsWhenTrue;
            uninits = uninitsEntry.andSet(uninitsWhenTrue);
            loopPassTwo = true;
            alive = true;
        } while (true);
        loopPassTwo = prevLoopPassTwo;
        inits = initsWhenFalse;
        uninits = uninitsWhenFalse;
        alive = alive && !tree.cond.type.isTrue();
        alive |= resolveBreaks(tree, prevPendingExits);
    }
    public void visitWhileLoop(JCWhileLoop tree) {
        ListBuffer<PendingExit> prevPendingExits = pendingExits;
        boolean prevLoopPassTwo = loopPassTwo;
        Bits initsCond;
        Bits uninitsCond;
        pendingExits = new ListBuffer<PendingExit>();
        int prevErrors = log.nerrors;
        do {
            Bits uninitsEntry = uninits.dup();
            uninitsEntry.excludeFrom(nextadr);
            scanCond(tree.cond);
            initsCond = initsWhenFalse;
            uninitsCond = uninitsWhenFalse;
            inits = initsWhenTrue;
            uninits = uninitsWhenTrue;
            alive = !tree.cond.type.isFalse();
            scanStat(tree.body);
            alive |= resolveContinues(tree);
            if (log.nerrors != prevErrors ||
                loopPassTwo ||
                uninitsEntry.dup().diffSet(uninits).nextBit(firstadr) == -1)
                break;
            uninits = uninitsEntry.andSet(uninits);
            loopPassTwo = true;
            alive = true;
        } while (true);
        loopPassTwo = prevLoopPassTwo;
        inits = initsCond;
        uninits = uninitsCond;
        alive = resolveBreaks(tree, prevPendingExits) ||
            !tree.cond.type.isTrue();
    }
    public void visitForLoop(JCForLoop tree) {
        ListBuffer<PendingExit> prevPendingExits = pendingExits;
        boolean prevLoopPassTwo = loopPassTwo;
        int nextadrPrev = nextadr;
        scanStats(tree.init);
        Bits initsCond;
        Bits uninitsCond;
        pendingExits = new ListBuffer<PendingExit>();
        int prevErrors = log.nerrors;
        do {
            Bits uninitsEntry = uninits.dup();
            uninitsEntry.excludeFrom(nextadr);
            if (tree.cond != null) {
                scanCond(tree.cond);
                initsCond = initsWhenFalse;
                uninitsCond = uninitsWhenFalse;
                inits = initsWhenTrue;
                uninits = uninitsWhenTrue;
                alive = !tree.cond.type.isFalse();
            } else {
                initsCond = inits.dup();
                initsCond.inclRange(firstadr, nextadr);
                uninitsCond = uninits.dup();
                uninitsCond.inclRange(firstadr, nextadr);
                alive = true;
            }
            scanStat(tree.body);
            alive |= resolveContinues(tree);
            scan(tree.step);
            if (log.nerrors != prevErrors ||
                loopPassTwo ||
                uninitsEntry.dup().diffSet(uninits).nextBit(firstadr) == -1)
                break;
            uninits = uninitsEntry.andSet(uninits);
            loopPassTwo = true;
            alive = true;
        } while (true);
        loopPassTwo = prevLoopPassTwo;
        inits = initsCond;
        uninits = uninitsCond;
        alive = resolveBreaks(tree, prevPendingExits) ||
            tree.cond != null && !tree.cond.type.isTrue();
        nextadr = nextadrPrev;
    }
    public void visitForeachLoop(JCEnhancedForLoop tree) {
        visitVarDef(tree.var);
        ListBuffer<PendingExit> prevPendingExits = pendingExits;
        boolean prevLoopPassTwo = loopPassTwo;
        int nextadrPrev = nextadr;
        scan(tree.expr);
        Bits initsStart = inits.dup();
        Bits uninitsStart = uninits.dup();
        letInit(tree.pos(), tree.var.sym);
        pendingExits = new ListBuffer<PendingExit>();
        int prevErrors = log.nerrors;
        do {
            Bits uninitsEntry = uninits.dup();
            uninitsEntry.excludeFrom(nextadr);
            scanStat(tree.body);
            alive |= resolveContinues(tree);
            if (log.nerrors != prevErrors ||
                loopPassTwo ||
                uninitsEntry.dup().diffSet(uninits).nextBit(firstadr) == -1)
                break;
            uninits = uninitsEntry.andSet(uninits);
            loopPassTwo = true;
            alive = true;
        } while (true);
        loopPassTwo = prevLoopPassTwo;
        inits = initsStart;
        uninits = uninitsStart.andSet(uninits);
        resolveBreaks(tree, prevPendingExits);
        alive = true;
        nextadr = nextadrPrev;
    }
    public void visitLabelled(JCLabeledStatement tree) {
        ListBuffer<PendingExit> prevPendingExits = pendingExits;
        pendingExits = new ListBuffer<PendingExit>();
        scanStat(tree.body);
        alive |= resolveBreaks(tree, prevPendingExits);
    }
    public void visitSwitch(JCSwitch tree) {
        ListBuffer<PendingExit> prevPendingExits = pendingExits;
        pendingExits = new ListBuffer<PendingExit>();
        int nextadrPrev = nextadr;
        scanExpr(tree.selector);
        Bits initsSwitch = inits;
        Bits uninitsSwitch = uninits.dup();
        boolean hasDefault = false;
        for (List<JCCase> l = tree.cases; l.nonEmpty(); l = l.tail) {
            alive = true;
            inits = initsSwitch.dup();
            uninits = uninits.andSet(uninitsSwitch);
            JCCase c = l.head;
            if (c.pat == null)
                hasDefault = true;
            else
                scanExpr(c.pat);
            scanStats(c.stats);
            addVars(c.stats, initsSwitch, uninitsSwitch);
            if (!loopPassTwo &&
                alive &&
                lint.isEnabled(Lint.LintCategory.FALLTHROUGH) &&
                c.stats.nonEmpty() && l.tail.nonEmpty())
                log.warning(Lint.LintCategory.FALLTHROUGH,
                            l.tail.head.pos(),
                            "possible.fall-through.into.case");
        }
        if (!hasDefault) {
            inits.andSet(initsSwitch);
            alive = true;
        }
        alive |= resolveBreaks(tree, prevPendingExits);
        nextadr = nextadrPrev;
    }
        private static void addVars(List<JCStatement> stats, Bits inits,
                                    Bits uninits) {
            for (;stats.nonEmpty(); stats = stats.tail) {
                JCTree stat = stats.head;
                if (stat.getTag() == JCTree.VARDEF) {
                    int adr = ((JCVariableDecl) stat).sym.adr;
                    inits.excl(adr);
                    uninits.incl(adr);
                }
            }
        }
    public void visitTry(JCTry tree) {
        List<Type> caughtPrev = caught;
        List<Type> thrownPrev = thrown;
        thrown = List.nil();
        for (List<JCCatch> l = tree.catchers; l.nonEmpty(); l = l.tail) {
            List<JCExpression> subClauses = TreeInfo.isMultiCatch(l.head) ?
                    ((JCTypeUnion)l.head.param.vartype).alternatives :
                    List.of(l.head.param.vartype);
            for (JCExpression ct : subClauses) {
                caught = chk.incl(ct.type, caught);
            }
        }
        ListBuffer<JCVariableDecl> resourceVarDecls = ListBuffer.lb();
        Bits uninitsTryPrev = uninitsTry;
        ListBuffer<PendingExit> prevPendingExits = pendingExits;
        pendingExits = new ListBuffer<PendingExit>();
        Bits initsTry = inits.dup();
        uninitsTry = uninits.dup();
        for (JCTree resource : tree.resources) {
            if (resource instanceof JCVariableDecl) {
                JCVariableDecl vdecl = (JCVariableDecl) resource;
                visitVarDef(vdecl);
                unrefdResources.enter(vdecl.sym);
                resourceVarDecls.append(vdecl);
            } else if (resource instanceof JCExpression) {
                scanExpr((JCExpression) resource);
            } else {
                throw new AssertionError(tree);  
            }
        }
        for (JCTree resource : tree.resources) {
            List<Type> closeableSupertypes = resource.type.isCompound() ?
                types.interfaces(resource.type).prepend(types.supertype(resource.type)) :
                List.of(resource.type);
            for (Type sup : closeableSupertypes) {
                if (types.asSuper(sup, syms.autoCloseableType.tsym) != null) {
                    Symbol closeMethod = rs.resolveQualifiedMethod(tree,
                            attrEnv,
                            sup,
                            names.close,
                            List.<Type>nil(),
                            List.<Type>nil());
                    if (closeMethod.kind == MTH) {
                        for (Type t : ((MethodSymbol)closeMethod).getThrownTypes()) {
                            markThrown(resource, t);
                        }
                    }
                }
            }
        }
        scanStat(tree.body);
        List<Type> thrownInTry = allowImprovedCatchAnalysis ?
            chk.union(thrown, List.of(syms.runtimeExceptionType, syms.errorType)) :
            thrown;
        thrown = thrownPrev;
        caught = caughtPrev;
        boolean aliveEnd = alive;
        uninitsTry.andSet(uninits);
        Bits initsEnd = inits;
        Bits uninitsEnd = uninits;
        int nextadrCatch = nextadr;
        if (!resourceVarDecls.isEmpty() &&
                lint.isEnabled(Lint.LintCategory.TRY)) {
            for (JCVariableDecl resVar : resourceVarDecls) {
                if (unrefdResources.includes(resVar.sym)) {
                    log.warning(Lint.LintCategory.TRY, resVar.pos(),
                                "try.resource.not.referenced", resVar.sym);
                    unrefdResources.remove(resVar.sym);
                }
            }
        }
        List<Type> caughtInTry = List.nil();
        for (List<JCCatch> l = tree.catchers; l.nonEmpty(); l = l.tail) {
            alive = true;
            JCVariableDecl param = l.head.param;
            List<JCExpression> subClauses = TreeInfo.isMultiCatch(l.head) ?
                    ((JCTypeUnion)l.head.param.vartype).alternatives :
                    List.of(l.head.param.vartype);
            List<Type> ctypes = List.nil();
            List<Type> rethrownTypes = chk.diff(thrownInTry, caughtInTry);
            for (JCExpression ct : subClauses) {
                Type exc = ct.type;
                if (exc != syms.unknownType) {
                    ctypes = ctypes.append(exc);
                    if (types.isSameType(exc, syms.objectType))
                        continue;
                    checkCaughtType(l.head.pos(), exc, thrownInTry, caughtInTry);
                    caughtInTry = chk.incl(exc, caughtInTry);
                }
            }
            inits = initsTry.dup();
            uninits = uninitsTry.dup();
            scan(param);
            inits.incl(param.sym.adr);
            uninits.excl(param.sym.adr);
            preciseRethrowTypes.put(param.sym, chk.intersect(ctypes, rethrownTypes));
            scanStat(l.head.body);
            initsEnd.andSet(inits);
            uninitsEnd.andSet(uninits);
            nextadr = nextadrCatch;
            preciseRethrowTypes.remove(param.sym);
            aliveEnd |= alive;
        }
        if (tree.finalizer != null) {
            List<Type> savedThrown = thrown;
            thrown = List.nil();
            inits = initsTry.dup();
            uninits = uninitsTry.dup();
            ListBuffer<PendingExit> exits = pendingExits;
            pendingExits = prevPendingExits;
            alive = true;
            scanStat(tree.finalizer);
            if (!alive) {
                thrown = chk.union(thrown, thrownPrev);
                if (!loopPassTwo &&
                    lint.isEnabled(Lint.LintCategory.FINALLY)) {
                    log.warning(Lint.LintCategory.FINALLY,
                            TreeInfo.diagEndPos(tree.finalizer),
                            "finally.cannot.complete");
                }
            } else {
                thrown = chk.union(thrown, chk.diff(thrownInTry, caughtInTry));
                thrown = chk.union(thrown, savedThrown);
                uninits.andSet(uninitsEnd);
                while (exits.nonEmpty()) {
                    PendingExit exit = exits.next();
                    if (exit.inits != null) {
                        exit.inits.orSet(inits);
                        exit.uninits.andSet(uninits);
                    }
                    pendingExits.append(exit);
                }
                inits.orSet(initsEnd);
                alive = aliveEnd;
            }
        } else {
            thrown = chk.union(thrown, chk.diff(thrownInTry, caughtInTry));
            inits = initsEnd;
            uninits = uninitsEnd;
            alive = aliveEnd;
            ListBuffer<PendingExit> exits = pendingExits;
            pendingExits = prevPendingExits;
            while (exits.nonEmpty()) pendingExits.append(exits.next());
        }
        uninitsTry.andSet(uninitsTryPrev).andSet(uninits);
    }
    void checkCaughtType(DiagnosticPosition pos, Type exc, List<Type> thrownInTry, List<Type> caughtInTry) {
        if (chk.subset(exc, caughtInTry)) {
            log.error(pos, "except.already.caught", exc);
        } else if (!chk.isUnchecked(pos, exc) &&
                !isExceptionOrThrowable(exc) &&
                !chk.intersects(exc, thrownInTry)) {
            log.error(pos, "except.never.thrown.in.try", exc);
        } else if (allowImprovedCatchAnalysis) {
            List<Type> catchableThrownTypes = chk.intersect(List.of(exc), thrownInTry);
            if (chk.diff(catchableThrownTypes, caughtInTry).isEmpty() &&
                    !isExceptionOrThrowable(exc)) {
                String key = catchableThrownTypes.length() == 1 ?
                        "unreachable.catch" :
                        "unreachable.catch.1";
                log.warning(pos, key, catchableThrownTypes);
            }
        }
    }
        private boolean isExceptionOrThrowable(Type exc) {
            return exc.tsym == syms.throwableType.tsym ||
                exc.tsym == syms.exceptionType.tsym;
        }
    public void visitConditional(JCConditional tree) {
        scanCond(tree.cond);
        Bits initsBeforeElse = initsWhenFalse;
        Bits uninitsBeforeElse = uninitsWhenFalse;
        inits = initsWhenTrue;
        uninits = uninitsWhenTrue;
        if (tree.truepart.type.tag == BOOLEAN &&
            tree.falsepart.type.tag == BOOLEAN) {
            scanCond(tree.truepart);
            Bits initsAfterThenWhenTrue = initsWhenTrue.dup();
            Bits initsAfterThenWhenFalse = initsWhenFalse.dup();
            Bits uninitsAfterThenWhenTrue = uninitsWhenTrue.dup();
            Bits uninitsAfterThenWhenFalse = uninitsWhenFalse.dup();
            inits = initsBeforeElse;
            uninits = uninitsBeforeElse;
            scanCond(tree.falsepart);
            initsWhenTrue.andSet(initsAfterThenWhenTrue);
            initsWhenFalse.andSet(initsAfterThenWhenFalse);
            uninitsWhenTrue.andSet(uninitsAfterThenWhenTrue);
            uninitsWhenFalse.andSet(uninitsAfterThenWhenFalse);
        } else {
            scanExpr(tree.truepart);
            Bits initsAfterThen = inits.dup();
            Bits uninitsAfterThen = uninits.dup();
            inits = initsBeforeElse;
            uninits = uninitsBeforeElse;
            scanExpr(tree.falsepart);
            inits.andSet(initsAfterThen);
            uninits.andSet(uninitsAfterThen);
        }
    }
    public void visitIf(JCIf tree) {
        scanCond(tree.cond);
        Bits initsBeforeElse = initsWhenFalse;
        Bits uninitsBeforeElse = uninitsWhenFalse;
        inits = initsWhenTrue;
        uninits = uninitsWhenTrue;
        scanStat(tree.thenpart);
        if (tree.elsepart != null) {
            boolean aliveAfterThen = alive;
            alive = true;
            Bits initsAfterThen = inits.dup();
            Bits uninitsAfterThen = uninits.dup();
            inits = initsBeforeElse;
            uninits = uninitsBeforeElse;
            scanStat(tree.elsepart);
            inits.andSet(initsAfterThen);
            uninits.andSet(uninitsAfterThen);
            alive = alive | aliveAfterThen;
        } else {
            inits.andSet(initsBeforeElse);
            uninits.andSet(uninitsBeforeElse);
            alive = true;
        }
    }
    public void visitBreak(JCBreak tree) {
        recordExit(tree);
    }
    public void visitContinue(JCContinue tree) {
        recordExit(tree);
    }
    public void visitReturn(JCReturn tree) {
        scanExpr(tree.expr);
        recordExit(tree);
    }
    public void visitThrow(JCThrow tree) {
        scanExpr(tree.expr);
        Symbol sym = TreeInfo.symbol(tree.expr);
        if (sym != null &&
            sym.kind == VAR &&
            (sym.flags() & (FINAL | EFFECTIVELY_FINAL)) != 0 &&
            preciseRethrowTypes.get(sym) != null &&
            allowImprovedRethrowAnalysis) {
            for (Type t : preciseRethrowTypes.get(sym)) {
                markThrown(tree, t);
            }
        }
        else {
            markThrown(tree, tree.expr.type);
        }
        markDead();
    }
    public void visitApply(JCMethodInvocation tree) {
        scanExpr(tree.meth);
        scanExprs(tree.args);
        for (List<Type> l = tree.meth.type.getThrownTypes(); l.nonEmpty(); l = l.tail)
            markThrown(tree, l.head);
    }
    public void visitNewClass(JCNewClass tree) {
        scanExpr(tree.encl);
        scanExprs(tree.args);
        for (List<Type> l = tree.constructorType.getThrownTypes();
             l.nonEmpty();
             l = l.tail) {
            markThrown(tree, l.head);
        }
        List<Type> caughtPrev = caught;
        try {
            if (tree.def != null)
                for (List<Type> l = tree.constructor.type.getThrownTypes();
                     l.nonEmpty();
                     l = l.tail) {
                    caught = chk.incl(l.head, caught);
                }
            scan(tree.def);
        }
        finally {
            caught = caughtPrev;
        }
    }
    public void visitNewArray(JCNewArray tree) {
        scanExprs(tree.dims);
        scanExprs(tree.elems);
    }
    public void visitAssert(JCAssert tree) {
        Bits initsExit = inits.dup();
        Bits uninitsExit = uninits.dup();
        scanCond(tree.cond);
        uninitsExit.andSet(uninitsWhenTrue);
        if (tree.detail != null) {
            inits = initsWhenFalse;
            uninits = uninitsWhenFalse;
            scanExpr(tree.detail);
        }
        inits = initsExit;
        uninits = uninitsExit;
    }
    public void visitAssign(JCAssign tree) {
        JCTree lhs = TreeInfo.skipParens(tree.lhs);
        if (!(lhs instanceof JCIdent)) scanExpr(lhs);
        scanExpr(tree.rhs);
        letInit(lhs);
    }
    public void visitAssignop(JCAssignOp tree) {
        scanExpr(tree.lhs);
        scanExpr(tree.rhs);
        letInit(tree.lhs);
    }
    public void visitUnary(JCUnary tree) {
        switch (tree.getTag()) {
        case JCTree.NOT:
            scanCond(tree.arg);
            Bits t = initsWhenFalse;
            initsWhenFalse = initsWhenTrue;
            initsWhenTrue = t;
            t = uninitsWhenFalse;
            uninitsWhenFalse = uninitsWhenTrue;
            uninitsWhenTrue = t;
            break;
        case JCTree.PREINC: case JCTree.POSTINC:
        case JCTree.PREDEC: case JCTree.POSTDEC:
            scanExpr(tree.arg);
            letInit(tree.arg);
            break;
        default:
            scanExpr(tree.arg);
        }
    }
    public void visitBinary(JCBinary tree) {
        switch (tree.getTag()) {
        case JCTree.AND:
            scanCond(tree.lhs);
            Bits initsWhenFalseLeft = initsWhenFalse;
            Bits uninitsWhenFalseLeft = uninitsWhenFalse;
            inits = initsWhenTrue;
            uninits = uninitsWhenTrue;
            scanCond(tree.rhs);
            initsWhenFalse.andSet(initsWhenFalseLeft);
            uninitsWhenFalse.andSet(uninitsWhenFalseLeft);
            break;
        case JCTree.OR:
            scanCond(tree.lhs);
            Bits initsWhenTrueLeft = initsWhenTrue;
            Bits uninitsWhenTrueLeft = uninitsWhenTrue;
            inits = initsWhenFalse;
            uninits = uninitsWhenFalse;
            scanCond(tree.rhs);
            initsWhenTrue.andSet(initsWhenTrueLeft);
            uninitsWhenTrue.andSet(uninitsWhenTrueLeft);
            break;
        default:
            scanExpr(tree.lhs);
            scanExpr(tree.rhs);
        }
    }
    public void visitIdent(JCIdent tree) {
        if (tree.sym.kind == VAR) {
            checkInit(tree.pos(), (VarSymbol)tree.sym);
            referenced(tree.sym);
        }
    }
    void referenced(Symbol sym) {
        unrefdResources.remove(sym);
    }
    public void visitTypeCast(JCTypeCast tree) {
        super.visitTypeCast(tree);
        if (!tree.type.isErroneous()
            && lint.isEnabled(Lint.LintCategory.CAST)
            && types.isSameType(tree.expr.type, tree.clazz.type)
            && !is292targetTypeCast(tree)) {
            log.warning(Lint.LintCategory.CAST,
                    tree.pos(), "redundant.cast", tree.expr.type);
        }
    }
        private boolean is292targetTypeCast(JCTypeCast tree) {
            boolean is292targetTypeCast = false;
            JCExpression expr = TreeInfo.skipParens(tree.expr);
            if (expr.getTag() == JCTree.APPLY) {
                JCMethodInvocation apply = (JCMethodInvocation)expr;
                Symbol sym = TreeInfo.symbol(apply.meth);
                is292targetTypeCast = sym != null &&
                    sym.kind == MTH &&
                    (sym.flags() & POLYMORPHIC_SIGNATURE) != 0;
            }
            return is292targetTypeCast;
        }
    public void visitTopLevel(JCCompilationUnit tree) {
    }
    public void analyzeTree(Env<AttrContext> env, TreeMaker make) {
        try {
            attrEnv = env;
            JCTree tree = env.tree;
            this.make = make;
            inits = new Bits();
            uninits = new Bits();
            uninitsTry = new Bits();
            initsWhenTrue = initsWhenFalse =
                uninitsWhenTrue = uninitsWhenFalse = null;
            if (vars == null)
                vars = new VarSymbol[32];
            else
                for (int i=0; i<vars.length; i++)
                    vars[i] = null;
            firstadr = 0;
            nextadr = 0;
            pendingExits = new ListBuffer<PendingExit>();
            preciseRethrowTypes = new HashMap<Symbol, List<Type>>();
            alive = true;
            this.thrown = this.caught = null;
            this.classDef = null;
            unrefdResources = new Scope(env.enclClass.sym);
            scan(tree);
        } finally {
            inits = uninits = uninitsTry = null;
            initsWhenTrue = initsWhenFalse =
                uninitsWhenTrue = uninitsWhenFalse = null;
            if (vars != null) for (int i=0; i<vars.length; i++)
                vars[i] = null;
            firstadr = 0;
            nextadr = 0;
            pendingExits = null;
            this.make = null;
            this.thrown = this.caught = null;
            this.classDef = null;
            unrefdResources = null;
        }
    }
}
