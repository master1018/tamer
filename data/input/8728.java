public class ProxyPrintGraphics extends ProxyGraphics
                                implements PrintGraphics {
    private PrintJob printJob;
    public ProxyPrintGraphics(Graphics graphics, PrintJob thePrintJob) {
        super(graphics);
        printJob = thePrintJob;
    }
    public PrintJob getPrintJob() {
        return printJob;
    }
    public Graphics create() {
        return new ProxyPrintGraphics(getGraphics().create(), printJob);
    }
    public Graphics create(int x, int y, int width, int height) {
        Graphics g = getGraphics().create(x, y, width, height);
        return new ProxyPrintGraphics(g, printJob);
    }
    public Graphics getGraphics() {
        return super.getGraphics();
    }
    public void dispose() {
     super.dispose();
    }
}
