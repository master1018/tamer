public class PrintServiceAttributeEvent extends PrintEvent {
    private static final long serialVersionUID = -7565987018140326600L;
    private PrintServiceAttributeSet attributes;
    public PrintServiceAttributeEvent(PrintService source,
                                      PrintServiceAttributeSet attributes) {
        super(source);
        this.attributes = AttributeSetUtilities.unmodifiableView(attributes);
    }
    public PrintService getPrintService() {
        return (PrintService) getSource();
    }
    public PrintServiceAttributeSet getAttributes() {
        return attributes;
    }
}
