public class SwingLazyValueTest {
    public static void main(String[] args) throws Exception {
        if(new SwingLazyValue("javax.swing.JTable$DoubleRenderer").
                createValue(null) == null) {
            throw new RuntimeException("SwingLazyValue doesn't work");
        }
    }
}
