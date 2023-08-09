public class Test6660049 implements Runnable {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Test6660049(
                javax.swing.JButton.class,
                javax.swing.JCheckBox.class,
                javax.swing.JCheckBoxMenuItem.class,
                javax.swing.JColorChooser.class,
                javax.swing.JComboBox.class,
                javax.swing.JDesktopPane.class,
                javax.swing.JEditorPane.class,
                javax.swing.JFileChooser.class,
                javax.swing.JFormattedTextField.class,
                javax.swing.JInternalFrame.class,
                javax.swing.JLabel.class,
                javax.swing.JList.class,
                javax.swing.JMenu.class,
                javax.swing.JMenuBar.class,
                javax.swing.JMenuItem.class,
                javax.swing.JOptionPane.class,
                javax.swing.JPanel.class,
                javax.swing.JPasswordField.class,
                javax.swing.JPopupMenu.class,
                javax.swing.JProgressBar.class,
                javax.swing.JRadioButton.class,
                javax.swing.JRadioButtonMenuItem.class,
                javax.swing.JRootPane.class,
                javax.swing.JScrollBar.class,
                javax.swing.JScrollPane.class,
                javax.swing.JSeparator.class,
                javax.swing.JSlider.class,
                javax.swing.JSpinner.class,
                javax.swing.JSplitPane.class,
                javax.swing.JTabbedPane.class,
                javax.swing.JTable.class,
                javax.swing.JTextArea.class,
                javax.swing.JTextField.class,
                javax.swing.JTextPane.class,
                javax.swing.JToggleButton.class,
                javax.swing.JToolBar.class,
                javax.swing.JToolTip.class,
                javax.swing.JTree.class,
                javax.swing.JViewport.class,
                javax.swing.table.JTableHeader.class));
    }
    private final Class<? extends JComponent>[] types;
    private final Region region;
    private Test6660049(Class<? extends JComponent>... types) {
        this.types = types;
        run();
        this.region = new Region("Button", "ButtonUI", true) {
            @Override
            public String getName() {
                throw new Error("6660049: exploit is available");
            }
        };
    }
    public void run() {
        if (this.region != null) {
            SunToolkit.createNewAppContext();
        }
        for (Class<? extends JComponent> type : this.types) {
            Region region = getRegion(type);
            if (region == null) {
                throw new Error("6849518: region is not initialized");
            }
        }
        getRegion(JButton.class).getName();
    }
    private static Region getRegion(Class<? extends JComponent> type) {
        try {
            return SynthLookAndFeel.getRegion(type.newInstance());
        }
        catch (IllegalAccessException exception) {
            throw new Error("unexpected exception", exception);
        }
        catch (InstantiationException exception) {
            throw new Error("unexpected exception", exception);
        }
    }
}
