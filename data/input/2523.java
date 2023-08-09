public final class Test4222508 extends JApplet implements ItemListener {
    private JCheckBox checkbox;
    private JColorChooser chooser;
    @Override
    public void init() {
        this.chooser = new JColorChooser();
        this.checkbox = new JCheckBox("Enable the color chooser below", true);
        this.checkbox.addItemListener(this);
        add(BorderLayout.NORTH, this.checkbox);
        add(BorderLayout.CENTER, this.chooser);
    }
    public void itemStateChanged(ItemEvent event) {
        this.chooser.setEnabled(this.checkbox.isSelected());
    }
}
