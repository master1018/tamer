public class Test4247606 extends JApplet {
    public void init() {
        JButton button = new JButton("Button"); 
        button.setBorder(BorderFactory.createLineBorder(Color.red, 1));
        TitledBorder border = new TitledBorder("Bordered Pane"); 
        border.setTitlePosition(TitledBorder.BELOW_BOTTOM);
        JPanel panel = create(button, border);
        panel.setBackground(Color.green);
        getContentPane().add(create(panel, BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    }
    private static JPanel create(JComponent component, Border border) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(border);
        panel.add(component);
        return panel;
    }
}
