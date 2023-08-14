public class InsetClipping extends Frame {
    BufferedImage image;
    Area area;
    static boolean painted = false;
    static Color imageColor = Color.red;
    static Color fillColor = Color.blue;
    public InsetClipping() {
        image  = new BufferedImage( 300, 300,BufferedImage.TYPE_INT_RGB);
        Graphics g2 = image.createGraphics();
        g2.setColor(imageColor);
        g2.fillRect(0,0, 300,300);
    }
    public void paint(Graphics g) {
        Insets insets = getInsets();
        area = new Area( new Rectangle(0,0, getWidth(), getHeight()));
        area.subtract(new Area(new Rectangle(insets.left, insets.top,
                                      getWidth() - insets.right,
                                      getHeight() - insets.bottom)));
        g.setColor(fillColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setClip(area);
        g.drawImage(image, 0, 0, null);
        painted = true;
    }
    public static void main(String args[]) {
        InsetClipping clipTest = new InsetClipping();
        clipTest.setSize(300, 300);
        clipTest.setVisible(true);
        while (!painted) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {}
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {}
        try {
            Robot robot = new Robot();
            Point clientLoc = clipTest.getLocationOnScreen();
            Insets insets = clipTest.getInsets();
            clientLoc.x += insets.left;
            clientLoc.y += insets.top;
            BufferedImage clientPixels =
                robot.createScreenCapture(new Rectangle(clientLoc.x,
                                                        clientLoc.y,
                                                        clientLoc.x + 2,
                                                        clientLoc.y + 2));
            try {
                Thread.sleep(2000);
            } catch (Exception e) {}
            int pixelVal = clientPixels.getRGB(0, 0);
            clipTest.dispose();
            if ((new Color(pixelVal)).equals(fillColor)) {
                System.out.println("Passed");
            } else {
                throw new Error("Failed: incorrect color in pixel (0, 0)");
            }
        } catch (Exception e) {
            System.out.println("Problems creating Robot");
        }
    }
}
