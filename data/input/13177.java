public class MenuDragMouseEventAbsoluteCoordsTest extends Applet implements MouseListener
{
    Frame frame = new Frame("MenuDragMouseEvent Test Frame");
    Point mousePositionOnScreen = new Point(200, 200);
    Point mousePosition = new Point(100, 100);
    public void init()
    {
        frame.addMouseListener(this);
    }
    public void start ()
    {
        frame.setSize (200,200);
        frame.setVisible(true);
        validate();
        try {
            Util.waitForIdle(new Robot());
        }catch (Exception e){
            throw new RuntimeException("Test failed.", e);
        }
        System.out.println("New Ctor Stage MOUSE_PRESSED");
        postMenuDragMouseEventNewCtor(MouseEvent.MOUSE_PRESSED);
        mousePositionOnScreen = new Point(frame.getLocationOnScreen().x + mousePosition.x,
                                          frame.getLocationOnScreen().y + mousePosition.y);
        System.out.println("Old Ctor Stage MOUSE_PRESSED");
        postMenuDragMouseEventOldCtor(MouseEvent.MOUSE_PRESSED);
    }
    public void mousePressed(MouseEvent e){
        checkEventAbsolutePosition(e, "MousePressed OK");
    };
    public void mouseExited(MouseEvent e){
        System.out.println("mouse exited");
    };
    public void mouseReleased(MouseEvent e){
        checkEventAbsolutePosition(e, "MousePressed OK");
    };
    public void mouseEntered(MouseEvent e){
        System.out.println("mouse entered");
    };
    public void mouseClicked(MouseEvent e){
        checkEventAbsolutePosition(e, "MousePressed OK");
    };
    public void postMenuDragMouseEventNewCtor(int MouseEventType)    {
        MouseEvent me = new MenuDragMouseEvent(frame,
                                               MouseEventType,
                                               System.currentTimeMillis(),
                                               MouseEvent.BUTTON1_DOWN_MASK,
                                               mousePosition.x, mousePosition.y,
                                               mousePositionOnScreen.x,
                                               mousePositionOnScreen.y,
                                               1,                   
                                               false,              
                                               null,                
                                               null                 
                                               );
        frame.dispatchEvent( ( AWTEvent )me );
    }
    public void postMenuDragMouseEventOldCtor(int MouseEventType)    {
        MouseEvent meOld = new MenuDragMouseEvent(frame,
                                          MouseEventType,
                                          System.currentTimeMillis(),
                                          MouseEvent.BUTTON1_DOWN_MASK,
                                          mousePosition.x, mousePosition.y,
                                          1,
                                          false,              
                                          null, null
                                          );
        frame.dispatchEvent( ( AWTEvent )meOld );
    }
    public void checkEventAbsolutePosition(MouseEvent evt, String message){
            if (evt.getXOnScreen() != mousePositionOnScreen.x ||
                evt.getYOnScreen() != mousePositionOnScreen.y ||
                !evt.getLocationOnScreen().equals( mousePositionOnScreen )  ){
                System.out.println("evt.location = "+evt.getLocationOnScreen());
                System.out.println("mouse.location = "+mousePositionOnScreen);
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
