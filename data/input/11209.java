public class InfiniteRecursion_2 extends Applet {
    final static Robot robot = Util.createRobot();
    final static int MOVE_COUNT = 5;
    final static int EXPECTED_COUNT = MOVE_COUNT * 2 * 2;
    static int actualEvents = 0;
    public void init()
    {
        setLayout (new BorderLayout ());
    }
    public void start ()
    {
        JPanel outputBox = new JPanel();
        JButton jButton = new JButton();
        this.setSize(200, 200);
        this.addMouseWheelListener(new MouseWheelListener() {
                public void mouseWheelMoved(MouseWheelEvent e)
                {
                    System.out.println("Wheel moved on APPLET : "+e);
                    actualEvents++;
                }
            });
        outputBox.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e)
                {
                    System.out.println("MousePressed on OUTBOX : "+e);
                }
            });
        this.add(outputBox);
        outputBox.add(jButton);
        this.setVisible(true);
        this.validate();
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
