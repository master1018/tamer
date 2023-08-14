public class TestXEmbedServerJava extends TestXEmbedServer {
    public static void main(String[] args) {
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            return;
        }
        System.setProperty("sun.awt.xembedserver", "true");
        String instruction =
            "This is a manual test for XEmbed server functionality. \n" +
            "You may start XEmbed client by pressing 'Add client' button.\n" +
            "Check that focus transfer with mouse works, that focus traversal with Tab/Shift-Tab works.\n" +
            "Check that XEmbed server client's growing and shrinking.\n" +
            "Check that Drag&Drop works in all combinations.\n" +
            "Check the keyboard input works in both text fields.\n";
        Frame f = new Frame("Instructions");
        f.setLayout(new BorderLayout());
        f.add(new TextArea(instruction), BorderLayout.CENTER);
        f.pack();
        f.setLocation(0, 400);
        f.setVisible(true);
        TestXEmbedServerJava lock = new TestXEmbedServerJava();
        try {
            synchronized(lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
        }
        if (!lock.isPassed()) {
            throw new RuntimeException("Test failed");
        }
    }
    public TestXEmbedServerJava() {
        super(true);
    }
    public Process startClient(Rectangle[] bounds, long window) {
        try {
            String java_home = System.getProperty("java.home");
            return Runtime.getRuntime().exec(java_home + "/bin/java JavaClient " + window);
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
        return null;
    }
}
