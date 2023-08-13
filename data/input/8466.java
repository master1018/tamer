public final class PrinterInfo extends TextSyntax
        implements PrintServiceAttribute {
    private static final long serialVersionUID = 7765280618777599727L;
    public PrinterInfo(String info, Locale locale) {
        super (info, locale);
    }
    public boolean equals(Object object) {
        return (super.equals(object) && object instanceof PrinterInfo);
    }
    public final Class<? extends Attribute> getCategory() {
        return PrinterInfo.class;
    }
    public final String getName() {
        return "printer-info";
    }
}
