public class AWTInputDialog extends Dialog implements ActionListener, KeyListener {
    private static final long serialVersionUID = 1L;
    private Button button;
    private AWTLabel label;
    private TextField textMessage;
    public AWTInputDialog(String title, String message) {
        this(LSystem.getSystemHandler().getWindow(), title, message);
    }
    public AWTInputDialog(Window parent, String title, String message) {
        super((Frame) parent, title, true);
        if (message == null) {
            message = "";
        }
        setLayout(null);
        textMessage = new TextField();
        label = new AWTLabel(message);
        button = new Button();
        textMessage.setText("");
        textMessage.setFont(GraphicsUtils.getFont("黑体", 0, 25));
        textMessage.setBounds(30, 60, 270, 40);
        add(textMessage);
        textMessage.addActionListener(this);
        label.setBounds(25, 30, 360, 22);
        label.setFont(GraphicsUtils.getFont("黑体", 0, 15));
        add(label);
        button.setLabel(" 确定输入 ");
        button.setBounds(310, 60, 80, 40);
        add(button);
        button.addActionListener(this);
        this.pack();
        this.setSize(415, 120);
        this.setResizable(false);
        this.setModal(true);
        LSystem.centerOn(this);
    }
    public String getTextMessage() {
        return textMessage.getText();
    }
    public void actionPerformed(ActionEvent e) {
        dispose();
    }
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_ESCAPE) {
            dispose();
        }
    }
    public void keyReleased(KeyEvent e) {
    }
    public void keyTyped(KeyEvent e) {
    }
}
