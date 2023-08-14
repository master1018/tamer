public class MakeWindowAlwaysOnTop
{
    private static Frame f;
    private static Dialog d;
    public static void main(String[] args) throws Exception
    {
        Robot r = Util.createRobot();
        Util.waitForIdle(r);
        f = new Frame("Test frame");
        f.setBounds(100, 100, 400, 300);
        f.setBackground(Color.RED);
        f.setVisible(true);
        r.delay(100);
        Util.waitForIdle(r);
        d = new Dialog(null, "Modal dialog", Dialog.ModalityType.APPLICATION_MODAL);
        d.setBounds(500, 500, 160, 160);
        d.setAlwaysOnTop(true);
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                d.setVisible(true);
            }
        });
        EventQueue.invokeAndWait(new Runnable()
        {
            public void run()
            {
            }
        });
        r.delay(100);
        Util.waitForIdle(r);
        Point p = f.getLocationOnScreen();
        r.mouseMove(p.x + f.getWidth() / 2, p.y + f.getHeight() / 2);
        Util.waitForIdle(r);
        r.mousePress(InputEvent.BUTTON1_MASK);
        Util.waitForIdle(r);
        r.mouseRelease(InputEvent.BUTTON1_MASK);
        Util.waitForIdle(r);
        r.delay(100);
        Util.waitForIdle(r);
        d.dispose();
        r.delay(100);
        Util.waitForIdle(r);
        Frame t = new Frame("Check");
        t.setBounds(100, 100, 400, 300);
        t.setBackground(Color.BLUE);
        t.setVisible(true);
        r.delay(100);
        Util.waitForIdle(r);
        t.toFront();
        r.delay(100);
        Util.waitForIdle(r);
        Color c = r.getPixelColor(p.x + f.getWidth() / 2, p.y + f.getHeight() / 2);
        System.out.println("Color = " + c);
        System.out.flush();
        if (Color.RED.equals(c))
        {
            throw new RuntimeException("Test FAILED: the frame is always-on-top");
        }
        else if (!Color.BLUE.equals(c))
        {
            throw new RuntimeException("Test FAILED: unknown window is on top of the frame");
        }
        else
        {
            System.out.println("Test PASSED");
            System.out.flush();
        }
        t.dispose();
        f.dispose();
    }
}
