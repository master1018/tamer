public class MouseWheelEventAbsoluteCoordsTest extends Applet implements MouseWheelListener
{
    Frame frame = new Frame("MouseWheelEvent Test Frame");
    Point mousePositionOnScreen = new Point(200, 200);
    Point mousePosition = new Point(100, 100);
    public void init()
    {
        this.setLayout (new BorderLayout ());
        frame.addMouseWheelListener(this);
    }
    public void start ()
    {
        frame.setSize (200,200);
        frame.setLocation(47, 47);
        frame.setVisible(true);
        validate();
        try {
            Util.waitForIdle(new Robot());
        }catch (Exception e){
            throw new RuntimeException("Test failed.", e);
        }
        System.out.println("New Ctor Stage");
        postMouseWheelEventNewCtor(MouseEvent.MOUSE_CLICKED);
        mousePositionOnScreen = new Point(frame.getLocationOnScreen().x + mousePosition.x,
                                          frame.getLocationOnScreen().y + mousePosition.y);
        System.out.println("Old Ctor Stage");
        postMouseWheelEventOldCtor(MouseEvent.MOUSE_CLICKED);
    }
    public void mouseWheelMoved(MouseWheelEvent e){
        checkEventAbsolutePosition(e, "MouseWheelMoved OK");
    };
    public void postMouseWheelEventNewCtor(int MouseEventType)    {
        MouseWheelEvent me = new MouseWheelEvent(frame,
                                                 MouseEventType,
                                                 System.currentTimeMillis(),
                                                 MouseEvent.BUTTON1_DOWN_MASK,
                                                 mousePosition.x, mousePosition.y,
                                                 mousePositionOnScreen.x,
                                                 mousePositionOnScreen.y,
                                                 1,
                                                 false,  
                                                 MouseWheelEvent.WHEEL_UNIT_SCROLL,
                                                 1,  
                                                 1  
                                                 );
        frame.dispatchEvent( ( AWTEvent )me );
    }
    public void postMouseWheelEventOldCtor(int MouseEventType)    {
        MouseWheelEvent meOld = new MouseWheelEvent(frame,
                                                 MouseEventType,
                                                 System.currentTimeMillis(),
                                                 MouseEvent.BUTTON1_DOWN_MASK,
                                                 mousePosition.x, mousePosition.y,
                                                 1,
                                                 false,  
                                                 MouseWheelEvent.WHEEL_UNIT_SCROLL,
                                                 1,  
                                                 1  
                                                 );
        frame.dispatchEvent( ( AWTEvent )meOld );
    }
    public void checkEventAbsolutePosition(MouseEvent evt, String message){
            if (evt.getXOnScreen() != mousePositionOnScreen.x ||
                evt.getYOnScreen() != mousePositionOnScreen.y ||
                !evt.getLocationOnScreen().equals( mousePositionOnScreen )  ){
                throw new RuntimeException("get(X|Y)OnScreen() or getPointOnScreen() work incorrectly");
            }
            if (evt.getX() != mousePosition.x ||
                evt.getY() != mousePosition.y ||
                !evt.getPoint().equals( mousePosition )  ){
                throw new RuntimeException("get(X|Y)() or getPoint() work incorrectly");
            }
        System.out.println(message);
    }
}
