public class GraphApplet extends java.applet.Applet {
    double f(double x) {
        return (Math.cos(x / 5) + Math.sin(x / 7) + 2) * getSize().height / 4;
    }
    @Override
    public void paint(Graphics g) {
        for (int x = 0; x < getSize().width; x++) {
            g.drawLine(x, (int) f(x), x + 1, (int) f(x + 1));
        }
    }
    @Override
    public String getAppletInfo() {
        return "Draws a sin graph.";
    }
}
