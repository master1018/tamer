class PrintJobFlavorException extends PrintException
    implements FlavorException {
    private DocFlavor flavor;
    PrintJobFlavorException(String s, DocFlavor f) {
        super(s);
        flavor = f;
        }
    public DocFlavor[] getUnsupportedFlavors() {
        DocFlavor [] flavors = { flavor};
            return flavors;
    }
}
