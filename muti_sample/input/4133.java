public class PrintJobAttributeEvent extends PrintEvent {
    private static final long serialVersionUID = -6534469883874742101L;
    private PrintJobAttributeSet attributes;
    public PrintJobAttributeEvent (DocPrintJob source,
                                   PrintJobAttributeSet attributes)  {
        super(source);
        this.attributes = AttributeSetUtilities.unmodifiableView(attributes);
    }
    public DocPrintJob getPrintJob() {
        return (DocPrintJob) getSource();
    }
    public PrintJobAttributeSet getAttributes() {
        return attributes;
    }
}
