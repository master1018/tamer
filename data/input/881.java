public class InfiniteRecursion {
    final static Robot robot = Util.createRobot();
    final static int MOVE_COUNT = 5;
    final static int EXPECTED_COUNT = MOVE_COUNT * 2 * 2;
    static int actualEvents = 0;
    public static void main(String []s)
    {
        JFrame frame = new JFrame("A test frame");
        frame.setSize(200, 200);
        frame.addMouseWheelListener(new MouseWheelListener() {
                public void mouseWheelMoved(MouseWheelEvent e) {
                    System.out.println("Wheel moved on FRAME : "+e);
                    actualEvents++;
                }
            });
        JButton jButton = new JButton();
        jButton.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    System.out.println("MousePressed on jButton : "+e);
                }
            });
        frame.add(jButton);
        frame.setVisible(true);
        Util.waitForIdle(robot);
        Util.pointOnComp(jButton, robot);
        Util.waitForIdle(robot);
        for (int i = 0; i < MOVE_COUNT; i++){
            robot.mouseWheel(1);
            robot.delay(10);
        }
        for (int i = 0; i < MOVE_COUNT; i++){
            robot.mouseWheel(-1);
            robot.delay(10);
        }
        Util.waitForIdle(robot);
        if (actualEvents != EXPECTED_COUNT) {
            AbstractTest.fail("Expected events count: "+ EXPECTED_COUNT+" Actual events count: "+ actualEvents);
        }
    }
}
