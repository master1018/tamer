public class InfiniteRecursion_4 {
    final static Robot robot = Util.createRobot();
    final static int MOVE_COUNT = 5;
    final static int EXPECTED_COUNT = MOVE_COUNT * 2;
    static int actualEvents = 0;
    public static void main(String []s)
    {
        JFrame frame = new JFrame("A test frame");
        frame.setSize(200, 200);
        frame.addMouseWheelListener(new MouseWheelListener() {
                public void mouseWheelMoved(MouseWheelEvent e)
                {
                    System.out.println("Wheel moved on FRAME : "+e);
                    actualEvents++;
                }
            });
        frame.setVisible(true);
        Util.waitForIdle(robot);
        Util.pointOnComp(frame, robot);
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
