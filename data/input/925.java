public abstract class AbstractPMDProcessor {
    protected final Configuration configuration;
    public AbstractPMDProcessor(Configuration configuration) {
        this.configuration = configuration;
    }
    public void renderReports(final List<Renderer> renderers, final Report report) {
        long start = System.nanoTime();
        try {
            for (Renderer r : renderers) {
                r.renderFileReport(report);
            }
            long end = System.nanoTime();
            Benchmarker.mark(Benchmark.Reporting, end - start, 1);
        } catch (IOException ioe) {
        }
    }
    protected String filenameFrom(DataSource dataSource) {
        return dataSource.getNiceFileName(configuration.isReportShortNames(), configuration.getInputPaths());
    }
    protected RuleSets createRuleSets(RuleSetFactory factory) {
        try {
            return factory.createRuleSets(configuration.getRuleSets());
        } catch (RuleSetNotFoundException rsnfe) {
            return null;
        }
    }
}
