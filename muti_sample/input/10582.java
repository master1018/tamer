public class TestSinhalaChar {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TestSinhalaChar().run();
            }
        });
    }
    public static boolean AUTOMATIC_TEST=true;  
    private void run() {
        JFrame frame = new JFrame("Test Character (no crash = PASS)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        final JLabel label = new JLabel("(empty)");
        label.setSize(400, 100);
        label.setBorder(new LineBorder(Color.black));
        label.setFont(new Font("Lucida Bright", Font.PLAIN, 12));
        if(AUTOMATIC_TEST) {  
           label.setText(Character.toString('\u0DDD'));
        } else {
        JButton button = new JButton("Set Char x0DDD");
        button.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
           label.setText(Character.toString('\u0DDD'));
            }
        });
        panel.add(button);
        }
        panel.add(label);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
