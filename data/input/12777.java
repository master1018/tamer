public final class java_awt_ScrollPane extends AbstractTest<ScrollPane> {
    public static void main(String[] args) {
        new java_awt_ScrollPane().test(true);
    }
    protected ScrollPane getObject() {
        return new ScrollPane(ScrollPane.SCROLLBARS_NEVER);
    }
    protected ScrollPane getAnotherObject() {
        return null; 
    }
}
