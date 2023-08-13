public class UISwitchListener implements PropertyChangeListener {
    JComponent componentToSwitch;
    public UISwitchListener(JComponent c) {
        componentToSwitch = c;
    }
    public void propertyChange(PropertyChangeEvent e) {
        String name = e.getPropertyName();
        if (name.equals("lookAndFeel")) {
            SwingUtilities.updateComponentTreeUI(componentToSwitch);
            componentToSwitch.invalidate();
            componentToSwitch.validate();
            componentToSwitch.repaint();
        }
    }
}
