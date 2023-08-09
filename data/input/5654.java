public abstract class AbstractLog {
    AbstractLog(JCDiagnostic.Factory diags) {
        this.diags = diags;
        sourceMap = new HashMap<JavaFileObject, DiagnosticSource>();
    }
    public JavaFileObject useSource(JavaFileObject file) {
        JavaFileObject prev = (source == null ? null : source.getFile());
        source = getSource(file);
        return prev;
    }
    protected DiagnosticSource getSource(JavaFileObject file) {
        if (file == null)
            return DiagnosticSource.NO_SOURCE;
        DiagnosticSource s = sourceMap.get(file);
        if (s == null) {
            s = new DiagnosticSource(file, this);
            sourceMap.put(file, s);
        }
        return s;
    }
    public DiagnosticSource currentSource() {
        return source;
    }
    public void error(String key, Object ... args) {
        report(diags.error(source, null, key, args));
    }
    public void error(DiagnosticPosition pos, String key, Object ... args) {
        report(diags.error(source, pos, key, args));
    }
    public void error(int pos, String key, Object ... args) {
        report(diags.error(source, wrap(pos), key, args));
    }
    public void error(DiagnosticFlag flag, int pos, String key, Object ... args) {
        JCDiagnostic d = diags.error(source, wrap(pos), key, args);
        d.setFlag(flag);
        report(d);
    }
    public void warning(String key, Object ... args) {
        report(diags.warning(source, null, key, args));
    }
    public void warning(LintCategory lc, String key, Object ... args) {
        report(diags.warning(lc, key, args));
    }
    public void warning(DiagnosticPosition pos, String key, Object ... args) {
        report(diags.warning(source, pos, key, args));
    }
    public void warning(LintCategory lc, DiagnosticPosition pos, String key, Object ... args) {
        report(diags.warning(lc, source, pos, key, args));
    }
    public void warning(int pos, String key, Object ... args) {
        report(diags.warning(source, wrap(pos), key, args));
    }
    public void mandatoryWarning(DiagnosticPosition pos, String key, Object ... args) {
        report(diags.mandatoryWarning(source, pos, key, args));
    }
    public void mandatoryWarning(LintCategory lc, DiagnosticPosition pos, String key, Object ... args) {
        report(diags.mandatoryWarning(lc, source, pos, key, args));
    }
    public void note(String key, Object ... args) {
        report(diags.note(source, null, key, args));
    }
    public void note(DiagnosticPosition pos, String key, Object ... args) {
        report(diags.note(source, pos, key, args));
    }
    public void note(int pos, String key, Object ... args) {
        report(diags.note(source, wrap(pos), key, args));
    }
    public void note(JavaFileObject file, String key, Object ... args) {
        report(diags.note(getSource(file), null, key, args));
    }
    public void mandatoryNote(final JavaFileObject file, String key, Object ... args) {
        report(diags.mandatoryNote(getSource(file), key, args));
    }
    protected abstract void report(JCDiagnostic diagnostic);
    protected abstract void directError(String key, Object... args);
    private DiagnosticPosition wrap(int pos) {
        return (pos == Position.NOPOS ? null : new SimpleDiagnosticPosition(pos));
    }
    protected JCDiagnostic.Factory diags;
    protected DiagnosticSource source;
    protected Map<JavaFileObject, DiagnosticSource> sourceMap;
}
