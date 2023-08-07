public class Symbol implements java.io.Serializable, jscheme.SchemeSymbol {
    private String name;
    public static final Hashtable symbolTable = new Hashtable(500);
    public static final Symbol BEGIN = intern("begin"), CAR = intern("car"), CDR = intern("cdr"), DEFINE = intern("define"), IF = intern("if"), LAMBDA = intern("lambda"), MACRO = intern("macro"), NEWLINE = intern("newline"), NULL = intern("null"), OR = intern("or"), QUASIQUOTE = intern("quasiquote"), QUOTE = intern("quote"), SET = intern("set!"), SPACE = intern("space"), UNQUOTE = intern("unquote"), UNQUOTE_SPLICING = intern("unquote-splicing"), PACKAGE = intern("package");
    private Symbol(String name) {
        this.name = name;
    }
    private Object readResolve() throws java.io.ObjectStreamException {
        return intern(name);
    }
    public static synchronized Symbol intern(String name) {
        Symbol result = (Symbol) symbolTable.get(name);
        if (result == null) symbolTable.put(name, result = new Symbol(name));
        return result;
    }
    public String toString() {
        return name;
    }
    public Object getGlobalValue() {
        return Scheme.getInteractionEnvironment().getValue(this);
    }
    public synchronized Object setGlobalValue(Object newval) {
        return Scheme.getInteractionEnvironment().setValue(this, newval);
    }
    public boolean isDefined() {
        return Scheme.getInteractionEnvironment().isDefined(this);
    }
}
