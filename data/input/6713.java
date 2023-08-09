public class TitleBarDoubleClick extends Applet implements MouseListener,
 WindowListener
{
    private final static Rectangle BOUNDS = new Rectangle(300, 300, 300, 300);
    private final static int TITLE_BAR_OFFSET = 10;
    Frame frame;
    Robot robot;
    public void init()
    {
        this.setLayout (new BorderLayout ());
    }
    public void start ()
    {
        setSize (200,200);
        setVisible(true);
        validate();
            robot = Util.createRobot();
            robot.setAutoDelay(100);
            robot.mouseMove(BOUNDS.x + (BOUNDS.width / 2),
                            BOUNDS.y + (BOUNDS.height/ 2));
            frame = new Frame("TitleBarDoubleClick");
            frame.setBounds(BOUNDS);
            frame.addMouseListener(this);
            frame.addWindowListener(this);
            frame.setVisible(true);
            Util.waitForIdle(robot);
    }
    static boolean hasRun = false;
    private void doTest() {
        if (hasRun) return;
        hasRun = true;
        System.out.println("doing test");
            robot.mouseMove(BOUNDS.x + (BOUNDS.width / 2),
                            BOUNDS.y + TITLE_BAR_OFFSET);
            robot.delay(50);
            System.out.println("1st press:   currentTimeMillis: " + System.currentTimeMillis());
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.delay(50);
            System.out.println("1st release: currentTimeMillis: " + System.currentTimeMillis());
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            robot.delay(50);
            System.out.println("2nd press:   currentTimeMillis: " + System.currentTimeMillis());
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.delay(50);
            System.out.println("2nd release: currentTimeMillis: " + System.currentTimeMillis());
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            System.out.println("done:        currentTimeMillis: " + System.currentTimeMillis());
    }
    private void fail() {
        throw new AWTError("Test failed");
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {fail();}
    public void mouseReleased(MouseEvent e) {fail();}
    public void mouseClicked(MouseEvent e) {fail();}
    public void windowActivated(WindowEvent  e) {doTest();}
    public void windowClosed(WindowEvent  e) {}
    public void windowClosing(WindowEvent  e) {}
    public void windowDeactivated(WindowEvent  e) {}
    public void windowDeiconified(WindowEvent  e) {}
    public void windowIconified(WindowEvent  e) {}
    public void windowOpened(WindowEvent  e) {}
}
