public class Test4711996 {
    public static void main(String[] args) {
        JColorChooser chooser = new JColorChooser();
        AbstractColorChooserPanel[] panels = chooser.getChooserPanels();
        chooser.removeChooserPanel(panels[0]);
        chooser.updateUI();
    }
}
