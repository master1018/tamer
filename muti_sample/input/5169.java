public class bug6489130 {
    private final JFileChooser chooser = new JFileChooser();
    private static final CountDownLatch MUX = new CountDownLatch(1);
    private final Timer timer = new Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            switch (state) {
                case 0:
                case 1: {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            chooser.showOpenDialog(null);
                        }
                    });
                    break;
                }
                case 2:
                case 3: {
                    Window[] windows = Frame.getWindows();
                    if (windows.length > 0) {
                        windows[0].dispose();
                    }
                    break;
                }
                case 4: {
                    MUX.countDown();
                    break;
                }
            }
            state++;
        }
    });
    private int state = 0;
    public static void main(String[] args) throws InterruptedException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new bug6489130().run();
            }
        });
        if (!MUX.await(10, TimeUnit.SECONDS)) {
            throw new RuntimeException("Timeout");
        }
    }
    private void run() {
        timer.start();
    }
}
