public class Test4759934 extends JApplet implements ActionListener {
    private static final String CMD_DIALOG = "Show Dialog"; 
    private static final String CMD_CHOOSER = "Show ColorChooser"; 
    private final JFrame frame = new JFrame("Test"); 
    public void init() {
        show(this.frame, CMD_DIALOG);
    }
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (CMD_DIALOG.equals(command)) {
            JDialog dialog = new JDialog(this.frame, "Dialog"); 
            dialog.setLocation(200, 0);
            show(dialog, CMD_CHOOSER);
        }
        else if (CMD_CHOOSER.equals(command)) {
            Object source = event.getSource();
            Component component = (source instanceof Component)
                    ? (Component) source
                    : null;
            JColorChooser.showDialog(component, "ColorChooser", Color.BLUE); 
        }
    }
    private void show(Window window, String command) {
        JButton button = new JButton(command);
        button.setActionCommand(command);
        button.addActionListener(this);
        button.setFont(button.getFont().deriveFont(64.0f));
        window.add(button);
        window.pack();
        window.setVisible(true);
    }
}
