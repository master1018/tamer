public final class DiagnosticCollector<S> implements DiagnosticListener<S> {
    private List<Diagnostic<? extends S>> diagnostics =
        Collections.synchronizedList(new ArrayList<Diagnostic<? extends S>>());
    public void report(Diagnostic<? extends S> diagnostic) {
        diagnostic.getClass(); 
        diagnostics.add(diagnostic);
    }
    public List<Diagnostic<? extends S>> getDiagnostics() {
        return Collections.unmodifiableList(diagnostics);
    }
}
