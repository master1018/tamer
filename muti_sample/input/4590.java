public class ForwardingDiagnosticFormatter<D extends Diagnostic<?>, F extends DiagnosticFormatter<D>>
        implements DiagnosticFormatter<D> {
    protected F formatter;
    protected ForwardingConfiguration configuration;
    public ForwardingDiagnosticFormatter(F formatter) {
        this.formatter = formatter;
        this.configuration = new ForwardingConfiguration(formatter.getConfiguration());
    }
    public F getDelegatedFormatter() {
        return formatter;
    }
    public Configuration getConfiguration() {
        return configuration;
    }
    public boolean displaySource(D diag) {
        return formatter.displaySource(diag);
    }
    public String format(D diag, Locale l) {
        return formatter.format(diag, l);
    }
    public String formatKind(D diag, Locale l) {
        return formatter.formatKind(diag, l);
    }
    public String formatMessage(D diag, Locale l) {
        return formatter.formatMessage(diag, l);
    }
    public String formatPosition(D diag, PositionKind pk, Locale l) {
        return formatter.formatPosition(diag, pk, l);
    }
    public String formatSource(D diag, boolean fullname, Locale l) {
        return formatter.formatSource(diag, fullname, l);
    }
    public static class ForwardingConfiguration implements DiagnosticFormatter.Configuration {
        protected Configuration configuration;
        public ForwardingConfiguration(Configuration configuration) {
            this.configuration = configuration;
        }
        public Configuration getDelegatedConfiguration() {
            return configuration;
        }
        public int getMultilineLimit(MultilineLimit limit) {
            return configuration.getMultilineLimit(limit);
        }
        public Set<DiagnosticPart> getVisible() {
            return configuration.getVisible();
        }
        public void setMultilineLimit(MultilineLimit limit, int value) {
            configuration.setMultilineLimit(limit, value);
        }
        public void setVisible(Set<DiagnosticPart> diagParts) {
            configuration.setVisible(diagParts);
        }
    }
}
