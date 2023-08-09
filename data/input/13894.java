public class PrivateConstructorRB extends ListResourceBundle {
    private PrivateConstructorRB() {
    }
    public Object[][] getContents() {
        return new Object[][] {
            { "type", "class (private constructor)" }
        };
    }
}
