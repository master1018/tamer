public class bug7004134 {
    private static volatile JFrame frame;
    private static volatile JLabel label;
    private static volatile int toolTipWidth;
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                label = new JLabel("A JLabel used as object for an HTML-formatted tooltip");
                label.setToolTipText("<html><body bgcolor=FFFFE1>An HTML-formatted ToolTip</body></html>");
                frame = new JFrame();
                frame.add(label);
                frame.pack();
                frame.setVisible(true);
            }
        });
        ((SunToolkit) SunToolkit.getDefaultToolkit()).realSync();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
                toolTipManager.setInitialDelay(0);
                toolTipManager.mouseMoved(new MouseEvent(label, 0, 0, 0, 0, 0, 0, false));
            }
        });
        Thread.sleep(500);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                toolTipWidth = getTipWindow().getWidth();
                frame.dispose();
            }
        });
        Thread thread = new Thread(new ThreadGroup("Some ThreadGroup"), new Runnable() {
            public void run() {
                SunToolkit.createNewAppContext();
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            frame = new JFrame();
                            frame.add(label);
                            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            frame.pack();
                            frame.setVisible(true);
                        }
                    });
                    ((SunToolkit) SunToolkit.getDefaultToolkit()).realSync();
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
                            toolTipManager.setInitialDelay(0);
                            toolTipManager.mouseMoved(new MouseEvent(label, 0, 0, 0, 0, 0, 0, false));
                        }
                    });
                    Thread.sleep(500);
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            int newToolTipWidth = getTipWindow().getWidth();
                            frame.dispose();
                            if (toolTipWidth != newToolTipWidth) {
                                throw new RuntimeException("Tooltip width is different. Initial: " + toolTipWidth +
                                        ", new: " + newToolTipWidth);
                            }
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
    private static Component getTipWindow() {
        try {
            Field tipWindowField = ToolTipManager.class.getDeclaredField("tipWindow");
            tipWindowField.setAccessible(true);
            Popup value = (Popup) tipWindowField.get(ToolTipManager.sharedInstance());
            Field componentField = Popup.class.getDeclaredField("component");
            componentField.setAccessible(true);
            return (Component) componentField.get(value);
        } catch (Exception e) {
            throw new RuntimeException("getToolTipComponent failed", e);
        }
    }
}
