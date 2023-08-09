public class QuoteAndBackslashTest {
    public static void main(String[] args) {
        new QuoteAndBackslashTest().start();
    }
    public void start() {
        new QuoteAndBackslashTestFrame();
    }
}
class QuoteAndBackslashTestFrame extends Frame implements ActionListener {
    PrintCanvas canvas;
    public QuoteAndBackslashTestFrame () {
        super("QuoteAndBackslashTest");
        canvas = new PrintCanvas ();
        add("Center", canvas);
        Button b = new Button("Print");
        b.setActionCommand ("print");
        b.addActionListener (this);
        add("South", b);
        pack();
        setVisible(true);
    }
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("print")) {
            PrintJob pjob = getToolkit().getPrintJob(this, "\\\"\"\\\"",
                                                     null);
            if (pjob != null) {
                Graphics pg = pjob.getGraphics();
                if (pg != null)  {
                    canvas.printAll(pg);
                    pg.dispose();  
                }
                pjob.end();
            }
        }
    }
}
class PrintCanvas extends Canvas {
    public Dimension getPreferredSize() {
        return new Dimension(659, 792);
    }
    public void paint (Graphics g) {
        setBackground(Color.white);
        g.setColor(Color.blue);
        g.fillRoundRect(50, 50, 100, 200, 50, 50);
    }
}
