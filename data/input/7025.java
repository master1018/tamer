public class bug4625203 extends JFrame {
    JTextField jTextField1 = new JTextField();
    JButton jButton1 = new JButton();
    java.util.Locale locale;
    public int n = 0;
    public bug4625203() {
        try {
            jbInit();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        bug4625203 frame1 = new bug4625203();
        frame1.setSize(400,300);
        frame1.setVisible(true);
    }
    private void jbInit() throws Exception {
        jTextField1.setText("jTextField1");
        jButton1.setText("jButton1");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton1_actionPerformed(e);
            }
        });
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                this_windowClosing(e);
            }
        });
        this.getContentPane().add(jTextField1, BorderLayout.CENTER);
        this.getContentPane().add(jButton1, BorderLayout.SOUTH);
    }
    void jButton1_actionPerformed(ActionEvent e) {
        locale = ((JButton) e.getSource()).getInputContext().getLocale();
        System.out.println("locale" + n + ":" + locale.toString());
        bug4625203 frame2 = new bug4625203();
        frame2.n = n + 1;
        frame2.setSize(400,300);
        frame2.setTitle("test:" +n);
        frame2.setVisible(true);
    }
    void this_windowClosing(WindowEvent e) {
        System.exit(0);
    }
}
