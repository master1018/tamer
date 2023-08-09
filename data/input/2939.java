public class Test4234761 implements PropertyChangeListener {
    private static final Color COLOR = new Color(51, 51, 51);
    public static void main(String[] args) {
        JColorChooser chooser = new JColorChooser(COLOR);
        JDialog dialog = Test4177735.show(chooser);
        PropertyChangeListener listener = new Test4234761();
        chooser.addPropertyChangeListener("color", listener); 
        JTabbedPane tabbedPane = (JTabbedPane) chooser.getComponent(0);
        tabbedPane.setSelectedIndex(1); 
        if (!chooser.getColor().equals(COLOR)) {
            listener.propertyChange(null);
        }
        dialog.dispose();
    }
    public void propertyChange(PropertyChangeEvent event) {
        throw new Error("RGB value is changed after transition to HSB tab");
    }
}
