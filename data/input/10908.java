public class bug6924059 {
    private static boolean isMethodCalled;
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new SynthLookAndFeel());
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                new JScrollBar().setUI(new SynthScrollBarUI() {
                    protected void configureScrollBarColors() {
                        super.configureScrollBarColors();
                        isMethodCalled = true;
                    }
                });
                if (!isMethodCalled) {
                    throw new RuntimeException("The configureScrollBarColors was not called");
                }
            }
        });
    }
}
