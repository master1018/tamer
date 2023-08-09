public final class DialogOwner
    implements PrintRequestAttribute {
    private Frame dlgOwner;
    public DialogOwner(Frame frame) {
        dlgOwner = frame;
    }
    public Frame getOwner() {
        return dlgOwner;
    }
    public final Class getCategory() {
        return DialogOwner.class;
    }
    public final String getName() {
        return "dialog-owner";
    }
}
