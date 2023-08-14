public class RichDiagnosticFormatter extends
        ForwardingDiagnosticFormatter<JCDiagnostic, AbstractDiagnosticFormatter> {
    final Symtab syms;
    final Types types;
    final JCDiagnostic.Factory diags;
    final JavacMessages messages;
    protected ClassNameSimplifier nameSimplifier;
    private RichPrinter printer;
    Map<WhereClauseKind, Map<Type, JCDiagnostic>> whereClauses;
    public static RichDiagnosticFormatter instance(Context context) {
        RichDiagnosticFormatter instance = context.get(RichDiagnosticFormatter.class);
        if (instance == null)
            instance = new RichDiagnosticFormatter(context);
        return instance;
    }
    protected RichDiagnosticFormatter(Context context) {
        super((AbstractDiagnosticFormatter)Log.instance(context).getDiagnosticFormatter());
        setRichPrinter(new RichPrinter());
        this.syms = Symtab.instance(context);
        this.diags = JCDiagnostic.Factory.instance(context);
        this.types = Types.instance(context);
        this.messages = JavacMessages.instance(context);
        whereClauses = new LinkedHashMap<WhereClauseKind, Map<Type, JCDiagnostic>>();
        configuration = new RichConfiguration(Options.instance(context), formatter);
        for (WhereClauseKind kind : WhereClauseKind.values())
            whereClauses.put(kind, new LinkedHashMap<Type, JCDiagnostic>());
    }
    @Override
    public String format(JCDiagnostic diag, Locale l) {
        StringBuilder sb = new StringBuilder();
        nameSimplifier = new ClassNameSimplifier();
        for (WhereClauseKind kind : WhereClauseKind.values())
            whereClauses.get(kind).clear();
        preprocessDiagnostic(diag);
        sb.append(formatter.format(diag, l));
        if (getConfiguration().isEnabled(RichFormatterFeature.WHERE_CLAUSES)) {
            List<JCDiagnostic> clauses = getWhereClauses();
            String indent = formatter.isRaw() ? "" :
                formatter.indentString(DetailsInc);
            for (JCDiagnostic d : clauses) {
                String whereClause = formatter.format(d, l);
                if (whereClause.length() > 0) {
                    sb.append('\n' + indent + whereClause);
                }
            }
        }
        return sb.toString();
    }
    protected void setRichPrinter(RichPrinter printer) {
        this.printer = printer;
        formatter.setPrinter(printer);
    }
    protected RichPrinter getRichPrinter() {
        return printer;
    }
    protected void preprocessDiagnostic(JCDiagnostic diag) {
        for (Object o : diag.getArgs()) {
            if (o != null) {
                preprocessArgument(o);
            }
        }
        if (diag.isMultiline()) {
            for (JCDiagnostic d : diag.getSubdiagnostics())
                preprocessDiagnostic(d);
        }
    }
    protected void preprocessArgument(Object arg) {
        if (arg instanceof Type) {
            preprocessType((Type)arg);
        }
        else if (arg instanceof Symbol) {
            preprocessSymbol((Symbol)arg);
        }
        else if (arg instanceof JCDiagnostic) {
            preprocessDiagnostic((JCDiagnostic)arg);
        }
        else if (arg instanceof Iterable<?>) {
            for (Object o : (Iterable<?>)arg) {
                preprocessArgument(o);
            }
        }
    }
    protected List<JCDiagnostic> getWhereClauses() {
        List<JCDiagnostic> clauses = List.nil();
        for (WhereClauseKind kind : WhereClauseKind.values()) {
            List<JCDiagnostic> lines = List.nil();
            for (Map.Entry<Type, JCDiagnostic> entry : whereClauses.get(kind).entrySet()) {
                lines = lines.prepend(entry.getValue());
            }
            if (!lines.isEmpty()) {
                String key = kind.key();
                if (lines.size() > 1)
                    key += ".1";
                JCDiagnostic d = diags.fragment(key, whereClauses.get(kind).keySet());
                d = new JCDiagnostic.MultilineDiagnostic(d, lines.reverse());
                clauses = clauses.prepend(d);
            }
        }
        return clauses.reverse();
    }
    private int indexOf(Type type, WhereClauseKind kind) {
        int index = 1;
        for (Type t : whereClauses.get(kind).keySet()) {
            if (t.tsym == type.tsym) {
                return index;
            }
            if (kind != WhereClauseKind.TYPEVAR ||
                    t.toString().equals(type.toString())) {
                index++;
            }
        }
        return -1;
    }
    private boolean unique(TypeVar typevar) {
        int found = 0;
        for (Type t : whereClauses.get(WhereClauseKind.TYPEVAR).keySet()) {
            if (t.toString().equals(typevar.toString())) {
                found++;
            }
        }
        if (found < 1)
            throw new AssertionError("Missing type variable in where clause " + typevar);
        return found == 1;
    }
    enum WhereClauseKind {
        TYPEVAR("where.description.typevar"),
        CAPTURED("where.description.captured"),
        INTERSECTION("where.description.intersection");
        private String key;
        WhereClauseKind(String key) {
            this.key = key;
        }
        String key() {
            return key;
        }
    }
    protected class ClassNameSimplifier {
        Map<Name, List<Symbol>> nameClashes = new HashMap<Name, List<Symbol>>();
        protected void addUsage(Symbol sym) {
            Name n = sym.getSimpleName();
            List<Symbol> conflicts = nameClashes.get(n);
            if (conflicts == null) {
                conflicts = List.nil();
            }
            if (!conflicts.contains(sym))
                nameClashes.put(n, conflicts.append(sym));
        }
        public String simplify(Symbol s) {
            String name = s.getQualifiedName().toString();
            if (!s.type.isCompound()) {
                List<Symbol> conflicts = nameClashes.get(s.getSimpleName());
                if (conflicts == null ||
                    (conflicts.size() == 1 &&
                    conflicts.contains(s))) {
                    List<Name> l = List.nil();
                    Symbol s2 = s;
                    while (s2.type.getEnclosingType().tag == CLASS
                            && s2.owner.kind == Kinds.TYP) {
                        l = l.prepend(s2.getSimpleName());
                        s2 = s2.owner;
                    }
                    l = l.prepend(s2.getSimpleName());
                    StringBuilder buf = new StringBuilder();
                    String sep = "";
                    for (Name n2 : l) {
                        buf.append(sep);
                        buf.append(n2);
                        sep = ".";
                    }
                    name = buf.toString();
                }
            }
            return name;
        }
    };
    protected class RichPrinter extends Printer {
        @Override
        public String localize(Locale locale, String key, Object... args) {
            return formatter.localize(locale, key, args);
        }
        @Override
        public String capturedVarId(CapturedType t, Locale locale) {
            return indexOf(t, WhereClauseKind.CAPTURED) + "";
        }
        @Override
        public String visitType(Type t, Locale locale) {
            String s = super.visitType(t, locale);
            if (t == syms.botType)
                s = localize(locale, "compiler.misc.type.null");
            return s;
        }
        @Override
        public String visitCapturedType(CapturedType t, Locale locale) {
            if (getConfiguration().isEnabled(RichFormatterFeature.WHERE_CLAUSES)) {
                return localize(locale,
                    "compiler.misc.captured.type",
                    indexOf(t, WhereClauseKind.CAPTURED));
            }
            else
                return super.visitCapturedType(t, locale);
        }
        @Override
        public String visitClassType(ClassType t, Locale locale) {
            if (t.isCompound() &&
                    getConfiguration().isEnabled(RichFormatterFeature.WHERE_CLAUSES)) {
                return localize(locale,
                        "compiler.misc.intersection.type",
                        indexOf(t, WhereClauseKind.INTERSECTION));
            }
            else
                return super.visitClassType(t, locale);
        }
        @Override
        protected String className(ClassType t, boolean longform, Locale locale) {
            Symbol sym = t.tsym;
            if (sym.name.length() == 0 ||
                    !getConfiguration().isEnabled(RichFormatterFeature.SIMPLE_NAMES)) {
                return super.className(t, longform, locale);
            }
            else if (longform)
                return nameSimplifier.simplify(sym).toString();
            else
                return sym.name.toString();
        }
        @Override
        public String visitTypeVar(TypeVar t, Locale locale) {
            if (unique(t) ||
                    !getConfiguration().isEnabled(RichFormatterFeature.UNIQUE_TYPEVAR_NAMES)) {
                return t.toString();
            }
            else {
                return localize(locale,
                        "compiler.misc.type.var",
                        t.toString(), indexOf(t, WhereClauseKind.TYPEVAR));
            }
        }
        @Override
        protected String printMethodArgs(List<Type> args, boolean varArgs, Locale locale) {
            return super.printMethodArgs(args, varArgs, locale);
        }
        @Override
        public String visitClassSymbol(ClassSymbol s, Locale locale) {
            String name = nameSimplifier.simplify(s);
            if (name.length() == 0 ||
                    !getConfiguration().isEnabled(RichFormatterFeature.SIMPLE_NAMES)) {
                return super.visitClassSymbol(s, locale);
            }
            else {
                return name;
            }
        }
        @Override
        public String visitMethodSymbol(MethodSymbol s, Locale locale) {
            String ownerName = visit(s.owner, locale);
            if ((s.flags() & BLOCK) != 0) {
               return ownerName;
            } else {
                String ms = (s.name == s.name.table.names.init)
                    ? ownerName
                    : s.name.toString();
                if (s.type != null) {
                    if (s.type.tag == FORALL) {
                        ms = "<" + visitTypes(s.type.getTypeArguments(), locale) + ">" + ms;
                    }
                    ms += "(" + printMethodArgs(
                            s.type.getParameterTypes(),
                            (s.flags() & VARARGS) != 0,
                            locale) + ")";
                }
                return ms;
            }
        }
    };
    protected void preprocessType(Type t) {
        typePreprocessor.visit(t);
    }
    protected Types.UnaryVisitor<Void> typePreprocessor =
            new Types.UnaryVisitor<Void>() {
        public Void visit(List<Type> ts) {
            for (Type t : ts)
                visit(t);
            return null;
        }
        @Override
        public Void visitForAll(ForAll t, Void ignored) {
            visit(t.tvars);
            visit(t.qtype);
            return null;
        }
        @Override
        public Void visitMethodType(MethodType t, Void ignored) {
            visit(t.argtypes);
            visit(t.restype);
            return null;
        }
        @Override
        public Void visitErrorType(ErrorType t, Void ignored) {
            Type ot = t.getOriginalType();
            if (ot != null)
                visit(ot);
            return null;
        }
        @Override
        public Void visitArrayType(ArrayType t, Void ignored) {
            visit(t.elemtype);
            return null;
        }
        @Override
        public Void visitWildcardType(WildcardType t, Void ignored) {
            visit(t.type);
            return null;
        }
        public Void visitType(Type t, Void ignored) {
            return null;
        }
        @Override
        public Void visitCapturedType(CapturedType t, Void ignored) {
            if (indexOf(t, WhereClauseKind.CAPTURED) == -1) {
                String suffix = t.lower == syms.botType ? ".1" : "";
                JCDiagnostic d = diags.fragment("where.captured"+ suffix, t, t.bound, t.lower, t.wildcard);
                whereClauses.get(WhereClauseKind.CAPTURED).put(t, d);
                visit(t.wildcard);
                visit(t.lower);
                visit(t.bound);
            }
            return null;
        }
        @Override
        public Void visitClassType(ClassType t, Void ignored) {
            if (t.isCompound()) {
                if (indexOf(t, WhereClauseKind.INTERSECTION) == -1) {
                    Type supertype = types.supertype(t);
                    List<Type> interfaces = types.interfaces(t);
                    JCDiagnostic d = diags.fragment("where.intersection", t, interfaces.prepend(supertype));
                    whereClauses.get(WhereClauseKind.INTERSECTION).put(t, d);
                    visit(supertype);
                    visit(interfaces);
                }
            }
            nameSimplifier.addUsage(t.tsym);
            visit(t.getTypeArguments());
            if (t.getEnclosingType() != Type.noType)
                visit(t.getEnclosingType());
            return null;
        }
        @Override
        public Void visitTypeVar(TypeVar t, Void ignored) {
            if (indexOf(t, WhereClauseKind.TYPEVAR) == -1) {
                Type bound = t.bound;
                while ((bound instanceof ErrorType))
                    bound = ((ErrorType)bound).getOriginalType();
                List<Type> bounds = bound != null ?
                    types.getBounds(t) :
                    List.<Type>nil();
                nameSimplifier.addUsage(t.tsym);
                boolean boundErroneous = bounds.head == null ||
                                         bounds.head.tag == NONE ||
                                         bounds.head.tag == ERROR;
                JCDiagnostic d = diags.fragment("where.typevar" +
                        (boundErroneous ? ".1" : ""), t, bounds,
                        Kinds.kindName(t.tsym.location()), t.tsym.location());
                whereClauses.get(WhereClauseKind.TYPEVAR).put(t, d);
                symbolPreprocessor.visit(t.tsym.location(), null);
                visit(bounds);
            }
            return null;
        }
    };
    protected void preprocessSymbol(Symbol s) {
        symbolPreprocessor.visit(s, null);
    }
    protected Types.DefaultSymbolVisitor<Void, Void> symbolPreprocessor =
            new Types.DefaultSymbolVisitor<Void, Void>() {
        @Override
        public Void visitClassSymbol(ClassSymbol s, Void ignored) {
            nameSimplifier.addUsage(s);
            return null;
        }
        @Override
        public Void visitSymbol(Symbol s, Void ignored) {
            return null;
        }
        @Override
        public Void visitMethodSymbol(MethodSymbol s, Void ignored) {
            visit(s.owner, null);
            if (s.type != null)
                typePreprocessor.visit(s.type);
            return null;
        }
    };
    @Override
    public RichConfiguration getConfiguration() {
        return (RichConfiguration)configuration;
    }
    public static class RichConfiguration extends ForwardingDiagnosticFormatter.ForwardingConfiguration {
        protected java.util.EnumSet<RichFormatterFeature> features;
        @SuppressWarnings("fallthrough")
        public RichConfiguration(Options options, AbstractDiagnosticFormatter formatter) {
            super(formatter.getConfiguration());
            features = formatter.isRaw() ? EnumSet.noneOf(RichFormatterFeature.class) :
                EnumSet.of(RichFormatterFeature.SIMPLE_NAMES,
                    RichFormatterFeature.WHERE_CLAUSES,
                    RichFormatterFeature.UNIQUE_TYPEVAR_NAMES);
            String diagOpts = options.get("diags");
            if (diagOpts != null) {
                for (String args: diagOpts.split(",")) {
                    if (args.equals("-where")) {
                        features.remove(RichFormatterFeature.WHERE_CLAUSES);
                    }
                    else if (args.equals("where")) {
                        features.add(RichFormatterFeature.WHERE_CLAUSES);
                    }
                    if (args.equals("-simpleNames")) {
                        features.remove(RichFormatterFeature.SIMPLE_NAMES);
                    }
                    else if (args.equals("simpleNames")) {
                        features.add(RichFormatterFeature.SIMPLE_NAMES);
                    }
                    if (args.equals("-disambiguateTvars")) {
                        features.remove(RichFormatterFeature.UNIQUE_TYPEVAR_NAMES);
                    }
                    else if (args.equals("disambiguateTvars")) {
                        features.add(RichFormatterFeature.UNIQUE_TYPEVAR_NAMES);
                    }
                }
            }
        }
        public RichFormatterFeature[] getAvailableFeatures() {
            return RichFormatterFeature.values();
        }
        public void enable(RichFormatterFeature feature) {
            features.add(feature);
        }
        public void disable(RichFormatterFeature feature) {
            features.remove(feature);
        }
        public boolean isEnabled(RichFormatterFeature feature) {
            return features.contains(feature);
        }
        public enum RichFormatterFeature {
            WHERE_CLAUSES,
            SIMPLE_NAMES,
            UNIQUE_TYPEVAR_NAMES;
        }
    }
}
