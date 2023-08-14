public class Test6559154 implements ActionListener, Runnable {
    private JDialog dialog;
    public void actionPerformed(ActionEvent event) {
        if (this.dialog != null) {
            this.dialog.dispose();
        }
    }
    public void run() {
        Timer timer = new Timer(1000, this);
        timer.setRepeats(false);
        timer.start();
        JColorChooser chooser = new JColorChooser();
        setEnabledRecursive(chooser, false);
        this.dialog = new JDialog();
        this.dialog.add(chooser);
        this.dialog.setVisible(true);
    }
    private static void setEnabledRecursive(Container container, boolean enabled) {
        for (Component component : container.getComponents()) {
            component.setEnabled(enabled);
            if (component instanceof Container) {
                setEnabledRecursive((Container) component, enabled);
            }
        }
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Test6559154());
    }
}
