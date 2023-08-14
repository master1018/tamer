public class BadStaticInitRB extends ListResourceBundle {
    static {
        int[] x = new int[1];
        x[100] = 100;
    }
    public Object[][] getContents() {
        return new Object[][] {
            { "type", "class (static initializer error)" }
        };
    }
}
