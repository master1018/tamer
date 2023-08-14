public class MaximizedFrameTest extends Applet
{
    final int ITERATIONS_COUNT = 20;
    Robot robot;
    Point framePosition;
    Point newFrameLocation;
    JFrame  frame;
    Rectangle gcBounds;
    public static Object LOCK = new Object();
    public void init()
    {
        String[] instructions =
        {
            "This is an AUTOMATIC test",
            "simply wait until it is done"
        };
        JFrame.setDefaultLookAndFeelDecorated(true);
        frame = new JFrame("JFrame Maximization Test");
        frame.pack();
        frame.setSize(450, 260);
    }
    public void start ()
    {
        frame.setVisible(true);
        validate();
        JLayeredPane lPane = frame.getLayeredPane();
        Component titleComponent = null;
        boolean titleFound = false;
        for (int j=0; j < lPane.getComponentsInLayer(JLayeredPane.FRAME_CONTENT_LAYER.intValue()).length; j++){
            titleComponent = lPane.getComponentsInLayer(JLayeredPane.FRAME_CONTENT_LAYER.intValue())[j];
            if (titleComponent.getClass().getName().equals("javax.swing.plaf.metal.MetalTitlePane")){
                titleFound = true;
                break;
            }
        }
        if ( !titleFound ){
            throw new RuntimeException("Test Failed. Unable to determine title's size.");
        }
        Point tempMousePosition;
        framePosition = frame.getLocationOnScreen();
        try {
            robot = new Robot();
            tempMousePosition = new Point(framePosition.x +
                                          frame.getWidth()/2,
                                          framePosition.y +
                                          titleComponent.getHeight()/2);
            robot.mouseMove(tempMousePosition.x, tempMousePosition.y);
            for (int iteration=0; iteration < ITERATIONS_COUNT; iteration++){
                robot.mousePress(InputEvent.BUTTON1_MASK);
                gcBounds =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getConfigurations()[0].getBounds();
                tempMousePosition.x += 5;
                robot.mouseMove(tempMousePosition.x, tempMousePosition.y);
                robot.delay(70);
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
                if ( frame.getExtendedState() != 0 ){
                    throw new RuntimeException ("Test failed. JFrame was maximized. ExtendedState is : "+frame.getExtendedState());
                }
                robot.delay(500);
            } 
            }catch(AWTException e) {
                throw new RuntimeException("Test Failed. AWTException thrown.");
            }
        System.out.println("Test passed.");
    }
}
