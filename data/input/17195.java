public class bug6986385 {
    public static void main(String... args) throws Exception {
        JLayer l = new JLayer();
        AccessibleContext acc = l.getAccessibleContext();
        if (acc == null) {
            throw new RuntimeException("JLayer's AccessibleContext is null");
        }
        if (acc.getAccessibleRole() != AccessibleRole.PANEL) {
            throw new RuntimeException("JLayer's AccessibleRole must be PANEL");
        }
    }
}
