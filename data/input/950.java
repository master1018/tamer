public class JTextFieldTest extends Applet implements ActionListener {
    TextField  tf1;
    JTextField tf2;
    public JTextFieldTest() {
        tf1 = new TextField("ABCDEFGH", 10);
        tf1.setEditable(false);
        tf2 = new JTextField("12345678", 10);
        setLayout(new FlowLayout());
        add(tf1);
        add(tf2);
    }
    public void actionPerformed(ActionEvent ae) {
    }
    public static void main(String args[]) {
        JFrame  win = new JFrame();
        JTextFieldTest jtf = new JTextFieldTest();
        win.getContentPane().setLayout(new FlowLayout());
        win.getContentPane().add(jtf);
        win.pack();
        win.show();
    }
}
