public class URIListBetweenJVMsTest extends Applet {
    static int VISIBLE_RAWS_IN_LIST=15;
    public void init() {
        setLayout(new BorderLayout());
    }
    public void start() {
    String toolkit = Toolkit.getDefaultToolkit().getClass().getName();
    if (toolkit.equals("sun.awt.windows.WToolkit")){
        System.out.println("This test is not for the Windows platform. Passed.");
        return;
    } else {
        System.out.println("Toolkit = " + toolkit);
    }
        SourceFileListFrame sourceFrame = new SourceFileListFrame();
        Util.waitForIdle(null);
        String [] args = new String [] {
                String.valueOf(sourceFrame.getNextLocationX()),
                String.valueOf(sourceFrame.getNextLocationY()),
                String.valueOf(sourceFrame.getDragSourcePointX()),
                String.valueOf(sourceFrame.getDragSourcePointY()),
                String.valueOf(sourceFrame.getSourceFilesNumber())
        };
        String classpath = System.getProperty("java.class.path");
        ProcessResults processResults =
                ProcessCommunicator.executeChildProcess(this.getClass(), classpath, args);
        verifyTestResults(processResults);
    }
    private static void verifyTestResults(ProcessResults processResults) {
        if ( InterprocessMessages.WRONG_FILES_NUMBER_ON_TARGET ==
                processResults.getExitValue())
        {
            processResults.printProcessErrorOutput(System.err);
            throw new RuntimeException("TEST IS FAILED: Target has recieved" +
                    " wrong number of files.");
        }
        processResults.verifyStdErr(System.err);
        processResults.verifyProcessExitValue(System.err);
        processResults.printProcessStandartOutput(System.out);
    }
    public URIListBetweenJVMsTest () {
        super();
    }
    public URIListBetweenJVMsTest (Point targetFrameLocation, Point dragSourcePoint,
            int transferredFilesNumber)
            throws InterruptedException
    {
        TargetFileListFrame targetFrame = new TargetFileListFrame(targetFrameLocation,
                transferredFilesNumber);
        Util.waitForIdle(null);
        final Robot robot = Util.createRobot();
        robot.mouseMove((int)dragSourcePoint.getX(),(int)dragSourcePoint.getY());
        sleep(100);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        sleep(100);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        sleep(100);
        Util.drag(robot, dragSourcePoint, targetFrame.getDropTargetPoint(),
                InputEvent.BUTTON1_MASK);
    }
    enum InterprocessArguments {
        TARGET_FRAME_X_POSITION_ARGUMENT,
        TARGET_FRAME_Y_POSITION_ARGUMENT,
        DRAG_SOURCE_POINT_X_ARGUMENT,
        DRAG_SOURCE_POINT_Y_ARGUMENT,
        FILES_IN_THE_LIST_NUMBER_ARGUMENT;
        int extract (String [] args) {
            return Integer.parseInt(args[this.ordinal()]);
        }
    }
    public static void main (String [] args) {
        Point dragSourcePoint = new Point(InterprocessArguments.DRAG_SOURCE_POINT_X_ARGUMENT.extract(args),
                InterprocessArguments.DRAG_SOURCE_POINT_Y_ARGUMENT.extract(args));
        Point targetFrameLocation = new Point(InterprocessArguments.TARGET_FRAME_X_POSITION_ARGUMENT.extract(args),
                InterprocessArguments.TARGET_FRAME_Y_POSITION_ARGUMENT.extract(args));
        int transferredFilesNumber = InterprocessArguments.FILES_IN_THE_LIST_NUMBER_ARGUMENT.extract(args);
        try {
            new URIListBetweenJVMsTest(targetFrameLocation, dragSourcePoint, transferredFilesNumber);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
