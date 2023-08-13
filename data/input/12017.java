public class Bark extends Log {
    protected static final Context.Key<Bark> barkKey =
        new Context.Key<Bark>();
    public static void preRegister(Context context) {
        context.put(barkKey, new Context.Factory<Bark>() {
            public Bark make(Context c) {
                return new Bark(c);
            }
        });
        context.put(Log.logKey, new Context.Factory<Log>() {
            public Log make(Context c) {
                return Bark.instance(c);
            }
        });
    }
    public static Bark instance(Context context) {
        Bark instance = context.get(barkKey);
        if (instance == null)
            instance = new Bark(context);
        return instance;
    }
    private boolean ignoreDiagnostics;
    private JCDiagnostic.Factory aptDiags;
    protected Bark(Context context) {
        super(context); 
        context.put(barkKey, this);
        JavacMessages aptMessages = JavacMessages.instance(context);
        aptMessages.add("com.sun.tools.apt.resources.apt");
        aptDiags = new JCDiagnostic.Factory(aptMessages, "apt");
        multipleErrors = true;
    }
    public boolean setDiagnosticsIgnored(boolean b) {
        boolean prev = ignoreDiagnostics;
        ignoreDiagnostics = b;
        return prev;
    }
    @Override
    public void report(JCDiagnostic diagnostic) {
        if (ignoreDiagnostics)
            return;
        super.report(diagnostic);
    }
    public void aptError(String key, Object... args) {
        aptError(Position.NOPOS, key, args);
    }
    public void aptError(int pos, String key, Object ... args) {
        report(aptDiags.error(source, new SimpleDiagnosticPosition(pos), key, args));
    }
    public void aptWarning(String key, Object... args) {
        aptWarning(Position.NOPOS, key, args);
    }
    public void aptWarning(int pos, String key, Object ... args) {
        report(aptDiags.warning(source, new SimpleDiagnosticPosition(pos), key, args));
    }
    public void aptNote(String key, Object... args) {
        aptNote(Position.NOPOS, key, args);
    }
    public void aptNote(int pos, String key, Object ... args) {
        report(aptDiags.note(source, new SimpleDiagnosticPosition(pos), key, args));
    }
}
