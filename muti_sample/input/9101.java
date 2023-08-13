public class Applet4PrintService implements PrintService {
   public Applet4PrintService() {
   }
    public String getName() {
        return "Applet 4 Printer";
    }
    public DocPrintJob createPrintJob() {
        return null;
    }
    public PrintServiceAttributeSet getUpdatedAttributes() {
        return null;
    }
    public void addPrintServiceAttributeListener(
                                 PrintServiceAttributeListener listener) {
          return;
    }
    public void removePrintServiceAttributeListener(
                                  PrintServiceAttributeListener listener) {
        return;
    }
    public PrintServiceAttribute getAttribute(Class category) {
            return null;
    }
    public PrintServiceAttributeSet getAttributes() {
        return null;
    }
    public DocFlavor[] getSupportedDocFlavors() {
        return null;
    }
    public boolean isDocFlavorSupported(DocFlavor flavor) {
        return false;
    }
    public Class[] getSupportedAttributeCategories() {
        return null;
    }
    public boolean isAttributeCategorySupported(Class category) {
        return false;
    }
    public Object getDefaultAttributeValue(Class category) {
        return null;
    }
    public Object getSupportedAttributeValues(Class category,
                                              DocFlavor flavor,
                                              AttributeSet attributes) {
            return null;
    }
    public boolean isAttributeValueSupported(Attribute attr,
                                             DocFlavor flavor,
                                             AttributeSet attributes) {
        return false;
    }
    public AttributeSet getUnsupportedAttributes(DocFlavor flavor,
                                                 AttributeSet attributes) {
            return null;
        }
    public ServiceUIFactory getServiceUIFactory() {
        return null;
    }
    public String toString() {
        return "Printer : " + getName();
    }
    public boolean equals(Object obj) {
        return  (obj == this ||
                 (obj instanceof Applet4PrintService &&
                  ((Applet4PrintService)obj).getName().equals(getName())));
    }
    public int hashCode() {
        return this.getClass().hashCode()+getName().hashCode();
    }
}
