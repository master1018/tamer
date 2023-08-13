public class DragUnicodeBetweenJVMTest extends Applet
{
    public void init() {
        setLayout(new BorderLayout());
    }
    public void start() {
        String toolkit = Toolkit.getDefaultToolkit().getClass().getName();
        if (!toolkit.equals("sun.awt.windows.WToolkit")){
            System.out.println("This test is for Windows only. Passed.");
            return;
        }
        else{
            System.out.println("Toolkit = " + toolkit);
        }
        final Frame sourceFrame = new Frame("Source frame");
        final SourcePanel sourcePanel = new SourcePanel();
        sourceFrame.add(sourcePanel);
        sourceFrame.pack();
        sourceFrame.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sourceFrame.dispose();
            }
        });
        sourceFrame.setVisible(true);
        Util.waitForIdle(null);
        NextFramePositionCalculator positionCalculator = new NextFramePositionCalculator(sourceFrame);
        String [] args = new String [] {
                String.valueOf(positionCalculator.getNextLocationX()),
                String.valueOf(positionCalculator.getNextLocationY()),
                String.valueOf(AbsoluteComponentCenterCalculator.calculateXCenterCoordinate(sourcePanel)),
                String.valueOf(AbsoluteComponentCenterCalculator.calculateYCenterCoordinate(sourcePanel)),
        };
       ProcessResults processResults =
                ProcessCommunicator.executeChildProcess(this.getClass(), args);
        verifyTestResults(processResults);
    }
    private static void verifyTestResults(ProcessResults processResults) {
        if ( InterprocessMessages.FILES_ON_TARGET_ARE_CORRUPTED ==
                processResults.getExitValue())
        {
            processResults.printProcessErrorOutput(System.err);
            throw new RuntimeException("TEST IS FAILED: Target has recieved" +
                    " broken file list.");
        }
        processResults.verifyStdErr(System.err);
        processResults.verifyProcessExitValue(System.err);
        processResults.printProcessStandartOutput(System.out);
    }
    public DragUnicodeBetweenJVMTest () {
        super();
    }
    public DragUnicodeBetweenJVMTest (Point targetFrameLocation, Point dragSourcePoint)
            throws InterruptedException
    {
        final Frame targetFrame = new Frame("Target frame");
        final TargetPanel targetPanel = new TargetPanel(targetFrame);
        targetFrame.add(targetPanel);
        targetFrame.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                targetFrame.dispose();
            }
        });
        targetFrame.setLocation(targetFrameLocation);
        targetFrame.pack();
        targetFrame.setVisible(true);
        doTest(dragSourcePoint, targetPanel);
    }
    private void doTest(Point dragSourcePoint, TargetPanel targetPanel) {
        Util.waitForIdle(null);
        final Robot robot = Util.createRobot();
        robot.mouseMove((int)dragSourcePoint.getX(),(int)dragSourcePoint.getY());
        try {
            sleep(100);
            robot.mousePress(InputEvent.BUTTON1_MASK);
            sleep(100);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
            sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Util.drag(robot, dragSourcePoint, new Point (AbsoluteComponentCenterCalculator.calculateXCenterCoordinate(targetPanel),
                AbsoluteComponentCenterCalculator.calculateYCenterCoordinate(targetPanel)),
                InputEvent.BUTTON1_MASK);
    }
    enum InterprocessArguments {
        TARGET_FRAME_X_POSITION_ARGUMENT,
        TARGET_FRAME_Y_POSITION_ARGUMENT,
        DRAG_SOURCE_POINT_X_ARGUMENT,
        DRAG_SOURCE_POINT_Y_ARGUMENT;
        int extract (String [] args) {
            return Integer.parseInt(args[this.ordinal()]);
        }
    }
    public static void main (String [] args) {
        Point dragSourcePoint = new Point(InterprocessArguments.DRAG_SOURCE_POINT_X_ARGUMENT.extract(args),
                InterprocessArguments.DRAG_SOURCE_POINT_Y_ARGUMENT.extract(args));
        Point targetFrameLocation = new Point(InterprocessArguments.TARGET_FRAME_X_POSITION_ARGUMENT.extract(args),
                InterprocessArguments.TARGET_FRAME_Y_POSITION_ARGUMENT.extract(args));
        try {
            new DragUnicodeBetweenJVMTest(targetFrameLocation, dragSourcePoint);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
