public class EdgeTest extends Panel {
    public void init() {
        Frame f = new Frame("EdgeTest");
        f.setSize(50, 50);
        f.addWindowListener( new WindowAdapter() {
                                    public void windowClosing(WindowEvent ev) {
                                        System.exit(0);
                                    }
                                }
                            );
        f.setVisible(true);
        JobAttributes job = new JobAttributes();
        job.setDialog(JobAttributes.DialogType.NONE);
        PrintJob pj = getToolkit().getPrintJob(f, "EdgeTest", job, null);
        if (pj != null) {
            Graphics g = pj.getGraphics();
            Dimension d = pj.getPageDimension();
            g.setColor(Color.black);
            g.setFont(new Font("Serif", Font.PLAIN, 12));
            g.drawLine(0, 0, d.width, 0);
            g.drawLine(0, 0, 0, d.height);
            g.drawLine(0, d.height, d.width, d.height);
            g.drawLine(d.width, 0, d.width, d.height);
            g.drawString("This page should have no borders!",
                         d.width / 2 - 100, d.height / 2 - 10);
            g.dispose();
            pj.end();
        }
    }
    public static void main(String[] args) {
        new EdgeTest().init();
    }
}
