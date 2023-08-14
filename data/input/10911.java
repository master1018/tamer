public class Lower extends TreeTranslator {
    protected static final Context.Key<Lower> lowerKey =
        new Context.Key<Lower>();
    public static Lower instance(Context context) {
        Lower instance = context.get(lowerKey);
        if (instance == null)
            instance = new Lower(context);
        return instance;
    }
    private Names names;
    private Log log;
    private Symtab syms;
    private Resolve rs;
    private Check chk;
    private Attr attr;
    private TreeMaker make;
    private DiagnosticPosition make_pos;
    private ClassWriter writer;
    private ClassReader reader;
    private ConstFold cfolder;
    private Target target;
    private Source source;
    private boolean allowEnums;
    private final Name dollarAssertionsDisabled;
    private final Name classDollar;
    private Types types;
    private boolean debugLower;
    private PkgInfo pkginfoOpt;
    protected Lower(Context context) {
        context.put(lowerKey, this);
        names = Names.instance(context);
        log = Log.instance(context);
        syms = Symtab.instance(context);
        rs = Resolve.instance(context);
        chk = Check.instance(context);
        attr = Attr.instance(context);
        make = TreeMaker.instance(context);
        writer = ClassWriter.instance(context);
        reader = ClassReader.instance(context);
        cfolder = ConstFold.instance(context);
        target = Target.instance(context);
        source = Source.instance(context);
        allowEnums = source.allowEnums();
        dollarAssertionsDisabled = names.
            fromString(target.syntheticNameChar() + "assertionsDisabled");
        classDollar = names.
            fromString("class" + target.syntheticNameChar());
        types = Types.instance(context);
        Options options = Options.instance(context);
        debugLower = options.isSet("debuglower");
        pkginfoOpt = PkgInfo.get(options);
    }
    ClassSymbol currentClass;
    ListBuffer<JCTree> translated;
    Env<AttrContext> attrEnv;
    Map<JCTree, Integer> endPositions;
    Map<ClassSymbol, JCClassDecl> classdefs;
    Map<Symbol,Symbol> actualSymbols;
    JCMethodDecl currentMethodDef;
    MethodSymbol currentMethodSym;
    JCClassDecl outermostClassDef;
    JCTree outermostMemberDef;
    class ClassMap extends TreeScanner {
        public void visitClassDef(JCClassDecl tree) {
            classdefs.put(tree.sym, tree);
            super.visitClassDef(tree);
        }
    }
    ClassMap classMap = new ClassMap();
    JCClassDecl classDef(ClassSymbol c) {
        JCClassDecl def = classdefs.get(c);
        if (def == null && outermostMemberDef != null) {
            classMap.scan(outermostMemberDef);
            def = classdefs.get(c);
        }
        if (def == null) {
            classMap.scan(outermostClassDef);
            def = classdefs.get(c);
        }
        return def;
    }
    Map<ClassSymbol,List<VarSymbol>> freevarCache;
    class FreeVarCollector extends TreeScanner {
        Symbol owner;
        ClassSymbol clazz;
        List<VarSymbol> fvs;
        FreeVarCollector(ClassSymbol clazz) {
            this.clazz = clazz;
            this.owner = clazz.owner;
            this.fvs = List.nil();
        }
        private void addFreeVar(VarSymbol v) {
            for (List<VarSymbol> l = fvs; l.nonEmpty(); l = l.tail)
                if (l.head == v) return;
            fvs = fvs.prepend(v);
        }
        private void addFreeVars(ClassSymbol c) {
            List<VarSymbol> fvs = freevarCache.get(c);
            if (fvs != null) {
                for (List<VarSymbol> l = fvs; l.nonEmpty(); l = l.tail) {
                    addFreeVar(l.head);
                }
            }
        }
        public void visitIdent(JCIdent tree) {
            result = tree;
            visitSymbol(tree.sym);
        }
        private void visitSymbol(Symbol _sym) {
            Symbol sym = _sym;
            if (sym.kind == VAR || sym.kind == MTH) {
                while (sym != null && sym.owner != owner)
                    sym = proxies.lookup(proxyName(sym.name)).sym;
                if (sym != null && sym.owner == owner) {
                    VarSymbol v = (VarSymbol)sym;
                    if (v.getConstValue() == null) {
                        addFreeVar(v);
                    }
                } else {
                    if (outerThisStack.head != null &&
                        outerThisStack.head != _sym)
                        visitSymbol(outerThisStack.head);
                }
            }
        }
        public void visitNewClass(JCNewClass tree) {
            ClassSymbol c = (ClassSymbol)tree.constructor.owner;
            addFreeVars(c);
            if (tree.encl == null &&
                c.hasOuterInstance() &&
                outerThisStack.head != null)
                visitSymbol(outerThisStack.head);
            super.visitNewClass(tree);
        }
        public void visitSelect(JCFieldAccess tree) {
            if ((tree.name == names._this || tree.name == names._super) &&
                tree.selected.type.tsym != clazz &&
                outerThisStack.head != null)
                visitSymbol(outerThisStack.head);
            super.visitSelect(tree);
        }
        public void visitApply(JCMethodInvocation tree) {
            if (TreeInfo.name(tree.meth) == names._super) {
                addFreeVars((ClassSymbol) TreeInfo.symbol(tree.meth).owner);
                Symbol constructor = TreeInfo.symbol(tree.meth);
                ClassSymbol c = (ClassSymbol)constructor.owner;
                if (c.hasOuterInstance() &&
                    tree.meth.getTag() != JCTree.SELECT &&
                    outerThisStack.head != null)
                    visitSymbol(outerThisStack.head);
            }
            super.visitApply(tree);
        }
    }
    List<VarSymbol> freevars(ClassSymbol c)  {
        if ((c.owner.kind & (VAR | MTH)) != 0) {
            List<VarSymbol> fvs = freevarCache.get(c);
            if (fvs == null) {
                FreeVarCollector collector = new FreeVarCollector(c);
                collector.scan(classDef(c));
                fvs = collector.fvs;
                freevarCache.put(c, fvs);
            }
            return fvs;
        } else {
            return List.nil();
        }
    }
    Map<TypeSymbol,EnumMapping> enumSwitchMap = new LinkedHashMap<TypeSymbol,EnumMapping>();
    EnumMapping mapForEnum(DiagnosticPosition pos, TypeSymbol enumClass) {
        EnumMapping map = enumSwitchMap.get(enumClass);
        if (map == null)
            enumSwitchMap.put(enumClass, map = new EnumMapping(pos, enumClass));
        return map;
    }
    class EnumMapping {
        EnumMapping(DiagnosticPosition pos, TypeSymbol forEnum) {
            this.forEnum = forEnum;
            this.values = new LinkedHashMap<VarSymbol,Integer>();
            this.pos = pos;
            Name varName = names
                .fromString(target.syntheticNameChar() +
                            "SwitchMap" +
                            target.syntheticNameChar() +
                            writer.xClassName(forEnum.type).toString()
                            .replace('/', '.')
                            .replace('.', target.syntheticNameChar()));
            ClassSymbol outerCacheClass = outerCacheClass();
            this.mapVar = new VarSymbol(STATIC | SYNTHETIC | FINAL,
                                        varName,
                                        new ArrayType(syms.intType, syms.arrayClass),
                                        outerCacheClass);
            enterSynthetic(pos, mapVar, outerCacheClass.members());
        }
        DiagnosticPosition pos = null;
        int next = 1; 
        final TypeSymbol forEnum;
        final VarSymbol mapVar;
        final Map<VarSymbol,Integer> values;
        JCLiteral forConstant(VarSymbol v) {
            Integer result = values.get(v);
            if (result == null)
                values.put(v, result = next++);
            return make.Literal(result);
        }
        void translate() {
            make.at(pos.getStartPosition());
            JCClassDecl owner = classDef((ClassSymbol)mapVar.owner);
            MethodSymbol valuesMethod = lookupMethod(pos,
                                                     names.values,
                                                     forEnum.type,
                                                     List.<Type>nil());
            JCExpression size = make 
                .Select(make.App(make.QualIdent(valuesMethod)),
                        syms.lengthVar);
            JCExpression mapVarInit = make
                .NewArray(make.Type(syms.intType), List.of(size), null)
                .setType(new ArrayType(syms.intType, syms.arrayClass));
            ListBuffer<JCStatement> stmts = new ListBuffer<JCStatement>();
            Symbol ordinalMethod = lookupMethod(pos,
                                                names.ordinal,
                                                forEnum.type,
                                                List.<Type>nil());
            List<JCCatch> catcher = List.<JCCatch>nil()
                .prepend(make.Catch(make.VarDef(new VarSymbol(PARAMETER, names.ex,
                                                              syms.noSuchFieldErrorType,
                                                              syms.noSymbol),
                                                null),
                                    make.Block(0, List.<JCStatement>nil())));
            for (Map.Entry<VarSymbol,Integer> e : values.entrySet()) {
                VarSymbol enumerator = e.getKey();
                Integer mappedValue = e.getValue();
                JCExpression assign = make
                    .Assign(make.Indexed(mapVar,
                                         make.App(make.Select(make.QualIdent(enumerator),
                                                              ordinalMethod))),
                            make.Literal(mappedValue))
                    .setType(syms.intType);
                JCStatement exec = make.Exec(assign);
                JCStatement _try = make.Try(make.Block(0, List.of(exec)), catcher, null);
                stmts.append(_try);
            }
            owner.defs = owner.defs
                .prepend(make.Block(STATIC, stmts.toList()))
                .prepend(make.VarDef(mapVar, mapVarInit));
        }
    }
    TreeMaker make_at(DiagnosticPosition pos) {
        make_pos = pos;
        return make.at(pos);
    }
    JCExpression makeLit(Type type, Object value) {
        return make.Literal(type.tag, value).setType(type.constType(value));
    }
    JCExpression makeNull() {
        return makeLit(syms.botType, null);
    }
    JCNewClass makeNewClass(Type ctype, List<JCExpression> args) {
        JCNewClass tree = make.NewClass(null,
            null, make.QualIdent(ctype.tsym), args, null);
        tree.constructor = rs.resolveConstructor(
            make_pos, attrEnv, ctype, TreeInfo.types(args), null, false, false);
        tree.type = ctype;
        return tree;
    }
    JCUnary makeUnary(int optag, JCExpression arg) {
        JCUnary tree = make.Unary(optag, arg);
        tree.operator = rs.resolveUnaryOperator(
            make_pos, optag, attrEnv, arg.type);
        tree.type = tree.operator.type.getReturnType();
        return tree;
    }
    JCBinary makeBinary(int optag, JCExpression lhs, JCExpression rhs) {
        JCBinary tree = make.Binary(optag, lhs, rhs);
        tree.operator = rs.resolveBinaryOperator(
            make_pos, optag, attrEnv, lhs.type, rhs.type);
        tree.type = tree.operator.type.getReturnType();
        return tree;
    }
    JCAssignOp makeAssignop(int optag, JCTree lhs, JCTree rhs) {
        JCAssignOp tree = make.Assignop(optag, lhs, rhs);
        tree.operator = rs.resolveBinaryOperator(
            make_pos, tree.getTag() - JCTree.ASGOffset, attrEnv, lhs.type, rhs.type);
        tree.type = lhs.type;
        return tree;
    }
    JCExpression makeString(JCExpression tree) {
        if (tree.type.tag >= CLASS) {
            return tree;
        } else {
            Symbol valueOfSym = lookupMethod(tree.pos(),
                                             names.valueOf,
                                             syms.stringType,
                                             List.of(tree.type));
            return make.App(make.QualIdent(valueOfSym), List.of(tree));
        }
    }
    ClassSymbol makeEmptyClass(long flags, ClassSymbol owner) {
        ClassSymbol c = reader.defineClass(names.empty, owner);
        c.flatname = chk.localClassName(c);
        c.sourcefile = owner.sourcefile;
        c.completer = null;
        c.members_field = new Scope(c);
        c.flags_field = flags;
        ClassType ctype = (ClassType) c.type;
        ctype.supertype_field = syms.objectType;
        ctype.interfaces_field = List.nil();
        JCClassDecl odef = classDef(owner);
        enterSynthetic(odef.pos(), c, owner.members());
        chk.compiled.put(c.flatname, c);
        JCClassDecl cdef = make.ClassDef(
            make.Modifiers(flags), names.empty,
            List.<JCTypeParameter>nil(),
            null, List.<JCExpression>nil(), List.<JCTree>nil());
        cdef.sym = c;
        cdef.type = c.type;
        odef.defs = odef.defs.prepend(cdef);
        return c;
    }
    private void enterSynthetic(DiagnosticPosition pos, Symbol sym, Scope s) {
        s.enter(sym);
    }
    private Name makeSyntheticName(Name name, Scope s) {
        do {
            name = name.append(
                    target.syntheticNameChar(),
                    names.empty);
        } while (lookupSynthetic(name, s) != null);
        return name;
    }
    void checkConflicts(List<JCTree> translatedTrees) {
        for (JCTree t : translatedTrees) {
            t.accept(conflictsChecker);
        }
    }
    JCTree.Visitor conflictsChecker = new TreeScanner() {
        TypeSymbol currentClass;
        @Override
        public void visitMethodDef(JCMethodDecl that) {
            chk.checkConflicts(that.pos(), that.sym, currentClass);
            super.visitMethodDef(that);
        }
        @Override
        public void visitVarDef(JCVariableDecl that) {
            if (that.sym.owner.kind == TYP) {
                chk.checkConflicts(that.pos(), that.sym, currentClass);
            }
            super.visitVarDef(that);
        }
        @Override
        public void visitClassDef(JCClassDecl that) {
            TypeSymbol prevCurrentClass = currentClass;
            currentClass = that.sym;
            try {
                super.visitClassDef(that);
            }
            finally {
                currentClass = prevCurrentClass;
            }
        }
    };
    private Symbol lookupSynthetic(Name name, Scope s) {
        Symbol sym = s.lookup(name).sym;
        return (sym==null || (sym.flags()&SYNTHETIC)==0) ? null : sym;
    }
    private MethodSymbol lookupMethod(DiagnosticPosition pos, Name name, Type qual, List<Type> args) {
        return rs.resolveInternalMethod(pos, attrEnv, qual, name, args, null);
    }
    private MethodSymbol lookupConstructor(DiagnosticPosition pos, Type qual, List<Type> args) {
        return rs.resolveInternalConstructor(pos, attrEnv, qual, args, null);
    }
    private VarSymbol lookupField(DiagnosticPosition pos, Type qual, Name name) {
        return rs.resolveInternalField(pos, attrEnv, qual, name);
    }
    private void checkAccessConstructorTags() {
        for (List<ClassSymbol> l = accessConstrTags; l.nonEmpty(); l = l.tail) {
            ClassSymbol c = l.head;
            if (isTranslatedClassAvailable(c))
                continue;
            JCClassDecl cdef = make.ClassDef(
                make.Modifiers(STATIC | SYNTHETIC), names.empty,
                List.<JCTypeParameter>nil(),
                null, List.<JCExpression>nil(), List.<JCTree>nil());
            cdef.sym = c;
            cdef.type = c.type;
            translated.append(cdef);
        }
    }
    private boolean isTranslatedClassAvailable(ClassSymbol c) {
        for (JCTree tree: translated) {
            if (tree.getTag() == JCTree.CLASSDEF
                    && ((JCClassDecl) tree).sym == c) {
                return true;
            }
        }
        return false;
    }
    private static final int
        DEREFcode = 0,
        ASSIGNcode = 2,
        PREINCcode = 4,
        PREDECcode = 6,
        POSTINCcode = 8,
        POSTDECcode = 10,
        FIRSTASGOPcode = 12;
    private static final int NCODES = accessCode(ByteCodes.lushrl) + 2;
    private Map<Symbol,Integer> accessNums;
    private Map<Symbol,MethodSymbol[]> accessSyms;
    private Map<Symbol,MethodSymbol> accessConstrs;
    private List<ClassSymbol> accessConstrTags;
    private ListBuffer<Symbol> accessed;
    private static int accessCode(int bytecode) {
        if (ByteCodes.iadd <= bytecode && bytecode <= ByteCodes.lxor)
            return (bytecode - iadd) * 2 + FIRSTASGOPcode;
        else if (bytecode == ByteCodes.string_add)
            return (ByteCodes.lxor + 1 - iadd) * 2 + FIRSTASGOPcode;
        else if (ByteCodes.ishll <= bytecode && bytecode <= ByteCodes.lushrl)
            return (bytecode - ishll + ByteCodes.lxor + 2 - iadd) * 2 + FIRSTASGOPcode;
        else
            return -1;
    }
    private static int accessCode(JCTree tree, JCTree enclOp) {
        if (enclOp == null)
            return DEREFcode;
        else if (enclOp.getTag() == JCTree.ASSIGN &&
                 tree == TreeInfo.skipParens(((JCAssign) enclOp).lhs))
            return ASSIGNcode;
        else if (JCTree.PREINC <= enclOp.getTag() && enclOp.getTag() <= JCTree.POSTDEC &&
                 tree == TreeInfo.skipParens(((JCUnary) enclOp).arg))
            return (enclOp.getTag() - JCTree.PREINC) * 2 + PREINCcode;
        else if (JCTree.BITOR_ASG <= enclOp.getTag() && enclOp.getTag() <= JCTree.MOD_ASG &&
                 tree == TreeInfo.skipParens(((JCAssignOp) enclOp).lhs))
            return accessCode(((OperatorSymbol) ((JCAssignOp) enclOp).operator).opcode);
        else
            return DEREFcode;
    }
    private OperatorSymbol binaryAccessOperator(int acode) {
        for (Scope.Entry e = syms.predefClass.members().elems;
             e != null;
             e = e.sibling) {
            if (e.sym instanceof OperatorSymbol) {
                OperatorSymbol op = (OperatorSymbol)e.sym;
                if (accessCode(op.opcode) == acode) return op;
            }
        }
        return null;
    }
    private static int treeTag(OperatorSymbol operator) {
        switch (operator.opcode) {
        case ByteCodes.ior: case ByteCodes.lor:
            return JCTree.BITOR_ASG;
        case ByteCodes.ixor: case ByteCodes.lxor:
            return JCTree.BITXOR_ASG;
        case ByteCodes.iand: case ByteCodes.land:
            return JCTree.BITAND_ASG;
        case ByteCodes.ishl: case ByteCodes.lshl:
        case ByteCodes.ishll: case ByteCodes.lshll:
            return JCTree.SL_ASG;
        case ByteCodes.ishr: case ByteCodes.lshr:
        case ByteCodes.ishrl: case ByteCodes.lshrl:
            return JCTree.SR_ASG;
        case ByteCodes.iushr: case ByteCodes.lushr:
        case ByteCodes.iushrl: case ByteCodes.lushrl:
            return JCTree.USR_ASG;
        case ByteCodes.iadd: case ByteCodes.ladd:
        case ByteCodes.fadd: case ByteCodes.dadd:
        case ByteCodes.string_add:
            return JCTree.PLUS_ASG;
        case ByteCodes.isub: case ByteCodes.lsub:
        case ByteCodes.fsub: case ByteCodes.dsub:
            return JCTree.MINUS_ASG;
        case ByteCodes.imul: case ByteCodes.lmul:
        case ByteCodes.fmul: case ByteCodes.dmul:
            return JCTree.MUL_ASG;
        case ByteCodes.idiv: case ByteCodes.ldiv:
        case ByteCodes.fdiv: case ByteCodes.ddiv:
            return JCTree.DIV_ASG;
        case ByteCodes.imod: case ByteCodes.lmod:
        case ByteCodes.fmod: case ByteCodes.dmod:
            return JCTree.MOD_ASG;
        default:
            throw new AssertionError();
        }
    }
    Name accessName(int anum, int acode) {
        return names.fromString(
            "access" + target.syntheticNameChar() + anum + acode / 10 + acode % 10);
    }
    MethodSymbol accessSymbol(Symbol sym, JCTree tree, JCTree enclOp,
                              boolean protAccess, boolean refSuper) {
        ClassSymbol accOwner = refSuper && protAccess
            ? (ClassSymbol)((JCFieldAccess) tree).selected.type.tsym
            : accessClass(sym, protAccess, tree);
        Symbol vsym = sym;
        if (sym.owner != accOwner) {
            vsym = sym.clone(accOwner);
            actualSymbols.put(vsym, sym);
        }
        Integer anum              
            = accessNums.get(vsym);
        if (anum == null) {
            anum = accessed.length();
            accessNums.put(vsym, anum);
            accessSyms.put(vsym, new MethodSymbol[NCODES]);
            accessed.append(vsym);
        }
        int acode;                
        List<Type> argtypes;      
        Type restype;             
        List<Type> thrown;        
        switch (vsym.kind) {
        case VAR:
            acode = accessCode(tree, enclOp);
            if (acode >= FIRSTASGOPcode) {
                OperatorSymbol operator = binaryAccessOperator(acode);
                if (operator.opcode == string_add)
                    argtypes = List.of(syms.objectType);
                else
                    argtypes = operator.type.getParameterTypes().tail;
            } else if (acode == ASSIGNcode)
                argtypes = List.of(vsym.erasure(types));
            else
                argtypes = List.nil();
            restype = vsym.erasure(types);
            thrown = List.nil();
            break;
        case MTH:
            acode = DEREFcode;
            argtypes = vsym.erasure(types).getParameterTypes();
            restype = vsym.erasure(types).getReturnType();
            thrown = vsym.type.getThrownTypes();
            break;
        default:
            throw new AssertionError();
        }
        if (protAccess && refSuper) acode++;
        if ((vsym.flags() & STATIC) == 0) {
            argtypes = argtypes.prepend(vsym.owner.erasure(types));
        }
        MethodSymbol[] accessors = accessSyms.get(vsym);
        MethodSymbol accessor = accessors[acode];
        if (accessor == null) {
            accessor = new MethodSymbol(
                STATIC | SYNTHETIC,
                accessName(anum.intValue(), acode),
                new MethodType(argtypes, restype, thrown, syms.methodClass),
                accOwner);
            enterSynthetic(tree.pos(), accessor, accOwner.members());
            accessors[acode] = accessor;
        }
        return accessor;
    }
    JCExpression accessBase(DiagnosticPosition pos, Symbol sym) {
        return (sym.flags() & STATIC) != 0
            ? access(make.at(pos.getStartPosition()).QualIdent(sym.owner))
            : makeOwnerThis(pos, sym, true);
    }
    boolean needsPrivateAccess(Symbol sym) {
        if ((sym.flags() & PRIVATE) == 0 || sym.owner == currentClass) {
            return false;
        } else if (sym.name == names.init && (sym.owner.owner.kind & (VAR | MTH)) != 0) {
            sym.flags_field &= ~PRIVATE;
            return false;
        } else {
            return true;
        }
    }
    boolean needsProtectedAccess(Symbol sym, JCTree tree) {
        if ((sym.flags() & PROTECTED) == 0 ||
            sym.owner.owner == currentClass.owner || 
            sym.packge() == currentClass.packge())
            return false;
        if (!currentClass.isSubClass(sym.owner, types))
            return true;
        if ((sym.flags() & STATIC) != 0 ||
            tree.getTag() != JCTree.SELECT ||
            TreeInfo.name(((JCFieldAccess) tree).selected) == names._super)
            return false;
        return !((JCFieldAccess) tree).selected.type.tsym.isSubClass(currentClass, types);
    }
    ClassSymbol accessClass(Symbol sym, boolean protAccess, JCTree tree) {
        if (protAccess) {
            Symbol qualifier = null;
            ClassSymbol c = currentClass;
            if (tree.getTag() == JCTree.SELECT && (sym.flags() & STATIC) == 0) {
                qualifier = ((JCFieldAccess) tree).selected.type.tsym;
                while (!qualifier.isSubClass(c, types)) {
                    c = c.owner.enclClass();
                }
                return c;
            } else {
                while (!c.isSubClass(sym.owner, types)) {
                    c = c.owner.enclClass();
                }
            }
            return c;
        } else {
            return sym.owner.enclClass();
        }
    }
    JCExpression access(Symbol sym, JCExpression tree, JCExpression enclOp, boolean refSuper) {
        while (sym.kind == VAR && sym.owner.kind == MTH &&
            sym.owner.enclClass() != currentClass) {
            Object cv = ((VarSymbol)sym).getConstValue();
            if (cv != null) {
                make.at(tree.pos);
                return makeLit(sym.type, cv);
            }
            sym = proxies.lookup(proxyName(sym.name)).sym;
            Assert.check(sym != null && (sym.flags_field & FINAL) != 0);
            tree = make.at(tree.pos).Ident(sym);
        }
        JCExpression base = (tree.getTag() == JCTree.SELECT) ? ((JCFieldAccess) tree).selected : null;
        switch (sym.kind) {
        case TYP:
            if (sym.owner.kind != PCK) {
                Name flatname = Convert.shortName(sym.flatName());
                while (base != null &&
                       TreeInfo.symbol(base) != null &&
                       TreeInfo.symbol(base).kind != PCK) {
                    base = (base.getTag() == JCTree.SELECT)
                        ? ((JCFieldAccess) base).selected
                        : null;
                }
                if (tree.getTag() == JCTree.IDENT) {
                    ((JCIdent) tree).name = flatname;
                } else if (base == null) {
                    tree = make.at(tree.pos).Ident(sym);
                    ((JCIdent) tree).name = flatname;
                } else {
                    ((JCFieldAccess) tree).selected = base;
                    ((JCFieldAccess) tree).name = flatname;
                }
            }
            break;
        case MTH: case VAR:
            if (sym.owner.kind == TYP) {
                boolean protAccess = refSuper && !needsPrivateAccess(sym)
                    || needsProtectedAccess(sym, tree);
                boolean accReq = protAccess || needsPrivateAccess(sym);
                boolean baseReq =
                    base == null &&
                    sym.owner != syms.predefClass &&
                    !sym.isMemberOf(currentClass, types);
                if (accReq || baseReq) {
                    make.at(tree.pos);
                    if (sym.kind == VAR) {
                        Object cv = ((VarSymbol)sym).getConstValue();
                        if (cv != null) return makeLit(sym.type, cv);
                    }
                    if (accReq) {
                        List<JCExpression> args = List.nil();
                        if ((sym.flags() & STATIC) == 0) {
                            if (base == null)
                                base = makeOwnerThis(tree.pos(), sym, true);
                            args = args.prepend(base);
                            base = null;   
                        }
                        Symbol access = accessSymbol(sym, tree,
                                                     enclOp, protAccess,
                                                     refSuper);
                        JCExpression receiver = make.Select(
                            base != null ? base : make.QualIdent(access.owner),
                            access);
                        return make.App(receiver, args);
                    } else if (baseReq) {
                        return make.at(tree.pos).Select(
                            accessBase(tree.pos(), sym), sym).setType(tree.type);
                    }
                }
            }
        }
        return tree;
    }
    JCExpression access(JCExpression tree) {
        Symbol sym = TreeInfo.symbol(tree);
        return sym == null ? tree : access(sym, tree, null, false);
    }
    Symbol accessConstructor(DiagnosticPosition pos, Symbol constr) {
        if (needsPrivateAccess(constr)) {
            ClassSymbol accOwner = constr.owner.enclClass();
            MethodSymbol aconstr = accessConstrs.get(constr);
            if (aconstr == null) {
                List<Type> argtypes = constr.type.getParameterTypes();
                if ((accOwner.flags_field & ENUM) != 0)
                    argtypes = argtypes
                        .prepend(syms.intType)
                        .prepend(syms.stringType);
                aconstr = new MethodSymbol(
                    SYNTHETIC,
                    names.init,
                    new MethodType(
                        argtypes.append(
                            accessConstructorTag().erasure(types)),
                        constr.type.getReturnType(),
                        constr.type.getThrownTypes(),
                        syms.methodClass),
                    accOwner);
                enterSynthetic(pos, aconstr, accOwner.members());
                accessConstrs.put(constr, aconstr);
                accessed.append(constr);
            }
            return aconstr;
        } else {
            return constr;
        }
    }
    ClassSymbol accessConstructorTag() {
        ClassSymbol topClass = currentClass.outermostClass();
        Name flatname = names.fromString("" + topClass.getQualifiedName() +
                                         target.syntheticNameChar() +
                                         "1");
        ClassSymbol ctag = chk.compiled.get(flatname);
        if (ctag == null)
            ctag = makeEmptyClass(STATIC | SYNTHETIC, topClass);
        accessConstrTags = accessConstrTags.prepend(ctag);
        return ctag;
    }
    void makeAccessible(Symbol sym) {
        JCClassDecl cdef = classDef(sym.owner.enclClass());
        if (cdef == null) Assert.error("class def not found: " + sym + " in " + sym.owner);
        if (sym.name == names.init) {
            cdef.defs = cdef.defs.prepend(
                accessConstructorDef(cdef.pos, sym, accessConstrs.get(sym)));
        } else {
            MethodSymbol[] accessors = accessSyms.get(sym);
            for (int i = 0; i < NCODES; i++) {
                if (accessors[i] != null)
                    cdef.defs = cdef.defs.prepend(
                        accessDef(cdef.pos, sym, accessors[i], i));
            }
        }
    }
    JCTree accessDef(int pos, Symbol vsym, MethodSymbol accessor, int acode) {
        currentClass = vsym.owner.enclClass();
        make.at(pos);
        JCMethodDecl md = make.MethodDef(accessor, null);
        Symbol sym = actualSymbols.get(vsym);
        if (sym == null) sym = vsym;
        JCExpression ref;           
        List<JCExpression> args;    
        if ((sym.flags() & STATIC) != 0) {
            ref = make.Ident(sym);
            args = make.Idents(md.params);
        } else {
            ref = make.Select(make.Ident(md.params.head), sym);
            args = make.Idents(md.params.tail);
        }
        JCStatement stat;          
        if (sym.kind == VAR) {
            int acode1 = acode - (acode & 1);
            JCExpression expr;      
            switch (acode1) {
            case DEREFcode:
                expr = ref;
                break;
            case ASSIGNcode:
                expr = make.Assign(ref, args.head);
                break;
            case PREINCcode: case POSTINCcode: case PREDECcode: case POSTDECcode:
                expr = makeUnary(
                    ((acode1 - PREINCcode) >> 1) + JCTree.PREINC, ref);
                break;
            default:
                expr = make.Assignop(
                    treeTag(binaryAccessOperator(acode1)), ref, args.head);
                ((JCAssignOp) expr).operator = binaryAccessOperator(acode1);
            }
            stat = make.Return(expr.setType(sym.type));
        } else {
            stat = make.Call(make.App(ref, args));
        }
        md.body = make.Block(0, List.of(stat));
        for (List<JCVariableDecl> l = md.params; l.nonEmpty(); l = l.tail)
            l.head.vartype = access(l.head.vartype);
        md.restype = access(md.restype);
        for (List<JCExpression> l = md.thrown; l.nonEmpty(); l = l.tail)
            l.head = access(l.head);
        return md;
    }
    JCTree accessConstructorDef(int pos, Symbol constr, MethodSymbol accessor) {
        make.at(pos);
        JCMethodDecl md = make.MethodDef(accessor,
                                      accessor.externalType(types),
                                      null);
        JCIdent callee = make.Ident(names._this);
        callee.sym = constr;
        callee.type = constr.type;
        md.body =
            make.Block(0, List.<JCStatement>of(
                make.Call(
                    make.App(
                        callee,
                        make.Idents(md.params.reverse().tail.reverse())))));
        return md;
    }
    Scope proxies;
    Scope twrVars;
    List<VarSymbol> outerThisStack;
    Name proxyName(Name name) {
        return names.fromString("val" + target.syntheticNameChar() + name);
    }
    List<JCVariableDecl> freevarDefs(int pos, List<VarSymbol> freevars, Symbol owner) {
        long flags = FINAL | SYNTHETIC;
        if (owner.kind == TYP &&
            target.usePrivateSyntheticFields())
            flags |= PRIVATE;
        List<JCVariableDecl> defs = List.nil();
        for (List<VarSymbol> l = freevars; l.nonEmpty(); l = l.tail) {
            VarSymbol v = l.head;
            VarSymbol proxy = new VarSymbol(
                flags, proxyName(v.name), v.erasure(types), owner);
            proxies.enter(proxy);
            JCVariableDecl vd = make.at(pos).VarDef(proxy, null);
            vd.vartype = access(vd.vartype);
            defs = defs.prepend(vd);
        }
        return defs;
    }
    Name outerThisName(Type type, Symbol owner) {
        Type t = type.getEnclosingType();
        int nestingLevel = 0;
        while (t.tag == CLASS) {
            t = t.getEnclosingType();
            nestingLevel++;
        }
        Name result = names.fromString("this" + target.syntheticNameChar() + nestingLevel);
        while (owner.kind == TYP && ((ClassSymbol)owner).members().lookup(result).scope != null)
            result = names.fromString(result.toString() + target.syntheticNameChar());
        return result;
    }
    JCVariableDecl outerThisDef(int pos, Symbol owner) {
        long flags = FINAL | SYNTHETIC;
        if (owner.kind == TYP &&
            target.usePrivateSyntheticFields())
            flags |= PRIVATE;
        Type target = types.erasure(owner.enclClass().type.getEnclosingType());
        VarSymbol outerThis = new VarSymbol(
            flags, outerThisName(target, owner), target, owner);
        outerThisStack = outerThisStack.prepend(outerThis);
        JCVariableDecl vd = make.at(pos).VarDef(outerThis, null);
        vd.vartype = access(vd.vartype);
        return vd;
    }
    List<JCExpression> loadFreevars(DiagnosticPosition pos, List<VarSymbol> freevars) {
        List<JCExpression> args = List.nil();
        for (List<VarSymbol> l = freevars; l.nonEmpty(); l = l.tail)
            args = args.prepend(loadFreevar(pos, l.head));
        return args;
    }
        JCExpression loadFreevar(DiagnosticPosition pos, VarSymbol v) {
            return access(v, make.at(pos).Ident(v), null, false);
        }
    JCExpression makeThis(DiagnosticPosition pos, TypeSymbol c) {
        if (currentClass == c) {
            return make.at(pos).This(c.erasure(types));
        } else {
            return makeOuterThis(pos, c);
        }
    }
    JCTree makeTwrTry(JCTry tree) {
        make_at(tree.pos());
        twrVars = twrVars.dup();
        JCBlock twrBlock = makeTwrBlock(tree.resources, tree.body, 0);
        if (tree.catchers.isEmpty() && tree.finalizer == null)
            result = translate(twrBlock);
        else
            result = translate(make.Try(twrBlock, tree.catchers, tree.finalizer));
        twrVars = twrVars.leave();
        return result;
    }
    private JCBlock makeTwrBlock(List<JCTree> resources, JCBlock block, int depth) {
        if (resources.isEmpty())
            return block;
        ListBuffer<JCStatement> stats = new ListBuffer<JCStatement>();
        JCTree resource = resources.head;
        JCExpression expr = null;
        if (resource instanceof JCVariableDecl) {
            JCVariableDecl var = (JCVariableDecl) resource;
            expr = make.Ident(var.sym).setType(resource.type);
            stats.add(var);
        } else {
            Assert.check(resource instanceof JCExpression);
            VarSymbol syntheticTwrVar =
            new VarSymbol(SYNTHETIC | FINAL,
                          makeSyntheticName(names.fromString("twrVar" +
                                           depth), twrVars),
                          (resource.type.tag == TypeTags.BOT) ?
                          syms.autoCloseableType : resource.type,
                          currentMethodSym);
            twrVars.enter(syntheticTwrVar);
            JCVariableDecl syntheticTwrVarDecl =
                make.VarDef(syntheticTwrVar, (JCExpression)resource);
            expr = (JCExpression)make.Ident(syntheticTwrVar);
            stats.add(syntheticTwrVarDecl);
        }
        VarSymbol primaryException =
            new VarSymbol(SYNTHETIC,
                          makeSyntheticName(names.fromString("primaryException" +
                          depth), twrVars),
                          syms.throwableType,
                          currentMethodSym);
        twrVars.enter(primaryException);
        JCVariableDecl primaryExceptionTreeDecl = make.VarDef(primaryException, makeNull());
        stats.add(primaryExceptionTreeDecl);
        VarSymbol param =
            new VarSymbol(FINAL|SYNTHETIC,
                          names.fromString("t" +
                                           target.syntheticNameChar()),
                          syms.throwableType,
                          currentMethodSym);
        JCVariableDecl paramTree = make.VarDef(param, null);
        JCStatement assign = make.Assignment(primaryException, make.Ident(param));
        JCStatement rethrowStat = make.Throw(make.Ident(param));
        JCBlock catchBlock = make.Block(0L, List.<JCStatement>of(assign, rethrowStat));
        JCCatch catchClause = make.Catch(paramTree, catchBlock);
        int oldPos = make.pos;
        make.at(TreeInfo.endPos(block));
        JCBlock finallyClause = makeTwrFinallyClause(primaryException, expr);
        make.at(oldPos);
        JCTry outerTry = make.Try(makeTwrBlock(resources.tail, block, depth + 1),
                                  List.<JCCatch>of(catchClause),
                                  finallyClause);
        stats.add(outerTry);
        return make.Block(0L, stats.toList());
    }
    private JCBlock makeTwrFinallyClause(Symbol primaryException, JCExpression resource) {
        VarSymbol catchException =
            new VarSymbol(0, make.paramName(2),
                          syms.throwableType,
                          currentMethodSym);
        JCStatement addSuppressionStatement =
            make.Exec(makeCall(make.Ident(primaryException),
                               names.addSuppressed,
                               List.<JCExpression>of(make.Ident(catchException))));
        JCBlock tryBlock =
            make.Block(0L, List.<JCStatement>of(makeResourceCloseInvocation(resource)));
        JCVariableDecl catchExceptionDecl = make.VarDef(catchException, null);
        JCBlock catchBlock = make.Block(0L, List.<JCStatement>of(addSuppressionStatement));
        List<JCCatch> catchClauses = List.<JCCatch>of(make.Catch(catchExceptionDecl, catchBlock));
        JCTry tryTree = make.Try(tryBlock, catchClauses, null);
        JCIf closeIfStatement = make.If(makeNonNullCheck(make.Ident(primaryException)),
                                        tryTree,
                                        makeResourceCloseInvocation(resource));
        return make.Block(0L,
                          List.<JCStatement>of(make.If(makeNonNullCheck(resource),
                                                       closeIfStatement,
                                                       null)));
    }
    private JCStatement makeResourceCloseInvocation(JCExpression resource) {
        JCExpression resourceClose = makeCall(resource,
                                              names.close,
                                              List.<JCExpression>nil());
        return make.Exec(resourceClose);
    }
    private JCExpression makeNonNullCheck(JCExpression expression) {
        return makeBinary(JCTree.NE, expression, makeNull());
    }
    JCExpression makeOuterThis(DiagnosticPosition pos, TypeSymbol c) {
        List<VarSymbol> ots = outerThisStack;
        if (ots.isEmpty()) {
            log.error(pos, "no.encl.instance.of.type.in.scope", c);
            Assert.error();
            return makeNull();
        }
        VarSymbol ot = ots.head;
        JCExpression tree = access(make.at(pos).Ident(ot));
        TypeSymbol otc = ot.type.tsym;
        while (otc != c) {
            do {
                ots = ots.tail;
                if (ots.isEmpty()) {
                    log.error(pos,
                              "no.encl.instance.of.type.in.scope",
                              c);
                    Assert.error(); 
                    return tree;
                }
                ot = ots.head;
            } while (ot.owner != otc);
            if (otc.owner.kind != PCK && !otc.hasOuterInstance()) {
                chk.earlyRefError(pos, c);
                Assert.error(); 
                return makeNull();
            }
            tree = access(make.at(pos).Select(tree, ot));
            otc = ot.type.tsym;
        }
        return tree;
    }
    JCExpression makeOwnerThis(DiagnosticPosition pos, Symbol sym, boolean preciseMatch) {
        Symbol c = sym.owner;
        if (preciseMatch ? sym.isMemberOf(currentClass, types)
                         : currentClass.isSubClass(sym.owner, types)) {
            return make.at(pos).This(c.erasure(types));
        } else {
            return makeOwnerThisN(pos, sym, preciseMatch);
        }
    }
    JCExpression makeOwnerThisN(DiagnosticPosition pos, Symbol sym, boolean preciseMatch) {
        Symbol c = sym.owner;
        List<VarSymbol> ots = outerThisStack;
        if (ots.isEmpty()) {
            log.error(pos, "no.encl.instance.of.type.in.scope", c);
            Assert.error();
            return makeNull();
        }
        VarSymbol ot = ots.head;
        JCExpression tree = access(make.at(pos).Ident(ot));
        TypeSymbol otc = ot.type.tsym;
        while (!(preciseMatch ? sym.isMemberOf(otc, types) : otc.isSubClass(sym.owner, types))) {
            do {
                ots = ots.tail;
                if (ots.isEmpty()) {
                    log.error(pos,
                        "no.encl.instance.of.type.in.scope",
                        c);
                    Assert.error();
                    return tree;
                }
                ot = ots.head;
            } while (ot.owner != otc);
            tree = access(make.at(pos).Select(tree, ot));
            otc = ot.type.tsym;
        }
        return tree;
    }
    JCStatement initField(int pos, Name name) {
        Scope.Entry e = proxies.lookup(name);
        Symbol rhs = e.sym;
        Assert.check(rhs.owner.kind == MTH);
        Symbol lhs = e.next().sym;
        Assert.check(rhs.owner.owner == lhs.owner);
        make.at(pos);
        return
            make.Exec(
                make.Assign(
                    make.Select(make.This(lhs.owner.erasure(types)), lhs),
                    make.Ident(rhs)).setType(lhs.erasure(types)));
    }
    JCStatement initOuterThis(int pos) {
        VarSymbol rhs = outerThisStack.head;
        Assert.check(rhs.owner.kind == MTH);
        VarSymbol lhs = outerThisStack.tail.head;
        Assert.check(rhs.owner.owner == lhs.owner);
        make.at(pos);
        return
            make.Exec(
                make.Assign(
                    make.Select(make.This(lhs.owner.erasure(types)), lhs),
                    make.Ident(rhs)).setType(lhs.erasure(types)));
    }
    private ClassSymbol outerCacheClass() {
        ClassSymbol clazz = outermostClassDef.sym;
        if ((clazz.flags() & INTERFACE) == 0 &&
            !target.useInnerCacheClass()) return clazz;
        Scope s = clazz.members();
        for (Scope.Entry e = s.elems; e != null; e = e.sibling)
            if (e.sym.kind == TYP &&
                e.sym.name == names.empty &&
                (e.sym.flags() & INTERFACE) == 0) return (ClassSymbol) e.sym;
        return makeEmptyClass(STATIC | SYNTHETIC, clazz);
    }
    private MethodSymbol classDollarSym(DiagnosticPosition pos) {
        ClassSymbol outerCacheClass = outerCacheClass();
        MethodSymbol classDollarSym =
            (MethodSymbol)lookupSynthetic(classDollar,
                                          outerCacheClass.members());
        if (classDollarSym == null) {
            classDollarSym = new MethodSymbol(
                STATIC | SYNTHETIC,
                classDollar,
                new MethodType(
                    List.of(syms.stringType),
                    types.erasure(syms.classType),
                    List.<Type>nil(),
                    syms.methodClass),
                outerCacheClass);
            enterSynthetic(pos, classDollarSym, outerCacheClass.members());
            JCMethodDecl md = make.MethodDef(classDollarSym, null);
            try {
                md.body = classDollarSymBody(pos, md);
            } catch (CompletionFailure ex) {
                md.body = make.Block(0, List.<JCStatement>nil());
                chk.completionError(pos, ex);
            }
            JCClassDecl outerCacheClassDef = classDef(outerCacheClass);
            outerCacheClassDef.defs = outerCacheClassDef.defs.prepend(md);
        }
        return classDollarSym;
    }
    JCBlock classDollarSymBody(DiagnosticPosition pos, JCMethodDecl md) {
        MethodSymbol classDollarSym = md.sym;
        ClassSymbol outerCacheClass = (ClassSymbol)classDollarSym.owner;
        JCBlock returnResult;
        if (target.classLiteralsNoInit()) {
            VarSymbol clsym = new VarSymbol(STATIC|SYNTHETIC,
                                            names.fromString("cl" + target.syntheticNameChar()),
                                            syms.classLoaderType,
                                            outerCacheClass);
            enterSynthetic(pos, clsym, outerCacheClass.members());
            JCVariableDecl cldef = make.VarDef(clsym, null);
            JCClassDecl outerCacheClassDef = classDef(outerCacheClass);
            outerCacheClassDef.defs = outerCacheClassDef.defs.prepend(cldef);
            JCNewArray newcache = make.
                NewArray(make.Type(outerCacheClass.type),
                         List.<JCExpression>of(make.Literal(INT, 0).setType(syms.intType)),
                         null);
            newcache.type = new ArrayType(types.erasure(outerCacheClass.type),
                                          syms.arrayClass);
            Symbol forNameSym = lookupMethod(make_pos, names.forName,
                                             types.erasure(syms.classType),
                                             List.of(syms.stringType,
                                                     syms.booleanType,
                                                     syms.classLoaderType));
            JCExpression clvalue =
                make.Conditional(
                    makeBinary(JCTree.EQ, make.Ident(clsym), makeNull()),
                    make.Assign(
                        make.Ident(clsym),
                        makeCall(
                            makeCall(makeCall(newcache,
                                              names.getClass,
                                              List.<JCExpression>nil()),
                                     names.getComponentType,
                                     List.<JCExpression>nil()),
                            names.getClassLoader,
                            List.<JCExpression>nil())).setType(syms.classLoaderType),
                    make.Ident(clsym)).setType(syms.classLoaderType);
            List<JCExpression> args = List.of(make.Ident(md.params.head.sym),
                                              makeLit(syms.booleanType, 0),
                                              clvalue);
            returnResult = make.
                Block(0, List.<JCStatement>of(make.
                              Call(make. 
                                   App(make.
                                       Ident(forNameSym), args))));
        } else {
            Symbol forNameSym = lookupMethod(make_pos,
                                             names.forName,
                                             types.erasure(syms.classType),
                                             List.of(syms.stringType));
            returnResult = make.
                Block(0, List.of(make.
                          Call(make. 
                              App(make.
                                  QualIdent(forNameSym),
                                  List.<JCExpression>of(make.
                                                        Ident(md.params.
                                                              head.sym))))));
        }
        VarSymbol catchParam =
            new VarSymbol(0, make.paramName(1),
                          syms.classNotFoundExceptionType,
                          classDollarSym);
        JCStatement rethrow;
        if (target.hasInitCause()) {
            JCTree throwExpr =
                makeCall(makeNewClass(syms.noClassDefFoundErrorType,
                                      List.<JCExpression>nil()),
                         names.initCause,
                         List.<JCExpression>of(make.Ident(catchParam)));
            rethrow = make.Throw(throwExpr);
        } else {
            Symbol getMessageSym = lookupMethod(make_pos,
                                                names.getMessage,
                                                syms.classNotFoundExceptionType,
                                                List.<Type>nil());
            rethrow = make.
                Throw(makeNewClass(syms.noClassDefFoundErrorType,
                          List.<JCExpression>of(make.App(make.Select(make.Ident(catchParam),
                                                                     getMessageSym),
                                                         List.<JCExpression>nil()))));
        }
        JCBlock rethrowStmt = make.Block(0, List.of(rethrow));
        JCCatch catchBlock = make.Catch(make.VarDef(catchParam, null),
                                      rethrowStmt);
        JCStatement tryCatch = make.Try(returnResult,
                                        List.of(catchBlock), null);
        return make.Block(0, List.of(tryCatch));
    }
        private JCMethodInvocation makeCall(JCExpression left, Name name, List<JCExpression> args) {
            Assert.checkNonNull(left.type);
            Symbol funcsym = lookupMethod(make_pos, name, left.type,
                                          TreeInfo.types(args));
            return make.App(make.Select(left, funcsym), args);
        }
    private Name cacheName(String sig) {
        StringBuffer buf = new StringBuffer();
        if (sig.startsWith("[")) {
            buf = buf.append("array");
            while (sig.startsWith("[")) {
                buf = buf.append(target.syntheticNameChar());
                sig = sig.substring(1);
            }
            if (sig.startsWith("L")) {
                sig = sig.substring(0, sig.length() - 1);
            }
        } else {
            buf = buf.append("class" + target.syntheticNameChar());
        }
        buf = buf.append(sig.replace('.', target.syntheticNameChar()));
        return names.fromString(buf.toString());
    }
    private VarSymbol cacheSym(DiagnosticPosition pos, String sig) {
        ClassSymbol outerCacheClass = outerCacheClass();
        Name cname = cacheName(sig);
        VarSymbol cacheSym =
            (VarSymbol)lookupSynthetic(cname, outerCacheClass.members());
        if (cacheSym == null) {
            cacheSym = new VarSymbol(
                STATIC | SYNTHETIC, cname, types.erasure(syms.classType), outerCacheClass);
            enterSynthetic(pos, cacheSym, outerCacheClass.members());
            JCVariableDecl cacheDef = make.VarDef(cacheSym, null);
            JCClassDecl outerCacheClassDef = classDef(outerCacheClass);
            outerCacheClassDef.defs = outerCacheClassDef.defs.prepend(cacheDef);
        }
        return cacheSym;
    }
    private JCExpression classOf(JCTree clazz) {
        return classOfType(clazz.type, clazz.pos());
    }
    private JCExpression classOfType(Type type, DiagnosticPosition pos) {
        switch (type.tag) {
        case BYTE: case SHORT: case CHAR: case INT: case LONG: case FLOAT:
        case DOUBLE: case BOOLEAN: case VOID:
            ClassSymbol c = types.boxedClass(type);
            Symbol typeSym =
                rs.access(
                    rs.findIdentInType(attrEnv, c.type, names.TYPE, VAR),
                    pos, c.type, names.TYPE, true);
            if (typeSym.kind == VAR)
                ((VarSymbol)typeSym).getConstValue(); 
            return make.QualIdent(typeSym);
        case CLASS: case ARRAY:
            if (target.hasClassLiterals()) {
                VarSymbol sym = new VarSymbol(
                        STATIC | PUBLIC | FINAL, names._class,
                        syms.classType, type.tsym);
                return make_at(pos).Select(make.Type(type), sym);
            }
            String sig =
                writer.xClassName(type).toString().replace('/', '.');
            Symbol cs = cacheSym(pos, sig);
            return make_at(pos).Conditional(
                makeBinary(JCTree.EQ, make.Ident(cs), makeNull()),
                make.Assign(
                    make.Ident(cs),
                    make.App(
                        make.Ident(classDollarSym(pos)),
                        List.<JCExpression>of(make.Literal(CLASS, sig)
                                              .setType(syms.stringType))))
                .setType(types.erasure(syms.classType)),
                make.Ident(cs)).setType(types.erasure(syms.classType));
        default:
            throw new AssertionError();
        }
    }
    private JCExpression assertFlagTest(DiagnosticPosition pos) {
        ClassSymbol outermostClass = outermostClassDef.sym;
        ClassSymbol container = currentClass;
        VarSymbol assertDisabledSym =
            (VarSymbol)lookupSynthetic(dollarAssertionsDisabled,
                                       container.members());
        if (assertDisabledSym == null) {
            assertDisabledSym =
                new VarSymbol(STATIC | FINAL | SYNTHETIC,
                              dollarAssertionsDisabled,
                              syms.booleanType,
                              container);
            enterSynthetic(pos, assertDisabledSym, container.members());
            Symbol desiredAssertionStatusSym = lookupMethod(pos,
                                                            names.desiredAssertionStatus,
                                                            types.erasure(syms.classType),
                                                            List.<Type>nil());
            JCClassDecl containerDef = classDef(container);
            make_at(containerDef.pos());
            JCExpression notStatus = makeUnary(JCTree.NOT, make.App(make.Select(
                    classOfType(types.erasure(outermostClass.type),
                                containerDef.pos()),
                    desiredAssertionStatusSym)));
            JCVariableDecl assertDisabledDef = make.VarDef(assertDisabledSym,
                                                   notStatus);
            containerDef.defs = containerDef.defs.prepend(assertDisabledDef);
        }
        make_at(pos);
        return makeUnary(JCTree.NOT, make.Ident(assertDisabledSym));
    }
    interface TreeBuilder {
        JCTree build(JCTree arg);
    }
    JCTree abstractRval(JCTree rval, Type type, TreeBuilder builder) {
        rval = TreeInfo.skipParens(rval);
        switch (rval.getTag()) {
        case JCTree.LITERAL:
            return builder.build(rval);
        case JCTree.IDENT:
            JCIdent id = (JCIdent) rval;
            if ((id.sym.flags() & FINAL) != 0 && id.sym.owner.kind == MTH)
                return builder.build(rval);
        }
        VarSymbol var =
            new VarSymbol(FINAL|SYNTHETIC,
                          names.fromString(
                                          target.syntheticNameChar()
                                          + "" + rval.hashCode()),
                                      type,
                                      currentMethodSym);
        rval = convert(rval,type);
        JCVariableDecl def = make.VarDef(var, (JCExpression)rval); 
        JCTree built = builder.build(make.Ident(var));
        JCTree res = make.LetExpr(def, built);
        res.type = built.type;
        return res;
    }
    JCTree abstractRval(JCTree rval, TreeBuilder builder) {
        return abstractRval(rval, rval.type, builder);
    }
    JCTree abstractLval(JCTree lval, final TreeBuilder builder) {
        lval = TreeInfo.skipParens(lval);
        switch (lval.getTag()) {
        case JCTree.IDENT:
            return builder.build(lval);
        case JCTree.SELECT: {
            final JCFieldAccess s = (JCFieldAccess)lval;
            JCTree selected = TreeInfo.skipParens(s.selected);
            Symbol lid = TreeInfo.symbol(s.selected);
            if (lid != null && lid.kind == TYP) return builder.build(lval);
            return abstractRval(s.selected, new TreeBuilder() {
                    public JCTree build(final JCTree selected) {
                        return builder.build(make.Select((JCExpression)selected, s.sym));
                    }
                });
        }
        case JCTree.INDEXED: {
            final JCArrayAccess i = (JCArrayAccess)lval;
            return abstractRval(i.indexed, new TreeBuilder() {
                    public JCTree build(final JCTree indexed) {
                        return abstractRval(i.index, syms.intType, new TreeBuilder() {
                                public JCTree build(final JCTree index) {
                                    JCTree newLval = make.Indexed((JCExpression)indexed,
                                                                (JCExpression)index);
                                    newLval.setType(i.type);
                                    return builder.build(newLval);
                                }
                            });
                    }
                });
        }
        case JCTree.TYPECAST: {
            return abstractLval(((JCTypeCast)lval).expr, builder);
        }
        }
        throw new AssertionError(lval);
    }
    JCTree makeComma(final JCTree expr1, final JCTree expr2) {
        return abstractRval(expr1, new TreeBuilder() {
                public JCTree build(final JCTree discarded) {
                    return expr2;
                }
            });
    }
    private JCExpression enclOp;
    public <T extends JCTree> T translate(T tree) {
        if (tree == null) {
            return null;
        } else {
            make_at(tree.pos());
            T result = super.translate(tree);
            if (endPositions != null && result != tree) {
                Integer endPos = endPositions.remove(tree);
                if (endPos != null) endPositions.put(result, endPos);
            }
            return result;
        }
    }
    public <T extends JCTree> T translate(T tree, Type type) {
        return (tree == null) ? null : boxIfNeeded(translate(tree), type);
    }
    public <T extends JCTree> T translate(T tree, JCExpression enclOp) {
        JCExpression prevEnclOp = this.enclOp;
        this.enclOp = enclOp;
        T res = translate(tree);
        this.enclOp = prevEnclOp;
        return res;
    }
    public <T extends JCTree> List<T> translate(List<T> trees, JCExpression enclOp) {
        JCExpression prevEnclOp = this.enclOp;
        this.enclOp = enclOp;
        List<T> res = translate(trees);
        this.enclOp = prevEnclOp;
        return res;
    }
    public <T extends JCTree> List<T> translate(List<T> trees, Type type) {
        if (trees == null) return null;
        for (List<T> l = trees; l.nonEmpty(); l = l.tail)
            l.head = translate(l.head, type);
        return trees;
    }
    public void visitTopLevel(JCCompilationUnit tree) {
        if (needPackageInfoClass(tree)) {
            Name name = names.package_info;
            long flags = Flags.ABSTRACT | Flags.INTERFACE;
            if (target.isPackageInfoSynthetic())
                flags = flags | Flags.SYNTHETIC;
            JCClassDecl packageAnnotationsClass
                = make.ClassDef(make.Modifiers(flags,
                                               tree.packageAnnotations),
                                name, List.<JCTypeParameter>nil(),
                                null, List.<JCExpression>nil(), List.<JCTree>nil());
            ClassSymbol c = tree.packge.package_info;
            c.flags_field |= flags;
            c.attributes_field = tree.packge.attributes_field;
            ClassType ctype = (ClassType) c.type;
            ctype.supertype_field = syms.objectType;
            ctype.interfaces_field = List.nil();
            packageAnnotationsClass.sym = c;
            translated.append(packageAnnotationsClass);
        }
    }
    private boolean needPackageInfoClass(JCCompilationUnit tree) {
        switch (pkginfoOpt) {
            case ALWAYS:
                return true;
            case LEGACY:
                return tree.packageAnnotations.nonEmpty();
            case NONEMPTY:
                for (Attribute.Compound a: tree.packge.attributes_field) {
                    Attribute.RetentionPolicy p = types.getRetention(a);
                    if (p != Attribute.RetentionPolicy.SOURCE)
                        return true;
                }
                return false;
        }
        throw new AssertionError();
    }
    public void visitClassDef(JCClassDecl tree) {
        ClassSymbol currentClassPrev = currentClass;
        MethodSymbol currentMethodSymPrev = currentMethodSym;
        currentClass = tree.sym;
        currentMethodSym = null;
        classdefs.put(currentClass, tree);
        proxies = proxies.dup(currentClass);
        List<VarSymbol> prevOuterThisStack = outerThisStack;
        if ((tree.mods.flags & ENUM) != 0 &&
            (types.supertype(currentClass.type).tsym.flags() & ENUM) == 0)
            visitEnumDef(tree);
        JCVariableDecl otdef = null;
        if (currentClass.hasOuterInstance())
            otdef = outerThisDef(tree.pos, currentClass);
        List<JCVariableDecl> fvdefs = freevarDefs(
            tree.pos, freevars(currentClass), currentClass);
        tree.extending = translate(tree.extending);
        tree.implementing = translate(tree.implementing);
        List<JCTree> seen = List.nil();
        while (tree.defs != seen) {
            List<JCTree> unseen = tree.defs;
            for (List<JCTree> l = unseen; l.nonEmpty() && l != seen; l = l.tail) {
                JCTree outermostMemberDefPrev = outermostMemberDef;
                if (outermostMemberDefPrev == null) outermostMemberDef = l.head;
                l.head = translate(l.head);
                outermostMemberDef = outermostMemberDefPrev;
            }
            seen = unseen;
        }
        if ((tree.mods.flags & PROTECTED) != 0) tree.mods.flags |= PUBLIC;
        tree.mods.flags &= ClassFlags;
        tree.name = Convert.shortName(currentClass.flatName());
        for (List<JCVariableDecl> l = fvdefs; l.nonEmpty(); l = l.tail) {
            tree.defs = tree.defs.prepend(l.head);
            enterSynthetic(tree.pos(), l.head.sym, currentClass.members());
        }
        if (currentClass.hasOuterInstance()) {
            tree.defs = tree.defs.prepend(otdef);
            enterSynthetic(tree.pos(), otdef.sym, currentClass.members());
        }
        proxies = proxies.leave();
        outerThisStack = prevOuterThisStack;
        translated.append(tree);
        currentClass = currentClassPrev;
        currentMethodSym = currentMethodSymPrev;
        result = make_at(tree.pos()).Block(0, List.<JCStatement>nil());
    }
    private void visitEnumDef(JCClassDecl tree) {
        make_at(tree.pos());
        if (tree.extending == null)
            tree.extending = make.Type(types.supertype(tree.type));
        JCExpression e_class = classOfType(tree.sym.type, tree.pos()).
            setType(types.erasure(syms.classType));
        int nextOrdinal = 0;
        ListBuffer<JCExpression> values = new ListBuffer<JCExpression>();
        ListBuffer<JCTree> enumDefs = new ListBuffer<JCTree>();
        ListBuffer<JCTree> otherDefs = new ListBuffer<JCTree>();
        for (List<JCTree> defs = tree.defs;
             defs.nonEmpty();
             defs=defs.tail) {
            if (defs.head.getTag() == JCTree.VARDEF && (((JCVariableDecl) defs.head).mods.flags & ENUM) != 0) {
                JCVariableDecl var = (JCVariableDecl)defs.head;
                visitEnumConstantDef(var, nextOrdinal++);
                values.append(make.QualIdent(var.sym));
                enumDefs.append(var);
            } else {
                otherDefs.append(defs.head);
            }
        }
        Name valuesName = names.fromString(target.syntheticNameChar() + "VALUES");
        while (tree.sym.members().lookup(valuesName).scope != null) 
            valuesName = names.fromString(valuesName + "" + target.syntheticNameChar());
        Type arrayType = new ArrayType(types.erasure(tree.type), syms.arrayClass);
        VarSymbol valuesVar = new VarSymbol(PRIVATE|FINAL|STATIC|SYNTHETIC,
                                            valuesName,
                                            arrayType,
                                            tree.type.tsym);
        JCNewArray newArray = make.NewArray(make.Type(types.erasure(tree.type)),
                                          List.<JCExpression>nil(),
                                          values.toList());
        newArray.type = arrayType;
        enumDefs.append(make.VarDef(valuesVar, newArray));
        tree.sym.members().enter(valuesVar);
        Symbol valuesSym = lookupMethod(tree.pos(), names.values,
                                        tree.type, List.<Type>nil());
        List<JCStatement> valuesBody;
        if (useClone()) {
            JCTypeCast valuesResult =
                make.TypeCast(valuesSym.type.getReturnType(),
                              make.App(make.Select(make.Ident(valuesVar),
                                                   syms.arrayCloneMethod)));
            valuesBody = List.<JCStatement>of(make.Return(valuesResult));
        } else {
            Name resultName = names.fromString(target.syntheticNameChar() + "result");
            while (tree.sym.members().lookup(resultName).scope != null) 
                resultName = names.fromString(resultName + "" + target.syntheticNameChar());
            VarSymbol resultVar = new VarSymbol(FINAL|SYNTHETIC,
                                                resultName,
                                                arrayType,
                                                valuesSym);
            JCNewArray resultArray = make.NewArray(make.Type(types.erasure(tree.type)),
                                  List.of(make.Select(make.Ident(valuesVar), syms.lengthVar)),
                                  null);
            resultArray.type = arrayType;
            JCVariableDecl decl = make.VarDef(resultVar, resultArray);
            if (systemArraycopyMethod == null) {
                systemArraycopyMethod =
                    new MethodSymbol(PUBLIC | STATIC,
                                     names.fromString("arraycopy"),
                                     new MethodType(List.<Type>of(syms.objectType,
                                                            syms.intType,
                                                            syms.objectType,
                                                            syms.intType,
                                                            syms.intType),
                                                    syms.voidType,
                                                    List.<Type>nil(),
                                                    syms.methodClass),
                                     syms.systemType.tsym);
            }
            JCStatement copy =
                make.Exec(make.App(make.Select(make.Ident(syms.systemType.tsym),
                                               systemArraycopyMethod),
                          List.of(make.Ident(valuesVar), make.Literal(0),
                                  make.Ident(resultVar), make.Literal(0),
                                  make.Select(make.Ident(valuesVar), syms.lengthVar))));
            JCStatement ret = make.Return(make.Ident(resultVar));
            valuesBody = List.<JCStatement>of(decl, copy, ret);
        }
        JCMethodDecl valuesDef =
             make.MethodDef((MethodSymbol)valuesSym, make.Block(0, valuesBody));
        enumDefs.append(valuesDef);
        if (debugLower)
            System.err.println(tree.sym + ".valuesDef = " + valuesDef);
        MethodSymbol valueOfSym = lookupMethod(tree.pos(),
                         names.valueOf,
                         tree.sym.type,
                         List.of(syms.stringType));
        Assert.check((valueOfSym.flags() & STATIC) != 0);
        VarSymbol nameArgSym = valueOfSym.params.head;
        JCIdent nameVal = make.Ident(nameArgSym);
        JCStatement enum_ValueOf =
            make.Return(make.TypeCast(tree.sym.type,
                                      makeCall(make.Ident(syms.enumSym),
                                               names.valueOf,
                                               List.of(e_class, nameVal))));
        JCMethodDecl valueOf = make.MethodDef(valueOfSym,
                                           make.Block(0, List.of(enum_ValueOf)));
        nameVal.sym = valueOf.params.head.sym;
        if (debugLower)
            System.err.println(tree.sym + ".valueOf = " + valueOf);
        enumDefs.append(valueOf);
        enumDefs.appendList(otherDefs.toList());
        tree.defs = enumDefs.toList();
        if (target.compilerBootstrap(tree.sym)) {
            addEnumCompatibleMembers(tree);
        }
    }
        private MethodSymbol systemArraycopyMethod;
        private boolean useClone() {
            try {
                Scope.Entry e = syms.objectType.tsym.members().lookup(names.clone);
                return (e.sym != null);
            }
            catch (CompletionFailure e) {
                return false;
            }
        }
    private void visitEnumConstantDef(JCVariableDecl var, int ordinal) {
        JCNewClass varDef = (JCNewClass)var.init;
        varDef.args = varDef.args.
            prepend(makeLit(syms.intType, ordinal)).
            prepend(makeLit(syms.stringType, var.name.toString()));
    }
    public void visitMethodDef(JCMethodDecl tree) {
        if (tree.name == names.init && (currentClass.flags_field&ENUM) != 0) {
            JCVariableDecl nameParam = make_at(tree.pos()).
                Param(names.fromString(target.syntheticNameChar() +
                                       "enum" + target.syntheticNameChar() + "name"),
                      syms.stringType, tree.sym);
            nameParam.mods.flags |= SYNTHETIC; nameParam.sym.flags_field |= SYNTHETIC;
            JCVariableDecl ordParam = make.
                Param(names.fromString(target.syntheticNameChar() +
                                       "enum" + target.syntheticNameChar() +
                                       "ordinal"),
                      syms.intType, tree.sym);
            ordParam.mods.flags |= SYNTHETIC; ordParam.sym.flags_field |= SYNTHETIC;
            tree.params = tree.params.prepend(ordParam).prepend(nameParam);
            MethodSymbol m = tree.sym;
            Type olderasure = m.erasure(types);
            m.erasure_field = new MethodType(
                olderasure.getParameterTypes().prepend(syms.intType).prepend(syms.stringType),
                olderasure.getReturnType(),
                olderasure.getThrownTypes(),
                syms.methodClass);
            if (target.compilerBootstrap(m.owner)) {
                Symbol nameVarSym = lookupSynthetic(names.fromString("$name"),
                                                    tree.sym.owner.members());
                JCIdent nameIdent = make.Ident(nameParam.sym);
                JCIdent id1 = make.Ident(nameVarSym);
                JCAssign newAssign = make.Assign(id1, nameIdent);
                newAssign.type = id1.type;
                JCExpressionStatement nameAssign = make.Exec(newAssign);
                nameAssign.type = id1.type;
                tree.body.stats = tree.body.stats.prepend(nameAssign);
                Symbol ordinalVarSym = lookupSynthetic(names.fromString("$ordinal"),
                                                       tree.sym.owner.members());
                JCIdent ordIdent = make.Ident(ordParam.sym);
                id1 = make.Ident(ordinalVarSym);
                newAssign = make.Assign(id1, ordIdent);
                newAssign.type = id1.type;
                JCExpressionStatement ordinalAssign = make.Exec(newAssign);
                ordinalAssign.type = id1.type;
                tree.body.stats = tree.body.stats.prepend(ordinalAssign);
            }
        }
        JCMethodDecl prevMethodDef = currentMethodDef;
        MethodSymbol prevMethodSym = currentMethodSym;
        try {
            currentMethodDef = tree;
            currentMethodSym = tree.sym;
            visitMethodDefInternal(tree);
        } finally {
            currentMethodDef = prevMethodDef;
            currentMethodSym = prevMethodSym;
        }
    }
    private void visitMethodDefInternal(JCMethodDecl tree) {
        if (tree.name == names.init &&
            (currentClass.isInner() ||
             (currentClass.owner.kind & (VAR | MTH)) != 0)) {
            MethodSymbol m = tree.sym;
            proxies = proxies.dup(m);
            List<VarSymbol> prevOuterThisStack = outerThisStack;
            List<VarSymbol> fvs = freevars(currentClass);
            JCVariableDecl otdef = null;
            if (currentClass.hasOuterInstance())
                otdef = outerThisDef(tree.pos, m);
            List<JCVariableDecl> fvdefs = freevarDefs(tree.pos, fvs, m);
            tree.restype = translate(tree.restype);
            tree.params = translateVarDefs(tree.params);
            tree.thrown = translate(tree.thrown);
            if (tree.body == null) {
                result = tree;
                return;
            }
            tree.params = tree.params.appendList(fvdefs);
            if (currentClass.hasOuterInstance())
                tree.params = tree.params.prepend(otdef);
            JCStatement selfCall = translate(tree.body.stats.head);
            List<JCStatement> added = List.nil();
            if (fvs.nonEmpty()) {
                List<Type> addedargtypes = List.nil();
                for (List<VarSymbol> l = fvs; l.nonEmpty(); l = l.tail) {
                    if (TreeInfo.isInitialConstructor(tree))
                        added = added.prepend(
                            initField(tree.body.pos, proxyName(l.head.name)));
                    addedargtypes = addedargtypes.prepend(l.head.erasure(types));
                }
                Type olderasure = m.erasure(types);
                m.erasure_field = new MethodType(
                    olderasure.getParameterTypes().appendList(addedargtypes),
                    olderasure.getReturnType(),
                    olderasure.getThrownTypes(),
                    syms.methodClass);
            }
            if (currentClass.hasOuterInstance() &&
                TreeInfo.isInitialConstructor(tree))
            {
                added = added.prepend(initOuterThis(tree.body.pos));
            }
            proxies = proxies.leave();
            List<JCStatement> stats = translate(tree.body.stats.tail);
            if (target.initializeFieldsBeforeSuper())
                tree.body.stats = stats.prepend(selfCall).prependList(added);
            else
                tree.body.stats = stats.prependList(added).prepend(selfCall);
            outerThisStack = prevOuterThisStack;
        } else {
            super.visitMethodDef(tree);
        }
        result = tree;
    }
    public void visitTypeCast(JCTypeCast tree) {
        tree.clazz = translate(tree.clazz);
        if (tree.type.isPrimitive() != tree.expr.type.isPrimitive())
            tree.expr = translate(tree.expr, tree.type);
        else
            tree.expr = translate(tree.expr);
        result = tree;
    }
    public void visitNewClass(JCNewClass tree) {
        ClassSymbol c = (ClassSymbol)tree.constructor.owner;
        boolean isEnum = (tree.constructor.owner.flags() & ENUM) != 0;
        List<Type> argTypes = tree.constructor.type.getParameterTypes();
        if (isEnum) argTypes = argTypes.prepend(syms.intType).prepend(syms.stringType);
        tree.args = boxArgs(argTypes, tree.args, tree.varargsElement);
        tree.varargsElement = null;
        if ((c.owner.kind & (VAR | MTH)) != 0) {
            tree.args = tree.args.appendList(loadFreevars(tree.pos(), freevars(c)));
        }
        Symbol constructor = accessConstructor(tree.pos(), tree.constructor);
        if (constructor != tree.constructor) {
            tree.args = tree.args.append(makeNull());
            tree.constructor = constructor;
        }
        if (c.hasOuterInstance()) {
            JCExpression thisArg;
            if (tree.encl != null) {
                thisArg = attr.makeNullCheck(translate(tree.encl));
                thisArg.type = tree.encl.type;
            } else if ((c.owner.kind & (MTH | VAR)) != 0) {
                thisArg = makeThis(tree.pos(), c.type.getEnclosingType().tsym);
            } else {
                thisArg = makeOwnerThis(tree.pos(), c, false);
            }
            tree.args = tree.args.prepend(thisArg);
        }
        tree.encl = null;
        if (tree.def != null) {
            translate(tree.def);
            tree.clazz = access(make_at(tree.clazz.pos()).Ident(tree.def.sym));
            tree.def = null;
        } else {
            tree.clazz = access(c, tree.clazz, enclOp, false);
        }
        result = tree;
    }
    public void visitConditional(JCConditional tree) {
        JCTree cond = tree.cond = translate(tree.cond, syms.booleanType);
        if (cond.type.isTrue()) {
            result = convert(translate(tree.truepart, tree.type), tree.type);
        } else if (cond.type.isFalse()) {
            result = convert(translate(tree.falsepart, tree.type), tree.type);
        } else {
            tree.truepart = translate(tree.truepart, tree.type);
            tree.falsepart = translate(tree.falsepart, tree.type);
            result = tree;
        }
    }
        private JCTree convert(JCTree tree, Type pt) {
            if (tree.type == pt || tree.type.tag == TypeTags.BOT)
                return tree;
            JCTree result = make_at(tree.pos()).TypeCast(make.Type(pt), (JCExpression)tree);
            result.type = (tree.type.constValue() != null) ? cfolder.coerce(tree.type, pt)
                                                           : pt;
            return result;
        }
    public void visitIf(JCIf tree) {
        JCTree cond = tree.cond = translate(tree.cond, syms.booleanType);
        if (cond.type.isTrue()) {
            result = translate(tree.thenpart);
        } else if (cond.type.isFalse()) {
            if (tree.elsepart != null) {
                result = translate(tree.elsepart);
            } else {
                result = make.Skip();
            }
        } else {
            tree.thenpart = translate(tree.thenpart);
            tree.elsepart = translate(tree.elsepart);
            result = tree;
        }
    }
    public void visitAssert(JCAssert tree) {
        DiagnosticPosition detailPos = (tree.detail == null) ? tree.pos() : tree.detail.pos();
        tree.cond = translate(tree.cond, syms.booleanType);
        if (!tree.cond.type.isTrue()) {
            JCExpression cond = assertFlagTest(tree.pos());
            List<JCExpression> exnArgs = (tree.detail == null) ?
                List.<JCExpression>nil() : List.of(translate(tree.detail));
            if (!tree.cond.type.isFalse()) {
                cond = makeBinary
                    (JCTree.AND,
                     cond,
                     makeUnary(JCTree.NOT, tree.cond));
            }
            result =
                make.If(cond,
                        make_at(detailPos).
                           Throw(makeNewClass(syms.assertionErrorType, exnArgs)),
                        null);
        } else {
            result = make.Skip();
        }
    }
    public void visitApply(JCMethodInvocation tree) {
        Symbol meth = TreeInfo.symbol(tree.meth);
        List<Type> argtypes = meth.type.getParameterTypes();
        if (allowEnums &&
            meth.name==names.init &&
            meth.owner == syms.enumSym)
            argtypes = argtypes.tail.tail;
        tree.args = boxArgs(argtypes, tree.args, tree.varargsElement);
        tree.varargsElement = null;
        Name methName = TreeInfo.name(tree.meth);
        if (meth.name==names.init) {
            Symbol constructor = accessConstructor(tree.pos(), meth);
            if (constructor != meth) {
                tree.args = tree.args.append(makeNull());
                TreeInfo.setSymbol(tree.meth, constructor);
            }
            ClassSymbol c = (ClassSymbol)constructor.owner;
            if ((c.owner.kind & (VAR | MTH)) != 0) {
                tree.args = tree.args.appendList(loadFreevars(tree.pos(), freevars(c)));
            }
            if ((c.flags_field&ENUM) != 0 || c.getQualifiedName() == names.java_lang_Enum) {
                List<JCVariableDecl> params = currentMethodDef.params;
                if (currentMethodSym.owner.hasOuterInstance())
                    params = params.tail; 
                tree.args = tree.args
                    .prepend(make_at(tree.pos()).Ident(params.tail.head.sym)) 
                    .prepend(make.Ident(params.head.sym)); 
            }
            if (c.hasOuterInstance()) {
                JCExpression thisArg;
                if (tree.meth.getTag() == JCTree.SELECT) {
                    thisArg = attr.
                        makeNullCheck(translate(((JCFieldAccess) tree.meth).selected));
                    tree.meth = make.Ident(constructor);
                    ((JCIdent) tree.meth).name = methName;
                } else if ((c.owner.kind & (MTH | VAR)) != 0 || methName == names._this){
                    thisArg = makeThis(tree.meth.pos(), c.type.getEnclosingType().tsym);
                } else {
                    thisArg = makeOwnerThisN(tree.meth.pos(), c, false);
                }
                tree.args = tree.args.prepend(thisArg);
            }
        } else {
            tree.meth = translate(tree.meth);
            if (tree.meth.getTag() == JCTree.APPLY) {
                JCMethodInvocation app = (JCMethodInvocation)tree.meth;
                app.args = tree.args.prependList(app.args);
                result = app;
                return;
            }
        }
        result = tree;
    }
    List<JCExpression> boxArgs(List<Type> parameters, List<JCExpression> _args, Type varargsElement) {
        List<JCExpression> args = _args;
        if (parameters.isEmpty()) return args;
        boolean anyChanges = false;
        ListBuffer<JCExpression> result = new ListBuffer<JCExpression>();
        while (parameters.tail.nonEmpty()) {
            JCExpression arg = translate(args.head, parameters.head);
            anyChanges |= (arg != args.head);
            result.append(arg);
            args = args.tail;
            parameters = parameters.tail;
        }
        Type parameter = parameters.head;
        if (varargsElement != null) {
            anyChanges = true;
            ListBuffer<JCExpression> elems = new ListBuffer<JCExpression>();
            while (args.nonEmpty()) {
                JCExpression arg = translate(args.head, varargsElement);
                elems.append(arg);
                args = args.tail;
            }
            JCNewArray boxedArgs = make.NewArray(make.Type(varargsElement),
                                               List.<JCExpression>nil(),
                                               elems.toList());
            boxedArgs.type = new ArrayType(varargsElement, syms.arrayClass);
            result.append(boxedArgs);
        } else {
            if (args.length() != 1) throw new AssertionError(args);
            JCExpression arg = translate(args.head, parameter);
            anyChanges |= (arg != args.head);
            result.append(arg);
            if (!anyChanges) return _args;
        }
        return result.toList();
    }
    @SuppressWarnings("unchecked") 
    <T extends JCTree> T boxIfNeeded(T tree, Type type) {
        boolean havePrimitive = tree.type.isPrimitive();
        if (havePrimitive == type.isPrimitive())
            return tree;
        if (havePrimitive) {
            Type unboxedTarget = types.unboxedType(type);
            if (unboxedTarget.tag != NONE) {
                if (!types.isSubtype(tree.type, unboxedTarget)) 
                    tree.type = unboxedTarget.constType(tree.type.constValue());
                return (T)boxPrimitive((JCExpression)tree, type);
            } else {
                tree = (T)boxPrimitive((JCExpression)tree);
            }
        } else {
            tree = (T)unbox((JCExpression)tree, type);
        }
        return tree;
    }
    JCExpression boxPrimitive(JCExpression tree) {
        return boxPrimitive(tree, types.boxedClass(tree.type).type);
    }
    JCExpression boxPrimitive(JCExpression tree, Type box) {
        make_at(tree.pos());
        if (target.boxWithConstructors()) {
            Symbol ctor = lookupConstructor(tree.pos(),
                                            box,
                                            List.<Type>nil()
                                            .prepend(tree.type));
            return make.Create(ctor, List.of(tree));
        } else {
            Symbol valueOfSym = lookupMethod(tree.pos(),
                                             names.valueOf,
                                             box,
                                             List.<Type>nil()
                                             .prepend(tree.type));
            return make.App(make.QualIdent(valueOfSym), List.of(tree));
        }
    }
    JCExpression unbox(JCExpression tree, Type primitive) {
        Type unboxedType = types.unboxedType(tree.type);
        if (unboxedType.tag == NONE) {
            unboxedType = primitive;
            if (!unboxedType.isPrimitive())
                throw new AssertionError(unboxedType);
            make_at(tree.pos());
            tree = make.TypeCast(types.boxedClass(unboxedType).type, tree);
        } else {
            if (!types.isSubtype(unboxedType, primitive))
                throw new AssertionError(tree);
        }
        make_at(tree.pos());
        Symbol valueSym = lookupMethod(tree.pos(),
                                       unboxedType.tsym.name.append(names.Value), 
                                       tree.type,
                                       List.<Type>nil());
        return make.App(make.Select(tree, valueSym));
    }
    public void visitParens(JCParens tree) {
        JCTree expr = translate(tree.expr);
        result = ((expr == tree.expr) ? tree : expr);
    }
    public void visitIndexed(JCArrayAccess tree) {
        tree.indexed = translate(tree.indexed);
        tree.index = translate(tree.index, syms.intType);
        result = tree;
    }
    public void visitAssign(JCAssign tree) {
        tree.lhs = translate(tree.lhs, tree);
        tree.rhs = translate(tree.rhs, tree.lhs.type);
        if (tree.lhs.getTag() == JCTree.APPLY) {
            JCMethodInvocation app = (JCMethodInvocation)tree.lhs;
            app.args = List.of(tree.rhs).prependList(app.args);
            result = app;
        } else {
            result = tree;
        }
    }
    public void visitAssignop(final JCAssignOp tree) {
        if (!tree.lhs.type.isPrimitive() &&
            tree.operator.type.getReturnType().isPrimitive()) {
            JCTree newTree = abstractLval(tree.lhs, new TreeBuilder() {
                    public JCTree build(final JCTree lhs) {
                        int newTag = tree.getTag() - JCTree.ASGOffset;
                        Symbol newOperator = rs.resolveBinaryOperator(tree.pos(),
                                                                      newTag,
                                                                      attrEnv,
                                                                      tree.type,
                                                                      tree.rhs.type);
                        JCExpression expr = (JCExpression)lhs;
                        if (expr.type != tree.type)
                            expr = make.TypeCast(tree.type, expr);
                        JCBinary opResult = make.Binary(newTag, expr, tree.rhs);
                        opResult.operator = newOperator;
                        opResult.type = newOperator.type.getReturnType();
                        JCTypeCast newRhs = make.TypeCast(types.unboxedType(tree.type),
                                                          opResult);
                        return make.Assign((JCExpression)lhs, newRhs).setType(tree.type);
                    }
                });
            result = translate(newTree);
            return;
        }
        tree.lhs = translate(tree.lhs, tree);
        tree.rhs = translate(tree.rhs, tree.operator.type.getParameterTypes().tail.head);
        if (tree.lhs.getTag() == JCTree.APPLY) {
            JCMethodInvocation app = (JCMethodInvocation)tree.lhs;
            JCExpression rhs = (((OperatorSymbol)tree.operator).opcode == string_add)
              ? makeString(tree.rhs)
              : tree.rhs;
            app.args = List.of(rhs).prependList(app.args);
            result = app;
        } else {
            result = tree;
        }
    }
    JCTree lowerBoxedPostop(final JCUnary tree) {
        final boolean cast = TreeInfo.skipParens(tree.arg).getTag() == JCTree.TYPECAST;
        return abstractLval(tree.arg, new TreeBuilder() {
                public JCTree build(final JCTree tmp1) {
                    return abstractRval(tmp1, tree.arg.type, new TreeBuilder() {
                            public JCTree build(final JCTree tmp2) {
                                int opcode = (tree.getTag() == JCTree.POSTINC)
                                    ? JCTree.PLUS_ASG : JCTree.MINUS_ASG;
                                JCTree lhs = cast
                                    ? make.TypeCast(tree.arg.type, (JCExpression)tmp1)
                                    : tmp1;
                                JCTree update = makeAssignop(opcode,
                                                             lhs,
                                                             make.Literal(1));
                                return makeComma(update, tmp2);
                            }
                        });
                }
            });
    }
    public void visitUnary(JCUnary tree) {
        boolean isUpdateOperator =
            JCTree.PREINC <= tree.getTag() && tree.getTag() <= JCTree.POSTDEC;
        if (isUpdateOperator && !tree.arg.type.isPrimitive()) {
            switch(tree.getTag()) {
            case JCTree.PREINC:            
            case JCTree.PREDEC:            
                {
                    int opcode = (tree.getTag() == JCTree.PREINC)
                        ? JCTree.PLUS_ASG : JCTree.MINUS_ASG;
                    JCAssignOp newTree = makeAssignop(opcode,
                                                    tree.arg,
                                                    make.Literal(1));
                    result = translate(newTree, tree.type);
                    return;
                }
            case JCTree.POSTINC:           
            case JCTree.POSTDEC:           
                {
                    result = translate(lowerBoxedPostop(tree), tree.type);
                    return;
                }
            }
            throw new AssertionError(tree);
        }
        tree.arg = boxIfNeeded(translate(tree.arg, tree), tree.type);
        if (tree.getTag() == JCTree.NOT && tree.arg.type.constValue() != null) {
            tree.type = cfolder.fold1(bool_not, tree.arg.type);
        }
        if (isUpdateOperator && tree.arg.getTag() == JCTree.APPLY) {
            result = tree.arg;
        } else {
            result = tree;
        }
    }
    public void visitBinary(JCBinary tree) {
        List<Type> formals = tree.operator.type.getParameterTypes();
        JCTree lhs = tree.lhs = translate(tree.lhs, formals.head);
        switch (tree.getTag()) {
        case JCTree.OR:
            if (lhs.type.isTrue()) {
                result = lhs;
                return;
            }
            if (lhs.type.isFalse()) {
                result = translate(tree.rhs, formals.tail.head);
                return;
            }
            break;
        case JCTree.AND:
            if (lhs.type.isFalse()) {
                result = lhs;
                return;
            }
            if (lhs.type.isTrue()) {
                result = translate(tree.rhs, formals.tail.head);
                return;
            }
            break;
        }
        tree.rhs = translate(tree.rhs, formals.tail.head);
        result = tree;
    }
    public void visitIdent(JCIdent tree) {
        result = access(tree.sym, tree, enclOp, false);
    }
    public void visitForeachLoop(JCEnhancedForLoop tree) {
        if (types.elemtype(tree.expr.type) == null)
            visitIterableForeachLoop(tree);
        else
            visitArrayForeachLoop(tree);
    }
        private void visitArrayForeachLoop(JCEnhancedForLoop tree) {
            make_at(tree.expr.pos());
            VarSymbol arraycache = new VarSymbol(0,
                                                 names.fromString("arr" + target.syntheticNameChar()),
                                                 tree.expr.type,
                                                 currentMethodSym);
            JCStatement arraycachedef = make.VarDef(arraycache, tree.expr);
            VarSymbol lencache = new VarSymbol(0,
                                               names.fromString("len" + target.syntheticNameChar()),
                                               syms.intType,
                                               currentMethodSym);
            JCStatement lencachedef = make.
                VarDef(lencache, make.Select(make.Ident(arraycache), syms.lengthVar));
            VarSymbol index = new VarSymbol(0,
                                            names.fromString("i" + target.syntheticNameChar()),
                                            syms.intType,
                                            currentMethodSym);
            JCVariableDecl indexdef = make.VarDef(index, make.Literal(INT, 0));
            indexdef.init.type = indexdef.type = syms.intType.constType(0);
            List<JCStatement> loopinit = List.of(arraycachedef, lencachedef, indexdef);
            JCBinary cond = makeBinary(JCTree.LT, make.Ident(index), make.Ident(lencache));
            JCExpressionStatement step = make.Exec(makeUnary(JCTree.PREINC, make.Ident(index)));
            Type elemtype = types.elemtype(tree.expr.type);
            JCExpression loopvarinit = make.Indexed(make.Ident(arraycache),
                                                    make.Ident(index)).setType(elemtype);
            JCVariableDecl loopvardef = (JCVariableDecl)make.VarDef(tree.var.mods,
                                                  tree.var.name,
                                                  tree.var.vartype,
                                                  loopvarinit).setType(tree.var.type);
            loopvardef.sym = tree.var.sym;
            JCBlock body = make.
                Block(0, List.of(loopvardef, tree.body));
            result = translate(make.
                               ForLoop(loopinit,
                                       cond,
                                       List.of(step),
                                       body));
            patchTargets(body, tree, result);
        }
        private void patchTargets(JCTree body, final JCTree src, final JCTree dest) {
            class Patcher extends TreeScanner {
                public void visitBreak(JCBreak tree) {
                    if (tree.target == src)
                        tree.target = dest;
                }
                public void visitContinue(JCContinue tree) {
                    if (tree.target == src)
                        tree.target = dest;
                }
                public void visitClassDef(JCClassDecl tree) {}
            }
            new Patcher().scan(body);
        }
        private void visitIterableForeachLoop(JCEnhancedForLoop tree) {
            make_at(tree.expr.pos());
            Type iteratorTarget = syms.objectType;
            Type iterableType = types.asSuper(types.upperBound(tree.expr.type),
                                              syms.iterableType.tsym);
            if (iterableType.getTypeArguments().nonEmpty())
                iteratorTarget = types.erasure(iterableType.getTypeArguments().head);
            Type eType = tree.expr.type;
            tree.expr.type = types.erasure(eType);
            if (eType.tag == TYPEVAR && eType.getUpperBound().isCompound())
                tree.expr = make.TypeCast(types.erasure(iterableType), tree.expr);
            Symbol iterator = lookupMethod(tree.expr.pos(),
                                           names.iterator,
                                           types.erasure(syms.iterableType),
                                           List.<Type>nil());
            VarSymbol itvar = new VarSymbol(0, names.fromString("i" + target.syntheticNameChar()),
                                            types.erasure(iterator.type.getReturnType()),
                                            currentMethodSym);
            JCStatement init = make.
                VarDef(itvar,
                       make.App(make.Select(tree.expr, iterator)));
            Symbol hasNext = lookupMethod(tree.expr.pos(),
                                          names.hasNext,
                                          itvar.type,
                                          List.<Type>nil());
            JCMethodInvocation cond = make.App(make.Select(make.Ident(itvar), hasNext));
            Symbol next = lookupMethod(tree.expr.pos(),
                                       names.next,
                                       itvar.type,
                                       List.<Type>nil());
            JCExpression vardefinit = make.App(make.Select(make.Ident(itvar), next));
            if (tree.var.type.isPrimitive())
                vardefinit = make.TypeCast(types.upperBound(iteratorTarget), vardefinit);
            else
                vardefinit = make.TypeCast(tree.var.type, vardefinit);
            JCVariableDecl indexDef = (JCVariableDecl)make.VarDef(tree.var.mods,
                                                  tree.var.name,
                                                  tree.var.vartype,
                                                  vardefinit).setType(tree.var.type);
            indexDef.sym = tree.var.sym;
            JCBlock body = make.Block(0, List.of(indexDef, tree.body));
            body.endpos = TreeInfo.endPos(tree.body);
            result = translate(make.
                ForLoop(List.of(init),
                        cond,
                        List.<JCExpressionStatement>nil(),
                        body));
            patchTargets(body, tree, result);
        }
    public void visitVarDef(JCVariableDecl tree) {
        MethodSymbol oldMethodSym = currentMethodSym;
        tree.mods = translate(tree.mods);
        tree.vartype = translate(tree.vartype);
        if (currentMethodSym == null) {
            currentMethodSym =
                new MethodSymbol((tree.mods.flags&STATIC) | BLOCK,
                                 names.empty, null,
                                 currentClass);
        }
        if (tree.init != null) tree.init = translate(tree.init, tree.type);
        result = tree;
        currentMethodSym = oldMethodSym;
    }
    public void visitBlock(JCBlock tree) {
        MethodSymbol oldMethodSym = currentMethodSym;
        if (currentMethodSym == null) {
            currentMethodSym =
                new MethodSymbol(tree.flags | BLOCK,
                                 names.empty, null,
                                 currentClass);
        }
        super.visitBlock(tree);
        currentMethodSym = oldMethodSym;
    }
    public void visitDoLoop(JCDoWhileLoop tree) {
        tree.body = translate(tree.body);
        tree.cond = translate(tree.cond, syms.booleanType);
        result = tree;
    }
    public void visitWhileLoop(JCWhileLoop tree) {
        tree.cond = translate(tree.cond, syms.booleanType);
        tree.body = translate(tree.body);
        result = tree;
    }
    public void visitForLoop(JCForLoop tree) {
        tree.init = translate(tree.init);
        if (tree.cond != null)
            tree.cond = translate(tree.cond, syms.booleanType);
        tree.step = translate(tree.step);
        tree.body = translate(tree.body);
        result = tree;
    }
    public void visitReturn(JCReturn tree) {
        if (tree.expr != null)
            tree.expr = translate(tree.expr,
                                  types.erasure(currentMethodDef
                                                .restype.type));
        result = tree;
    }
    public void visitSwitch(JCSwitch tree) {
        Type selsuper = types.supertype(tree.selector.type);
        boolean enumSwitch = selsuper != null &&
            (tree.selector.type.tsym.flags() & ENUM) != 0;
        boolean stringSwitch = selsuper != null &&
            types.isSameType(tree.selector.type, syms.stringType);
        Type target = enumSwitch ? tree.selector.type :
            (stringSwitch? syms.stringType : syms.intType);
        tree.selector = translate(tree.selector, target);
        tree.cases = translateCases(tree.cases);
        if (enumSwitch) {
            result = visitEnumSwitch(tree);
        } else if (stringSwitch) {
            result = visitStringSwitch(tree);
        } else {
            result = tree;
        }
    }
    public JCTree visitEnumSwitch(JCSwitch tree) {
        TypeSymbol enumSym = tree.selector.type.tsym;
        EnumMapping map = mapForEnum(tree.pos(), enumSym);
        make_at(tree.pos());
        Symbol ordinalMethod = lookupMethod(tree.pos(),
                                            names.ordinal,
                                            tree.selector.type,
                                            List.<Type>nil());
        JCArrayAccess selector = make.Indexed(map.mapVar,
                                        make.App(make.Select(tree.selector,
                                                             ordinalMethod)));
        ListBuffer<JCCase> cases = new ListBuffer<JCCase>();
        for (JCCase c : tree.cases) {
            if (c.pat != null) {
                VarSymbol label = (VarSymbol)TreeInfo.symbol(c.pat);
                JCLiteral pat = map.forConstant(label);
                cases.append(make.Case(pat, c.stats));
            } else {
                cases.append(c);
            }
        }
        JCSwitch enumSwitch = make.Switch(selector, cases.toList());
        patchTargets(enumSwitch, tree, enumSwitch);
        return enumSwitch;
    }
    public JCTree visitStringSwitch(JCSwitch tree) {
        List<JCCase> caseList = tree.getCases();
        int alternatives = caseList.size();
        if (alternatives == 0) { 
            return make.at(tree.pos()).Exec(attr.makeNullCheck(tree.getExpression()));
        } else {
            ListBuffer<JCStatement> stmtList = new ListBuffer<JCStatement>();
            Map<String, Integer> caseLabelToPosition =
                new LinkedHashMap<String, Integer>(alternatives + 1, 1.0f);
            Map<Integer, Set<String>> hashToString =
                new LinkedHashMap<Integer, Set<String>>(alternatives + 1, 1.0f);
            int casePosition = 0;
            for(JCCase oneCase : caseList) {
                JCExpression expression = oneCase.getExpression();
                if (expression != null) { 
                    String labelExpr = (String) expression.type.constValue();
                    Integer mapping = caseLabelToPosition.put(labelExpr, casePosition);
                    Assert.checkNull(mapping);
                    int hashCode = labelExpr.hashCode();
                    Set<String> stringSet = hashToString.get(hashCode);
                    if (stringSet == null) {
                        stringSet = new LinkedHashSet<String>(1, 1.0f);
                        stringSet.add(labelExpr);
                        hashToString.put(hashCode, stringSet);
                    } else {
                        boolean added = stringSet.add(labelExpr);
                        Assert.check(added);
                    }
                }
                casePosition++;
            }
            VarSymbol dollar_s = new VarSymbol(FINAL|SYNTHETIC,
                                               names.fromString("s" + tree.pos + target.syntheticNameChar()),
                                               syms.stringType,
                                               currentMethodSym);
            stmtList.append(make.at(tree.pos()).VarDef(dollar_s, tree.getExpression()).setType(dollar_s.type));
            VarSymbol dollar_tmp = new VarSymbol(SYNTHETIC,
                                                 names.fromString("tmp" + tree.pos + target.syntheticNameChar()),
                                                 syms.intType,
                                                 currentMethodSym);
            JCVariableDecl dollar_tmp_def =
                (JCVariableDecl)make.VarDef(dollar_tmp, make.Literal(INT, -1)).setType(dollar_tmp.type);
            dollar_tmp_def.init.type = dollar_tmp.type = syms.intType;
            stmtList.append(dollar_tmp_def);
            ListBuffer<JCCase> caseBuffer = ListBuffer.lb();
            JCMethodInvocation hashCodeCall = makeCall(make.Ident(dollar_s),
                                                       names.hashCode,
                                                       List.<JCExpression>nil()).setType(syms.intType);
            JCSwitch switch1 = make.Switch(hashCodeCall,
                                        caseBuffer.toList());
            for(Map.Entry<Integer, Set<String>> entry : hashToString.entrySet()) {
                int hashCode = entry.getKey();
                Set<String> stringsWithHashCode = entry.getValue();
                Assert.check(stringsWithHashCode.size() >= 1);
                JCStatement elsepart = null;
                for(String caseLabel : stringsWithHashCode ) {
                    JCMethodInvocation stringEqualsCall = makeCall(make.Ident(dollar_s),
                                                                   names.equals,
                                                                   List.<JCExpression>of(make.Literal(caseLabel)));
                    elsepart = make.If(stringEqualsCall,
                                       make.Exec(make.Assign(make.Ident(dollar_tmp),
                                                             make.Literal(caseLabelToPosition.get(caseLabel))).
                                                 setType(dollar_tmp.type)),
                                       elsepart);
                }
                ListBuffer<JCStatement> lb = ListBuffer.lb();
                JCBreak breakStmt = make.Break(null);
                breakStmt.target = switch1;
                lb.append(elsepart).append(breakStmt);
                caseBuffer.append(make.Case(make.Literal(hashCode), lb.toList()));
            }
            switch1.cases = caseBuffer.toList();
            stmtList.append(switch1);
            ListBuffer<JCCase> lb = ListBuffer.lb();
            JCSwitch switch2 = make.Switch(make.Ident(dollar_tmp), lb.toList());
            for(JCCase oneCase : caseList ) {
                patchTargets(oneCase, tree, switch2);
                boolean isDefault = (oneCase.getExpression() == null);
                JCExpression caseExpr;
                if (isDefault)
                    caseExpr = null;
                else {
                    caseExpr = make.Literal(caseLabelToPosition.get((String)oneCase.
                                                                    getExpression().
                                                                    type.constValue()));
                }
                lb.append(make.Case(caseExpr,
                                    oneCase.getStatements()));
            }
            switch2.cases = lb.toList();
            stmtList.append(switch2);
            return make.Block(0L, stmtList.toList());
        }
    }
    public void visitNewArray(JCNewArray tree) {
        tree.elemtype = translate(tree.elemtype);
        for (List<JCExpression> t = tree.dims; t.tail != null; t = t.tail)
            if (t.head != null) t.head = translate(t.head, syms.intType);
        tree.elems = translate(tree.elems, types.elemtype(tree.type));
        result = tree;
    }
    public void visitSelect(JCFieldAccess tree) {
        boolean qualifiedSuperAccess =
            tree.selected.getTag() == JCTree.SELECT &&
            TreeInfo.name(tree.selected) == names._super;
        tree.selected = translate(tree.selected);
        if (tree.name == names._class)
            result = classOf(tree.selected);
        else if (tree.name == names._this || tree.name == names._super)
            result = makeThis(tree.pos(), tree.selected.type.tsym);
        else
            result = access(tree.sym, tree, enclOp, qualifiedSuperAccess);
    }
    public void visitLetExpr(LetExpr tree) {
        tree.defs = translateVarDefs(tree.defs);
        tree.expr = translate(tree.expr, tree.type);
        result = tree;
    }
    public void visitAnnotation(JCAnnotation tree) {
        result = tree;
    }
    @Override
    public void visitTry(JCTry tree) {
        if (tree.resources.isEmpty()) {
            super.visitTry(tree);
        } else {
            result = makeTwrTry(tree);
        }
    }
    public List<JCTree> translateTopLevelClass(Env<AttrContext> env, JCTree cdef, TreeMaker make) {
        ListBuffer<JCTree> translated = null;
        try {
            attrEnv = env;
            this.make = make;
            endPositions = env.toplevel.endPositions;
            currentClass = null;
            currentMethodDef = null;
            outermostClassDef = (cdef.getTag() == JCTree.CLASSDEF) ? (JCClassDecl)cdef : null;
            outermostMemberDef = null;
            this.translated = new ListBuffer<JCTree>();
            classdefs = new HashMap<ClassSymbol,JCClassDecl>();
            actualSymbols = new HashMap<Symbol,Symbol>();
            freevarCache = new HashMap<ClassSymbol,List<VarSymbol>>();
            proxies = new Scope(syms.noSymbol);
            twrVars = new Scope(syms.noSymbol);
            outerThisStack = List.nil();
            accessNums = new HashMap<Symbol,Integer>();
            accessSyms = new HashMap<Symbol,MethodSymbol[]>();
            accessConstrs = new HashMap<Symbol,MethodSymbol>();
            accessConstrTags = List.nil();
            accessed = new ListBuffer<Symbol>();
            translate(cdef, (JCExpression)null);
            for (List<Symbol> l = accessed.toList(); l.nonEmpty(); l = l.tail)
                makeAccessible(l.head);
            for (EnumMapping map : enumSwitchMap.values())
                map.translate();
            checkConflicts(this.translated.toList());
            checkAccessConstructorTags();
            translated = this.translated;
        } finally {
            attrEnv = null;
            this.make = null;
            endPositions = null;
            currentClass = null;
            currentMethodDef = null;
            outermostClassDef = null;
            outermostMemberDef = null;
            this.translated = null;
            classdefs = null;
            actualSymbols = null;
            freevarCache = null;
            proxies = null;
            outerThisStack = null;
            accessNums = null;
            accessSyms = null;
            accessConstrs = null;
            accessConstrTags = null;
            accessed = null;
            enumSwitchMap.clear();
        }
        return translated.toList();
    }
    private void addEnumCompatibleMembers(JCClassDecl cdef) {
        make_at(null);
        VarSymbol ordinalFieldSym = addEnumOrdinalField(cdef);
        VarSymbol nameFieldSym = addEnumNameField(cdef);
        MethodSymbol ordinalMethodSym = addEnumFieldOrdinalMethod(cdef, ordinalFieldSym);
        MethodSymbol nameMethodSym = addEnumFieldNameMethod(cdef, nameFieldSym);
        addEnumToString(cdef, nameFieldSym);
        addEnumCompareTo(cdef, ordinalFieldSym);
    }
    private VarSymbol addEnumOrdinalField(JCClassDecl cdef) {
        VarSymbol ordinal = new VarSymbol(PRIVATE|FINAL|SYNTHETIC,
                                          names.fromString("$ordinal"),
                                          syms.intType,
                                          cdef.sym);
        cdef.sym.members().enter(ordinal);
        cdef.defs = cdef.defs.prepend(make.VarDef(ordinal, null));
        return ordinal;
    }
    private VarSymbol addEnumNameField(JCClassDecl cdef) {
        VarSymbol name = new VarSymbol(PRIVATE|FINAL|SYNTHETIC,
                                          names.fromString("$name"),
                                          syms.stringType,
                                          cdef.sym);
        cdef.sym.members().enter(name);
        cdef.defs = cdef.defs.prepend(make.VarDef(name, null));
        return name;
    }
    private MethodSymbol addEnumFieldOrdinalMethod(JCClassDecl cdef, VarSymbol ordinalSymbol) {
        Symbol ordinalSym = lookupMethod(cdef.pos(),
                                         names.ordinal,
                                         cdef.type,
                                         List.<Type>nil());
        Assert.check(ordinalSym instanceof MethodSymbol);
        JCStatement ret = make.Return(make.Ident(ordinalSymbol));
        cdef.defs = cdef.defs.append(make.MethodDef((MethodSymbol)ordinalSym,
                                                    make.Block(0L, List.of(ret))));
        return (MethodSymbol)ordinalSym;
    }
    private MethodSymbol addEnumFieldNameMethod(JCClassDecl cdef, VarSymbol nameSymbol) {
        Symbol nameSym = lookupMethod(cdef.pos(),
                                   names._name,
                                   cdef.type,
                                   List.<Type>nil());
        Assert.check(nameSym instanceof MethodSymbol);
        JCStatement ret = make.Return(make.Ident(nameSymbol));
        cdef.defs = cdef.defs.append(make.MethodDef((MethodSymbol)nameSym,
                                                    make.Block(0L, List.of(ret))));
        return (MethodSymbol)nameSym;
    }
    private MethodSymbol addEnumToString(JCClassDecl cdef,
                                         VarSymbol nameSymbol) {
        Symbol toStringSym = lookupMethod(cdef.pos(),
                                          names.toString,
                                          cdef.type,
                                          List.<Type>nil());
        JCTree toStringDecl = null;
        if (toStringSym != null)
            toStringDecl = TreeInfo.declarationFor(toStringSym, cdef);
        if (toStringDecl != null)
            return (MethodSymbol)toStringSym;
        JCStatement ret = make.Return(make.Ident(nameSymbol));
        JCTree resTypeTree = make.Type(syms.stringType);
        MethodType toStringType = new MethodType(List.<Type>nil(),
                                                 syms.stringType,
                                                 List.<Type>nil(),
                                                 cdef.sym);
        toStringSym = new MethodSymbol(PUBLIC,
                                       names.toString,
                                       toStringType,
                                       cdef.type.tsym);
        toStringDecl = make.MethodDef((MethodSymbol)toStringSym,
                                      make.Block(0L, List.of(ret)));
        cdef.defs = cdef.defs.prepend(toStringDecl);
        cdef.sym.members().enter(toStringSym);
        return (MethodSymbol)toStringSym;
    }
    private MethodSymbol addEnumCompareTo(JCClassDecl cdef, VarSymbol ordinalSymbol) {
        Symbol compareToSym = lookupMethod(cdef.pos(),
                                   names.compareTo,
                                   cdef.type,
                                   List.of(cdef.sym.type));
        Assert.check(compareToSym instanceof MethodSymbol);
        JCMethodDecl compareToDecl = (JCMethodDecl) TreeInfo.declarationFor(compareToSym, cdef);
        ListBuffer<JCStatement> blockStatements = new ListBuffer<JCStatement>();
        JCModifiers mod1 = make.Modifiers(0L);
        Name oName = names.fromString("o");
        JCVariableDecl par1 = make.Param(oName, cdef.type, compareToSym);
        JCIdent paramId1 = make.Ident(names.java_lang_Object);
        paramId1.type = cdef.type;
        paramId1.sym = par1.sym;
        ((MethodSymbol)compareToSym).params = List.of(par1.sym);
        JCIdent par1UsageId = make.Ident(par1.sym);
        JCIdent castTargetIdent = make.Ident(cdef.sym);
        JCTypeCast cast = make.TypeCast(castTargetIdent, par1UsageId);
        cast.setType(castTargetIdent.type);
        Name otherName = names.fromString("other");
        VarSymbol otherVarSym = new VarSymbol(mod1.flags,
                                              otherName,
                                              cdef.type,
                                              compareToSym);
        JCVariableDecl otherVar = make.VarDef(otherVarSym, cast);
        blockStatements.append(otherVar);
        JCIdent id1 = make.Ident(ordinalSymbol);
        JCIdent fLocUsageId = make.Ident(otherVarSym);
        JCExpression sel = make.Select(fLocUsageId, ordinalSymbol);
        JCBinary bin = makeBinary(JCTree.MINUS, id1, sel);
        JCReturn ret = make.Return(bin);
        blockStatements.append(ret);
        JCMethodDecl compareToMethod = make.MethodDef((MethodSymbol)compareToSym,
                                                   make.Block(0L,
                                                              blockStatements.toList()));
        compareToMethod.params = List.of(par1);
        cdef.defs = cdef.defs.append(compareToMethod);
        return (MethodSymbol)compareToSym;
    }
}
