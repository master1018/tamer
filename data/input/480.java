public class Test6984643 {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new BasicLookAndFeel() {
            public String getName() {
                return "A name";
            }
            public String getID() {
                return "An id";
            }
            public String getDescription() {
                return "A description";
            }
            public boolean isNativeLookAndFeel() {
                return false;
            }
            public boolean isSupportedLookAndFeel() {
                return true;
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new JFileChooser();
            }
        });
    }
}
