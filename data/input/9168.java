public class RoundedRectTest {
    public static void main(String[] args) {
        new RoundedRectTest().start();
    }
    public void start() {
        new RoundedRectTestFrame();
    }
}
class RoundedRectTestFrame extends Frame implements ActionListener {
    PrintCanvas canvas;
    public RoundedRectTestFrame () {
        super("RoundedRectTest");
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
            PrintJob pjob = getToolkit().getPrintJob(this, "RoundedRectTest",
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
        g.fillRoundRect(200, 50, 100, 100, 50, 50);
        g.fillRoundRect(350, 50, 200, 100, 50, 50);
        g.fillRoundRect(50, 300, 100, 200, 41, 97);
        g.fillRoundRect(200, 300, 100, 100, 41, 97);
        g.fillRoundRect(350, 300, 200, 100, 41, 97);
        g.fillRoundRect(50, 550, 100, 200, 97, 41);
        g.fillRoundRect(200, 550, 100, 100, 97, 41);
        g.fillRoundRect(350, 550, 200, 100, 97, 41);
    }
}
