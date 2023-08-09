public class MandatoryWarningHandler {
    private enum DeferredDiagnosticKind {
        IN_FILE(".filename"),
        ADDITIONAL_IN_FILE(".filename.additional"),
        IN_FILES(".plural"),
        ADDITIONAL_IN_FILES(".plural.additional");
        DeferredDiagnosticKind(String v) { value = v; }
        String getKey(String prefix) { return prefix + value; }
        private String value;
    }
    public MandatoryWarningHandler(Log log, boolean verbose,
                                   boolean enforceMandatory, String prefix,
                                   LintCategory lc) {
        this.log = log;
        this.verbose = verbose;
        this.prefix = prefix;
        this.enforceMandatory = enforceMandatory;
        this.lintCategory = lc;
    }
    public void report(DiagnosticPosition pos, String msg, Object... args) {
        JavaFileObject currentSource = log.currentSourceFile();
        if (verbose) {
            if (sourcesWithReportedWarnings == null)
                sourcesWithReportedWarnings = new HashSet<JavaFileObject>();
            if (log.nwarnings < log.MaxWarnings) {
                logMandatoryWarning(pos, msg, args);
                sourcesWithReportedWarnings.add(currentSource);
            } else if (deferredDiagnosticKind == null) {
                if (sourcesWithReportedWarnings.contains(currentSource)) {
                    deferredDiagnosticKind = DeferredDiagnosticKind.ADDITIONAL_IN_FILE;
                } else {
                    deferredDiagnosticKind = DeferredDiagnosticKind.IN_FILE;
                }
                deferredDiagnosticSource = currentSource;
                deferredDiagnosticArg = currentSource;
            } else if ((deferredDiagnosticKind == DeferredDiagnosticKind.IN_FILE
                        || deferredDiagnosticKind == DeferredDiagnosticKind.ADDITIONAL_IN_FILE)
                       && !equal(deferredDiagnosticSource, currentSource)) {
                deferredDiagnosticKind = DeferredDiagnosticKind.ADDITIONAL_IN_FILES;
                deferredDiagnosticArg = null;
            }
        } else {
            if (deferredDiagnosticKind == null) {
                deferredDiagnosticKind = DeferredDiagnosticKind.IN_FILE;
                deferredDiagnosticSource = currentSource;
                deferredDiagnosticArg = currentSource;
            }  else if (deferredDiagnosticKind == DeferredDiagnosticKind.IN_FILE &&
                        !equal(deferredDiagnosticSource, currentSource)) {
                deferredDiagnosticKind = DeferredDiagnosticKind.IN_FILES;
                deferredDiagnosticArg = null;
            }
        }
    }
    public void reportDeferredDiagnostic() {
        if (deferredDiagnosticKind != null) {
            if (deferredDiagnosticArg == null)
                logMandatoryNote(deferredDiagnosticSource, deferredDiagnosticKind.getKey(prefix));
            else
                logMandatoryNote(deferredDiagnosticSource, deferredDiagnosticKind.getKey(prefix), deferredDiagnosticArg);
            if (!verbose)
                logMandatoryNote(deferredDiagnosticSource, prefix + ".recompile");
        }
    }
    private static boolean equal(Object o1, Object o2) {
        return ((o1 == null || o2 == null) ? (o1 == o2) : o1.equals(o2));
    }
    private Log log;
    private boolean verbose;
    private String prefix;
    private Set<JavaFileObject> sourcesWithReportedWarnings;
    private DeferredDiagnosticKind deferredDiagnosticKind;
    private JavaFileObject deferredDiagnosticSource;
    private Object deferredDiagnosticArg;
    private final boolean enforceMandatory;
    private final LintCategory lintCategory;
    private void logMandatoryWarning(DiagnosticPosition pos, String msg,
                                     Object... args) {
        if (enforceMandatory)
            log.mandatoryWarning(lintCategory, pos, msg, args);
        else
            log.warning(lintCategory, pos, msg, args);
    }
    private void logMandatoryNote(JavaFileObject file, String msg, Object... args) {
        if (enforceMandatory)
            log.mandatoryNote(file, msg, args);
        else
            log.note(file, msg, args);
    }
}
