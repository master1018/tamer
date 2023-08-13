public final class java_awt_Cursor extends AbstractTest<Cursor> {
    public static void main(String[] args) {
        new java_awt_Cursor().test(true);
    }
    protected Cursor getObject() {
        return new Cursor(Cursor.MOVE_CURSOR);
    }
    protected Cursor getAnotherObject() {
        return null; 
    }
}
