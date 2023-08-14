public class FrameMouseEventAbsoluteCoordsTest extends Applet implements MouseListener
{
    Robot robot;
    Frame frame = new Frame("MouseEvent Test Frame II");
    Button button = new Button("Just Button");
    Point mousePositionAbsolute;
    Point mousePosition;
    public void init()
    {
        this.setLayout (new BorderLayout ());
        button.addMouseListener(this);
        frame.add(button);
        frame.pack();
    }
    public void start ()
    {
        setSize (200,200);
        frame.setVisible(true);
        validate();
        Util.waitForIdle(robot);
        try {
            robot = new Robot();
            robot.setAutoWaitForIdle(true);
            mousePositionAbsolute = new Point(button.getLocationOnScreen().x + button.getWidth()/2,
                                              button.getLocationOnScreen().y + button.getHeight()/2);
            mousePosition = new Point(button.getWidth()/2,
                                      button.getHeight()/2);
            robot.mouseMove(mousePositionAbsolute.x,
                            mousePositionAbsolute.y );
            robot.mousePress(InputEvent.BUTTON1_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_MASK);
        }catch(AWTException e) {
            throw new RuntimeException("Test Failed. AWTException thrown.");
        }
    }
    public void mousePressed(MouseEvent e){
        checkEventAbsolutePosition(e, "MousePressed OK");
    };
    public void mouseReleased(MouseEvent e){
        checkEventAbsolutePosition(e, "MouseReleased OK");
    };
    public void mouseClicked(MouseEvent e){
        checkEventAbsolutePosition(e, "MouseClicked OK");
    };
    public void mouseEntered(MouseEvent e){
        System.out.println("mouse entered");
    };
    public void mouseExited(MouseEvent e){
        System.out.println("mouse exited");
    };
    public void checkEventAbsolutePosition(MouseEvent evt, String message){
        if (evt.getXOnScreen() != mousePositionAbsolute.x ||
            evt.getYOnScreen() != mousePositionAbsolute.y ||
            !evt.getLocationOnScreen().equals( mousePositionAbsolute )  ){
            throw new RuntimeException("get(X|Y)OnScreen() or getLocationOnScreen() works incorrectly: expected"+
                                       mousePositionAbsolute.x+":"+mousePositionAbsolute.y+
                                       "\n Got:"+ evt.getXOnScreen()+":"+evt.getYOnScreen());
        }
        if (evt.getX() != mousePosition.x ||
            evt.getY() != mousePosition.y ||
            !evt.getPoint().equals( mousePosition )  ){
            throw new RuntimeException("get(X|Y)() or getLocationOnScreen() works incorrectly: expected"+
                                       mousePositionAbsolute.x+":"+mousePositionAbsolute.y+"\n Got:"
                                       +evt.getX()+":"+evt.getY());
        }
        System.out.println(message);
    }
}
