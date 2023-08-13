public class AlwaysOnTopEvenOfWindow {
    static boolean clicked = false;
    public static void main(String args[]) {
        Window win = new Window(null);
        win.setBounds( 50,50, 300,50);
        win.addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent me ) {
                clicked = true;
            }
        });
        Frame frame = new Frame("top");
        frame.setBounds(100, 20, 50, 300);
        frame.setAlwaysOnTop( true );
        Robot robot = Util.createRobot();
        robot.mouseMove(125, 75);
        frame.setVisible(true);
        win.setVisible(true);
        Util.waitForIdle(robot);
        if(!frame.isAlwaysOnTopSupported())  {
            return;
        }
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        Util.waitForIdle(robot);
        if( clicked ) {
            throw new RuntimeException("This part of Window should be invisible");
        }
    }
}
