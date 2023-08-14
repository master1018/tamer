public class Test4903007 extends AbstractTest<JPanel> {
    public static void main(String[] args) throws Exception {
        new Test4903007().test(true);
    }
    protected JPanel getObject() {
        Box vBox = Box.createVerticalBox();
        vBox.add(new JButton("button"));
        vBox.add(Box.createVerticalStrut(10));
        vBox.add(new JLabel("label"));
        vBox.add(Box.createVerticalGlue());
        vBox.add(new JButton("button"));
        vBox.add(Box.createVerticalStrut(10));
        vBox.add(new JLabel("label"));
        Box hBox = Box.createHorizontalBox();
        hBox.add(new JButton("button"));
        hBox.add(Box.createHorizontalStrut(10));
        hBox.add(new JLabel("label"));
        hBox.add(Box.createHorizontalGlue());
        hBox.add(new JButton("button"));
        hBox.add(Box.createHorizontalStrut(10));
        hBox.add(new JLabel("label"));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(vBox);
        panel.add(Box.createGlue());
        panel.add(hBox);
        return panel;
    }
    protected JPanel getAnotherObject() {
        return null; 
    }
}
