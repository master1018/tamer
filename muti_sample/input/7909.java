public class Lint
{
    protected static final Context.Key<Lint> lintKey = new Context.Key<Lint>();
    public static Lint instance(Context context) {
        Lint instance = context.get(lintKey);
        if (instance == null)
            instance = new Lint(context);
        return instance;
    }
    public Lint augment(Attribute.Compound attr) {
        return augmentor.augment(this, attr);
    }
    public Lint augment(List<Attribute.Compound> attrs) {
        return augmentor.augment(this, attrs);
    }
    public Lint augment(List<Attribute.Compound> attrs, long flags) {
        Lint l = augmentor.augment(this, attrs);
        if ((flags & DEPRECATED) != 0) {
            if (l == this)
                l = new Lint(this);
            l.values.remove(LintCategory.DEPRECATION);
            l.suppressedValues.add(LintCategory.DEPRECATION);
        }
        return l;
    }
    private final AugmentVisitor augmentor;
    private final EnumSet<LintCategory> values;
    private final EnumSet<LintCategory> suppressedValues;
    private static Map<String, LintCategory> map = new HashMap<String,LintCategory>();
    protected Lint(Context context) {
        Options options = Options.instance(context);
        values = EnumSet.noneOf(LintCategory.class);
        for (Map.Entry<String, LintCategory> e: map.entrySet()) {
            if (options.lint(e.getKey()))
                values.add(e.getValue());
        }
        suppressedValues = EnumSet.noneOf(LintCategory.class);
        context.put(lintKey, this);
        augmentor = new AugmentVisitor(context);
    }
    protected Lint(Lint other) {
        this.augmentor = other.augmentor;
        this.values = other.values.clone();
        this.suppressedValues = other.suppressedValues.clone();
    }
    @Override
    public String toString() {
        return "Lint:[values" + values + " suppressedValues" + suppressedValues + "]";
    }
    public enum LintCategory {
        CAST("cast"),
        CLASSFILE("classfile"),
        DEPRECATION("deprecation"),
        DEP_ANN("dep-ann"),
        DIVZERO("divzero"),
        EMPTY("empty"),
        FALLTHROUGH("fallthrough"),
        FINALLY("finally"),
        OPTIONS("options"),
        OVERRIDES("overrides"),
        PATH("path"),
        PROCESSING("processing"),
        RAW("rawtypes"),
        SERIAL("serial"),
        STATIC("static"),
        SUNAPI("sunapi", true),
        TRY("try"),
        UNCHECKED("unchecked"),
        VARARGS("varargs");
        LintCategory(String option) {
            this(option, false);
        }
        LintCategory(String option, boolean hidden) {
            this.option = option;
            this.hidden = hidden;
            map.put(option, this);
        }
        static LintCategory get(String option) {
            return map.get(option);
        }
        public final String option;
        public final boolean hidden;
    };
    public boolean isEnabled(LintCategory lc) {
        return values.contains(lc);
    }
    public boolean isSuppressed(LintCategory lc) {
        return suppressedValues.contains(lc);
    }
    protected static class AugmentVisitor implements Attribute.Visitor {
        private final Context context;
        private Symtab syms;
        private Lint parent;
        private Lint lint;
        AugmentVisitor(Context context) {
            this.context = context;
        }
        Lint augment(Lint parent, Attribute.Compound attr) {
            initSyms();
            this.parent = parent;
            lint = null;
            attr.accept(this);
            return (lint == null ? parent : lint);
        }
        Lint augment(Lint parent, List<Attribute.Compound> attrs) {
            initSyms();
            this.parent = parent;
            lint = null;
            for (Attribute.Compound a: attrs) {
                a.accept(this);
            }
            return (lint == null ? parent : lint);
        }
        private void initSyms() {
            if (syms == null)
                syms = Symtab.instance(context);
        }
        private void suppress(LintCategory lc) {
            if (lint == null)
                lint = new Lint(parent);
            lint.suppressedValues.add(lc);
            lint.values.remove(lc);
        }
        public void visitConstant(Attribute.Constant value) {
            if (value.type.tsym == syms.stringType.tsym) {
                LintCategory lc = LintCategory.get((String) (value.value));
                if (lc != null)
                    suppress(lc);
            }
        }
        public void visitClass(Attribute.Class clazz) {
        }
        public void visitCompound(Attribute.Compound compound) {
            if (compound.type.tsym == syms.suppressWarningsType.tsym) {
                for (List<Pair<MethodSymbol,Attribute>> v = compound.values;
                     v.nonEmpty(); v = v.tail) {
                    Pair<MethodSymbol,Attribute> value = v.head;
                    if (value.fst.name.toString().equals("value"))
                        value.snd.accept(this);
                }
            }
        }
        public void visitArray(Attribute.Array array) {
            for (Attribute value : array.values)
                value.accept(this);
        }
        public void visitEnum(Attribute.Enum e) {
        }
        public void visitError(Attribute.Error e) {
        }
    };
}
