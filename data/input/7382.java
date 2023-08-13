public class PrintAllXcheckJNI
{
    public static void main(String []s)
    {
        Frame frame = new Frame();
        frame.setVisible(true);
        BufferedImage img = new BufferedImage(frame.getWidth(),
                                              frame.getHeight(),
                                              BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        frame.printAll(g);
        g.dispose();
        img.flush();
    }
}
