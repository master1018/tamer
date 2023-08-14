public class CTORRestrictions{
    static Frame frame = new Frame("MouseEvent Test Frame");
    static Point mousePosition;
    static Point mousePositionOnScreen;
    public static void main(String []s){
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            throw new RuntimeException("Test Failed", ex);
        }
        frame.setSize (200,200);
        frame.setLocation (300, 400);
        frame.setVisible(true);
        robot.delay(1000);
        System.out.println("sun.awt.enableExtraMouseButtons = "+Toolkit.getDefaultToolkit().getDesktopProperty("sun.awt.enableExtraMouseButtons"));
        mousePosition = new Point(100, 100);
        mousePositionOnScreen = new  Point(frame.getLocationOnScreen().x + mousePosition.x,
                                                 frame.getLocationOnScreen().y + mousePosition.y);
        int numberOfButtons;
        if (Toolkit.getDefaultToolkit().getClass().getName().equals("sun.awt.windows.WToolkit")){
            numberOfButtons = MouseInfo.getNumberOfButtons();
        } else {
            numberOfButtons = MouseInfo.getNumberOfButtons() - 2;
        }
        System.out.println("Stage 1. Number of buttons = "+ numberOfButtons);
        for (int buttonId = 1; buttonId <= numberOfButtons; buttonId++){
            postMouseEventNewCtor(buttonId);
        }
        System.out.println("Stage 2. Number of buttons = "+ numberOfButtons);
        for (int buttonId = 1; buttonId <= numberOfButtons; buttonId++){
            postMouseEventOldCtor(buttonId);
        }
        System.out.println("Test passed.");
    }
    public static void postMouseEventNewCtor(int buttonId)    {
        MouseEvent me = new MouseEvent(frame,
                                       MouseEvent.MOUSE_PRESSED,
                                       System.currentTimeMillis(),
                                       MouseEvent.BUTTON1_DOWN_MASK,
                                       mousePosition.x, mousePosition.y,
                                       mousePositionOnScreen.x,
                                       mousePositionOnScreen.y,
                                       1,
                                       false,              
                                       buttonId            
                                       );
        frame.dispatchEvent( ( AWTEvent )me );
    }
    public static void postMouseEventOldCtor(int buttonId)    {
        MouseEvent meOld = new MouseEvent(frame,
                                          MouseEvent.MOUSE_PRESSED,
                                          System.currentTimeMillis(),
                                          MouseEvent.BUTTON1_DOWN_MASK,
                                          mousePosition.x, mousePosition.y,
                                          1,
                                          false,              
                                          buttonId 
                                          );
        frame.dispatchEvent( ( AWTEvent )meOld );
    }
}
