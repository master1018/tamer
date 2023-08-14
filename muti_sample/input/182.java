public class PredefinedPrivate {
    public static void main(String args[]) {
        new MyCursor();
        if (Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR) instanceof MyCursor) {
            throw new RuntimeException("Test FAILED: getPredefinedCursor() returned modified cursor");
        }
    }
}
class MyCursor extends Cursor {
    public MyCursor() {
        super(DEFAULT_CURSOR);
        Cursor.predefined[DEFAULT_CURSOR] = this;
    }
}
