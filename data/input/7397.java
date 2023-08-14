public class bug4959409 extends javax.swing.JApplet {
    public void init() {
        new TestFrame();
    }
}
class TestFrame extends JFrame implements KeyListener {
    JTextField text;
    JLabel label;
    TestFrame () {
        text = new JTextField();
        text.addKeyListener(this);
        label = new JLabel(" ");
        Container c = getContentPane();
        BorderLayout borderLayout1 = new BorderLayout();
        c.setLayout(borderLayout1);
        c.add(text, BorderLayout.CENTER);
        c.add(label, BorderLayout.SOUTH);
        setSize(300, 200);
        setVisible(true);
    }
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        int mods = e.getModifiers();
        if (code == '1' && mods == KeyEvent.SHIFT_MASK) {
            label.setText("KEYPRESS received for Shift+1");
        } else {
            label.setText(" ");
        }
    }
    public void keyTyped(KeyEvent e) {
    }
    public void keyReleased(KeyEvent e) {
    }
}
