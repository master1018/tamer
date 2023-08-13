public class Warner {
    public static final Warner noWarnings = new Warner();
    private DiagnosticPosition pos = null;
    protected boolean warned = false;
    private EnumSet<LintCategory> nonSilentLintSet = EnumSet.noneOf(LintCategory.class);
    private EnumSet<LintCategory> silentLintSet = EnumSet.noneOf(LintCategory.class);
    public DiagnosticPosition pos() {
        return pos;
    }
    public void warn(LintCategory lint) {
        nonSilentLintSet.add(lint);
    }
    public void silentWarn(LintCategory lint) {
        silentLintSet.add(lint);
    }
    public Warner(DiagnosticPosition pos) {
        this.pos = pos;
    }
    public boolean hasSilentLint(LintCategory lint) {
        return silentLintSet.contains(lint);
    }
    public boolean hasNonSilentLint(LintCategory lint) {
        return nonSilentLintSet.contains(lint);
    }
    public boolean hasLint(LintCategory lint) {
        return hasSilentLint(lint) ||
                hasNonSilentLint(lint);
    }
    public void clear() {
        nonSilentLintSet.clear();
        silentLintSet.clear();
        this.warned = false;
    }
    public Warner() {
        this(null);
    }
}
