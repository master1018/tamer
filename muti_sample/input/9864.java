public class PrintArcTest extends Panel implements ActionListener {
    PrintCanvas canvas;
    public PrintArcTest () {
        setLayout(new BorderLayout());
        canvas = new PrintCanvas ();
        add("North", canvas);
        Button b = new Button("Click Me to Print!");
        b.setActionCommand ("print");
        b.addActionListener (this);
        add("South", b);
        validate();
    }
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("print")) {
            PrintJob pjob = getToolkit().getPrintJob(getFrame(), "Printing Test", null);
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
    private Frame getFrame() {
        Container cont = getParent();;
        while ( !(cont instanceof Frame  ) ) {
            cont = cont.getParent();
        }
        return (Frame)cont;
    }
    public static void main(String args[]) {
        PrintArcTest test = new PrintArcTest();
        Frame   f = new Frame( "PrintArcTest for Bug #4105609");
        f.add( test );
        f.setSize(500,400);
        f.show();
        f.addWindowListener( new WindowAdapter() {
                                        public void windowClosing(WindowEvent ev) {
                                            System.exit(0);
                                        }
                                    }
                            );
    }
}
class PrintCanvas extends Canvas {
    public Dimension getPreferredSize() {
            return new Dimension(300,300);
    }
    public void paint (Graphics g) {
        g.drawString("drawArc(25,50,150,100,45,90);",25,25);
        g.drawArc(25,50,150,100,45,90);
        g.drawString("drawOval(25,200,200,40);",25,175);
        g.drawOval(25,200,200,40);
        g.dispose();
    }
}
