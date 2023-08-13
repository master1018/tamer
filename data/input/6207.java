public class Annotate {
    protected static final Context.Key<Annotate> annotateKey =
        new Context.Key<Annotate>();
    public static Annotate instance(Context context) {
        Annotate instance = context.get(annotateKey);
        if (instance == null)
            instance = new Annotate(context);
        return instance;
    }
    final Attr attr;
    final TreeMaker make;
    final Log log;
    final Symtab syms;
    final Names names;
    final Resolve rs;
    final Types types;
    final ConstFold cfolder;
    final Check chk;
    protected Annotate(Context context) {
        context.put(annotateKey, this);
        attr = Attr.instance(context);
        make = TreeMaker.instance(context);
        log = Log.instance(context);
        syms = Symtab.instance(context);
        names = Names.instance(context);
        rs = Resolve.instance(context);
        types = Types.instance(context);
        cfolder = ConstFold.instance(context);
        chk = Check.instance(context);
    }
    private int enterCount = 0;
    ListBuffer<Annotator> q = new ListBuffer<Annotator>();
    public void later(Annotator a) {
        q.append(a);
    }
    public void earlier(Annotator a) {
        q.prepend(a);
    }
    public void enterStart() {
        enterCount++;
    }
    public void enterDone() {
        enterCount--;
        flush();
    }
    public void flush() {
        if (enterCount != 0) return;
        enterCount++;
        try {
            while (q.nonEmpty())
                q.next().enterAnnotation();
        } finally {
            enterCount--;
        }
    }
    public interface Annotator {
        void enterAnnotation();
        String toString();
    }
    Attribute.Compound enterAnnotation(JCAnnotation a,
                                       Type expected,
                                       Env<AttrContext> env) {
        Type at = (a.annotationType.type != null ? a.annotationType.type
                  : attr.attribType(a.annotationType, env));
        a.type = chk.checkType(a.annotationType.pos(), at, expected);
        if (a.type.isErroneous())
            return new Attribute.Compound(a.type, List.<Pair<MethodSymbol,Attribute>>nil());
        if ((a.type.tsym.flags() & Flags.ANNOTATION) == 0) {
            log.error(a.annotationType.pos(),
                      "not.annotation.type", a.type.toString());
            return new Attribute.Compound(a.type, List.<Pair<MethodSymbol,Attribute>>nil());
        }
        List<JCExpression> args = a.args;
        if (args.length() == 1 && args.head.getTag() != JCTree.ASSIGN) {
            args.head = make.at(args.head.pos).
                Assign(make.Ident(names.value), args.head);
        }
        ListBuffer<Pair<MethodSymbol,Attribute>> buf =
            new ListBuffer<Pair<MethodSymbol,Attribute>>();
        for (List<JCExpression> tl = args; tl.nonEmpty(); tl = tl.tail) {
            JCExpression t = tl.head;
            if (t.getTag() != JCTree.ASSIGN) {
                log.error(t.pos(), "annotation.value.must.be.name.value");
                continue;
            }
            JCAssign assign = (JCAssign)t;
            if (assign.lhs.getTag() != JCTree.IDENT) {
                log.error(t.pos(), "annotation.value.must.be.name.value");
                continue;
            }
            JCIdent left = (JCIdent)assign.lhs;
            Symbol method = rs.resolveQualifiedMethod(left.pos(),
                                                          env,
                                                          a.type,
                                                          left.name,
                                                          List.<Type>nil(),
                                                          null);
            left.sym = method;
            left.type = method.type;
            if (method.owner != a.type.tsym)
                log.error(left.pos(), "no.annotation.member", left.name, a.type);
            Type result = method.type.getReturnType();
            Attribute value = enterAttributeValue(result, assign.rhs, env);
            if (!method.type.isErroneous())
                buf.append(new Pair<MethodSymbol,Attribute>
                           ((MethodSymbol)method, value));
            t.type = result;
        }
        return new Attribute.Compound(a.type, buf.toList());
    }
    Attribute enterAttributeValue(Type expected,
                                  JCExpression tree,
                                  Env<AttrContext> env) {
        try {
            expected.tsym.complete();
        } catch(CompletionFailure e) {
            log.error(tree.pos(), "cant.resolve", Kinds.kindName(e.sym), e.sym);
            return new Attribute.Error(expected);
        }
        if (expected.isPrimitive() || types.isSameType(expected, syms.stringType)) {
            Type result = attr.attribExpr(tree, env, expected);
            if (result.isErroneous())
                return new Attribute.Error(expected);
            if (result.constValue() == null) {
                log.error(tree.pos(), "attribute.value.must.be.constant");
                return new Attribute.Error(expected);
            }
            result = cfolder.coerce(result, expected);
            return new Attribute.Constant(expected, result.constValue());
        }
        if (expected.tsym == syms.classType.tsym) {
            Type result = attr.attribExpr(tree, env, expected);
            if (result.isErroneous())
                return new Attribute.Error(expected);
            if (TreeInfo.name(tree) != names._class) {
                log.error(tree.pos(), "annotation.value.must.be.class.literal");
                return new Attribute.Error(expected);
            }
            return new Attribute.Class(types,
                                       (((JCFieldAccess) tree).selected).type);
        }
        if ((expected.tsym.flags() & Flags.ANNOTATION) != 0) {
            if (tree.getTag() != JCTree.ANNOTATION) {
                log.error(tree.pos(), "annotation.value.must.be.annotation");
                expected = syms.errorType;
            }
            return enterAnnotation((JCAnnotation)tree, expected, env);
        }
        if (expected.tag == TypeTags.ARRAY) { 
            if (tree.getTag() != JCTree.NEWARRAY) {
                tree = make.at(tree.pos).
                    NewArray(null, List.<JCExpression>nil(), List.of(tree));
            }
            JCNewArray na = (JCNewArray)tree;
            if (na.elemtype != null) {
                log.error(na.elemtype.pos(), "new.not.allowed.in.annotation");
                return new Attribute.Error(expected);
            }
            ListBuffer<Attribute> buf = new ListBuffer<Attribute>();
            for (List<JCExpression> l = na.elems; l.nonEmpty(); l=l.tail) {
                buf.append(enterAttributeValue(types.elemtype(expected),
                                               l.head,
                                               env));
            }
            na.type = expected;
            return new Attribute.
                Array(expected, buf.toArray(new Attribute[buf.length()]));
        }
        if (expected.tag == TypeTags.CLASS &&
            (expected.tsym.flags() & Flags.ENUM) != 0) {
            attr.attribExpr(tree, env, expected);
            Symbol sym = TreeInfo.symbol(tree);
            if (sym == null ||
                TreeInfo.nonstaticSelect(tree) ||
                sym.kind != Kinds.VAR ||
                (sym.flags() & Flags.ENUM) == 0) {
                log.error(tree.pos(), "enum.annotation.must.be.enum.constant");
                return new Attribute.Error(expected);
            }
            VarSymbol enumerator = (VarSymbol) sym;
            return new Attribute.Enum(expected, enumerator);
        }
        if (!expected.isErroneous())
            log.error(tree.pos(), "annotation.value.not.allowable.type");
        return new Attribute.Error(attr.attribExpr(tree, env, expected));
    }
}
