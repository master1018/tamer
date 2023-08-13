public final class PrinterMakeAndModel extends TextSyntax
        implements PrintServiceAttribute {
    private static final long serialVersionUID = 4580461489499351411L;
    public PrinterMakeAndModel(String makeAndModel, Locale locale) {
        super (makeAndModel, locale);
    }
    public boolean equals(Object object) {
        return (super.equals(object) &&
                object instanceof PrinterMakeAndModel);
    }
    public final Class<? extends Attribute> getCategory() {
        return PrinterMakeAndModel.class;
    }
    public final String getName() {
        return "printer-make-and-model";
    }
}
