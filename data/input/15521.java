public class TestOldHangul {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TestOldHangul().run();
            }
        });
    }
    public static boolean AUTOMATIC_TEST=true;  
    private void run()  {
        Font ourFont = null;
        final String fontName = "UnBatangOdal.ttf";  
        try {
            ourFont = Font.createFont(Font.TRUETYPE_FONT, new java.io.File(new java.io.File(System.getProperty("user.home"),"fonts"), fontName));
            ourFont = ourFont.deriveFont((float)48.0);
        } catch(Throwable t) {
            t.printStackTrace();
            System.err.println("Fail: " + t);
            return;
        }
        JFrame frame = new JFrame(System.getProperty("java.version"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        final JTextArea label = new JTextArea("(empty)");
        label.setSize(400, 300);
        label.setBorder(new LineBorder(Color.black));
        label.setFont(ourFont);
        final String str = "\u110A\u119E\u11B7\u0020\u1112\u119E\u11AB\uAE00\u0020\u1100\u119E\u11F9\u0020\u112B\u119E\u11BC\n";
        if(AUTOMATIC_TEST) {  
            label.setText(str);
        } else {
        JButton button = new JButton("Old Hangul");
        button.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                label.setText(str);
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
