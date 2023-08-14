public class HashPrintJobAttributeSet extends HashAttributeSet
    implements PrintJobAttributeSet, Serializable {
    private static final long serialVersionUID = -4204473656070350348L;
    public HashPrintJobAttributeSet() {
        super(PrintJobAttribute.class);
    }
    public HashPrintJobAttributeSet(PrintJobAttribute attribute) {
        super(attribute, PrintJobAttribute.class);
    }
    public HashPrintJobAttributeSet(PrintJobAttribute[] attributes) {
        super (attributes, PrintJobAttribute.class);
    }
    public HashPrintJobAttributeSet(PrintJobAttributeSet attributes) {
        super(attributes, PrintJobAttribute.class);
    }
}
