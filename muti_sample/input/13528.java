public class Test6199676 implements Runnable {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Test6199676());
    }
    private static void exit(String error) {
        if (error != null) {
            System.err.println(error);
            System.exit(1);
        }
        else {
            System.exit(0);
        }
    }
    private static Component getPreview(Container container) {
        String name = "ColorChooser.previewPanelHolder";
        for (Component component : container.getComponents()) {
            if (!name.equals(component.getName())) {
                component = (component instanceof Container)
                        ? getPreview((Container) component)
                        : null;
            }
            if (component instanceof Container) {
                container = (Container) component;
                return 1 == container.getComponentCount()
                        ? container.getComponent(0)
                        : null;
            }
        }
        return null;
    }
    private static boolean isShowing(Component component) {
        return (component != null) && component.isShowing();
    }
    private int index;
    private boolean updated;
    private JColorChooser chooser;
    public synchronized void run() {
        if (this.chooser == null) {
            this.chooser = new JColorChooser();
            JFrame frame = new JFrame(getClass().getName());
            frame.add(this.chooser);
            frame.setVisible(true);
        }
        else if (this.updated) {
            if (isShowing(this.chooser.getPreviewPanel())) {
                exit("custom preview panel is showing");
            }
            exit(null);
        }
        else {
            Component component = this.chooser.getPreviewPanel();
            if (component == null) {
                component = getPreview(this.chooser);
            }
            if (!isShowing(component)) {
                exit("default preview panel is not showing");
            }
            this.updated = true;
            this.chooser.setPreviewPanel(new JPanel());
        }
        LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
        LookAndFeelInfo info = infos[++this.index % infos.length];
        try {
            UIManager.setLookAndFeel(info.getClassName());
        }
        catch (Exception exception) {
            exit("could not change L&F");
        }
        SwingUtilities.updateComponentTreeUI(this.chooser);
        SwingUtilities.invokeLater(this);
    }
}
