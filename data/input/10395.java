public class bug6990651 {
    private static volatile JEditorPane editor;
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                editor = new JEditorPane("text/html", "Hello world!");
            }
        });
        Thread thread = new Thread(new ThreadGroup("Some ThreadGroup"), new Runnable() {
            public void run() {
                SunToolkit.createNewAppContext();
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            editor.setText("Hello world!");
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        thread.join();
    }
}
