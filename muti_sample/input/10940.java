public class HashPrintServiceAttributeSet extends HashAttributeSet
    implements PrintServiceAttributeSet, Serializable {
    private static final long serialVersionUID = 6642904616179203070L;
    public HashPrintServiceAttributeSet() {
        super (PrintServiceAttribute.class);
    }
    public HashPrintServiceAttributeSet(PrintServiceAttribute attribute) {
        super (attribute, PrintServiceAttribute.class);
    }
    public HashPrintServiceAttributeSet(PrintServiceAttribute[] attributes) {
        super (attributes, PrintServiceAttribute.class);
    }
    public HashPrintServiceAttributeSet(PrintServiceAttributeSet attributes)
    {
        super(attributes, PrintServiceAttribute.class);
    }
}
