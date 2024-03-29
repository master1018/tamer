public class OptionOutputFormatter implements OutputFormatter {
    private OptionFormat format;
    private String header;
    private MonitoredVm vm;
    public OptionOutputFormatter(MonitoredVm vm, OptionFormat format)
           throws MonitorException {
        this.vm = vm;
        this.format = format;
        resolve();
    }
    private void resolve() throws MonitorException {
        ExpressionEvaluator ee = new ExpressionResolver(vm);
        SymbolResolutionClosure ec = new SymbolResolutionClosure(ee);
        format.apply(ec);
    }
    public String getHeader() throws MonitorException {
        if (header == null) {
            HeaderClosure hc = new HeaderClosure();
            format.apply(hc);
            header = hc.getHeader();
        }
        return header;
    }
    public String getRow() throws MonitorException {
        RowClosure rc = new RowClosure(vm);
        format.apply(rc);
        return rc.getRow();
    }
}
