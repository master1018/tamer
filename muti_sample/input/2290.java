public final class Test6348456 extends JApplet implements ActionListener {
    private static final DefaultColorSelectionModel WHITE = new DefaultColorSelectionModel(Color.WHITE);
    private static final DefaultColorSelectionModel BLACK = new DefaultColorSelectionModel(Color.BLACK);
    private JColorChooser chooser;
    @Override
    public void init() {
        JButton button = new JButton("Swap models");
        button.addActionListener(this);
        this.chooser = new JColorChooser(Color.RED);
        this.chooser.setSelectionModel(WHITE);
        add(BorderLayout.NORTH, button);
        add(BorderLayout.CENTER, this.chooser);
    }
    public void actionPerformed(ActionEvent event){
        this.chooser.setSelectionModel(this.chooser.getSelectionModel() == BLACK ? WHITE : BLACK);
    }
}
